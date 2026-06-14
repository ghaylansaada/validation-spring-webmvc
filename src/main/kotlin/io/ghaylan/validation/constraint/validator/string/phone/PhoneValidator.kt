package io.ghaylan.validation.constraint.validator.string.phone

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import io.ghaylan.validation.utils.PhoneNumberUtils

object PhoneValidator : ConstraintValidator<CharSequence, PhoneConstraint>() {
	
     override fun validate(
        value: CharSequence?,
        constraint: PhoneConstraint,
        context: ValidationContext
    ) : ConstraintError<*>? {
    
		 value ?: return null
	    
	     // 1. Structural Syntax and Digit Check
	     if (!value.all(Char::isDigit) || !PhoneNumberUtils.isValidNumber(value)) {
			 return ConstraintError(code = ConstraintErrorCode.PHONE_INVALID_FORMAT)
	     }
	     
	     // 2. Device/Line Type Restriction Check (e.g., MOBILE, LANDLINE)
		if (constraint.allowedTypes.isNotEmpty()) {
			val actualType = PhoneNumberUtils.getNumberType(value)
			if (!constraint.allowedTypes.contains(actualType)) {
				return ConstraintError(
					code = ConstraintErrorCode.PHONE_TYPE_RESTRICTED,
					metadata = buildMap {
						put("allowed_types", constraint.allowedTypes)
						put("actual_type", actualType)
					}
				)
			}
		}
	     
	     // 3. Regional / Country ISO Restriction Check (e.g., US, DE)
		if (constraint.allowedCountries.isNotEmpty()) {
			val actualCountry = PhoneNumberUtils.getCountryISOCode(value)
			if (!constraint.allowedCountries.contains(actualCountry)) {
				return ConstraintError(
					code = ConstraintErrorCode.PHONE_COUNTRY_RESTRICTED,
					metadata = buildMap {
						put("allowed_countries", constraint.allowedCountries)
						put("actual_country", actualCountry)
					}
				)
			}
		}
	     
	    return null
    }
	
}