package io.ghaylan.validation.constraint.validator.comparison.valuenotin

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import io.ghaylan.validation.utils.ReflectionUtils

object ValueNotInValidator: ConstraintValidator<Any, ValueNotInConstraint>() {
	
	override fun validate(
		value: Any?,
		constraint: ValueNotInConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		if (!ReflectionUtils.isScalar(value)) return null
		
		if (value.toString() !in constraint.values) return null
		
		return ConstraintError(
			code = ConstraintErrorCode.VALUE_NOT_ALLOWED,
			message = "The provided value is present in the predefined exclusion list.",
			metadata = buildMap {
				put("exclusion_list", constraint.values)
			})
	}
	
}