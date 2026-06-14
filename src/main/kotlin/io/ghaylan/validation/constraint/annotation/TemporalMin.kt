package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.temporal.min.TemporalMinConstraint
import io.ghaylan.validation.constraint.validator.temporal.min.TemporalMinValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that a temporal value (such as a date, time, or timestamp)
 * is at least a specified minimum boundary.
 *
 * This annotation evaluates temporal properties to ensure they remain chronologically after or equal to the
 * configured reference threshold. The target [value] string must comply with the standard ISO-8601 format
 * corresponding to the concrete temporal type of the annotated field (e.g., `"2026-01-01"` for `LocalDate`,
 * or `"00:00:00"` for `LocalTime`). It offers an [inclusive] toggle to control whether exact chronological
 * equality satisfies the boundary threshold or if a strict greater-than inequality is enforced.
 *
 * @property value The ISO-8601 string representation of the minimum lower temporal boundary limit against which the target is evaluated.
 * @property inclusive Toggles whether exact chronological equality satisfies the rule. If `true`, acts as a greater-than-or-equal-to
 * (`>=`) boundary; if `false`, enforces a strict greater-than (`>`) check. Defaults to `true`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = TemporalMinConstraint::class, validatedBy = [TemporalMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMin(
	val value: String,
	val inclusive: Boolean = true,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)