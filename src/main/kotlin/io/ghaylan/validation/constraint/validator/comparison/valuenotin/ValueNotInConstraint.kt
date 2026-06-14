package io.ghaylan.validation.constraint.validator.comparison.valuenotin

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.ValueNotIn
import kotlin.reflect.KClass

/** Constraint metadata for [@ValueNotIn][ValueNotIn]. */
data class ValueNotInConstraint(
	val values: Set<String>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()