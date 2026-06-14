package io.ghaylan.validation.constraint.validator.string.language

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.util.*

object LanguageValidator: ConstraintValidator<CharSequence, LanguageConstraint>() {
	
	// Pre-computes and caches supported standard locale configurations on-demand
	private val validLanguageTags by lazy(LazyThreadSafetyMode.PUBLICATION) { fetchAllLanguages() }
	
	
	override fun validate(
		value: CharSequence?,
		constraint: LanguageConstraint,
		context: ValidationContext
	): ConstraintError<*>? {
		value ?: return null
		val inputTag = value.toString()
		if (validLanguageTags.contains(inputTag)) return null
		return ConstraintError(code = ConstraintErrorCode.LANGUAGE_CODE_INVALID)
	}
	
	private fun fetchAllLanguages(): Set<String> {
		val structuralFormat = Regex("^[a-z]{2}(-[A-Z]{2})?$") // Filters for strict 'xx' or 'xx-YY' variants
		
		return Locale.getAvailableLocales()
			.map { it.toLanguageTag() }
			.filter { structuralFormat.matches(it) }
			.toSet() // Converted to Set for O(1) lookups during high-throughput verification calls
	}
	
}