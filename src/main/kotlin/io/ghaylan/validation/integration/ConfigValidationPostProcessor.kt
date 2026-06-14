package io.ghaylan.validation.integration

import io.ghaylan.validation.engine.ValidatorEngine
import io.ghaylan.validation.exception.InvalidConfigurationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component

/**
 * A Spring [BeanPostProcessor] that intercepts initialized beans to validate system configuration structures.
 *
 * This processor scans for beans annotated with [ValidateConfig]. When found, it runs them through the
 * core [ValidatorEngine] immediately after bean initialization. If any configuration constraints are violated,
 * it intercepts the failure, builds a highly descriptive console report, and raises an [InvalidConfigurationException]
 * to fail the application startup fast.
 *
 * @property validatorEngine The underlying execution engine used to evaluate the bean's structural constraints.
 */
@Component
class ConfigValidationPostProcessor(
    private val validatorEngine: ValidatorEngine,
) : BeanPostProcessor {
	
	/**
	 * Intercepts a fully initialized bean instance to execute configuration validation rules if targeted.
	 *
	 * If the bean is meta-annotated or directly annotated with [ValidateConfig], its properties are evaluated.
	 * In the event of a validation failure, this method attempts to resolve any bound [ConfigurationProperties]
	 * prefix to contextualize the configuration paths inside the generated startup error report.
	 *
	 * @param bean The bean instance created by the container.
	 * @param beanName The registered name of the bean in the application context.
	 * @return The original bean instance if validation passes.
	 * @throws InvalidConfigurationException If one or more validation constraints are violated.
	 */
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
		
        // Check if the bean's class is annotated with @ValidateConfig
        if (bean::class.java.isAnnotationPresent(ValidateConfig::class.java)) {
			
            // Execute your custom validation logic
            val errors = validatorEngine.validate(bean)
            
            if (errors.isNotEmpty()) {
	            
	            val configProperties = AnnotationUtils.findAnnotation(bean::class.java, ConfigurationProperties::class.java)
	            val prefix = configProperties?.prefix?.takeIf { it.isNotBlank() }?.let { "$it." } ?: ""
	            
	            val report = buildString {
		            appendLine("Reason: Configuration validation failed for bean '${beanName}'.")
		            appendLine("Class : ${bean::class.qualifiedName}")
		            appendLine("Errors: ${errors.size} violation(s) found.")
		            appendLine()
		            
		            errors.forEachIndexed { index, error ->
			            val propertyPath = error.path?.let { "$prefix$it" } ?: "${prefix}[class-level]"
			            appendLine("(${index + 1}) Property : $propertyPath")
			            appendLine("    Path     : ${error.path ?: "UNKNOWN"}")
			            appendLine("    Code     : ${error.code ?: "UNKNOWN"}")
			            appendLine("    Message  : ${error.message ?: "No descriptive message provided."}")
			            if (error.metadata != null) {
				            appendLine("    Context   : ${error.metadata}")
			            }
			            appendLine()
		            }
	            }
				
                throw InvalidConfigurationException(report)
            }
        }
        return bean
    }
}