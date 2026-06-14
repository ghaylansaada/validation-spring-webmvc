package io.ghaylan.validation.constraint.validator.number.max

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object NumberMaxValidator: ConstraintValidator<Number, NumberMaxConstraint>() {
	
	 override fun validate(
		value: Number?,
		constraint: NumberMaxConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		 value ?: return null
		
		val asDouble = value.toDouble()
		 
		 // Fast-fail check for NaN or infinite bounds to protect calculation integrity
		if (!asDouble.isFinite()) {
			return ConstraintError(code = ConstraintErrorCode.NUMBER_FORMAT_INVALID)
		}
		
		// Evaluate boundary condition
		val isValid = if (constraint.inclusive) {
			asDouble <= constraint.value
		} else {
			asDouble < constraint.value
		}
		
		if (isValid) return null
		 
		 return ConstraintError(
			 code = ConstraintErrorCode.NUMBER_TOO_LARGE,
			 metadata = buildMap {
				 put("max", constraint.value)
				 put("inclusive", constraint.inclusive)
			 }
		 )
	}
	
}