package io.ghaylan.validation.constraint.validator.temporal.past

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.ext.isAfter
import io.ghaylan.validation.ext.isBefore
import io.ghaylan.validation.ext.now
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.time.Duration
import java.time.Period
import java.time.temporal.Temporal

object PastValidator: ConstraintValidator<Temporal, PastConstraint>() {
	
	 override fun validate(
		value: Temporal?,
		constraint: PastConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		
		value ?: return null
		
		val now = value.now()
		 
		 // 1. Fast fail if the value is in the future or exactly 'now'
		if (value.isAfter(now) || value == now) {
			return createError(
				code = ConstraintErrorCode.INSTANT_NOT_IN_PAST,
				min = calculateMinTemporalOrNull(now, constraint),
				max = now)
		}
		
		// 2. Only calculate past boundaries if constraints are actually specified
		if (hasAnyConstraints(constraint)) {
			val minTemporal = calculateMinTemporalOrNull(now, constraint)
			
			// If the value is further back in the past than our allowed floor limit
			if (value.isBefore(minTemporal)) {
				return createError(
					code = ConstraintErrorCode.INSTANT_TOO_EARLY,
					min = minTemporal,
					max = now)
			}
		}
		
		return null
	}
	
	/**
	 * Non-allocating check to determine if any history window limitations are defined.
	 */
	private fun hasAnyConstraints(constraint: PastConstraint): Boolean {
		return constraint.withinYears > 0
				|| constraint.withinMonths > 0
				|| constraint.withinWeeks > 0
				|| constraint.withinDays > 0
				|| constraint.withinHours > 0
				|| constraint.withinMinutes > 0
				|| constraint.withinSeconds > 0
	}
	
	/**
	 * Safe temporal anchor deduction calculation utilizing minus adjustments.
	 */
	private fun calculateMinTemporalOrNull(anchor: Temporal, constraint: PastConstraint): Temporal {
		var result = anchor
		if (constraint.withinYears > 0) result = result.minus(Period.ofYears(constraint.withinYears.toInt()))
		if (constraint.withinMonths > 0) result = result.minus(Period.ofMonths(constraint.withinMonths.toInt()))
		if (constraint.withinWeeks > 0) result = result.minus(Period.ofWeeks(constraint.withinWeeks.toInt()))
		if (constraint.withinDays > 0) result = result.minus(Duration.ofDays(constraint.withinDays))
		if (constraint.withinHours > 0) result = result.minus(Duration.ofHours(constraint.withinHours))
		if (constraint.withinMinutes > 0) result = result.minus(Duration.ofMinutes(constraint.withinMinutes))
		if (constraint.withinSeconds > 0) result = result.minus(Duration.ofSeconds(constraint.withinSeconds))
		return result
	}
	
	private fun createError(
		code: ConstraintErrorCode,
		min: Temporal,
		max: Temporal,
	): ConstraintError<*> {
		return ConstraintError(
			code = code,
			metadata = buildMap {
				put("min", min)
				put("max", max)
			})
	}
	
}