package io.ghaylan.validation.constraint.validator.number.latitude

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Latitude
import kotlin.reflect.KClass

/** Constraint metadata for [@Latitude][Latitude]. */
data class LatitudeConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()