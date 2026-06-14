package io.ghaylan.validation.constraint.validator.string.notcontains

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.annotation.Contain.StrOccMode
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object NotStrOccValidator: ConstraintValidator<CharSequence, NotStrOccConstraint>() {
	
	override fun validate(
		value: CharSequence?,
		constraint: NotStrOccConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		return when (constraint.mode) {
			StrOccMode.EQUALS -> {
				if (!value.contentEquals(constraint.value, constraint.ignoreCase)) return null
				buildError(message = "The value matches a prohibited sequence.", constraint = constraint)
			}
			
			StrOccMode.CONTAINS -> {
				if (!value.contains(constraint.value, constraint.ignoreCase)) return null
				buildError(message = "The value contains a prohibited substring segment.", constraint = constraint)
			}
			
			StrOccMode.STARTS_WITH -> {
				if (!value.startsWith(constraint.value, constraint.ignoreCase)) return null
				buildError(message = "The value starts with a prohibited prefix sequence.", constraint = constraint)
			}
			
			StrOccMode.ENDS_WITH -> {
				if (!value.endsWith(constraint.value, constraint.ignoreCase)) return null
				buildError(message = "The value ends with a prohibited suffix sequence.", constraint = constraint)
			}
		}
	}
	
	private fun buildError(
		message: String,
		constraint: NotStrOccConstraint,
	): ConstraintError<*> = ConstraintError(
		code = ConstraintErrorCode.TEXT_PATTERN_MISMATCH,
		message = message,
		metadata = buildMap {
			put("prohibited_value", constraint.value)
			put("ignore_case", constraint.ignoreCase)
		})
}