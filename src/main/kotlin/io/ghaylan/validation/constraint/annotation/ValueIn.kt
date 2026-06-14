package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.comparison.valuein.ValueInConstraint
import io.ghaylan.validation.constraint.validator.comparison.valuein.ValueInValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that an element's string representation resides
 * within a predefined structural domain of allowed literals.
 *
 * This annotation evaluates standalone properties, parameters, or individual elements within a collection
 * by converting the target value to a string and matching it against a configured whitelist. It is
 * commonly employed to enforce closed domain boundaries on input data when full enumeration types
 * are not practical (such as filtering dynamic system roles, status codes, or localized category tokens).
 *
 * @property values An array containing the exclusive set of permissible string representations.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = ValueInConstraint::class, validatedBy = [ValueInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueIn(
	val values: Array<String>,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)