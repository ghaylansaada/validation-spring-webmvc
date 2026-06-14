package io.ghaylan.validation.constraint.validator.number.latitude

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object LatitudeValidator: ConstraintValidator<Double, LatitudeConstraint>() {
	
	override fun validate(
		value: Double?,
		constraint: LatitudeConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		if (isValid(value)) return null
		return ConstraintError(code = ConstraintErrorCode.GEO_LATITUDE_INVALID)
	}
	
	/**
	 * Checks if a decimal value is a valid latitude.
	 *
	 * A valid latitude is a [Double] value between -90.0 and 90.0 degrees, inclusive.
	 *
	 * @param latitude The decimal value to check (type [Double]).
	 * @return `true` if the value is a valid latitude, `false` otherwise.
	 */
	fun isValid(latitude: Double): Boolean {
		return latitude in -90.0..90.0
	}
}