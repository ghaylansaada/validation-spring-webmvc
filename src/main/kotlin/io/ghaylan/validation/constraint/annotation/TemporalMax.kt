package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.temporal.max.TemporalMaxConstraint
import io.ghaylan.validation.constraint.validator.temporal.max.TemporalMaxValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that a temporal value (such as a date, time, or timestamp)
 * does not exceed a specified maximum boundary.
 *
 * This annotation evaluates temporal properties to ensure they remain chronologically before or equal to the
 * configured reference threshold. The target [value] string must comply with the standard ISO-8601 format
 * corresponding to the concrete temporal type of the annotated field (e.g., `"2026-12-31"` for `LocalDate`,
 * or `"23:59:59"` for `LocalTime`). It offers an [inclusive] toggle to control whether exact chronological
 * equality satisfies the boundary threshold or if a strict less-than inequality is enforced.
 *
 * @property value The ISO-8601 string representation of the maximum upper temporal boundary limit against which the target is evaluated.
 * @property inclusive Toggles whether exact chronological equality satisfies the rule. If `true`, acts as a less-than-or-equal-to
 * (`<=`) boundary; if `false`, enforces a strict less-than (`<`) check.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = TemporalMaxConstraint::class, validatedBy = [TemporalMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMax(
	val value: String,
	val inclusive: Boolean,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)