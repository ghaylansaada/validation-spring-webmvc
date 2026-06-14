package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.password.PasswordConstraint
import io.ghaylan.validation.constraint.validator.string.password.PasswordValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence satisfies configurable
 * password complexity and algorithmic strength criteria.
 *
 * This annotation provides comprehensive protection boundaries for identity verification layers.
 * It supports structural length limitations, explicit character diversity requirements (including
 * mandatory case switching, numerical digits, and custom symbols), algorithmic complexity analysis
 * using Shannon entropy thresholds, and sequential or repetitive pattern rejection flags to intercept
 * predictable user choices.
 *
 * @property minLength The minimum acceptable character count (inclusive). Defaults to `6`.
 * @property maxLength The maximum acceptable character count (inclusive). Defaults to `64`.
 * @property requireUppercase Enforces the presence of at least one uppercase alphabetic character. Defaults to `false`.
 * @property requireLowercase Enforces the presence of at least one lowercase alphabetic character. Defaults to `false`.
 * @property requireDigit Enforces the presence of at least one numerical digit. Defaults to `false`.
 * @property requireSpecialChar Enforces the presence of at least one non-alphanumeric symbol from [allowedSpecialChars]. Defaults to `false`.
 * @property allowedSpecialChars A literal sequence containing all symbols recognized as valid targets for [requireSpecialChar].
 * @property minEntropy The minimal [PasswordStrength] threshold evaluated via Shannon entropy calculations. Defaults to [PasswordStrength.VERY_WEAK].
 * @property noSequentialChars Rejects highly predictable alphanumeric character progressions (such as `"1234"` or `"abcd"`). Defaults to `false`.
 * @property noRepetitivePatterns Rejects immediate repeating substrings or simple repeating cycles (such as `"abcabc"` or `"aaaa"`). Defaults to `false`.
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = PasswordConstraint::class, validatedBy = [PasswordValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Password(
	val minLength: Int = 6,
	val maxLength: Int = 64,
	val requireUppercase: Boolean = false,
	val requireLowercase: Boolean = false,
	val requireDigit: Boolean = false,
	val requireSpecialChar: Boolean = false,
	val allowedSpecialChars: String = "!@#$%^&*()-_=+[{]};:,<.>/?",
	val minEntropy: PasswordStrength = PasswordStrength.VERY_WEAK,
	val noSequentialChars: Boolean = false,
	val noRepetitivePatterns: Boolean = false,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
) {
	
	/**
	 * Dictates the required information density thresholds for security analysis, mapped to Shannon entropy values in bits.
	 *
	 * @property entropy The lower bit boundary required to satisfy the strength tier.
	 */
	enum class PasswordStrength(val entropy: Int) {
		
		/** Unrestricted informational density checking; satisfies any state matching an entropy >= 0 bits. */
		VERY_WEAK(0),
		
		/** Basic informational density checking; typically requires an entropy >= 28 bits. */
		WEAK(28),
		
		/** Intermediate informational density checking; typically requires an entropy >= 36 bits. */
		MODERATE(36),
		
		/** Advanced corporate or platform informational density checking; typically requires an entropy >= 60 bits. */
		STRONG(60),
		
		/** High-security cryptographic or administrative informational density checking; typically requires an entropy >= 128 bits. */
		VERY_STRONG(128)
	}
}