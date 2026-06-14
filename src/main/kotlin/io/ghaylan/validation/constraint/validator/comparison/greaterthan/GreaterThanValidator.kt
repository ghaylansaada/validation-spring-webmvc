package io.ghaylan.validation.constraint.validator.comparison.greaterthan

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.time.temporal.Temporal
import java.util.*

object GreaterThanValidator: ConstraintValidator<Comparable<*>, GreaterThanConstraint>() {
	
	 override fun validate(
		value: Comparable<*>?,
		constraint: GreaterThanConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val otherValue = getPropertyValue(name = constraint.property, context = context) ?: return null
		
		// 1. Strict Type Alignment Guard
		if (!value.javaClass.isInstance(otherValue)) {
			return ConstraintError(
				code = ConstraintErrorCode.VALUE_COMPARISON_MISMATCH,
				metadata = buildMap {
					put("ref_name", constraint.property)
					put("input_type", value::class.simpleName)
					put("ref_type", otherValue::class.simpleName)
				}
			)
		}
		
		@Suppress("UNCHECKED_CAST")
		val comparison = (value as Comparable<Any>).compareTo(otherValue)
		
		// 2. Evaluation Rules Check
		val isViolation = if (constraint.inclusive) comparison < 0 else comparison <= 0
		
		if (isViolation) {
			
			val code = when (value) {
				is Date,
				is Temporal -> ConstraintErrorCode.INSTANT_TOO_EARLY
				is Number -> ConstraintErrorCode.NUMBER_TOO_SMALL
				else -> return null
			}
			
			return ConstraintError(
				code = code,
				message = "The value failed the relative greater-than comparison constraint.",
				metadata = buildMap {
					put("ref_name", constraint.property)
					put("inclusive", constraint.inclusive)
				}
			)
		}
		
		return null
	}
	
}