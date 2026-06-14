package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.regex.RegexConstraint
import io.ghaylan.validation.constraint.validator.string.regex.RegexValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to enforce structural format boundaries on character sequences via regular expressions.
 *
 * This annotation evaluates text properties or standalone parameters against a compiled regular expression schema.
 * To ensure secure API contract abstraction, it encapsulates the raw execution logic behind a logical structural identifier,
 * allowing downstream clients or public error payloads to understand the expected domain format without exposing the
 * underlying pattern details.
 *
 * @property pattern The structural regular expression string that the target character sequence must fully satisfy.
 * @property name A high-level, human-readable format abstraction identifier (such as `"POSTAL_CODE"` or `"ALPHANUMERIC"`)
 * used to describe the requirement to external consumers without revealing the internal regular expression structure.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = RegexConstraint::class, validatedBy = [RegexValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Regex(
	val pattern: String,
	val name: String,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)