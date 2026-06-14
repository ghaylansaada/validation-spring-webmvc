package io.ghaylan.validation.constraint.validator.comparison.valuein

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.ValueIn
import kotlin.reflect.KClass

/** Constraint metadata for [@ValueIn][ValueIn]. */
data class ValueInConstraint(
	val values: Set<String>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()