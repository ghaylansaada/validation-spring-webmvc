package io.ghaylan.validation.constraint.validator.comparison.lessthan

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.LessThan
import kotlin.reflect.KClass

/** Constraint metadata for [@LessThan][LessThan]. */
data class LessThanConstraint(
	val property: String,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()