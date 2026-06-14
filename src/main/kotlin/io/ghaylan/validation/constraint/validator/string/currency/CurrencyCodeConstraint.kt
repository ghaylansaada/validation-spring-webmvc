package io.ghaylan.validation.constraint.validator.string.currency

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.CurrencyCode
import kotlin.reflect.KClass

/** Constraint metadata for [@CurrencyCode][CurrencyCode]. */
data class CurrencyCodeConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()
