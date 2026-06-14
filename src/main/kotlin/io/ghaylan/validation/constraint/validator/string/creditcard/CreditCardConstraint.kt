package io.ghaylan.validation.constraint.validator.string.creditcard

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.CreditCard
import kotlin.reflect.KClass

/** Constraint metadata for [@CreditCard][CreditCard]. */
data class CreditCardConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()