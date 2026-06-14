package io.ghaylan.validation.constraint.validator.string.password

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Password
import io.ghaylan.validation.constraint.annotation.Password.PasswordStrength
import kotlin.reflect.KClass

/** Constraint metadata for [@Password][Password]. */
data class PasswordConstraint(
	val minLength: Int,
	val maxLength: Int,
	val requireUppercase: Boolean,
	val requireLowercase: Boolean,
	val requireDigit: Boolean,
	val requireSpecialChar: Boolean,
	val allowedSpecialChars: String,
	val minEntropy: PasswordStrength,
	val noSequentialChars: Boolean,
	val noRepetitivePatterns: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()