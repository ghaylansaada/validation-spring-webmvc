package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.number.max.NumberMaxConstraint
import io.ghaylan.validation.constraint.validator.number.max.NumberMaxValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that a numeric value does not exceed a specified maximum boundary.
 *
 * This annotation evaluates numeric fields or parameters to ensure they remain below or equal to the configured
 * [value]. It offers an [inclusive] toggle to control whether exact equality satisfies the boundary threshold
 * or if a strict mathematical less-than inequality is enforced.
 *
 * @property value The maximum upper numeric boundary limit against which the target value is evaluated.
 * @property inclusive Toggles whether equality satisfies the rule. If `true`, acts as a less-than-or-equal-to
 * (`<=`) boundary; if `false`, enforces a strict less-than (`<`) check. Defaults to `true`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = NumberMaxConstraint::class, validatedBy = [NumberMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMax(
	val value: Double,
	val inclusive: Boolean = true,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)