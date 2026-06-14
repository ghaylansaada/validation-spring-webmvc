package io.ghaylan.validation.constraint.validator.number.max

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.NumberMax
import kotlin.reflect.KClass

/** Constraint metadata for [@NumberMax][NumberMax]. */
data class NumberMaxConstraint(
	val value: Double,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()