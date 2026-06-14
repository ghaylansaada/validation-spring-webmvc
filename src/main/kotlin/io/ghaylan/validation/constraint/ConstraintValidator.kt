package io.ghaylan.validation.constraint

import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError

/**
 * Foundational abstract base class for all single-field and cross-field constraint validators within the framework.
 *
 * Implements a template method pattern via [runValidation] to orchestrate the validation execution lifecycle.
 * This design guarantees that cross-cutting framework features—such as contextual validation group filtering,
 * performance-optimized intersection checks, and automated error path/location enrichment—are executed uniformly
 * across all custom validation types.
 *
 * Custom validation logic must be declared by extending this class and implementing the protected [validate] hook.
 *
 * @param Value The target runtime data type checked by this validator.
 * @param Constraint The specific [ConstraintMetadata] descriptor type linking configuration metadata to this validator.
 */
abstract class ConstraintValidator<Value, Constraint: ConstraintMetadata> {
	
	/**
	 * The framework's public invocation entry point that manages pre-processing filtering and post-processing
	 * telemetry enrichment for an isolated validator instance.
	 *
	 * The lifecycle execution proceeds as follows:
	 * 1. **Group Matching:** Evaluates [shouldValidate] to ensure the constraint intersects with the active validation profiles.
	 * 2. **Constraint Execution:** Delegates execution down to the localized [validate] implementation loop.
	 * 3. **Payload Normalization:** If an [ConstraintError] is yielded, intercepts and enriches it with precise, structural
	 * information tracking (e.g., exact JSON dot paths, origin HTTP location layers, and localized error messages).
	 *
	 * @param value The raw, un-casted input property value undergoing verification.
	 * @param constraint The raw metadata rules and configurations associated with this validation check.
	 * @param context The active runtime tracking state map representing the field's lineage.
	 * @return A fully enriched [ConstraintError] if validation conditions fail; `null` if the input is valid or skipped.
	 */
    @Suppress("UNCHECKED_CAST")
    fun runValidation(
        value: Any?,
        constraint: ConstraintMetadata,
        context: ValidationContext
    ): ConstraintError<*>? {
		
        if (!shouldValidate(constraint = constraint as Constraint, context = context)) return null

        val error = validate(
            value = value as Value?,
            constraint = constraint,
            context = context
        ) ?: return null

        return error.copy(
            path = context.fieldPath,
            location = context.location,
            message = constraint.message.ifBlank { error.message ?: "" }.ifBlank { error.code?.message })
    }
	
	/**
	 * Determines whether the current constraint is active by evaluating intersections between the
	 * constraint's target boundaries and the runtime configuration profile.
	 *
	 * Performance optimization metrics include:
	 * - **No Constraint Bounds:** If the constraint specifies zero distinct group parameters, it defaults to active.
	 * - **No Context Bounds:** If the runtime request context is unbound, evaluation short-circuits to false.
	 * - **Smallest Set Iteration:** Compares set sizes dynamically, using the smaller collection to iterate
	 * against the larger collection. This minimizes lookup cycles and prevents cache thrashing under high concurrency.
	 *
	 * @param constraint The typed validation metadata containing targeted execution group profiles.
	 * @param context The current execution context encapsulating active runtime grouping markers.
	 * @return `true` if validation conditions match and execution should proceed; otherwise `false`.
	 */
    private fun shouldValidate(
        constraint : Constraint,
        context : ValidationContext
    ): Boolean {
        // Fast exit: if constraint has no groups, always validate
        if (constraint.groups.isEmpty()) return true

        // Fast exit: if context has no groups, no match possible
        if (context.groups.isEmpty()) return false

        // Smallest set iteration for performance
        val (small, large) = if (constraint.groups.size <= context.groups.size) {
            constraint.groups to context.groups
        }
        else context.groups to constraint.groups

        // Check for intersection
        for (validationGroup in small) {
            if (validationGroup in large) return true
        }

        return false
    }
	
	/**
	 * Executes the specific, core business validation invariant logic.
	 *
	 * Subclasses must override this method to inspect [value] against the parameters configured in [constraint].
	 * If a violation is caught, this method should return a baseline [ConstraintError] containing an alphanumeric error code
	 * and a fallback descriptive message. The parent routine automatically enriches contextual details like object path links.
	 *
	 * @param value The type-safe property value to test; may be `null` if the property is absent or optional.
	 * @param constraint The typed constraint metadata parameters driving this specific validation instance.
	 * @param context Read-only access to the active runtime validation tracking block.
	 * @return An [ConstraintError] tracking the root violation reason, or `null` if the invariant condition is satisfied.
	 */
    protected abstract fun validate(
		value: Value?,
		constraint: Constraint,
		context: ValidationContext,
    ) : ConstraintError<*>?
	
	/**
	 * Resolves and extracts the current value of a sibling property residing within the same parent object container.
	 *
	 * This utility method enables complex cross-field validation rules (e.g., verifying that a `confirmPassword` field
	 * matches the `password` field, or checking that an `endDate` field is chronologically after a `startDate` field).
	 * It uses non-reflective field accessors mapped inside the schema metadata layer to perform safe, high-speed lookups.
	 *
	 * @param name The exact property name of the sibling field to retrieve.
	 * @param context The active runtime validation tracking context containing reference pointers to the parent object node.
	 * @return The raw value of the sibling property, or `null` if the parent object wrapper is uninstantiated or the property is missing.
	 */
    protected fun getPropertyValue(
        name : String,
        context : ValidationContext
	): Any? {
        return context.containerObject
            ?.schema[name]
            ?.accessor
            ?.getFromAny(context.containerObject.value)
    }
}