package io.ghaylan.validation.engine

import io.ghaylan.validation.schema.RequestInputSchema
import io.ghaylan.validation.utils.ReflectionUtils

/**
 * Wraps a value with its schema and type info for cross-field/cross-element rule lookups.
 *
 * @property value Runtime value (e.g., a DTO instance or normalized list of array elements).
 * @property schema Property specs describing the value's fields or elements.
 * @property type Resolved type metadata for the value.
 */
data class ValidationContextValue<T>(
	val value: T?,
	val type: ReflectionUtils.TypeInfo,
	val schema: Map<String, RequestInputSchema.PropertySpec>)