package io.ghaylan.validation.schema

import io.ghaylan.validation.accessor.FieldAccessor
import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.utils.ReflectionUtils.TypeInfo
import kotlin.reflect.KClass

/**
 * An immutable, compiled validation schema representation for a specific Spring MVC HTTP controller endpoint.
 *
 * This class acts as a consolidated registry of all expected request parameters across four major HTTP vectors:
 * path variables, headers, query parameters, and the request payload body. Elements of this schema are constructed
 * during the application initialization phase by [ValidationSchemaBuilder] and are reused continuously at runtime
 * as a read-only lookup table to bypass subsequent reflection penalties.
 *
 * @property id The deterministic unique identifier for this schema instance, typically derived from the fully-qualified
 * class and method signature of the target controller endpoint handler.
 * @property pathVariables An unmodifiable map tracking expected URI template path variables, matching the logical path variable name
 * to its corresponding parameter specification.
 * @property headers An unmodifiable map tracking expected HTTP Request Headers, mapping the canonical lower-case or exact header string keys
 * to their respective parameter specifications.
 * @property queryParams An unmodifiable map tracking expected HTTP URI query parameters, mapping the parameter string keys
 * to their respective parameter specifications.
 * @property requestBody An unmodifiable, structural metadata map representing the flattened or deeply nested property model tree of the
 * expected JSON/XML payload. This map remains empty if no parameter is decorated with `org.springframework.web.bind.annotation.RequestBody`.
 * @property requestBodyTypeInfo Exhaustive generic type details ([TypeInfo]) for the root HTTP request body entity. Evaluates to `null` if the endpoint
 * does not accept a payload body.
 * @property validationConfig Operational runtime execution rules specifying target validation scopes, error thresholds, and contextual execution groups.
 */
data class RequestInputSchema(
	val id: String,
	val pathVariables: Map<String, PropertySpec> = emptyMap(),
	val headers: Map<String, PropertySpec> = emptyMap(),
	val queryParams: Map<String, PropertySpec> = emptyMap(),
	val requestBody: Map<String, PropertySpec> = emptyMap(),
	val requestBodyTypeInfo: TypeInfo?,
	val validationConfig: ValidationConfig
) {
	
	/**
	 * Configurable execution profile governing the runtime processing constraints and behavior of the validation engine for an endpoint.
	 *
	 * This configuration determines whether specific layers of an incoming request should be skipped altogether, restricts
	 * evaluation bounds based on active profiling groups, and fine-tunes performance/error verbosity.
	 *
	 * @property validateBody Instructs the validation engine to parse and run assertions against the HTTP request payload body.
	 * @property validateQuery Instructs the validation engine to evaluate assertions against incoming URI query string parameters.
	 * @property validateHeaders Instructs the validation engine to evaluate assertions against the incoming HTTP header collection.
	 * @property validatePathVariables Instructs the validation engine to evaluate assertions against the extracted URI path parameters.
	 * @property singleErrorPerField Performance-optimization and error-limiting flag. When set to `true`, the engine immediately
	 * short-circuits evaluation on a per-field basis upon encountering its first constraint failure, skipping any remaining checks on that specific field.
	 * @property groups An immutable set of validation group interfaces ([KClass]). Constraints on fields are only evaluated if they belong
	 * to a group that intersects with this set, mimicking Jakarta/JSR-383 validation grouping semantics.
	 */
	data class ValidationConfig(
		val validateBody: Boolean,
		val validateQuery: Boolean,
		val validateHeaders: Boolean,
		val validatePathVariables: Boolean,
		val singleErrorPerField: Boolean,
		val groups: Set<KClass<*>>
	)
	
	/**
	 * Complete metadata blueprint detailing the structural, semantic, and programmatic characteristics of a single field or parameter.
	 *
	 * `PropertySpec` forms a node within an endpoint's validation graph. If a property is a complex object or collection type,
	 * it forms a parent node that recursively references its children through the [nested] property map, forming a tree structure.
	 *
	 * @property realName The raw, unmodified field or parameter name as defined in the compiled Kotlin source code class signature.
	 * @property resolvedName The final, effective external name mapping used in transit (e.g., calculated using naming strategies, or
	 * explicitly declared via annotations like `@JsonProperty`, `@RequestParam`, or `@RequestHeader`).
	 * @property typeInfo Comprehensive, type-erasure-resistant metadata describing the runtime class type and structural generic type arguments of this property.
	 * @property accessor A compiled high-performance [FieldAccessor] handler utilized to dynamically read the underlying data value from an input object or map without reflection overhead.
	 * @property nested A map containing the schema specs for fields belonging to this property when it represents a nested object or complex type structure; empty for scalar properties.
	 * @property constraints An ordered map pairing explicit [ConstraintMetadata] configuration rules with their type-compatible, pre-resolved executable [ConstraintValidator] instances.
	 * @property typeArgumentConstraints An unmodifiable map tracking constraints applied directly to the generic type parameters of container types (e.g., collections, maps, optionals).
	 *          The outer map's integer key represents the zero-based index of the generic type argument as declared in the source code signature.
	 *          For example:
	 *          - For `List<@Enum String>`, key `0` contains the constraint registry for the `String` elements.
	 *          - For `Map<@NotBlank String, @Min(1) Int>`, key `0` targets the map keys, while key `1` targets the map values.
	 */
	data class PropertySpec(
		val realName: String,
		val resolvedName: String,
		val typeInfo: TypeInfo,
		val accessor: FieldAccessor<*>,
		val nested: Map<String, PropertySpec>,
		val constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>,
		val typeArgumentConstraints: Map<Int, Map<ConstraintMetadata, ConstraintValidator<*, *>>>
	)
}
