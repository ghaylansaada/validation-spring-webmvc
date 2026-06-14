package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.size.SizeConstraint
import io.ghaylan.validation.constraint.validator.size.SizeValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that the dimension or length of the annotated target
 * falls within a specified inclusive numeric range.
 *
 * This annotation can evaluate multi-element structures or character sequences. It verifies that the character count
 * of a [CharSequence], or the element count of an [Array], [Collection], or [Map], satisfies both the lower
 * and upper boundaries configured.
 *
 * @property min The minimum allowable element or character count (inclusive). Defaults to `0`.
 * @property max The maximum allowable element or character count (inclusive). Defaults to [Int.MAX_VALUE].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = SizeConstraint::class, validatedBy = [SizeValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Size(
	val min: Int = 0,
	val max: Int = Int.MAX_VALUE,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)