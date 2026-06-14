package io.ghaylan.validation.constraint.validator.number.min

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.NumberMin
import kotlin.reflect.KClass

/** Constraint metadata for [@NumberMin][NumberMin]. */
data class NumberMinConstraint(
	val value: Double,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()