package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.creditcard.CreditCardConstraint
import io.ghaylan.validation.constraint.validator.string.creditcard.CreditCardValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a structurally valid payment card number.
 *
 * This annotation performs a two-step validation: it asserts that the input sequence consists entirely of numeric
 * digits, and then evaluates the sequence against the Luhn algorithm (modulus 10) checksum to guard against
 * accidental mistyping or malformed numeric structures.
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = CreditCardConstraint::class, validatedBy = [CreditCardValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class CreditCard(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)