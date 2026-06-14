package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.country.ISOCountryConstraint
import io.ghaylan.validation.constraint.validator.string.country.ISOCountryValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a valid country identifier.
 *
 * This annotation checks the target string against recognized international standards, specifically verifying
 * that the value conforms to a valid ISO 3166-1 alpha-2 two-letter country code (e.g., "US", "FR", "TN").
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = ISOCountryConstraint::class, validatedBy = [ISOCountryValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class CountryCode(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)