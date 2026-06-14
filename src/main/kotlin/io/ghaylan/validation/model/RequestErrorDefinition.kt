package io.ghaylan.validation.model

/**
 * Defines the contract for top-level, macro-level API request execution failures.
 *
 * Implementing structures represent errors that break the request processing lifecycle
 * as a uniform unit (e.g., authentication failures, resource absence, or unhandled server exceptions).
 * This acts as the envelope error for the entire HTTP response.
 */
interface RequestErrorDefinition {
	
	/**
	 * A high-level, human-readable summary describing the request processing failure.
	 * This message is safe to be exposed directly as the main description in an API error response.
	 */
	val message: String
}