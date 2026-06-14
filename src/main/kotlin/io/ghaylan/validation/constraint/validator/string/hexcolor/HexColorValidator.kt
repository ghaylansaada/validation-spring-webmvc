package io.ghaylan.validation.constraint.validator.string.hexcolor

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.regex.Pattern

object HexColorValidator: ConstraintValidator<CharSequence, HexColorConstraint>() {
	
	private val PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
	
	
	override fun validate(
		value: CharSequence?,
		constraint: HexColorConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		if (isValid(value)) return null
		return ConstraintError(code = ConstraintErrorCode.HEX_COLOR_INVALID)
	}
	
	fun isValid(value: CharSequence): Boolean {
		return PATTERN.matcher(value).matches()
	}
}