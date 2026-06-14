package io.ghaylan.validation.utils

import io.ghaylan.validation.integration.ValidateRequest
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

/**
 * High-level framework utility providing deep integration bridges into the active Spring Boot container infrastructure.
 *
 * `SpringBootUtils` encapsulates the complex reflection and environment heuristic lookup logic required to
 * analyze the host application context. It serves two vital tasks during framework initialization:
 * 1. **Context Package Resolution:** Discovers application component-scanning boundaries to register target validation classes safely.
 * 2. **Endpoint Mapping Scanning:** Inspects Spring MVC routing tables to index active controller endpoints that explicitly request validation.
 */
object SpringBootUtils {
	
	/**
	 * Resolves the primary base packages of the running host application to establish scan boundaries for validation rules.
	 *
	 * To accommodate custom module layouts, starter configurations, and integration test slices, this method executes
	 * a multi-tiered fallback cascade strategy to identify package targets:
	 * 1. **[AutoConfigurationPackages]:** Queries Spring Boot's internal autoconfiguration package repository registry.
	 * 2. **[@SpringBootConfiguration][SpringBootConfiguration] Beans:** Reflects upon the package location of the main application entry class.
	 * 3. **Environment Properties:** Resolves class structures explicitly provided via the `spring.main.sources` configuration path.
	 * 4. **Hard Fallback:** Always includes `"io.ghaylan.validation"` to protect core framework scanning rules from failing.
	 *
	 * @param context The active application context holding the bean ecosystem and property environments.
	 * @param beanFactory The underlying low-level factory instance used to extract internal autoconfiguration state records.
	 * @return A linked set of unique package names, sorted in resolution priority order, guaranteed to contain at least one element.
	 */
    fun resolveBasePackages(
        context : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory
    ): Set<String> {
        val detected = linkedSetOf("io.ghaylan.validation")

        // 1. AutoConfigurationPackages
	    if (AutoConfigurationPackages.has(beanFactory)) {
            detected += AutoConfigurationPackages.get(beanFactory)
        }

        // 2. @SpringBootConfiguration beans
        detected += trySpringBootConfigurationBeans(context)

        // 3. spring.main.sources property
        detected += trySpringMainSources(context.environment)

        return detected
    }
	
	/**
	 * Traverses the active Spring MVC request-routing table to map endpoints guarded by the custom validation framework.
	 *
	 * This method fetches the [RequestMappingHandlerMapping] bean from the container context, unrolls its registered
	 * endpoint method descriptors, checks for the presence of the [@ValidateRequest][ValidateRequest] annotation,
	 * and assembles an index map mapping the executable method reflections to their configuration rules.
	 *
	 * @param appContext The active application context containing the live Web MVC infrastructure.
	 * @return A map linking reflection [Method] references directly to their binding [@ValidateRequest][ValidateRequest] metadata instances.
	 */
	fun findRequestValidationMethods(
		appContext: ApplicationContext,
	): Map<Method, ValidateRequest> {
		val handlerMapping: RequestMappingHandlerMapping = appContext.getBean<RequestMappingHandlerMapping>()
		val result = HashMap<Method, ValidateRequest>(handlerMapping.handlerMethods.size)
		for (handlerMethod in handlerMapping.handlerMethods.values) {
			result[handlerMethod.method] = handlerMethod.method.getAnnotation(ValidateRequest::class.java) ?: continue
		}
		return result
	}
	
	/**
	 * Extracts package naming identifiers from beans carrying a [@SpringBootConfiguration][SpringBootConfiguration] marker.
	 *
	 * This utility automatically accounts for and handles Spring AOP dynamic proxy configurations (such as CGLIB or JDK dynamic proxies).
	 * It extracts the true target class type via [AopUtils.getTargetClass] to isolate the true package layout origin coordinate.
	 *
	 * @param ctx The active application context used to perform configuration bean lookups.
	 * @return A list of unique package name coordinates where configuration beans reside.
	 */
    private fun trySpringBootConfigurationBeans(ctx: ApplicationContext): List<String> {
        return ctx.getBeansWithAnnotation<SpringBootConfiguration>().values
            .map { AopUtils.getTargetClass(it) }
            .map { it.packageName }
            .distinct()
    }
	
	/**
	 * Resolves the package declarations of classes listed under the standard `spring.main.sources` environment property.
	 *
	 * Designed to support complex multi-source application startups or custom testing frameworks, this method parses
	 * comma-delimited strings, attempts to resolve their raw reflection definitions via [Class.forName], and falls back
	 * cleanly if a class target cannot be loaded into the current classloader scope.
	 *
	 * @param env The target environment property source to query.
	 * @return A list of package name identifiers extracted from successfully resolved source class targets.
	 */
    private fun trySpringMainSources(env: Environment): List<String> {
        val raw = env.getProperty("spring.main.sources") ?: return emptyList()
        return raw.split(',')
            .mapNotNull { str -> str.trim().takeIf { it.isNotEmpty() } }
            .mapNotNull { runCatching { Class.forName(it).`package`.name }.getOrNull() }
            .distinct()
    }
}