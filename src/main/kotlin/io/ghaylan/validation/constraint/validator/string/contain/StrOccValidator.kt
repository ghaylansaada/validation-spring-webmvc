package io.ghaylan.validation.constraint.validator.string.contain

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.annotation.Contain.StrOccMode
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object StrOccValidator: ConstraintValidator<CharSequence, StrOccConstraint>() {
	
	override fun validate(
		value: CharSequence?,
		constraint: StrOccConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		// 1. Structural Match Condition Evaluation
		when (constraint.mode) {
			StrOccMode.EQUALS -> {
				if (value.contentEquals(constraint.value, constraint.ignoreCase)) return null
				return buildError(message = "The value does not equal the required text sequence.", constraint = constraint)
			}
			
			StrOccMode.CONTAINS -> {
				if (!value.contains(constraint.value, constraint.ignoreCase)) {
					return buildError(message = "The value does not contain the required substring segment.", constraint = constraint)
				}
			}
			
			StrOccMode.STARTS_WITH -> {
				if (!value.startsWith(constraint.value, constraint.ignoreCase)) {
					return buildError(message = "The value does not start with the required prefix sequence.", constraint = constraint)
				}
			}
			
			StrOccMode.ENDS_WITH -> {
				if (!value.endsWith(constraint.value, constraint.ignoreCase)) {
					return buildError(message = "The value does not end with the required suffix sequence.", constraint = constraint)
				}
			}
		}

		// 2. Frequency Boundary Check (Only evaluated if structural filters pass)
		val actualOccurrences = countOccurrences(value.toString(), constraint.value, constraint.ignoreCase)
		if (actualOccurrences !in constraint.minOccurrences..constraint.maxOccurrences) {
			return ConstraintError(
				code = ConstraintErrorCode.TEXT_PATTERN_MISMATCH,
				message = "The targeted sequence count falls outside the permitted frequency limits.",
				metadata = buildMap {
					put("expected_value", constraint.value)
					put("min_occurrences", constraint.minOccurrences)
					put("max_occurrences", constraint.maxOccurrences)
					put("actual_occurrences", actualOccurrences)
					put("ignore_case", constraint.ignoreCase)
				}
			)
		}
		
		return null
	}
	
	/**
	 * Scans text sequentially to calculate non-overlapping string match segments.
	 */
	private fun countOccurrences(text: String, sub: String, ignoreCase: Boolean): Int {
		if (sub.isEmpty()) return 0
		var count = 0
		var index = 0
		
		while (true) {
			index = text.indexOf(sub, index, ignoreCase)
			if (index < 0) break
			count++
			index += sub.length
		}
		
		return count
	}
	
	private fun buildError(
		message: String,
		constraint: StrOccConstraint,
	): ConstraintError<*> = ConstraintError(
		code = ConstraintErrorCode.TEXT_PATTERN_MISMATCH,
		message = message,
		metadata = buildMap {
			put("prohibited_value", constraint.value)
			put("ignore_case", constraint.ignoreCase)
		})
}