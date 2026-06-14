package io.ghaylan.validation.constraint.validator.string.password

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import kotlin.math.log2

object PasswordValidator : ConstraintValidator<CharSequence, PasswordConstraint>() {

     override fun validate(
        value: CharSequence?,
        constraint: PasswordConstraint,
        context: ValidationContext
    ) : ConstraintError<*>?
    {
        value ?: return null
	    
	    // 1. Minimum Length Rule Check
		if (value.length < constraint.minLength) {
			return ConstraintError(
				code = ConstraintErrorCode.PASSWORD_TOO_SHORT,
				metadata = buildMap {
					put("min_length", constraint.minLength)
				}
			)
		}
	    
	    // 2. Maximum Length Rule Check
		if (value.length > constraint.maxLength) {
			return ConstraintError(
				code = ConstraintErrorCode.PASSWORD_TOO_LONG,
				metadata = buildMap {
					put("max_length", constraint.maxLength)
				}
			)
		}

	    // 3. Uppercase Character Presence Check
		if (constraint.requireUppercase && value.none(Char::isUpperCase)) {
			return ConstraintError(code = ConstraintErrorCode.PASSWORD_UPPERCASE_MISSING)
		}
	    
	    // 4. Lowercase Character Presence Check
		if (constraint.requireLowercase && value.none(Char::isLowerCase)) {
			return ConstraintError(code = ConstraintErrorCode.PASSWORD_LOWERCASE_MISSING)
		}
	    
	    // 5. Numeric Digit Presence Check
		if (constraint.requireDigit && value.none(Char::isDigit)) {
			return ConstraintError(code = ConstraintErrorCode.PASSWORD_DIGIT_MISSING)
		}
	    
	    // 6. Special Character Presence Check
		if (constraint.requireSpecialChar && value.none { constraint.allowedSpecialChars.contains(it) }) {
			return ConstraintError(
				code = ConstraintErrorCode.PASSWORD_SPECIAL_CHARACTER_MISSING,
				metadata = buildMap {
					put("allowed_special_characters", constraint.allowedSpecialChars)
				})
		}
	    
	    // 7. Information Density / Entropy Strength Check
		val computedEntropy = calculateEntropy(value)
		if (constraint.minEntropy.entropy > computedEntropy) {
			return ConstraintError(
				code = ConstraintErrorCode.PASSWORD_TOO_WEAK,
				metadata = buildMap {
					put("min_required_entropy", constraint.minEntropy.entropy)
					put("actual_entropy", computedEntropy)
					put("strength_tier", constraint.minEntropy.name)
				})
		}
	    
        return null
    }

    /**
     * Calculates the entropy of a text value for password validation.
     *
     * Estimates the entropy based on the character set size (lowercase, uppercase, digits, special
     * characters) and the length of the [value]. Returns 0.0 for empty strings.
     *
     * @param value The text value to analyze (type [String]).
     * @return The calculated entropy in bits (type [Double]).
     */
    private fun calculateEntropy(value : CharSequence) : Double {
        if (value.isEmpty()) return 0.0

        var charsetSize = 0
        var hasLower = false
        var hasUpper = false
        var hasDigit = false
        var hasSpecial = false

        for (char in value) {
            when {
                char.isLowerCase() -> hasLower = true
                char.isUpperCase() -> hasUpper = true
                char.isDigit() -> hasDigit = true
                else -> hasSpecial = true
            }

            // Exit early if all character types are found
            if (hasLower && hasUpper && hasDigit && hasSpecial) break
        }

        // Estimate charset size based on detected character types
        if (hasLower) charsetSize += 26
        if (hasUpper) charsetSize += 26
        if (hasDigit) charsetSize += 10
        if (hasSpecial) charsetSize += 32 // Common printable symbols

        return if (charsetSize == 0) 0.0 else value.length * log2(charsetSize.toDouble())
    }
}