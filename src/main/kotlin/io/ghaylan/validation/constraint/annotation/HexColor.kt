package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.hexcolor.HexColorConstraint
import io.ghaylan.validation.constraint.validator.string.hexcolor.HexColorValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a valid hexadecimal color code.
 *
 * This annotation evaluates the target string against standard CSS color structures, ensuring it conforms to either
 * the short-form `#RGB` (3 digits) or the standard `#RRGGBB` (6 digits) format. The leading `#` symbol is required,
 * followed strictly by valid, case-insensitive hexadecimal digits (`0-9`, `a-f`, `A-F`).
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = HexColorConstraint::class, validatedBy = [HexColorValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class HexColor(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)