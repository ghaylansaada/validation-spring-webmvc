package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.comparison.valuenotin.ValueNotInConstraint
import io.ghaylan.validation.constraint.validator.comparison.valuenotin.ValueNotInValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that an element's string representation does not reside
 * within a predefined structural blacklist of disallowed literals.
 *
 * This annotation evaluates standalone properties, parameters, or individual elements within a collection
 * by converting the target value to a string and ensuring it is absent from the configured list. It is
 * commonly employed to enforce domain boundary rejections on input data, such as intercepting deprecated
 * legacy status codes, blacklisted identifiers, or restricted system tokens.
 *
 * @property values An array containing the exclusive set of forbidden string representations.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = ValueNotInConstraint::class, validatedBy = [ValueNotInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueNotIn(
	val values: Array<String>,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)