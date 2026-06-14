package io.ghaylan.validation.constraint.validator.number.multiple

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.math.BigDecimal

object MultipleOfValidator: ConstraintValidator<Number, MultipleOfConstraint>() {
	
	 override fun validate(
		value: Number?,
		constraint: MultipleOfConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val factor = constraint.factor
		val asDouble = value.toDouble()
		
		// 1. Guard against unparseable or infinite numeric states
		if (!asDouble.isFinite() || !factor.isFinite()) {
			return ConstraintError(code = ConstraintErrorCode.NUMBER_FORMAT_INVALID)
		}
		
		// Configuration guard: a factor of 0.0 or negative skips evaluation based on design rules
		if (factor <= 0.0) return null
		
		// 2. Perform an exact decimal arithmetic precision check
		val numberBD = BigDecimal(value.toString())
		val factorBD = BigDecimal(factor.toString())
		
		val isMultiple = numberBD.remainder(factorBD).compareTo(BigDecimal.ZERO) == 0
		
		if (isMultiple) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.NUMBER_NOT_MULTIPLE,
			metadata = buildMap {
				put("factor", factor)
			}
		)
	}
}