package io.ghaylan.validation.engine

import io.ghaylan.validation.exception.InvalidRequestException
import io.ghaylan.validation.model.RequestErrorDefinition
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorDefinition

/**
 * Fluent collector for building and aggregating [ConstraintError] instances across request sections.
 *
 * ```kotlin
 * val collector = RequestErrorCollector()
 *
 * collector.body(ConstraintErrorCode.VALUE_MISSING)
 *     .field("user.email")
 *     .message("Email is required")
 *
 * collector.throwIfNotEmpty(RequestError.ERROR_CODE)
 * ```
 */
class RequestErrorCollector {
	
	private val errorsBuilder = mutableListOf<ConstraintErrorBuilder<*>>()
	
	/**
	 * Adds a request body validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun <CodeT> body(code: CodeT) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition = add(ConstraintError.ErrorLocation.BODY, code)
	
	/**
	 * Adds a path variable validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun <CodeT> path(code: CodeT) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition = add(ConstraintError.ErrorLocation.PATH, code)
	
	/**
	 * Adds a query parameter validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun <CodeT> query(code: CodeT) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition = add(ConstraintError.ErrorLocation.QUERY, code)
	
	/**
	 * Adds an HTTP header validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun <CodeT> header(code: CodeT) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition = add(ConstraintError.ErrorLocation.HEADER, code)
	
	/**
	 * Adds a business logic validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun <CodeT> business(code: CodeT) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition = add(ConstraintError.ErrorLocation.BUSINESS, code)
	
	/**
	 * Internal helper to add errors with the specified location.
	 */
	private fun <CodeT> add(
		location: ConstraintError.ErrorLocation,
		code: CodeT
	): ConstraintErrorBuilder<CodeT> where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition {
		return ConstraintErrorBuilder(location, code).also(errorsBuilder::add)
	}
	
	/**
	 * Throws [InvalidRequestException] if any errors have been collected.
	 */
	fun <CodeT> throwIfNotEmpty(
		code: CodeT,
		message: String? = null
	) where CodeT : Enum<CodeT>, CodeT : RequestErrorDefinition {
		if (errorsBuilder.isEmpty()) return
		throw InvalidRequestException(errors = collect(), code = code, message = message ?: code.message)
	}
	
	/**
	 * Finalizes all builders and returns the collected errors.
	 */
	fun collect(): List<ConstraintError<*>> {
		val size = errorsBuilder.size
		if (size == 0) return emptyList()
		val allErrors = ArrayList<ConstraintError<*>>(size)
		
		for (builder in errorsBuilder) {
			allErrors.add(builder.build())
		}
		
		return allErrors
	}
}