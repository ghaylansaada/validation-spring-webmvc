package io.ghaylan.validation.constraint.validator.string.iban

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.IBAN
import kotlin.reflect.KClass

/** Constraint metadata for [@IBAN][IBAN]. */
data class IBANConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()