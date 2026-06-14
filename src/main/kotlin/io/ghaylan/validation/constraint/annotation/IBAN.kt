package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.iban.IBANConstraint
import io.ghaylan.validation.constraint.validator.string.iban.IBANValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a structurally valid International Bank Account Number (IBAN).
 *
 * This annotation performs a rigorous validation strategy in compliance with the ISO 13616 international standard.
 * It enforces structural correctness by evaluating the country-specific character lengths, validating the format
 * layout patterns, and executing the standardized Modulo 97 (ISO 7064) checksum algorithm to protect against
 * data-entry errors.
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = IBANConstraint::class, validatedBy = [IBANValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class IBAN(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)