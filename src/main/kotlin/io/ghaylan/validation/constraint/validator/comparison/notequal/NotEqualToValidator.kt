package io.ghaylan.validation.constraint.validator.comparison.notequal

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object NotEqualToValidator: ConstraintValidator<Comparable<*>, NotEqualToConstraint>() {
	
	 override fun validate(
		value: Comparable<*>?,
		constraint: NotEqualToConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		val otherValue = super.getPropertyValue(name = constraint.property, context = context)
		
		if (otherValue != null) {
			// 1. Strict Type Alignment Guard
			if (!value.javaClass.isInstance(otherValue)) {
				return ConstraintError(
					code = ConstraintErrorCode.VALUE_COMPARISON_MISMATCH,
					metadata = buildMap {
						put("input_type", value::class.simpleName)
						put("ref_name", constraint.property)
						put("ref_type", otherValue::class.simpleName)
					}
				)
			}
			
			@Suppress("UNCHECKED_CAST")
			val comparison = (value as Comparable<Any>).compareTo(otherValue)
			
			if (comparison != 0) return null
		} else {
			// If otherValue is null, a non-null actual value is structurally not equal to it.
			return null
		}
		
		// 2. Evaluation Rules Failure
		return ConstraintError(
			code = ConstraintErrorCode.VALUE_COLLIDING_WITH_REFERENCE,
			metadata = buildMap {
				put("ref_name", constraint.property)
			}
		)
	}
}