package io.ghaylan.validation.exception

import io.ghaylan.validation.constraint.ConstraintMetadata

/**
 * Thrown when a validation [ConstraintMetadata] definition is structurally invalid.
 *
 * Common triggers include referencing properties that do not exist, providing logically
 * inconsistent ranges, or violating target type expectations.
 *
 * @param message The specific reason explaining why the definition is flawed.
 * @property constraint The metadata instance representing the misconfigured constraint.
 * @param cause The underlying cause of the metadata resolution failure, if any.
 */
class InvalidConstraintDefinitionException(
	message: String,
	val constraint: ConstraintMetadata,
	cause: Throwable? = null
): RuntimeException("${constraint.javaClass.name} error: $message", cause)