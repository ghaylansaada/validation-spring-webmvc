package io.ghaylan.validation.engine

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.groups.OnDefault
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintError.ErrorLocation
import io.ghaylan.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.validation.schema.ValidationRegistry
import io.ghaylan.validation.utils.CollectionUtils
import io.ghaylan.validation.utils.ReflectionUtils
import io.ghaylan.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.validation.utils.ReflectionUtils.TypeKind
import kotlin.reflect.KClass

/**
 * The core, stateless execution runtime processor of the validation framework.
 *
 * `ValidatorEngine` coordinates recursive graph traversals to evaluate input data structures
 * against compiled metadata definitions. It evaluates four unique HTTP input locations independently
 * while maintaining strict thread safety across high-concurrency request paths.
 *
 * ### Key Capabilities
 * - **Deep Structural Recursion:** Traverses nested object graphs and tracks structural lineage path states.
 * - **Multi-Dimensional Matrix Unpacking:** Unpacks multi-dimensional arrays or collections recursively, preserving precise index path coordinates (e.g., `orders[0].items[3].id`).
 * - **Contextual Isolation Strategy:** Filters applicable constraints using validation group profiles, replicating JSR-380 group routing semantics.
 * - **Configurable Execution Profiling:** Restricts validation noise per property using short-circuiting fail-fast paths, or gathers all failures for comprehensive telemetry reporting.
 *
 * @property validationRegistry The underlying cache repository containing compiled static endpoint schemas and dynamic type specifications.
 */
open class ValidatorEngine(val validationRegistry : ValidationRegistry) {
	
	/**
	 * Unique structural identifier used to deduplicate matching constraint violations.
	 *
	 * Prevents duplicate log or payload output when identical tracking markers are encountered
	 * within the same property pathway across multiple validation ticks.
	 *
	 * @property field The normalized string path pointing to the property.
	 * @property code The unique alphanumeric error identifier.
	 * @property location The origin transport target layer.
	 */
    private data class ErrorKey(val field: String?, val code: Any?, val location: ErrorLocation?)
	
	
	/**
	 * Validates a standalone object instance or collection structure using an on-demand, class-mapped schema registry pathway.
	 *
	 * This entry point handles dynamic programmatic invocations or non-HTTP inputs (e.g., message queue listeners).
	 * It provisions a valid structural graph wrapper, creates a baseline root validation context, and delegates tasks
	 * to the generic validation pipeline.
	 *
	 * @param params The target instance data layout to analyze.
	 * @param location The contextual categorization assigned to logged failures. Defaults to [ErrorLocation.BODY].
	 * @param singleErrorPerField If `true`, stops evaluation for a property after its first constraint failure. Defaults to `true`.
	 * @param groups The set of active validation profiling boundaries to check against. Defaults to `[OnDefault::class]`.
	 * @return A list of collected, deduplicated [ConstraintError] violations; empty if the structure is valid.
	 */
    fun validate(
        params : Any,
        location : ErrorLocation = ErrorLocation.BODY,
        singleErrorPerField : Boolean = true,
        groups : Array<KClass<*>> = arrayOf(OnDefault::class)
	): List<ConstraintError<*>> {
		
        val schema = validationRegistry.resolveSchemaByClass(params.javaClass)
        val errors = mutableListOf<ConstraintError<*>>()

        val context = ValidationContext(
            fieldName = "",
            fieldPath = "",
            type = null,
            location = location,
            stopOnFirstError = singleErrorPerField,
            groups = groups.toSet(),
            array = null,
	        elementIndex = null,
            containerObject = ValidationContextValue(
                value = params,
                schema = schema.second,
                type = schema.first))

        validate(
            value = params,
            type = schema.first,
            fields = schema.second,
            context = context,
            errors = errors)

        return deduplicateErrors(errors)
    }
	
	/**
	 * Processes and evaluates all active segments of an incoming HTTP request using a pre-compiled metadata endpoint schema.
	 *
	 * Extracts parameters across four distinct HTTP payload targets, evaluating each sector independently.
	 * Violations are accumulated, normalized, and deduplicated before returning.
	 *
	 * @param id The deterministic lookup identifier representing the destination controller method handle signature.
	 * @param body The deserialized payload body object tree.
	 * @param params Mapped URI query string variables.
	 * @param headers Mapped HTTP tracking headers.
	 * @param pathVariables Mapped template path segments extracted from the request URI.
	 * @return A consolidated list of deduplicated [ConstraintError] metrics tracking validation failures across all vectors.
	 * @throws IllegalStateException if the requested endpoint signature has no matching compiled schema in the registry.
	 */
    fun validateRequest(
        id : String,
        body: Any?,
        params : Map<String, Any?>?,
        headers : Map<String, Any?>?,
        pathVariables : Map<String, Any?>?
    ) : List<ConstraintError<*>> {
		
        val schema = validationRegistry.getSchemaByRequest(id) ?: error("No validation schema found for request $id.")
        val errors = mutableListOf<ConstraintError<*>>()

        // Base context copied and specialized for each request section (body / query / headers / path).
        val baseCtx = ValidationContext(
            fieldName = "",
            fieldPath = "",
            type = null,
            location = ErrorLocation.BODY,
            groups = schema.validationConfig.groups.toSet(),
            stopOnFirstError = schema.validationConfig.singleErrorPerField,
            array = null,
	        elementIndex = null,
            containerObject = null)

        // ---------------- Body ----------------
        if (schema.validationConfig.validateBody && schema.requestBody.isNotEmpty()) {
            validate(
                value = body,
                type = schema.requestBodyTypeInfo!!,
                fields = schema.requestBody,
                context = baseCtx.copy(location = ErrorLocation.BODY, type = schema.requestBodyTypeInfo),
                errors = errors)
        }

        // ---------------- Query ----------------
        if (schema.validationConfig.validateQuery && schema.queryParams.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = params,
                schema = schema.queryParams,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val queryCtx = baseCtx.copy(location = ErrorLocation.QUERY, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = params,
                schema = schema.queryParams,
                context = queryCtx,
                errors = errors)
        }

        // ---------------- Headers ----------------
        if (schema.validationConfig.validateHeaders && schema.headers.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = headers,
                schema = schema.headers,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val headerCtx = baseCtx.copy(location = ErrorLocation.HEADER, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = headers,
                schema = schema.headers,
                context = headerCtx,
                errors = errors)
        }

        // ------------- Path Variables ------------
        if (schema.validationConfig.validatePathVariables && schema.pathVariables.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = pathVariables,
                schema = schema.pathVariables,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val pathVariableCtx = baseCtx.copy(location = ErrorLocation.PATH, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = pathVariables,
                schema = schema.pathVariables,
                context = pathVariableCtx,
                errors = errors)
        }

        return deduplicateErrors(errors)
    }
	
	/**
	 * Filters out matching violation metrics to maintain clean error logs and telemetry.
	 *
	 * Evaluates uniqueness based on an identity mapping of three key components: property pathway coordinates,
	 * error identifiers, and origin target layers.
	 *
	 * @param errors The list of violations collected during a validation run.
	 * @return A list containing only the first occurrence of each unique error signature.
	 */
    private fun deduplicateErrors(errors: List<ConstraintError<*>>) : List<ConstraintError<*>> {
        if (errors.isEmpty()) return errors

        val seen = HashSet<ErrorKey>()
        val unique = ArrayList<ConstraintError<*>>(errors.size)

        for (error in errors) {
            val key = ErrorKey(error.path, error.code, error.location)

            if (seen.add(key)) {
                unique.add(error)
            }
        }

        return unique
    }
	
	/**
	 * Resolves the structural shape of a target input value and routes it to the appropriate sub-validation loop.
	 *
	 * - **Arrays/Collections:** Handled by [validateArray].
	 * - **Complex Object Structs:** Handled by [validateObject].
	 * - **Raw Scalars:** Rejected if encountered at this entry level.
	 *
	 * @param value The value undergoing structural routing analysis.
	 * @param type Meta-type classification rules associated with the value.
	 * @param fields Mapped sub-property blueprints corresponding to this evaluation node.
	 * @param context The active runtime execution tracking context.
	 * @param errors The shared error tracking collector.
	 * @throws IllegalStateException if an unmappable scalar raw token reaches this root layer.
	 */
    private fun validate(
        value : Any?,
        type : TypeInfo,
        fields: Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ConstraintError<*>>
	) {
        if (type.isArray) {
            // forceNonEmpty=true so element-level constraints fire even on empty root arrays (path "[0]").
            validateArray(
                params = value,
                type = type,
                schema = fields,
                context = context,
                errors = errors,
                // only matters at root level when the input is an array
                forceNonEmpty = true,
                // no root-level constraints in this generic validation entry point
				typeArgumentConstraints = emptyMap())
        }
        else if (type.isObject) {
            validateObject(
                param = value,
                type = type,
                parentType = null,
                fields = fields,
                context = context,
                errors = errors)
        }
        else {
            error("Param must be an object or an array of objects to be validated.")
        }
    }
	
	/**
	 * Recursively traverses iterable boundaries, arrays, and multi-dimensional matrices to evaluate constraints.
	 *
	 * Handles nested dimensions by inspecting element-level details. It propagates path indices
	 * to child elements and manages container-level checks (e.g., verifying size or non-empty constraints).
	 *
	 * @param params The target array object or collection instance to unpack.
	 * @param type Meta-type details mapping the array dimension and type variables.
	 * @param schema Mapped child property specs applicable if elements are structured objects.
	 * @param context The current execution tracking state block.
	 * @param errors The shared error tracking collector.
	 * @param forceNonEmpty If `true`, injects a blank entry into empty or null root nodes to evaluate element-level criteria (like missing values).
	 * @param typeArgumentConstraints Master mapping grouping localized inner type-use validations by index position.
	 */
    private fun validateArray(
		params: Any?,
		type: TypeInfo,
		schema: Map<String, PropertySpec>,
		context: ValidationContext,
		errors: MutableList<ConstraintError<*>>,
		forceNonEmpty: Boolean,
		typeArgumentConstraints: Map<Int, Map<ConstraintMetadata, ConstraintValidator<*, *>>>
	) {
		val elementType = type.typeArguments.firstOrNull()
			?: type.arrayElemType?.let { ReflectionUtils.infoFromClass(it.java) }
			?: type

        val elements = CollectionUtils.normalizeList(params).let {
            if (forceNonEmpty) it.ifEmpty { listOf(Any()) } else it
        }

        val arrayValueCtx = ValidationContextValue(
            value = elements,
            schema = schema,
            type = type)
		
		// Unpack exactly index 0 for basic List/Set elements or array entries
		val elementLevelConstraints = typeArgumentConstraints[0] ?: emptyMap()

        when {
            // ----- Case 1: Current level is an array-of-arrays (multi-dimensional) -----
            type.isArrayOfArrays -> {

                elements.forEachIndexed { idx, element ->

                    val nestedCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        array = arrayValueCtx,
	                    elementIndex = idx,
                        containerObject = null)

                    validateArray(
                        params = element,
                        type = elementType,
                        schema = schema,
                        context = nestedCtx,
                        errors = errors,
                        forceNonEmpty = forceNonEmpty,
	                    typeArgumentConstraints = emptyMap())
                }
            }

            // ----- Case 2: Current level is an array-of-objects -----
            type.isArrayOfObjects -> {

                elements.forEachIndexed { idx, element ->

                    val elemCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        array = arrayValueCtx,
						elementIndex = idx)
	                
	                // Nested objects might rely on element level constraints before cascade processing
					if (elementLevelConstraints.isNotEmpty()) {
						validateValue(
							value = element,
							context = elemCtx,
							errors = errors,
							constraints = elementLevelConstraints)
					}

                    validateObject(
                        param = element,
                        type = elementType,
                        parentType = type,
                        fields = schema,
                        context = elemCtx,
                        errors = errors)
                }
            }

            // ----- Case 3: Current level is an array-of-scalars -----
            else -> {

                elements.forEachIndexed { idx, element ->

                    val elemCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        type = elementType,
                        array = arrayValueCtx,
	                    elementIndex = idx,
                        containerObject = null)

                    validateValue(
                        value = element,
                        context = elemCtx,
                        errors = errors,
                        constraints = elementLevelConstraints)
                }
            }
        }
    }
	
	/**
	 * Evaluates structural properties declared on an object node by reading values via non-reflective field accessors.
	 *
	 * For each field, this method evaluates localized constraints, updates tracking paths, and recurses
	 * into downstream object graphs or collection layers.
	 *
	 * @param param The parent object instance undergoing property inspection.
	 * @param type Meta-type specifications for the parent object.
	 * @param parentType Ancestor node type info; tracks array relationships across nested models.
	 * @param fields The map of properties to validate on this object.
	 * @param context The active tracking state context.
	 * @param errors The shared error tracking collector.
	 */
    private fun validateObject(
        param : Any?,
        type : TypeInfo,
        parentType : TypeInfo?,
        fields : Map<String, PropertySpec>,
        context : ValidationContext,
        errors : MutableList<ConstraintError<*>>
	) {
        fields.forEach { (_, field) ->

            val value = param?.let { field.accessor.getFromAny(instance = it, strict = false) }

            val arrayValueCtx = if (field.typeInfo.isArray) {
                ValidationContextValue(
                    value = CollectionUtils.normalizeList(value),
                    schema = field.nested,
                    type = field.typeInfo)
            }
            // Delegate array context upward so cross-element validators can reach the parent list.
            else if ((type.isObject || type.isMap) && (parentType?.isArrayOfObjects == true || parentType?.isArrayOfMaps == true)) {
                context.array
            } else null

            val objectValueCtx = ValidationContextValue(
                value = param,
                schema = fields,
                type = type)

            val fieldCtx = context.copy(
                fieldName = field.resolvedName,
                fieldPath = appendPath(context.fieldPath, field.resolvedName),
                type = field.typeInfo,
                containerObject = objectValueCtx,
                array = arrayValueCtx)

            validateValue(
                value = value,
                context = fieldCtx,
                errors = errors,
                constraints = field.constraints)

            if (value == null) return@forEach

            if (field.typeInfo.isArray) {
                validateArray(
                    params = value,
                    type = field.typeInfo,
                    schema = field.nested,
                    context = fieldCtx,
                    errors = errors,
                    forceNonEmpty = true,
					typeArgumentConstraints = field.typeArgumentConstraints)
            }
            else if (field.typeInfo.isObject) {
                validateObject(
                    param = value,
                    type = field.typeInfo,
                    fields = field.nested,
                    parentType = null,
                    context = fieldCtx,
                    errors = errors)
            }
        }
    }
	
	/**
	 * Validates un-flattened transport dictionaries, such as query string pairs, path variables, or transport headers.
	 *
	 * Supports both scalar properties and comma-separated/multi-value parameters mapped into flat lists.
	 *
	 * @param params Key-value parameter maps resolved from the transport layer.
	 * @param schema The expected validation blueprint rules assigned to these parameters.
	 * @param context The baseline execution context block.
	 * @param errors The shared error tracking collector.
	 */
    private fun validateHeadersOrParamsOrPathVariables(
        params : Map<String, Any?>?,
        schema : Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ConstraintError<*>>
	) {
        if (schema.isEmpty()) return

        schema.forEach { (_, param) ->

            val value = params?.get(param.resolvedName)
            val paramCtx = context.copy(fieldName = param.resolvedName, fieldPath = param.resolvedName)

            validateValue(
                value = value,
                context = paramCtx,
                errors = errors,
                constraints = param.constraints)

            if (param.typeInfo.isArrayOfScalars) {
                val elements = CollectionUtils.normalizeList(value)

                val parentArray = ValidationContextValue(
                    value = elements,
                    schema = emptyMap(),
                    type = param.typeInfo)

                elements.forEachIndexed { idx, elem ->

                    val elemCtx = paramCtx.copy(
                        fieldPath = appendIndex(paramCtx.fieldPath, idx),
                        array = parentArray,
                        containerObject = null)

                    validateValue(
                        value = elem,
                        context = elemCtx,
                        errors = errors,
                        constraints = param.constraints)
                }
            }
        }
    }
	
	/**
	 * Appends a child field token onto an existing object pathway string using dot notation.
	 *
	 * @param base The current accumulated property pathway.
	 * @param child The name of the property being appended.
	 * @return The combined path string (e.g., `user.profile`).
	 */
    private fun appendPath(base: String, child: String): String {
        if (child.isEmpty()) return base
        if (base.isEmpty()) return child
        return "$base.$child"
    }
	
	/**
	 * Appends an index bracket indicator onto an existing property pathway string.
	 *
	 * @param base The current accumulated property pathway.
	 * @param idx The current positional list index.
	 * @return The combined path string (e.g., `items[0]`).
	 */
    private fun appendIndex(base: String, idx: Int): String {
        return if (base.isEmpty()) "[$idx]" else "$base[$idx]"
    }
	
	/**
	 * Executes validation assertions against a property value by checking it against assigned constraints.
	 *
	 * Respects short-circuit execution boundaries; if [ValidationContext.stopOnFirstError] is configured,
	 * it halts validation for the current field upon encountering its first failure.
	 *
	 * @param value The raw target runtime property value to validate.
	 * @param context The current localized context block tracking path and group states.
	 * @param errors The shared list mapping cumulative failures.
	 * @param constraints An ordered map linking rule definitions to their matching executable validators.
	 */
    private fun validateValue(
		value: Any?,
		context: ValidationContext,
		errors: MutableList<ConstraintError<*>>,
		constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>
	) {
        if (constraints.isEmpty()) return

        if (context.stopOnFirstError) {
            for (constraint in constraints) {
                val error = constraint.value.runValidation(value, constraint.key, context)
                if (error != null) {
                    errors += error
                    return
                }
            }
        }
        else constraints.forEach {
	        it.value.runValidation(value, it.key, context)?.let(errors::add)
        }
    }
}