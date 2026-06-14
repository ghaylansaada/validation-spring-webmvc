package io.ghaylan.validation.constraint.validator.string.email

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.regex.Pattern


object EmailValidator: ConstraintValidator<CharSequence, EmailConstraint>() {
	
	private val PATTERN = Pattern.compile("^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$")
	
	
	override fun validate(
		value: CharSequence?,
		constraint: EmailConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		if (isValid(value)) return null
		return ConstraintError(code = ConstraintErrorCode.EMAIL_INVALID_FORMAT)
	}
	
	fun isValid(value: CharSequence): Boolean {
		return PATTERN.matcher(value).matches()
	}
}