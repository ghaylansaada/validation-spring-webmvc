package io.ghaylan.validation.constraint.validator.temporal.dayin

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.time.DayOfWeek
import java.time.temporal.Temporal

object AllowedDaysValidator: ConstraintValidator<Temporal, AllowedDaysConstraint>() {
	
	override fun validate(
		value: Temporal?,
		constraint: AllowedDaysConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		val dayOfWeek = runCatching {
			DayOfWeek.from(value)
		}.getOrNull() ?: return null
		
		if (constraint.days.contains(dayOfWeek)) return null
		
		return ConstraintError(code = ConstraintErrorCode.DAY_OF_WEEK_NOT_ALLOWED, metadata = buildMap {
			put("allowed_days", constraint.days)
		})
	}
}