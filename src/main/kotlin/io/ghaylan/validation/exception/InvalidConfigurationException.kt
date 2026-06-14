package io.ghaylan.validation.exception

/**
 * Thrown during the application bootstrap phase when a critical, irrecoverable validation
 * system misconfiguration is detected.
 *
 * This exception is designed to fail the application startup fast and is intercepted
 * by the [InvalidConfigurationFailureAnalyzer] to present a clean diagnostic report.
 *
 * @property message Detail message explaining the specific configuration violation.
 */
class InvalidConfigurationException(
	override val message: String = "Invalid system configuration detected."
): RuntimeException(message)