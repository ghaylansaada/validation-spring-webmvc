package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.email.EmailConstraint
import io.ghaylan.validation.constraint.validator.string.email.EmailValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a syntactically valid email address.
 *
 * This annotation evaluates the target string structure against standard email syntax rules, confirming the presence
 * of a valid localized user box segment, a single `@` separator, a valid domain host part, and a concluding top-level
 * domain (TLD) constraint bounded between 1 and 6 characters.
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = EmailConstraint::class, validatedBy = [EmailValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Email(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)