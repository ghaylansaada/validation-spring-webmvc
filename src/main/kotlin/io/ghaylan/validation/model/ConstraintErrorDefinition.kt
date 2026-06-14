package io.ghaylan.validation.model

/**
 * Defines the core contract for validation and API error structures across the application.
 *
 * Implementing classes or enumerations (such as [ConstraintErrorCode]) must provide a descriptive
 * string value representing the error. This interface standardizes how error information
 * is captured and exposed to upper layers or client applications.
 */
interface ConstraintErrorDefinition {
	
	/**
	 * A human-readable text message describing the nature of the error.
	 * * This message should be clear, concise, and safe to be exposed directly to end-users
	 * or included in API error response payloads.
	 */
	val message: String
}