package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.currency.CurrencyCodeConstraint
import io.ghaylan.validation.constraint.validator.string.currency.CurrencyCodeValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a valid currency identifier.
 *
 * This annotation checks the target string against recognized international monetary standards, specifically
 * verifying that the value conforms to a valid ISO 4217 three-letter alphabetic currency code (e.g., "USD", "EUR", "TND").
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = CurrencyCodeConstraint::class, validatedBy = [CurrencyCodeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class CurrencyCode(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)