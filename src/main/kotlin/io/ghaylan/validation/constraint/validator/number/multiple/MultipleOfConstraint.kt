package io.ghaylan.validation.constraint.validator.number.multiple

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.MultipleOf
import kotlin.reflect.KClass

/** Constraint metadata for [@MultipleOf][MultipleOf]. */
data class MultipleOfConstraint(
	val factor: Double,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()
