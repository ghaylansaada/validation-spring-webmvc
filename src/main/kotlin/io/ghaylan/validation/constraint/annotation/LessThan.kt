package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.comparison.lessthan.LessThanConstraint
import io.ghaylan.validation.constraint.validator.comparison.lessthan.LessThanValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that the annotated value is numerically or
 * chronologically less than a targeted sibling property within the same object structure.
 *
 * This cross-field constraint is commonly employed to enforce relative logical boundaries between
 * related properties, such as ensuring a `startDate` field precedes an `endDate` field. Both fields
 * must evaluate to a mutually compatible [Comparable] type. This annotation can be applied multiple
 * times to the same element to handle independent comparison rules.
 *
 * @property property The name of the companion sibling field inside the same class instance to compare against.
 * @property inclusive Toggles whether equality satisfies the rule. If `true`, acts as a less-than-or-equal-to
 * (`<=`) boundary; if `false`, enforces a strict less-than (`<`) check. Defaults to `false`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = LessThanConstraint::class, validatedBy = [LessThanValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class LessThan(
	val property: String,
	val inclusive: Boolean = false,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)