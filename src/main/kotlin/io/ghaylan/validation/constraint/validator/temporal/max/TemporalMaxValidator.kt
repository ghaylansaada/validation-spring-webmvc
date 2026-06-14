package io.ghaylan.validation.constraint.validator.temporal.max

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.ext.isBefore
import io.ghaylan.validation.ext.isEqual
import io.ghaylan.validation.ext.toTemporal
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.time.temporal.Temporal

object TemporalMaxValidator: ConstraintValidator<Temporal, TemporalMaxConstraint>() {
	
	 override fun validate(
		value: Temporal?,
		constraint: TemporalMaxConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val maxValue = constraint.value.toTemporal(value::class)
		
		// Evaluate baseline chronological rules based on inclusivity
		val isValid = if (constraint.inclusive) {
			value.isBefore(maxValue) || value.isEqual(maxValue)
		} else {
			value.isBefore(maxValue)
		}
		
		if (isValid) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.INSTANT_TOO_LATE,
			metadata = buildMap {
				put("max", constraint.value)
				put("inclusive", constraint.inclusive)
			})
	}
	
}