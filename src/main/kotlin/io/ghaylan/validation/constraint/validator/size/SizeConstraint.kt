package io.ghaylan.validation.constraint.validator.size

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Size
import kotlin.reflect.KClass

/** Constraint metadata for [@Size][Size]. */
data class SizeConstraint(
	val min: Int,
	val max: Int,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()