package io.ghaylan.validation.constraint.validator.string.creditcard

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object CreditCardValidator: ConstraintValidator<CharSequence, CreditCardConstraint>() {
	
	 override fun validate(
		value: CharSequence?,
		constraint: CreditCardConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		// 1. Sanitize whitespaces and dashes commonly found in card entry fields
		val cleanCard = value.filter { it.isDigit() }.toString()

		// 2. Format and Length Verification Guard (Most major networks use between 13 and 19 digits)
		if (cleanCard.length !in 13..19 || !value.all { it.isDigit() || it == ' ' || it == '-' }) {
			return ConstraintError(code = ConstraintErrorCode.CREDIT_CARD_FORMAT_INVALID)
		}

		// 3. Mathematical Integrity Verification (Luhn Mod-10 Checksum)
		if (!isValid(cleanCard)) {
			return ConstraintError(code = ConstraintErrorCode.CREDIT_CARD_CHECKSUM_INVALID)
		}
		
		return null
	}
	
	/**
	 * Checks if a text value is a valid credit card number.
	 *
	 * Validates that the [value] contains only digits and passes the Luhn algorithm checksum.
	 *
	 * @param value The text value to validate (type [String]?).
	 * @return `true` if the value is a valid credit card number, `false` otherwise.
	 */
	fun isValid(value: CharSequence?): Boolean {
		if (value.isNullOrBlank() || !value.all { it.isDigit() }) return false
		val digits = value.map { it.digitToInt() }
		val checksum = digits.reversed()
			.mapIndexed { index, digit ->
				if (index % 2 == 1) {
					val doubled = digit * 2
					if (doubled > 9) doubled - 9 else doubled
				}
				else digit
			}
			.sum()
		
		return checksum % 10 == 0
	}
}