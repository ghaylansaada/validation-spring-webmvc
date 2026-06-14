package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.number.longitude.LongitudeConstraint
import io.ghaylan.validation.constraint.validator.number.longitude.LongitudeValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a numeric value represents a valid geographic longitude coordinate.
 *
 * This annotation asserts that the annotated [Double] or numeric target falls within the strict boundary limits
 * established by global geodetic coordinate reference systems, enforcing a closed interval between `-180.0`
 * (West of the Prime Meridian) and `180.0` (East of the Prime Meridian) inclusive.
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = LongitudeConstraint::class, validatedBy = [LongitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Longitude(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)