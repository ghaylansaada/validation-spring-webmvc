package io.ghaylan.validation.constraint.validator.string.currency

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.*

object CurrencyCodeValidator: ConstraintValidator<CharSequence, CurrencyCodeConstraint>() {
	
	private val currencies by lazy { fetchCurrencies() }
	
	
	override fun validate(
		value: CharSequence?,
		constraint: CurrencyCodeConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		val currency = value.toString()
		if (currencies.contains(currency)) return null
		return ConstraintError(code = ConstraintErrorCode.CURRENCY_CODE_INVALID)
	}
	
	private fun fetchCurrencies(): List<String> {
		return Currency.getAvailableCurrencies()
			.map(Currency::getCurrencyCode)
			.distinct()
			.sorted()
	}
}