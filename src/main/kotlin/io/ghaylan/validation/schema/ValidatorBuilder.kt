package io.ghaylan.validation.schema

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.utils.ReflectionUtils
import io.ghaylan.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.validation.utils.SpringBootUtils
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.superclasses

/**
 * A startup-configured initialization engine that dynamically discovers, instantiates,
 * and indexes custom [ConstraintValidator] implementations across the application classpath.
 *
 * This coordinator works in tandem with Spring's low-level metadata scanning infrastructure
 * to parse annotations decorated with the meta-annotation [Constraint]. It automates the extraction
 * of generic signature type parameters and builds a highly optimized multi-level lookup table
 * utilized by the core validation engine at runtime.
 */
object ValidatorBuilder {
	
	/**
	 * Scans the resolved base packages of the application to build and wire a complete registry
	 * of all active validation constraints.
	 *
	 * This heavy lifecycle task must be executed exactly once at **application startup**. It targets all custom
	 * annotations marked with [Constraint], tracks down their specified array of implementation classes, resolves their
	 * invariant structural types, and fulfills dependency injection requirements via Spring's context.
	 *
	 * @param appContext The central, active Spring [ApplicationContext] powering the container environment.
	 * @return A nested, immutable registry map where the outer key corresponds to the metadata definition class ([ConstraintMetadata])
	 * and the inner map binds specific target incoming data formats ([TypeInfo]) to their prepared executable [ConstraintValidator] instances.
	 */
    fun buildValidators(
        appContext : ApplicationContext,
    ) : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>> {
        val validators = HashMap<KClass<out ConstraintMetadata>, HashMap<TypeInfo, ConstraintValidator<*,*>>>()

        val beanFactory = appContext.autowireCapableBeanFactory
		
		val allPackages = SpringBootUtils.resolveBasePackages(appContext, beanFactory)

        // Scanner that looks for annotations marked with @Constraint
        val scanner = object : ClassPathScanningCandidateComponentProvider(false) {
            // allow scanning for annotations themselves
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
                return beanDefinition.metadata.isAnnotated(Constraint::class.java.name) && beanDefinition.metadata.isAnnotation
            }
        }

        scanner.addIncludeFilter(AnnotationTypeFilter(Constraint::class.java))

        val annotationDefs = allPackages.flatMap { scanner.findCandidateComponents(it) }

        for (annotationDef in annotationDefs) {
            val validatorsClasses = resolveValidatorsClass(annotationDef.beanClassName) ?: continue
            for (validatorClass in validatorsClasses) {
                val (constraintType, valueType) = resolveConstraintAndValueType(validatorClass)
                val valueTypeMap = validators.getOrPut(constraintType) { hashMapOf() }
                valueTypeMap[valueType] = resolveValidatorInstance(appContext, beanFactory, validatorClass)
            }
        }

        return validators
    }
	
	/**
	 * Extracts the target implementation [ConstraintValidator] classes bound via the `validatedBy` parameter
	 * of a [Constraint]-annotated custom meta-annotation.
	 *
	 * Utilizes standard Java reflection class loading boundaries to safely inspect candidate annotation definitions.
	 *
	 * @param constraintName The fully-qualified class name of the target validation annotation discovered by the scanner.
	 * @return An array of Kotlin classes implementing the required validation logic interfaces, or `null` if the class
	 * cannot be found or is not a valid annotation.
	 */
    @Suppress("UNCHECKED_CAST")
    private fun resolveValidatorsClass(
        constraintName: String?
    ): Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>? {
        return Class.forName(constraintName ?: return null)
            .takeIf { it.isAnnotation }
            ?.let { it as? Class<out Annotation>? }
            ?.getAnnotation(Constraint::class.java)
            ?.validatedBy
    }
	
	/**
	 * Instantiates a functional instance of a [ConstraintValidator] using a hierarchical,
	 * multi-tiered provisioning resolution strategy.
	 *
	 * ### Resolution Lifecycle Order
	 * 1. **Kotlin Object Singlet**: Checks if the target validator type is defined as a Kotlin `object` declaration.
	 * 2. **Pre-Existing Spring Bean**: Queries the [ApplicationContext] provider cache to locate managed instances (e.g., components marked with `@Component`).
	 * 3. **Programmatic Autowiring**: Uses the [AutowireCapableBeanFactory] to provision, initialize, and satisfy fields/constructors decorated with dependency injection markers (e.g., `@Autowired`).
	 * 4. **No-Argument Fallback**: Explicitly invokes a zero-parameter default constructor using reflection.
	 *
	 * @param appContext The active Spring [ApplicationContext] backing runtime entity resolution.
	 * @param beanFactory The underlying factory context allowing explicit manual production of beans.
	 * @param validatorKClass The Kotlin class definition of the target validator being provisioned.
	 * @return A fully realized, active, and dependency-satisfied [ConstraintValidator] instance.
	 * @throws IllegalStateException if all instantiation strategies fail due to missing beans or unresolvable constructors.
	 */
    private fun resolveValidatorInstance(
        appContext : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory,
        validatorKClass: KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>
    ) : ConstraintValidator<out Any, out ConstraintMetadata> {
        // 1. Kotlin object?
        validatorKClass.objectInstance?.let { return it }

        // 2. A Spring-managed bean already exists (singleton / scoped)
        appContext.getBeanProvider(validatorKClass.java).ifAvailable?.let { return it }

        // 3. Create (autowire) a new instance
        runCatching {
            beanFactory.createBean(validatorKClass.java)
        }.getOrNull()?.let { return it }

        // 4. Fallback: bare no-arg constructor if autowire failed
        return validatorKClass.constructors.firstOrNull {
            it.parameters.isEmpty()
        }?.call() ?: error("Cannot instantiate ${validatorKClass.qualifiedName}: no bean and no no-arg constructor")
    }
	
	/**
	 * Extracts and maps structural type details from the generic type parameters of a validator implementation.
	 *
	 * Analyzes the exact type boundaries matching `ConstraintValidator<T, A>`, mapping index `0` to the supported incoming
	 * parameter value structure and index `1` to the specific metadata class payload.
	 *
	 * @param validatorClass The target validator reflection class variant to analyze.
	 * @return A [Pair] containing the [ConstraintMetadata] class definition and the generic argument [TypeInfo] detailing
	 * the target data parameter format.
	 * @throws IllegalStateException if the target class cannot be mapped to a [ConstraintValidator] lineage, or if generic signatures are incomplete.
	 */
    private fun resolveConstraintAndValueType(
        validatorClass: KClass<out ConstraintValidator<*, *>>
    ) : Pair<KClass<out ConstraintMetadata>, TypeInfo> {
        val superType = findConstraintValidatorSuperType(validatorClass)
            ?: error("Class ${validatorClass.simpleName} does not inherit from ConstraintValidator")

        val args = superType.arguments

        require(args.size == 2) { "Expected 2 generic arguments for ConstraintValidator" }

        @Suppress("UNCHECKED_CAST")
        val constraintType = args[1].type?.classifier as? KClass<out ConstraintMetadata>?
            ?: error("Second generic argument must be a BaseConstraintMetadata subtype")

        val valueTypeArg = superType.arguments
            .getOrNull(0)
            ?.type
            ?: error("Missing value type in generic arguments")

        return constraintType to ReflectionUtils.infoFromKType(valueTypeArg)
    }
	
	/**
	 * Traverses the supertype and superclass lineage of a given Kotlin class to extract
	 * the matching [ConstraintValidator] interface reference.
	 *
	 * This recursive traversal preserves nested generic argument signatures across complex multi-tier class hierarchies.
	 *
	 * @param validatorClass The class definition node currently undergoing evaluation.
	 * @return The matching resolved [KType] containing concrete generic type variables, or `null` if the interface is not found.
	 */
    private fun findConstraintValidatorSuperType(
        validatorClass: KClass<*>
    ): KType? {
        return validatorClass.supertypes.firstOrNull { it.classifier == ConstraintValidator::class }
            ?: validatorClass.superclasses.firstNotNullOfOrNull { findConstraintValidatorSuperType(it) }
    }
}