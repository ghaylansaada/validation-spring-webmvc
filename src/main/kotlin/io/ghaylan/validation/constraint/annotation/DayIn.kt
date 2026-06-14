package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.temporal.dayin.AllowedDaysConstraint
import io.ghaylan.validation.constraint.validator.temporal.dayin.AllowedDaysValidator
import io.ghaylan.validation.groups.OnDefault
import java.time.DayOfWeek
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a temporal value falls on specific days of the week.
 *
 * This annotation evaluates date-aware temporal instances (such as [java.time.LocalDate], [java.time.LocalDateTime],
 * or [java.time.ZonedDateTime]) to ensure their calendar day matches one of the declared [DayOfWeek] entries.
 * Validation is safely bypassed for purely time-centric temporal types that lack a calendar day component.
 *
 * @property days The collection of permitted days of the week.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = AllowedDaysConstraint::class, validatedBy = [AllowedDaysValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class DayIn(
	val days: Array<DayOfWeek>,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)