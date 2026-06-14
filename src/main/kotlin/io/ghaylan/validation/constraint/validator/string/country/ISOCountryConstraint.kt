package io.ghaylan.validation.constraint.validator.string.country

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.CountryCode
import kotlin.reflect.KClass

/** Constraint metadata for [@ISOCountryCode][CountryCode]. */
data class ISOCountryConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()
