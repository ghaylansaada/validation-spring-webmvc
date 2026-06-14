package io.ghaylan.validation.constraint.validator.comparison.equal

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object EqualToValidator: ConstraintValidator<Comparable<*>, EqualToConstraint>() {

	 override fun validate(
		value: Comparable<*>?,
		constraint: EqualToConstraint,
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
			val isIdentical = (value as Comparable<Any>).compareTo(otherValue) == 0
			if (isIdentical) return null
		}
		
		// 2. Evaluation Rules Failure
		return ConstraintError(
			code = ConstraintErrorCode.VALUE_NOT_MATCHING_REFERENCE,
			metadata = buildMap {
				put("ref_name", constraint.property)
			}
		)
	}
}