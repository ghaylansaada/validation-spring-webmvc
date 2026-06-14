package io.ghaylan.validation.config

import io.ghaylan.validation.engine.ValidatorEngine
import io.ghaylan.validation.schema.ValidationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Third-party extensible autoconfiguration class responsible for bootstrapping the custom validation framework
 * within the Spring Application Context.
 *
 * This configuration leverages Spring Boot's modular [@AutoConfiguration] model to conditionally register the core
 * architectural building blocks of the validation engine. By pairing [@Bean] declarations with [@ConditionalOnMissingBean],
 * this class acts as an extensible baseline, providing fully operational infrastructure while allowing consuming
 * microservices or modules to safely inject custom overrides for any component.
 */
@AutoConfiguration
class ValidationConfig {
	
	@Bean
	@ConditionalOnMissingBean
	fun validationRegistry(): ValidationRegistry = ValidationRegistry()
	
	@Bean
	@ConditionalOnMissingBean
	fun validatorEngine(
		validationRegistry: ValidationRegistry
	): ValidatorEngine = ValidatorEngine(validationRegistry)
}