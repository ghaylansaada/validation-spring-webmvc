package io.ghaylan.validation.constraint.validator.string.iban

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object IBANValidator: ConstraintValidator<CharSequence, IBANConstraint>() {
	
	// Sourced country codes mapping to mandatory ISO 13616 registration lengths
	private val ibanCountryLengths = mapOf(
		"AL" to 28, "AD" to 24, "AT" to 20, "AZ" to 28, "BH" to 22, "BE" to 16,
		"BA" to 20, "BR" to 29, "BG" to 22, "CR" to 22, "HR" to 21, "CY" to 28,
		"CZ" to 24, "DK" to 18, "DO" to 28, "EE" to 20, "FO" to 18, "FI" to 18,
		"FR" to 27, "GE" to 22, "DE" to 22, "GI" to 23, "GR" to 27, "GL" to 18,
		"GT" to 28, "HU" to 28, "IS" to 26, "IE" to 22, "IL" to 23, "IT" to 27,
		"JO" to 30, "KZ" to 20, "KW" to 30, "LV" to 21, "LB" to 28, "LI" to 21,
		"LT" to 20, "LU" to 20, "MT" to 31, "MR" to 27, "MU" to 30, "MC" to 27,
		"MD" to 24, "ME" to 22, "NL" to 18, "MK" to 19, "NO" to 15, "PK" to 24,
		"PS" to 29, "PL" to 28, "PT" to 25, "QA" to 29, "RO" to 24, "SM" to 27,
		"SA" to 24, "RS" to 22, "SK" to 24, "SI" to 19, "ES" to 24, "SE" to 24,
		"CH" to 21, "TN" to 24, "TR" to 26, "AE" to 23, "GB" to 22, "VG" to 24)
	
	
	 override fun validate(
		value: CharSequence?,
		constraint: IBANConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		// 1. Sanitize whitespaces and enforce baseline alpha-numeric uniformity
		val cleanIban = value.toString().filterNot(Char::isWhitespace).uppercase()
		
		if (cleanIban.length < 4 || !cleanIban.all { it.isDigit() || it in 'A'..'Z' }) {
			return ConstraintError(
				code = ConstraintErrorCode.IBAN_LENGTH_INVALID,
				message = "The account number layout structure or total length is invalid.")
		}
		
		// 2. Extract and match country specifications
		val countryCode = cleanIban.take(2)
		val expectedLength = ibanCountryLengths[countryCode]
			?: return ConstraintError(
				code = ConstraintErrorCode.IBAN_COUNTRY_CODE_INVALID,
				message = "The account number contains an unsupported or unrecognized country code.")
		
		// 3. Confirm expected string bounds match specific ISO country files
		if (cleanIban.length != expectedLength) {
			return ConstraintError(
				code = ConstraintErrorCode.IBAN_LENGTH_INVALID,
				message = "The account number length does not match requirements for the specified country region.",
				metadata = buildMap {
					put("expected_length", expectedLength)
				})
		}
		
		// 4. Evaluate checksum calculation using Modulo-97 algorithm rules
		if (!verifyMod97Checksum(cleanIban)) {
			return ConstraintError(
				code = ConstraintErrorCode.IBAN_CHECKSUM_INVALID,
				message = "The bank account number mathematical validation checksum failed.")
		}
		
		return null
	}
	
	/**
	 * Rearranges and verifies alphanumeric input values against modulo 97 rules.
	 * Moves the country code prefix to the end and evaluates individual character offsets sequentially.
	 */
	private fun verifyMod97Checksum(iban: String): Boolean {
		val rearranged = iban.substring(4) + iban.substring(0, 4)
		var remainder = 0
		
		for (i in rearranged.indices) {
			val ch = rearranged[i]
			if (ch.isDigit()) {
				remainder = (remainder * 10 + (ch - '0')) % 97
			} else {
				val letterNumericValue = ch - 'A' + 10
				remainder = (remainder * 10 + letterNumericValue / 10) % 97
				remainder = (remainder * 10 + letterNumericValue % 10) % 97
			}
		}
		
		return remainder == 1
	}
	
}