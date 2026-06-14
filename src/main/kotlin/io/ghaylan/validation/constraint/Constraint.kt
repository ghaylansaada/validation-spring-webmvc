package io.ghaylan.validation.constraint

import kotlin.reflect.KClass

/**
 * Meta-annotation used to define a custom validation constraint and bind it to its architectural execution components.
 *
 * Any custom validation annotation (such as `@NotBlank`, `@Min`, or `@CustomRule`) must be decorated with this
 * meta-annotation. During the application bootstrapping phase, the framework scans the classpath for classes
 * carrying this marker to compile static validation profiles and populate the central registry.
 *
 * This meta-annotation establishes a bridge connecting three distinct components:
 * 1. **The Annotation Target:** The custom annotation interface exposed to developers.
 * 2. **The Metadata Payload:** A backing [ConstraintMetadata] class that parses and stores annotation arguments (e.g., limits, formats, custom messages).
 * 3. **The Execution Runtime:** One or more [ConstraintValidator] implementations capable of evaluating the invariant logic against specific runtime types.
 *
 * ---
 *
 * ### Container-Level vs. Element-Level Targeting
 * The [appliesToContainer] property controls how the validation engine processes variables when confronting iterable
 * array or collection structures:
 * - **Element-Level (`false`):** The default behavior for scalar constraints (e.g., `@Email`, `@Size`). The engine traverses
 * *into* the collection and executes the validator against each individual element inside the list.
 * - **Container-Level (`true`):** Used for constraints that assert invariants across the collection wrapper itself (e.g., `@NotEmpty`,
 * `@Distinct`, `@ArraySize`). The validation engine evaluates the collection as a single cohesive unit instead of breaking it down into individual elements.
 *
 * ---
 *
 * ### Example Usage
 *
 * ```kotlin
 * @Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
 * @Retention(AnnotationRetention.RUNTIME)
 * @Constraint(metadata = SizeMetadata::class, validatedBy = [StringSizeValidator::class, CollectionSizeValidator::class], appliesToContainer = false)
 * annotation class Size(val min: Int = 0, val max: Int = Int.MAX_VALUE, val message: String = "")
 * ```
 *
 * @property metadata The [KClass] of the [ConstraintMetadata] responsible for parsing configuration values from the annotation instance.
 * @property validatedBy An array of [ConstraintValidator] [KClass] implementations mapped to execute this constraint check. Multiple validators can be supplied to support different runtime data types (e.g., handling both `String` and `Int` inputs).
 * @property appliesToContainer Strategy marker directing the validation traversal loop.
 *          If `true`, applies the validation constraint directly to the parent collection/array container wrapper.
 *          If `false`, applies the validation rule to each item nested within the collection graph. Defaults to `true`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Constraint(
	val metadata: KClass<out ConstraintMetadata>,
	val validatedBy: Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>,
	val appliesToContainer: Boolean = true
)