package io.ghaylan.validation.engine

import io.ghaylan.validation.model.ConstraintError.ErrorLocation
import io.ghaylan.validation.utils.ReflectionUtils.TypeInfo
import kotlin.reflect.KClass

/**
 * Contextual state passed through the validation chain for a single field.
 *
 * Carries the field path, active groups, error accumulation policy, and references
 * to the parent object and array for cross-field/cross-element validation.
 *
 * @property fieldPath Dot/bracket path from root (e.g., `"user.address[0].city"`).
 * @property fieldName Simple name of the current field.
 * @property location HTTP section (BODY, QUERY, HEADER, PATH) for error tagging.
 * @property stopOnFirstError When true, stops at the first violation for this field.
 * @property groups Active validation groups that determine which constraints apply.
 */
data class ValidationContext(
	val fieldPath: String,
	val fieldName: String,
	val type: TypeInfo?,
	val location: ErrorLocation,
	val stopOnFirstError: Boolean,
	val groups: Set<KClass<*>>,
	val elementIndex: Int?,
	
	/**
	 * Metadata for the array related to this field, if applicable.
	 *
	 * This represents:
	 * - The **parent array** if the current field is an item within an array.
	 * - The **field itself** if the current field is an array (e.g., a list of users).
	 *
	 * This enables advanced array-related validation features, including:
	 * - **Cross-item validation** when validating an element within an array (e.g., sibling uniqueness).
	 * - **Recursive and structural validation** when validating an array field (e.g., validating each item).
	 *
	 * Null if the field is not part of an array and is not an array itself.
	 */
	val array: ValidationContextValue<List<Any>>?,
	
	/**
	 * Metadata for the object that directly contains this field (if any).
	 * This is populated when the field belongs to a structured object, allowing access to:
	 * - The full containing object instance.
	 * - The schema of all its properties.
	 * - Type metadata for reflection or advanced introspection.
	 *
	 * This supports **cross-field validation** inside objects, such as:
	 * - Conditional requirements based on sibling fields.
	 * - Mutual exclusivity or dependency checks.
	 *
	 * Null when the field is not part of an object (e.g., array of primitives).
	 */
	val containerObject: ValidationContextValue<Any>?,
	
	val attributes: MutableMap<String, Any> = HashMap()
){
	/**
	 * Helper to compute a value exactly once if it doesn't exist in this context runner.
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T> getOrComputeAttribute(key: String, compute: () -> T): T {
		return attributes.computeIfAbsent(key) { compute() as Any } as T
	}
}