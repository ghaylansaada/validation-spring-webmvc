package io.ghaylan.validation.constraint.validator.temporal.min

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.ext.isAfter
import io.ghaylan.validation.ext.isEqual
import io.ghaylan.validation.ext.toTemporal
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.time.temporal.Temporal

object TemporalMinValidator: ConstraintValidator<Temporal, TemporalMinConstraint>() {
	
	 override fun validate(
		value: Temporal?,
		constraint: TemporalMinConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val minValue = constraint.value.toTemporal(value::class)
		
		// Evaluate baseline chronological rules
		val isValid = if (constraint.inclusive) {
			value.isAfter(minValue) || value.isEqual(minValue)
		} else {
			value.isAfter(minValue)
		}
		
		if (isValid) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.INSTANT_TOO_EARLY,
			metadata = buildMap {
				put("min", constraint.value)
				put("inclusive", constraint.inclusive)
			})
	}
	
}