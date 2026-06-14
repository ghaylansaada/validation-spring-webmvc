package io.ghaylan.validation.schema

import io.ghaylan.validation.accessor.AccessorRegistry
import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.validator.distinct.DistinctConstraint
import io.ghaylan.validation.constraint.validator.required.RequiredConstraint
import io.ghaylan.validation.ext.*
import io.ghaylan.validation.integration.ValidateRequest
import io.ghaylan.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.validation.schema.RequestInputSchema.ValidationConfig
import io.ghaylan.validation.schema.ValidationSchemaBuilder.generateSchemaForType
import io.ghaylan.validation.schema.ValidationSchemaBuilder.generateStaticSchemas
import io.ghaylan.validation.utils.ConstraintConverter.convertToMetadata
import io.ghaylan.validation.utils.ReflectionUtils
import io.ghaylan.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.validation.utils.SpringBootUtils
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.*
import kotlin.reflect.KClass

/**
 * A thread-safe singleton responsible for parsing, resolving, and constructing declarative
 * structural validation schemas ([RequestInputSchema]) from code components.
 *
 * This engine supports two primary operational modalities:
 * - **Static Analysis Mode ([generateStaticSchemas]):** Executed typically at application startup. Scans the
 * [ApplicationContext] for Spring MVC Controller methods decorated with [ValidateRequest] to build a
 * cached map of endpoint validation graphs.
 * - **Dynamic Analysis Mode ([generateSchemaForType]):** Executed programmatically on-demand at runtime.
 * Constructs localized schemas for arbitrary classes or complex DTOs outside the scope of web controller contexts.
 *
 * ### Key Capabilities
 * - **Deep Graph Traversal & Cycle Detection:** Recursively inspects non-scalar class properties while utilizing
 * re-entrant path guards to safely prevent infinite recursion loops on cyclic domain models.
 * - **Constraint Polymorphism Resolution:** Evaluates explicit method parameters and deeply nested generic type arguments
 * (e.g., `List<@NotBlank String>`) to bind compatible [ConstraintValidator] instances based on target assignability rules.
 * - **Data Extraction Abstraction:** Provisions high-performance data reading accessors through the [AccessorRegistry]
 * for optimal, low-overhead data fetching during validation pipelines.
 */
object ValidationSchemaBuilder {
	
	/**
	 * Scans the provided Spring application context to discover HTTP endpoints decorated with [ValidateRequest]
	 * and maps them to concrete validation schemas.
	 *
	 * This heavy reflection lifecycle phase should be invoked exclusively once during **application initialization** *
	 * (e.g., inside an `ApplicationListener` or `@PostConstruct` block). The resulting immutable map must be cached
	 * to eliminate reflection-driven performance penalties in the hot path of incoming requests.
	 *
	 * @param appContext The active Spring [ApplicationContext] used to query controller beans.
	 * @param allValidators Complete dictionary registry of all system-recognized constraint validators, grouped
	 * by metadata annotation class and filtered by their supported target [TypeInfo].
	 * @return An immutable map where keys represent deterministic, unique endpoint route identifiers generated via
	 * [Method.getUniqueIdentifier], and values provide structural validation rules for headers, queries,
	 * path variables, and request body segments.
	 */
    fun generateStaticSchemas(
        appContext : ApplicationContext,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Map<String, RequestInputSchema> {
    
		return SpringBootUtils.findRequestValidationMethods(appContext).entries.associate { (method, annotation) ->
			val parameters = method.parameters
			val requestId = method.getUniqueIdentifier()
			
			val (requestBodyTypeInfo, requestBodyMap) = buildRequestBodySchema(
				method = method,
				parameters = parameters,
				allValidators = allValidators
			) ?: (null to emptyMap())
			
			// Explicitly accept a lambda that operates on Parameter, matching buildNonRequestBodySchema's signature
			fun <T : Annotation> buildSchema(annotationClass: KClass<T>, nameExtractor: (Parameter) -> String) =
				buildNonRequestBodySchema(annotationClass, parameters, allValidators, nameExtractor)
			
			requestId to RequestInputSchema(
				id = requestId,
				headers = buildSchema(RequestHeader::class) { it.requestHeaderName() },
				queryParams = buildSchema(RequestParam::class) { it.requestParamName() },
				pathVariables = buildSchema(PathVariable::class) { it.pathVariableName() },
				requestBody = requestBodyMap,
				requestBodyTypeInfo = requestBodyTypeInfo,
				validationConfig = getValidationConfig(annotation))
        }
    }
	
	/**
	 * Dynamically generates a validation schema for a specified class structure on demand.
	 *
	 * Unlike [generateStaticSchemas], this utility functions independently of Spring Web constructs.
	 * It is designed for workflows executing standalone object processing, such as asynchronous message broker payload validation,
	 * file parser models, or programmatically driven object mapping assertions.
	 *
	 * Supports exhaustive structural traversal including deep nested classes, generic arguments, and collections.
	 *
	 * @param rootClass The Java reflection [Class] metadata representation of the model to parse.
	 * @param allValidators Dictionary registry of all system-recognized constraint validators, keyed by
	 * metadata types and mapped down to target type compatibilities.
	 * @return A [Pair] holding the root entity's [TypeInfo] accompanied by its field property mapping schema;
	 * returns `null` if the provided class is non-object-like (e.g., primitives, enums, strings) or contains no properties.
	 */
    fun generateSchemaForType(
        rootClass : Class<*>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Pair<TypeInfo, Map<String, PropertySpec>>? {
        val type = ReflectionUtils.infoFromClass(rootClass)
	    val fields = buildClassFields(
		    clazz = type.resolveType.java,
		    allValidators = allValidators
	    ) ?: return null
        return type to fields
    }
	
	/**
	 * Bridges external declarative definitions from a [ValidateRequest] annotation into internal,
	 * immutable domain execution rules represented as [ValidationConfig].
	 *
	 * @param annotation The active endpoint validation definition source.
	 * @return A configuration domain object defining active payload boundaries, evaluation limits, and validation groups.
	 */
    private fun getValidationConfig(annotation : ValidateRequest) = ValidationConfig(
	    validateBody = annotation.validateBody,
	    validateQuery = annotation.validateQuery,
	    validateHeaders = annotation.validateHeaders,
	    validatePathVariables = annotation.validatePath,
	    singleErrorPerField = annotation.singleErrorPerField,
	    groups = annotation.groups.toSet())
	
	/**
	 * Builds specialized, non-nested property descriptors for flat web execution spaces like HTTP Headers,
	 * Query Parameters, and URI Path Variables.
	 *
	 * Maps parameter targets matching [annotationClass], then runs validation tracking logic and creates virtualized
	 * access maps mimicking structured request properties.
	 *
	 * @param annotationClass Target filtering annotation marker (e.g., [RequestHeader]::class).
	 * @param parameters Array of Java parameters allocated to the target controller method.
	 * @param allValidators Comprehensive system validator matching registry.
	 * @param nameResolver Extract strategy computing logical public keys from raw signature [Parameter] components.
	 * @return Associated map tracking public keys to concrete parameter definitions ([PropertySpec]).
	 */
    private fun buildNonRequestBodySchema(
        annotationClass : KClass<out Annotation>,
        parameters : Array<Parameter>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>,
        nameResolver : (Parameter) -> String
    ) : Map<String, PropertySpec> {
        return parameters.asSequence()
            .filter { it.isAnnotationPresent(annotationClass.java) }
            .associate { param ->
				
                val type = ReflectionUtils.infoFromParameter(param)
                val resolvedName = nameResolver(param)
	            
                resolvedName to PropertySpec(
                    realName = param.name,
                    resolvedName = resolvedName,
                    typeInfo = type,
                    constraints = resolveConstraints(
	                    valueType = type,
	                    annotations = param.annotations,
	                    allValidators = allValidators),
	                typeArgumentConstraints = resolveTypeArgumentConstraints(
						valueType = type,
						annotatedType = param.annotatedType,
						allValidators = allValidators),
                    nested = emptyMap(),
                    accessor = AccessorRegistry.getOrCreate(
	                    containerClass = Map::class.java,
	                    fieldRealName = param.name,
	                    fieldResolvedName = resolvedName))
            }
    }
	
	/**
	 * Analyzes reflection parameters to detect and extract validation rules bound to the HTTP `@RequestBody` payload.
	 *
	 * Parses the composite DTO layout down to primitive values, capturing structural requirements and metadata constraints.
	 *
	 * @param method Target reflective [Method] pointer representing the handler endpoint.
	 * @param parameters All parameters present on the processing endpoint signature.
	 * @param allValidators System-wide validation engine registry dictionary.
	 * @return A resolved [Pair] capturing target structural [TypeInfo] details along with comprehensive nested properties layout;
	 * returns `null` if the method does not contain an incoming `@RequestBody` constraint parameter.
	 * @throws IllegalStateException if more than one parameter is annotated with `@RequestBody`, which violates standard Spring web specifications.
	 */
    private fun buildRequestBodySchema(
        method : Method,
        parameters : Array<Parameter>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Pair<TypeInfo, Map<String, PropertySpec>>? {
	    
	    val requestBodyParams = parameters.filter { it.isAnnotationPresent(RequestBody::class.java) }
	    
	    if (requestBodyParams.size > 1) {
		    error("Multiple @RequestBody parameters found in method ${method.name}. Only one is allowed.")
	    }
	    
	    val requestBody = requestBodyParams.firstOrNull() ?: return null

        val type = ReflectionUtils.infoFromParameter(requestBody)
	    val fields = buildClassFields(
		    clazz = type.resolveType.java,
		    allValidators = allValidators
	    ) ?: return null

        return type to fields
    }
	
	/**
	 * Core recursive reflection engine that walks the fields of a given class to construct a structured property map.
	 *
	 * Excludes non-logical runtime artifacts such as compiler synthetic hooks, static variables, or transient fields.
	 *
	 * ### Cycle Detection Strategy
	 * Relies on tracking types through a [visited] path collection. This ensures that cyclic data graphs
	 * (e.g., `Employee -> reportsTo -> Employee`) gracefully short-circuit, preventing deep stack overflows
	 * while safely allowing duplicate peer layouts (such as a single model possessing both a `shippingAddress` and a `billingAddress`).
	 *
	 * @param clazz Target structural entity type to introspect.
	 * @param parentConstraints Inherited contextual metadata propagated downward from upper container constructs
	 * (e.g., constraints like `@Distinct` passing constraints into member structures).
	 * @param allValidators Master framework system validator dictionary registry.
	 * @param visited Dynamic tracking set recording the current active re-entrant compilation path.
	 * @return Formatted map connecting field names to their specific structural parameters ([PropertySpec]),
	 * or `null` if the class is a basic scalar type or a cyclic reference is detected.
	 */
    private fun buildClassFields(
        clazz: Class<*>,
        parentConstraints : Map<ConstraintMetadata, ConstraintValidator<*, *>> = emptyMap(),
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>,
        visited: MutableSet<Class<*>> = mutableSetOf()
    ) : Map<String, PropertySpec>? {
		
	    if (!visited.add(clazz)) return null // Guard against actual circular dependencies
	    if (!ReflectionUtils.isObjectLike(clazz)) {
		    visited.remove(clazz) // Clean up before early exit
		    return null
	    }

        return try {
        
			ReflectionUtils.getFields(clazz)
            .asSequence()
            .filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) || Modifier.isTransient(it.modifiers) }
            .associate { field ->

                val resolvedName = field.bodyFieldName()
                val type = ReflectionUtils.infoFromField(field)

                val constraints = resolveConstraints(
		            valueType = type,
		            annotations = field.annotations,
					allValidators = allValidators)
	            
	            val typeArgumentConstraints = resolveTypeArgumentConstraints(
					valueType = type,
					annotatedType = field.annotatedType,
					allValidators = allValidators)
	            
	            // Only pass constraints down if it's an array/collection type container
                val nextParentConstraints = if (type.isArrayOfNonScalar) {
                    constraints
                } else emptyMap()

                // For array/list/map fields, pass their constraints down so element-level validators
                // (e.g., @Distinct) can access the parent container's schema.
	            val nested = buildClassFields(
		            clazz = type.resolveType.java,
		            parentConstraints = nextParentConstraints,
		            allValidators = allValidators,
		            visited = visited.toMutableSet()
	            ) ?: emptyMap()
	            
	            val localParentConstraints = parentConstraints.filter { (constraint, _) ->
		            constraint is DistinctConstraint && constraint.by.contains(resolvedName)
	            }

                resolvedName to PropertySpec(
                    realName = field.name,
                    resolvedName = resolvedName,
                    typeInfo = type,
                    constraints = localParentConstraints + constraints,
	                typeArgumentConstraints = typeArgumentConstraints,
                    accessor = AccessorRegistry.getOrCreate(
	                    containerClass = clazz,
	                    fieldRealName = field.name,
	                    fieldResolvedName = resolvedName),
                    nested = nested)
            }
        } finally {
			// Remove from visited once this class's branch is fully processed
	        // This ensures duplicate peer types (e.g. shipping vs billing address) don't break
	        visited.remove(clazz)
        }
    }
	
	/**
	 * Resolves validation annotations declared strictly on the root container property level.
	 *
	 * This method isolates and processes constraints applied directly to a field, parameter, or root collection
	 * instance itself (e.g., evaluating `@Required` or `@Size` on a `List` variable context), ensuring that
	 * elements deeper down the generic type tree are skipped during this phase.
	 *
	 * The resulting metadata mapping is ordered sequentially to ensure structural baseline assertions
	 * (such as [RequiredConstraint]) execute prior to semantic formatting or business rules.
	 *
	 * @param valueType Comprehensive runtime type evaluation descriptor for the target component.
	 * @param annotations Raw collection of candidate constraints extracted directly from the primary reflection layer.
	 * @param allValidators Complete system dictionary containing all globally discovered, type-bound validator implementations.
	 * @return A sorted, immutable map pairing resolved [ConstraintMetadata] profiles with their matched executable [ConstraintValidator] hooks.
	 */
	private fun resolveConstraints(
		valueType : TypeInfo,
		annotations: Array<Annotation>,
		allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
	): Map<ConstraintMetadata, ConstraintValidator<*, *>> {
		
		return annotations
			.filter { it.annotationClass.java.isAnnotationPresent(Constraint::class.java) }
			.distinctBy { it.annotationClass }
			.map {
				val constraint = it.convertToMetadata()
				val validator = getValidator(valueType, constraint, allValidators)
				constraint to validator
			}
			.sortedBy {
				// Prioritize required constraints first
                if (it.first is RequiredConstraint) 0 else 1
			}
			.toMap()
	}
	
	/**
	 * Resolves validation annotations declared strictly inside generic type arguments or array component definitions.
	 *
	 * This utility handles structural container element validation (JSR-380 container element use cases), bypasses the root
	 * container constraints completely, and introspects target nested arguments (e.g., extracting `@Enum` out of `List<@Enum String>`).
	 * It systematically differentiates complex, multi-parameter constructs such as [Map] keys and values using deterministic
	 * positional indexing.
	 *
	 * Validations are unrolled via structural checks matching either [AnnotatedParameterizedType] generic components or
	 * [AnnotatedArrayType] underlying matrices.
	 *
	 * @param valueType Comprehensive runtime type evaluation descriptor for the parent container component.
	 * @param annotatedType Structural reflection type pointer used to navigate deep type-use target declarations.
	 * @param allValidators Complete system dictionary containing all globally discovered, type-bound validator implementations.
	 * @return An immutable lookup table mapping zero-based generic parameter argument indexes (e.g., `0` for List elements,
	 * `0` for Map keys, `1` for Map values) to their respective pre-resolved executable validation sub-graphs.
	 */
	private fun resolveTypeArgumentConstraints(
	    valueType: TypeInfo,
	    annotatedType: AnnotatedType,
	    allValidators: Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*, *>>>
	): Map<Int, Map<ConstraintMetadata, ConstraintValidator<*, *>>> {
	
	    // Isolate the nested type arguments depending on whether it's a parameterized collection or an array type
	    val structuralArgs = when (annotatedType) {
	        is AnnotatedParameterizedType -> annotatedType.annotatedActualTypeArguments.toList()
	        is AnnotatedArrayType -> listOf(annotatedType.annotatedGenericComponentType)
	        else -> emptyList()
	    }
	
	    if (structuralArgs.isEmpty()) return emptyMap()
	
	    return structuralArgs.mapIndexedNotNull { index, argumentType ->
	
	        // Resolve the precise inner element TypeInfo from the type metadata tree
	        val elementValueType = valueType.typeArguments.getOrNull(index)
	            ?: valueType.arrayElemType?.let { ReflectionUtils.infoFromClass(it.java) }
	            ?: valueType
	
	        val resolvedArgMap = resolveConstraints(
		        valueType = elementValueType,
		        annotations = argumentType.annotations,
		        allValidators = allValidators)
	
	        if (resolvedArgMap.isNotEmpty()) index to resolvedArgMap else null
		    
	    }.toMap()
	}
	
	/**
	 * Resolves the most specific compatible [ConstraintValidator] for a given [TypeInfo] target
	 * and constraint definition.
	 *
	 * @param valueType The resolved runtime type information of the property being validated.
	 * @param constraint The converted constraint configuration parameters.
	 * @param allValidators Master framework system validator dictionary registry.
	 * @return A validator matching the target data value.
	 * @throws IllegalStateException if no registered validator corresponds to the constraint metadata,
	 * or if no variant matches the target type signature.
	 */
    private fun getValidator(
        valueType: TypeInfo,
        constraint: ConstraintMetadata,
        allValidators: Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*, *>>>
    ) : ConstraintValidator<*, *> {
		
        val constraintValidators = allValidators[constraint::class]
            ?: error("No validator found for constraint ${constraint::class.simpleName}")

        return constraintValidators.entries.firstOrNull { (validatorType, _) ->
            isValidatorCompatible(valueType, validatorType)
        }?.value ?: error("No validator found for constraint ${constraint::class.simpleName} and value type ${valueType.concreteType.java.name}")
    }
	
	/**
	 * Structural matching algorithm determining type assignment compatibility between data values
	 * and registered validators.
	 *
	 * Validates type definitions through the following hierarchical checks:
	 * 1. Exact Match (including type arguments)
	 * 2. Primitive vs Boxed equivalents (e.g., `Int` match to `java.lang.Integer`)
	 * 3. Number polymorphism (assignability mapping against numerical values)
	 * 4. [Comparable] boundary matching
	 * 5. Open wildcard capture targets (`Any` bounds matching)
	 * 6. Standard object assignability checking via [Class.isAssignableFrom]
	 * 7. Map-like collection wildcards
	 * 8. Recursive array component matching
	 * 9. Standard collection wildcards
	 *
	 * @param value The property target data type signature.
	 * @param validator The candidate validator target signature.
	 * @return `true` if the candidate validator accepts the value type signature; otherwise `false`.
	 */
    private fun isValidatorCompatible(
        value: TypeInfo,
        validator: TypeInfo
    ): Boolean {
		
		// Exact match with type arguments
		if (value.concreteType == validator.concreteType && typeArgsMatch(value.typeArguments, validator.typeArguments)) return true
	    
	    // Primitive ↔ boxed
	    if (ReflectionUtils.primitiveOrBoxedMatch(value.concreteType, validator.concreteType) && typeArgsMatch(value.typeArguments, validator.typeArguments)) return true

	    // Number compatibility
	    if (ReflectionUtils.isNumericType(value.concreteType) && validator.concreteType == Number::class) return true
	    if (value.concreteType == Number::class && ReflectionUtils.isNumericType(validator.concreteType)) return true
	    
	    // Comparable numeric
	    if (ReflectionUtils.isComparableNumeric(value.concreteType) && validator.concreteType == Comparable::class) return true

	    // Validator is Any
	    if (validator.concreteType == Any::class) return true

	    // Supertype match
	    if (validator.concreteType.java.isAssignableFrom(value.concreteType.java) && typeArgsMatch(value.typeArguments, validator.typeArguments)) return true

	    // Wildcard map match
	    if (ReflectionUtils.isTypeInfoMapLike(value) && ReflectionUtils.isTypeInfoMapLike(validator) && validator.typeArguments.all { ReflectionUtils.isWildcard(it) }) return true

	    // Array match (recursive)
	    if (value.isArray && validator.isArray) {
			val ve = value.arrayElemType
		    val va = validator.arrayElemType
		    if (va == Any::class) return true
		    if (ve != null && va != null) {
			    if (ReflectionUtils.primitiveOrBoxedMatch(ve, va)) return true
			    if (ReflectionUtils.isNumericType(ve) && va == Number::class) return true
		    }
		    return false
	    }

	    // Collection wildcard match
	    if (ReflectionUtils.isTypeInfoCollectionLike(value) && ReflectionUtils.isTypeInfoCollectionLike(validator) && validator.typeArguments.all { ReflectionUtils.isWildcard(it) }) return true

	    return false
    }
	
	/**
	 * Recursively verifies compatibility between the structural generic type arguments of a value
	 * type and a validator type.
	 *
	 * Account for type erasure boundaries and wildcard configurations (`*`, `?`).
	 *
	 * @param actual The nested generic type arguments declared on the runtime data property.
	 * @param expected The generic parameters expected by the target validator signature.
	 * @return `true` if all inner parameters match or map to open wildcards; otherwise `false`.
	 */
    private fun typeArgsMatch(actual: List<TypeInfo>, expected: List<TypeInfo>): Boolean {
        if (expected.isEmpty()) return true
	    if (actual.isEmpty()) return expected.all { ReflectionUtils.isWildcard(it) }
        if (actual.size != expected.size) return false
        return actual.zip(expected).all { (act, exp) ->
	        ReflectionUtils.isWildcard(exp) || (act.concreteType == exp.concreteType && typeArgsMatch(act.typeArguments, exp.typeArguments))
        }
    }
}