package io.ghaylan.validation.constraint.validator.required

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.annotation.Required.RequirementCondition
import io.ghaylan.validation.ext.isDeepNullOrEmpty
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object RequiredValidator: ConstraintValidator<Any, RequiredConstraint>() {
	
	override fun validate(
		value: Any?,
		constraint: RequiredConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		
		// 1. Determine if the current field's value is missing based on configuration
		val isMissing = if (constraint.allowEmpty) {
			value == null
		} else {
			value.isDeepNullOrEmpty()
		}
		
		// If the field is present, no further conditional/requirement logic is needed
		if (!isMissing) return null
		
		// Determine the error code to return based on the structural state of the value
		val errorCode = if (value == null) ConstraintErrorCode.VALUE_MISSING else ConstraintErrorCode.VALUE_EMPTY
		val baseMessage = if (value == null) "Field can not be null" else "Field can not be empty"
		
		// 2. Unconditional evaluation
		if (constraint.condition == RequirementCondition.ALWAYS || constraint.dependentField.isBlank()) {
			return ConstraintError(code = errorCode, message = baseMessage)
		}
		
		// 3. Conditional evaluation against sibling properties
		val dependentValue = super.getPropertyValue(name = constraint.dependentField, context = context)
		val isDependentMissing = if (constraint.allowEmpty) {
			dependentValue == null
		} else {
			value.isDeepNullOrEmpty()
		}
		
		if (constraint.condition == RequirementCondition.IF_DEPENDENT_NULL && isDependentMissing) {
			return ConstraintError(code = errorCode, message = "$baseMessage (Triggered by missing field: '${constraint.dependentField}').")
		}
		
		if (constraint.condition == RequirementCondition.IF_DEPENDENT_NOT_NULL && !isDependentMissing) {
			return ConstraintError(code = errorCode, message = "$baseMessage (Triggered by provided field: '${constraint.dependentField}').")
		}
		
		return null
	}
	
}