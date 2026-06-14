package io.ghaylan.validation.constraint.validator.string.hexcolor

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.HexColor
import kotlin.reflect.KClass

/** Constraint metadata for [@HexColor][HexColor]. */
data class HexColorConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()