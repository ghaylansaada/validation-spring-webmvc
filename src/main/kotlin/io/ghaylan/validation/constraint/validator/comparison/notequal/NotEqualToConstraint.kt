package io.ghaylan.validation.constraint.validator.comparison.notequal

import io.ghaylan.validation.constraint.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@NotEqualTo][io.ghaylan.validation.constraint.annotation.NotEqualTo]. */
data class NotEqualToConstraint(
	val property: String,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()