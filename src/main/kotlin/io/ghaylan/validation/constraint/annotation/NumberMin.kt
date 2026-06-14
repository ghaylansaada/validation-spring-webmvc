package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.number.min.NumberMinConstraint
import io.ghaylan.validation.constraint.validator.number.min.NumberMinValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that a numeric value is at least a specified minimum boundary.
 *
 * This annotation evaluates numeric fields or parameters to ensure they remain above or equal to the configured
 * [value]. It offers an [inclusive] toggle to control whether exact equality satisfies the boundary threshold
 * or if a strict mathematical greater-than inequality is enforced.
 *
 * @property value The minimum lower numeric boundary limit against which the target value is evaluated.
 * @property inclusive Toggles whether equality satisfies the rule. If `true`, acts as a greater-than-or-equal-to
 * (`>=`) boundary; if `false`, enforces a strict greater-than (`>`) check. Defaults to `true`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = NumberMinConstraint::class, validatedBy = [NumberMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMin(
	val value: Double,
	val inclusive: Boolean = true,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)