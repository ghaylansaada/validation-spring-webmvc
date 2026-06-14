package io.ghaylan.validation.constraint.validator.comparison.valuein

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import io.ghaylan.validation.utils.ReflectionUtils

object ValueInValidator: ConstraintValidator<Any, ValueInConstraint>() {
	
	override fun validate(
		value: Any?,
		constraint: ValueInConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		
		if (!ReflectionUtils.isScalar(value)) return null
		
		if (value.toString() in constraint.values) return null
		
		return ConstraintError(code = ConstraintErrorCode.VALUE_NOT_ALLOWED,
			message = "The provided value is not present within the predefined allowed options list.",
			metadata = buildMap {
				put("allowed_list", constraint.values)
			})
	}
	
}