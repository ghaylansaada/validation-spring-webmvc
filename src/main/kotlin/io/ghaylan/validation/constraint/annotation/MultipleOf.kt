package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.number.multiple.MultipleOfConstraint
import io.ghaylan.validation.constraint.validator.number.multiple.MultipleOfValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a numeric value is an exact mathematical multiple of a specified factor.
 *
 * This annotation evaluates numeric properties (or individual elements within a collection) to assert that
 * dividing the target value by the configured [factor] results in a remainder of zero. It can be applied multiple
 * times to the same target element to enforce compound multiplicity constraints. Validation is gracefully
 * bypassed if the factor is explicitly set to zero.
 *
 * @property factor The numeric divisor used to evaluate multiplicity. If configured as `0.0`, the validation logic is skipped.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = MultipleOfConstraint::class, validatedBy = [MultipleOfValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class MultipleOf(
	val factor: Double,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)