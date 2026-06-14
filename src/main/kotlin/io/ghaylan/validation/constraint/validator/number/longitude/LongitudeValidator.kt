package io.ghaylan.validation.constraint.validator.number.longitude

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object LongitudeValidator: ConstraintValidator<Double, LongitudeConstraint>() {
	
	override fun validate(
		value: Double?,
		constraint: LongitudeConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		if (isValid(value)) return null
		return ConstraintError(code = ConstraintErrorCode.GEO_LONGITUDE_INVALID)
	}
	
	/**
	 * Checks if a decimal value is a valid longitude.
	 *
	 * A valid longitude is a [Double] value between -180.0 and 180.0 degrees, inclusive.
	 *
	 * @param longitude The decimal value to check (type [Double]).
	 * @return `true` if the value is a valid longitude, `false` otherwise.
	 */
	fun isValid(longitude: Double): Boolean {
		return longitude in -180.0..180.0
	}
}