package io.ghaylan.validation.constraint.validator.comparison.greaterthan

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.GreaterThan
import kotlin.reflect.KClass

/** Constraint metadata for [@GreaterThan][GreaterThan]. */
data class GreaterThanConstraint(
	val property: String,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()