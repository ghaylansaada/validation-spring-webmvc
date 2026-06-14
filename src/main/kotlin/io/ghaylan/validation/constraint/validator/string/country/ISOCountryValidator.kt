package io.ghaylan.validation.constraint.validator.string.country

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.*

object ISOCountryValidator: ConstraintValidator<CharSequence, ISOCountryConstraint>() {
	
	// Pre-computes and caches supported standard country symbols on-demand
	private val validCountryCodes: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
		Locale.getISOCountries().toSet()
	}
	
	
	override fun validate(
		value: CharSequence?,
		constraint: ISOCountryConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		val countryCode = value.toString()
		if (validCountryCodes.contains(countryCode)) return null
		return ConstraintError(code = ConstraintErrorCode.COUNTRY_CODE_INVALID)
	}
}