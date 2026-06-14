package io.ghaylan.validation.exception

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

/**
 * A Spring Boot failure analyzer that intercepts [InvalidConfigurationException] during startup.
 *
 * It formats and extracts the pre-formatted error reporting embedded inside the exception's
 * message, presenting a clean, readable diagnostic layout to the console before the
 * application terminates.
 */
class InvalidConfigurationFailureAnalyzer : AbstractFailureAnalyzer<InvalidConfigurationException>() {
	
	/**
	 * Analyzes the given startup failure and constructs a [FailureAnalysis] report.
	 *
	 * @param rootFailure The root cause exception thrown during bootstrap.
	 * @param cause The specific [InvalidConfigurationException] instance containing the report payload.
	 * @return A standalone [FailureAnalysis] featuring the self-contained error report.
	 */
    override fun analyze(rootFailure: Throwable, cause: InvalidConfigurationException): FailureAnalysis {
        // Return your report as the main message. Leaving description/action blank
        // because your report is already self-contained and pre-formatted.
        return FailureAnalysis(cause.message, "", cause)
    }
}