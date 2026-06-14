package io.ghaylan.validation.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a single validation error returned to the API caller.
 *
 * @property path Dot/bracket path of the invalid field (e.g., `"user.address[0].city"`).
 * @property code Error classification (e.g., [ConstraintErrorCode.VALUE_EMPTY]).
 * @property message Optional human-readable error message.
 * @property location Request section where the error originated (body, query, header, path, or business).
 * @property metadata Optional additional context data.
 */
@Schema(name = "ConstraintError", description = "Detailed error entry for failed API request.")
data class ConstraintError<CodeT>(
	
	@Schema(
		description = """
            Fully qualified path of the invalid parameter relative to the request model. 
            Null if the error is not tied to a specific parameter.
            Examples:
            - `"field"`
            - `"field.nested"`
            - `"field[0]"`
            - `"field[0][1]"`
            - `"field[0].nested"`
            - `"field[0][1].nested"`
            - `"[0]nested[0][1].nested"`""",
		nullable = true,
		example = "param")
	val path: String? = null,
	
	@Schema(
		description = "Classification of the error.",
		enumAsRef = true,
		example = "MUST_NOT_NULL",
		implementation = ConstraintErrorCode::class)
	val code: CodeT? = null,
	
	@Schema(
		description = "Human-readable error message.",
		example = "Param is required",
		nullable = true)
	var message: String? = null,
	
	@Schema(
		description = "Location in the request where the invalid value originated.",
		enumAsRef = true,
		example = "QUERY",
		implementation = ErrorLocation::class)
	val location: ErrorLocation? = null,
	
	@Schema(
		description = "Optional additional context data for the error.",
		example = """{ "key": "value" }""",
		nullable = true)
	val metadata: Any? = null
	
) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition {
	
	/**
	 * Lists the high-level location within an HTTP request that produced a validation error.
	 *
	 * Use these to help clients decide how to highlight or map errors back to UI inputs.
	 */
	enum class ErrorLocation {
		
		/** Error in a URL query parameter. */
		QUERY,
		
		/** Error in an HTTP header value. */
		HEADER,
		
		/** Error in a URI path variable (templated segment). */
		PATH,
		
		/** Error in the request body payload (e.g., JSON, XML, form, multipart). */
		BODY,
		
		/** Error from business logic, not tied to a specific field. */
		BUSINESS
	}
}