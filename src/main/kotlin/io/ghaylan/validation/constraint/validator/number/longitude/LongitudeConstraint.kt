package io.ghaylan.validation.constraint.validator.number.longitude

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Longitude
import kotlin.reflect.KClass

/** Constraint metadata for [@Longitude][Longitude]. */
data class LongitudeConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()