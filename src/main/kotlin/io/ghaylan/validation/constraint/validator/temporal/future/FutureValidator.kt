package io.ghaylan.validation.constraint.validator.temporal.future

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

object FutureValidator: ConstraintValidator<Temporal, FutureConstraint>() {
	
	override fun validate(
		value: Temporal?,
		constraint: FutureConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val now = value.now()
		
		// 1. Instantly fail fast for past/present values before doing any math
		if (value.isBefore(now) || value == now) {
			return createError(
				code = ConstraintErrorCode.INSTANT_NOT_IN_FUTURE,
				min = now,
				max = calculateMaxTemporalOrNull(now, constraint))
		}
		
		// 2. Only perform limit calculations if constraints actually exist
		if (hasAnyConstraints(constraint)) {
			
			val maxTemporal = calculateMaxTemporalOrNull(now, constraint)
			
			if (value.isAfter(maxTemporal)) {
				return createError(
					code = ConstraintErrorCode.INSTANT_TOO_LATE,
					min = now,
					max = maxTemporal)
			}
		}
		
		return null
	}
	
	/**
	 * Fast, non-allocating check to determine if any bounds are defined.
	 */
	private fun hasAnyConstraints(constraint: FutureConstraint): Boolean {
		return constraint.withinYears > 0
				|| constraint.withinMonths > 0
				|| constraint.withinWeeks > 0
				|| constraint.withinDays > 0
				|| constraint.withinHours > 0
				|| constraint.withinMinutes > 0
				|| constraint.withinSeconds > 0
	}
	
	/**
	 * Sequentially updates the temporal anchor. Wrapped securely to handle type mismatches cleanly.
	 */
	private fun calculateMaxTemporalOrNull(anchor: Temporal, constraint: FutureConstraint): Temporal {
		var result = anchor
		if (constraint.withinYears > 0) result = result.plus(Period.ofYears(constraint.withinYears.toInt()))
		if (constraint.withinMonths > 0) result = result.plus(Period.ofMonths(constraint.withinMonths.toInt()))
		if (constraint.withinWeeks > 0) result = result.plus(Period.ofWeeks(constraint.withinWeeks.toInt()))
		if (constraint.withinDays > 0) result = result.plus(Duration.ofDays(constraint.withinDays))
		if (constraint.withinHours > 0) result = result.plus(Duration.ofHours(constraint.withinHours))
		if (constraint.withinMinutes > 0) result = result.plus(Duration.ofMinutes(constraint.withinMinutes))
		if (constraint.withinSeconds > 0) result = result.plus(Duration.ofSeconds(constraint.withinSeconds))
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