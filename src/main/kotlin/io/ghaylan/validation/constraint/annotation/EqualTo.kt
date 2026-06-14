package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.comparison.equal.EqualToConstraint
import io.ghaylan.validation.constraint.validator.comparison.equal.EqualToValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert object equality between the annotated target
 * and a targeted sibling property within the same object structure.
 *
 * This cross-field constraint is commonly employed for multi-field synchronization checks,
 * such as confirming a `password` fields match a `passwordConfirmation` field. It utilizes
 * reflection to extract the value of the companion property and supports multiple independent
 * comparison assertions on the same element.
 *
 * @property property The name of the sibling field inside the same class instance to compare against.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = EqualToConstraint::class, validatedBy = [EqualToValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class EqualTo(
	val property: String,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)