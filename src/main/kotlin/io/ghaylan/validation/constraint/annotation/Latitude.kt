package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.number.latitude.LatitudeConstraint
import io.ghaylan.validation.constraint.validator.number.latitude.LatitudeValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a numeric value represents a valid geographic latitude coordinate.
 *
 * This annotation asserts that the annotated [Double] or numeric target falls within the strict boundary limits
 * established by global geodetic coordinate reference systems, enforcing a closed interval between `-90.0`
 * (South Pole) and `90.0` (North Pole) inclusive.
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = LatitudeConstraint::class, validatedBy = [LatitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Latitude(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)