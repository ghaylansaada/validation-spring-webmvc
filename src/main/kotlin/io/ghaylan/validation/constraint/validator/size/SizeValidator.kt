package io.ghaylan.validation.constraint.validator.size

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object SizeValidator: ConstraintValidator<Any, SizeConstraint>() {
	
	override fun validate(
		value: Any?,
		constraint: SizeConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		
		value ?: return null
		
		val size = when (value) {
			is CharSequence -> value.length
			is Map<*, *> -> value.size
			is Collection<*> -> value.size
			is Array<*> -> value.size
			is ByteArray -> value.size
			is ShortArray -> value.size
			is IntArray -> value.size
			is LongArray -> value.size
			is FloatArray -> value.size
			is DoubleArray -> value.size
			is BooleanArray -> value.size
			is CharArray -> value.size
			else -> return null
		}
		
		val (underMinimumCode, exceedsMaximumCode) = when (value) {
			is CharSequence -> ConstraintErrorCode.TEXT_TOO_SHORT to ConstraintErrorCode.TEXT_TOO_LONG
			is Map<*, *> -> ConstraintErrorCode.OBJECT_TOO_SMALL to ConstraintErrorCode.OBJECT_TOO_LARGE
			else -> ConstraintErrorCode.COLLECTION_TOO_SMALL to ConstraintErrorCode.COLLECTION_TOO_LARGE
		}
		
		val data = buildMap {
			put("min", constraint.min)
			put("max", if (constraint.max == Int.MAX_VALUE) null else constraint.max)
		}
		
		// Validate against boundaries
		if (size < constraint.min) {
			return ConstraintError(code = underMinimumCode, metadata = data)
		}
		
		if (size > constraint.max) {
			return ConstraintError(code = exceedsMaximumCode, metadata = data)
		}
		
		return null
	}
}