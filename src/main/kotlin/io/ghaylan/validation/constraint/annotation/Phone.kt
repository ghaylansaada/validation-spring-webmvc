package io.ghaylan.validation.constraint.annotation

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.phone.PhoneConstraint
import io.ghaylan.validation.constraint.validator.string.phone.PhoneValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a valid international phone number.
 *
 * This annotation delegates evaluation logic to Google's standardized `libphonenumber` library to parse, format, and
 * validate dialing sequences. It provides filtering hooks to limit accepted entries based on regional routing schemas
 * or explicit hardware classification profiles (such as isolating mobile connections from fixed landlines).
 *
 * @property allowedTypes An array of permissible [PhoneNumberUtil.PhoneNumberType] categories (for example, `MOBILE`,
 * `FIXED_LINE`, or `TOLL_FREE`). If left empty, all structural number types are accepted. Defaults to empty.
 * @property allowedCountries An array of permitted ISO 3166-1 alpha-2 country codes (such as `"US"`, `"FR"`, or `"TN"`)
 * authorized to pass validation boundaries. If left empty, no regional constraints are imposed. Defaults to empty.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = PhoneConstraint::class, validatedBy = [PhoneValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Phone(
	val allowedTypes: Array<PhoneNumberUtil.PhoneNumberType> = [],
	val allowedCountries: Array<String> = [],
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)