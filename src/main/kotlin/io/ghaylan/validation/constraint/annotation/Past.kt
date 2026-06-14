package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.temporal.past.PastConstraint
import io.ghaylan.validation.constraint.validator.temporal.past.PastValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a temporal value occurs strictly in the past.
 *
 * This annotation evaluates date, time, and timezone-aware temporal instances to ensure they represent
 * a moment trailing or preceding the current system time. It also offers a suite of tracking window fields (`within*`)
 * to define an optional lower boundary duration, effectively forcing the past date to fall within a specific
 * lookback horizon (e.g., setting `withinDays = 7` requires the value to be in the past, but no earlier than
 * 7 days prior to the evaluation moment).
 *
 * @property withinSeconds The maximum allowed past offset window in seconds. Disabled if set to `0`.
 * @property withinMinutes The maximum allowed past offset window in minutes. Disabled if set to `0`.
 * @property withinHours The maximum allowed past offset window in hours. Disabled if set to `0`.
 * @property withinDays The maximum allowed past offset window in days. Disabled if set to `0`.
 * @property withinWeeks The maximum allowed past offset window in weeks. Disabled if set to `0`.
 * @property withinMonths The maximum allowed past offset window in months. Disabled if set to `0`.
 * @property withinYears The maximum allowed past offset window in years. Disabled if set to `0`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = PastConstraint::class, validatedBy = [PastValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Past(
	val withinSeconds: Long = 0,
	val withinMinutes: Long = 0,
	val withinHours: Long = 0,
	val withinDays: Long = 0,
	val withinWeeks: Long = 0,
	val withinMonths: Long = 0,
	val withinYears: Long = 0,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)