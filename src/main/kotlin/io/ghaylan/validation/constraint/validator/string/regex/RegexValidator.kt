package io.ghaylan.validation.constraint.validator.string.regex

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.regex.Pattern

object RegexValidator: ConstraintValidator<CharSequence, RegexConstraint>() {
	
	override fun validate(
		value: CharSequence?,
		constraint: RegexConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val pattern = Pattern.compile(constraint.pattern)
		
		if (pattern.matcher(value).matches()) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.TEXT_PATTERN_MISMATCH,
			metadata = buildMap {
				put("format_type", constraint.name)
			})
	}
	
}