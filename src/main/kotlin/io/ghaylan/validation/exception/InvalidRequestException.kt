package io.ghaylan.validation.exception

import io.ghaylan.validation.model.RequestErrorDefinition
import io.ghaylan.validation.model.ConstraintError
import org.springframework.http.HttpStatus

/**
 * Thrown by the core validation engine when one or more constraint violations are detected
 * against processing data.
 *
 * This is intended to be caught globally via a `@ControllerAdvice` or `@ExceptionHandler`
 * to easily map the internal structured errors into a standard client-facing payload.
 *
 * @property code An error category or classification indicator, defaulting to HTTP [HttpStatus.BAD_REQUEST].
 * @property errors The comprehensive collection of specific field or target validation failures.
 * @property message A high-level description summarizing the processing conflict or constraint failure.
 */
class InvalidRequestException(
	val code: RequestErrorDefinition? = null,
	val errors: List<ConstraintError<*>>,
	override val message: String = "The request could not be processed due to invalid or conflicting information.",
): RuntimeException(message)