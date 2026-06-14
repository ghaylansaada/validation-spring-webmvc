package io.ghaylan.validation.utils

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.ConstraintMetadata
import kotlin.reflect.*
import kotlin.reflect.jvm.jvmName

/**
 * High-performance reflection and type-coercion utility used to convert raw validation annotations
 * into strongly-typed [ConstraintMetadata] configuration profiles.
 *
 * This component handles structural blueprint generation during the framework's startup phase.
 * It maps values from user-facing annotation interfaces to matching constructor definitions inside target
 * metadata instances.
 *
 * ### Advanced Capabilities
 * - **Smart Type Coercion:** Seamlessly bridges impedance mismatches between native Java annotation types
 * and elegant Kotlin models—such as unrolling an `Array<KClass<*>>` from an annotation into a faster,
 * read-only `Set<KClass<*>>` used during validation checks.
 * - **Recursive Compound Processing:** Automatically descends and processes nested annotations, converting
 * composite annotation parameters into their constituent metadata layouts.
 * - **Metadata Enrichment Injection:** Dynamically binds container traversal markers ([Constraint.appliesToContainer])
 * into instantiated metadata blocks via safe, reflective member property assignment.
 */
object ConstraintConverter {
	
	/**
	 * Extension method that reflects upon an [Annotation] instance to map its parameters into a compiled
	 * [ConstraintMetadata] backing instance.
	 *
	 * This method locates the associated [@Constraint][Constraint] descriptor, determines the destination
	 * metadata constructor footprint, correlates argument parameters by structural naming conventions, and
	 * executes necessary type adaptations before invoking the instance allocator via [kotlin.reflect.KFunction.callBy].
	 *
	 * @receiver The active annotation instance located on a field or property parameter.
	 * @return An initialized, type-safe [ConstraintMetadata] container mirror matching the source annotation parameters.
	 * @throws IllegalStateException If the receiver is missing a [@Constraint][Constraint] marker, if parameter
	 * names do not align, or if a structural type coercion failure occurs.
	 */
    fun Annotation.convertToMetadata(): ConstraintMetadata {
        val constraintAnn = this.annotationClass.annotations
            .find { it is Constraint } as? Constraint
            ?: error("${this.annotationClass.jvmName} is not annotated with @Constraint")

        val metadataClass = constraintAnn.metadata
        val constructor = metadataClass.constructors.firstOrNull()
            ?: error("No constructor found for metadata class: ${metadataClass.simpleName}")

        val constructorParams = constructor.parameters.associateBy { it.name }

        val annotationProperties = this.annotationClass.members
            .filterIsInstance<KProperty1<Annotation, *>>()
            .associateBy { it.name }

        val args = mutableMapOf<KParameter, Any?>()
	    
	    for ((name, param) in constructorParams) {
            val prop = annotationProperties[name]
                ?: error("Parameter '$name' in metadata constructor not found in annotation ${this.annotationClass.simpleName}")

            val value = prop.get(this)

            // Optional: Convert arrays to sets for group and validator handling
            val expectedType = param.type.classifier
		    val finalValue = when (value) {
                is Array<*> -> {

                    val mappedValues = value.map {

                        when (it) {
                            is Annotation if ConstraintMetadata::class.java.isAssignableFrom(it.javaClass) -> it.convertToMetadata()
                            is Class<*> -> it.kotlin
                            else -> it
                        }
                    }

                    when (expectedType) {
                        Array::class -> mappedValues.toTypedArray()
                        Set::class -> mappedValues.toSet()
                        List::class -> mappedValues.toList()
                        Collection::class -> mappedValues.toCollection(ArrayList())
                        else -> mappedValues
                    }
                }

                // Recursively convert nested single annotation -> metadata
                is Annotation if expectedType is KClass<*> && ConstraintMetadata::class.java.isAssignableFrom(expectedType.java) -> {
                    value.convertToMetadata()
                }

                else -> {
                    if (!areTypesCompatible(prop.returnType.classifier, expectedType)) {
                        error("Type mismatch for property '$name': Annotation has ${prop.returnType.classifier}, but Metadata expects $expectedType")
                    }
                    value
                }
            }

            args[param] = finalValue
        }

        // Construct instance
        val instance = constructor.callBy(args)

        // If the ConstraintMetadata instance supports `placeHolders` or `appliesToContainer`,
        // set them dynamically via reflection (if they are mutable `var` properties).
        // - `placeHolders`: sets the placeholders for validation messages.
        // - `appliesToContainer`: indicates whether the constraint applies to container/aggregate types.
        // Any exceptions are caught and ignored to allow optional presence of these properties.
        runCatching {
            val appliesToContainerKey = ConstraintMetadata::appliesToContainer.name
            val appliesToContainerProp = metadataClass.members
                .filterIsInstance<KMutableProperty1<Any, Any?>>()
                .firstOrNull { it.name == appliesToContainerKey }
            appliesToContainerProp?.set(instance, constraintAnn.appliesToContainer)
        }.getOrNull()

        return instance
    }
	
	/**
	 * Structural assignment safety routine that asserts whether a source classifier can be safely assigned
	 * to a target metadata constructor classifier.
	 *
	 * Recognizes out-of-the-box structural assignments, such as transforming raw reflection array markers
	 * (`Array<KClass<*>>`) into collection boundaries (`Set::class`), alongside standard class inheritance matching.
	 *
	 * @param from The origin data classifier returned by the annotation property accessor.
	 * @param to The target data classifier expected by the constructor method parameter profile.
	 * @return `true` if type shapes are compatible or can be safely adapted; otherwise `false`.
	 */
    private fun areTypesCompatible(
	    from: KClassifier?,
	    to: KClassifier?
    ): Boolean {
        if (from == to) return true

        // Handle common special cases
	    return when {
            isArrayOfKClass(from) && to == Set::class -> true
            from is KClass<*> && to is KClass<*> && from.java.isAssignableFrom(to.java) -> true
            else -> false
        }
    }
	
	/**
	 * Asserts whether a given structural classifier represents a raw array of Kotlin [KClass] descriptors.
	 *
	 * Evaluates underlying class structures to determine if the variable represents a Java array instance
	 * constructed from components matching `kotlin.reflect.KClass`.
	 *
	 * @param type The unknown reflection classifier type to inspect.
	 * @return `true` if the type represents an uncoerced `Array<KClass<*>>` construct; otherwise `false`.
	 */
	private fun isArrayOfKClass(type: KClassifier?): Boolean {
        // Check raw class and generic type
        return type is KClass<*> &&
                type.java.isArray &&
                type.java.componentType == KClass::class.java
    }
}