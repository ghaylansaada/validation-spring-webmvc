package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.notcontains.NotStrOccConstraint
import io.ghaylan.validation.constraint.validator.string.notcontains.NotStrOccValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that a [String] does *not* match a forbidden sequence.
 *
 * This annotation evaluates whether a target character sequence successfully avoids a specified
 * value according to a given matching strategy. If the forbidden text is detected under the
 * configured rules, the validation fails. It supports multiple matching modes and case-sensitivity
 * toggles, and can be applied multiple times to the same target element.
 *
 * @property value The forbidden substring or sequence that the validated text must not match or contain.
 * @property ignoreCase Toggles whether character case boundaries are disregarded during evaluation. Defaults to `false`.
 * @property mode The matching strategy indicating what constitutes an invalid match (e.g., exact equality or prefix presence). Defaults to [Contain.StrOccMode.EQUALS].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = NotStrOccConstraint::class, validatedBy = [NotStrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class NotContain(
	val value: String,
	val ignoreCase: Boolean = false,
	val mode: Contain.StrOccMode = Contain.StrOccMode.EQUALS,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)