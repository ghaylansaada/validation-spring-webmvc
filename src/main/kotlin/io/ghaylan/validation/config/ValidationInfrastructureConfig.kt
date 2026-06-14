package io.ghaylan.validation.config

import io.ghaylan.validation.engine.ValidatorEngine
import io.ghaylan.validation.integration.ValidatingServletInvocableHandlerMethod
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod

/**
 * Low-level Spring MVC infrastructure configuration class that registers the custom request validation framework
 * directly into the framework's core request-handling pipeline.
 *
 * By implementing the [WebMvcRegistrations] contract, this configuration customizes the behavior of Spring's internal
 * [RequestMappingHandlerAdapter] without completely overriding or breaking Spring Boot's autowired MVC configuration presets.
 * This approach represents the standard enterprise methodology for safely substituting framework-level request executors.
 *
 * @property validatorEngine The core stateless validation runtime engine injected and propagated down to custom request execution contexts.
 */
@Configuration
class ValidationInfrastructureConfig(
    private val validatorEngine: ValidatorEngine
) : WebMvcRegistrations {
	
	/**
	 * Overrides and provisions a custom [RequestMappingHandlerAdapter] containing a tailored execution handler factory.
	 *
	 * This method intercepts the creation of the handler adapter and overrides [RequestMappingHandlerAdapter.createInvocableHandlerMethod].
	 * Instead of returning Spring’s default, standard request executor, it instantiates and injects your framework-specific
	 * [ValidatingServletInvocableHandlerMethod].
	 *
	 * This substitution secures an entry point into the early request-execution phase—allowing custom schema processing to occur
	 * immediately after arguments are fully resolved and bound, but prior to routing the request to the final controller bean method.
	 *
	 * @return A customized anonymous implementation of [RequestMappingHandlerAdapter] wired with the custom execution validation hook.
	 */
    override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
        return object : RequestMappingHandlerAdapter() {
            override fun createInvocableHandlerMethod(handlerMethod: HandlerMethod): ServletInvocableHandlerMethod {
                // Swap Spring's standard executor with your custom validating handler
                return ValidatingServletInvocableHandlerMethod(handlerMethod, validatorEngine)
            }
        }
    }
}