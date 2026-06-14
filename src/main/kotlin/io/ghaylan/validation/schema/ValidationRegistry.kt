package io.ghaylan.validation.schema

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.utils.ReflectionUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * The central runtime coordination hub and memory cache for the declarative validation framework.
 *
 * Implements Spring's [org.springframework.context.ApplicationListener] to hook into the container's lifecycle. Upon receiving a
 * [org.springframework.context.event.ContextRefreshedEvent], it triggers classpath introspection to discover [io.ghaylan.validation.constraint.Constraint] definitions
 * and pre-compiles validation graphs for all static web handler endpoints.
 *
 * ### Thread-Safety & Storage Modalities
 * - **Static Components:** Validators and HTTP endpoint schemas (`staticSchemas`) are eagerly generated
 * sequentially on a single thread during startup. They are treated as read-only pools at runtime.
 * - **Dynamic Components:** Schemas for arbitrary data shapes or programmatic models are lazily compiled, cached,
 * and managed within a thread-safe [java.util.concurrent.ConcurrentHashMap] (`dynamicSchemas`) to withstand high-concurrency lookup paths.
 */
open class ValidationRegistry: ApplicationListener<ContextRefreshedEvent>, InitializingBean, ApplicationContextAware {
	
	private lateinit var appContext: ApplicationContext
	
	/**
	 * Read-only dictionary matching validation constraints to their compatible target type implementations.
	 * Initialized completely at startup during [onApplicationEvent].
	 */
	private val validators = HashMap<KClass<out ConstraintMetadata>, Map<ReflectionUtils.TypeInfo, ConstraintValidator<*, *>>>()
	
	/**
	 * Thread-safe memoization cache storing dynamically evaluated class structures against their [RequestInputSchema.PropertySpec] graphs.
	 */
	private val dynamicSchemas = ConcurrentHashMap<Class<*>, Map<String, RequestInputSchema.PropertySpec>>()
	
	/**
	 * High-performance lookup directory storing pre-compiled structural HTTP schemas keyed by
	 * unique endpoint signature identifiers.
	 */
	val staticSchemas = mutableMapOf<String, RequestInputSchema>()
	
	
	override fun setApplicationContext(applicationContext: ApplicationContext) {
		this.appContext = applicationContext
	}
	
	/**
	 * PHASE 1: Eager Lifecycle Initialization
	 *
	 * Executes immediately after this registry bean is constructed by the container.
	 * This scans and loads framework constraint validators (like @Required) ahead of time
	 * so they are available when early BeanPostProcessors evaluate `@ConfigurationProperties`.
	 */
	override fun afterPropertiesSet() {
		ValidatorBuilder.buildValidators(appContext)
			.forEach {
				validators[it.key] = it.value
			}
	}
	
	/**
	 * PHASE 2: Post-Stabilization Lifecycle Hook
	 *
	 * Application lifecycle hook that intercepts Spring container stabilization to initialize the validation framework.
	 *
	 * Performs consecutive execution steps:
	 * 1. Executes [ValidatorBuilder.buildValidators] to scan and instantiate custom framework constraints.
	 * 2. Executes [ValidationSchemaBuilder.generateStaticSchemas] to map out the application's active HTTP endpoint perimeter.
	 *
	 * @param event The Spring context refreshed event payload tracking container readiness.
	 */
	override fun onApplicationEvent(event: ContextRefreshedEvent) {
		ValidationSchemaBuilder.generateStaticSchemas(
			appContext = appContext,
			allValidators = validators
		).forEach {
			staticSchemas[it.key] = it.value
		}
	}
	
	/**
	 * Retrieves an immutable HTTP request validation schema matching the specified endpoint route identifier.
	 *
	 * This operation functions as a constant-time ($O(1)$) read-only cache lookup, avoiding subsequent runtime reflection overhead.
	 *
	 * @param id The deterministic, unique signature string of the target controller method handler.
	 * @return The pre-compiled [RequestInputSchema] structural rule set, or `null` if no matching validation scope was initialized.
	 */
	fun getSchemaByRequest(id: String): RequestInputSchema? {
		return staticSchemas[id]
	}
	
	/**
	 * Resolves structural schema specifications for an arbitrary class type, utilizing an on-demand,
	 * lazy computation lifecycle model.
	 *
	 * If structural metadata for the target [clazz] is absent from the [dynamicSchemas] registry, this method initiates
	 * reflection traversal via [ValidationSchemaBuilder.generateSchemaForType], asserts schema correctness,
	 * and registers it inside the concurrent thread-safe pool.
	 *
	 * @param clazz The target Java model type representation to analyze and validate.
	 * @return A [Pair] linking the mapped generic type info wrapper ([ReflectionUtils.TypeInfo]) to the evaluated property model definitions.
	 * @throws IllegalStateException if the target type is non-object-like, possesses no evaluable field properties,
	 * or does not contain a single valid framework validation constraint.
	 */
	fun resolveSchemaByClass(
		clazz: Class<*>,
	): Pair<ReflectionUtils.TypeInfo, Map<String, RequestInputSchema.PropertySpec>> {
		val typeInfo = ReflectionUtils.infoFromClass(clazz)
		val type = typeInfo.resolveType.java
		
		return typeInfo to dynamicSchemas.computeIfAbsent(type) {
			val specs = ValidationSchemaBuilder.generateSchemaForType(rootClass = type, allValidators = validators)
				?: error("Could not create validation schema for type ${type.name}")
			
			require(specs.second.isNotEmpty()) {
				"Object specs must contain at least one field for type ${type.name}"
			}
			
			require(specs.second.any { it.value.constraints.isNotEmpty() }) {
				"Object specs must contain at least one constraint for type ${type.name}"
			}
			
			specs.second
		}
	}
}