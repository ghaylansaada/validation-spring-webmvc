package io.ghaylan.validation.constraint.validator.number.min

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object NumberMinValidator: ConstraintValidator<Number, NumberMinConstraint>() {
	
	 override fun validate(
		value: Number?,
		constraint: NumberMinConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val asDouble = value.toDouble()
		
		// Guard clause to catch structural anomalies like Double.NaN
		if (!asDouble.isFinite()) {
			return ConstraintError(code = ConstraintErrorCode.NUMBER_FORMAT_INVALID)
		}
		
		// Evaluate boundary condition based on inclusivity
		val isValid = if (constraint.inclusive) {
			asDouble >= constraint.value
		} else {
			asDouble > constraint.value
		}
		
		if (isValid) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.NUMBER_TOO_SMALL,
			metadata = buildMap {
				put("min", constraint.value)
				put("inclusive", constraint.inclusive)
			}
		)
	}
	
}