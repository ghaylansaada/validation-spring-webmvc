package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.required.RequiredConstraint
import io.ghaylan.validation.constraint.validator.required.RequiredValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert value presence under unconditional or structural conditional rules.
 *
 * This annotation serves as a robust lifecycle boundary control for data-transfer objects. It extends standard nullability
 * checks by evaluating content evaluation properties (such as blank character sequences or empty data containers).
 * By leveraging the [dependentField] and [condition] properties, it can enforce conditional structural mandates
 * (e.g., a field becomes mandatory only if a companion field is populated or left empty). This annotation is repeatable
 * to allow declarations of overlapping validation criteria.
 *
 * ### Evaluation Strategy
 * * By default, an element is considered present if it is non-null, non-blank (for [CharSequence]), and non-empty
 * (for [Collection], [Map], or [Array]).
 * * When [allowEmpty] is configured as `true`, the structural presence check is relaxed, and only a non-null evaluation
 * is required to pass.
 *
 * @property allowEmpty Toggles whether structurally empty states pass evaluation. If `false`, strings must not be blank
 * and collections, maps, or arrays must contain at least one element. If `true`, empty structures are accepted as long
 * as the property reference is not `null`. Defaults to `false`.
 * @property dependentField The name of a target sibling field within the same class instance whose presence or absence state
 * governs the conditional evaluation of this constraint. This property is ignored if [condition] is configured as [RequirementCondition.ALWAYS].
 * @property condition Dictates the operational lifecycle rule or activation phase for this validation boundary. Defaults to [RequirementCondition.ALWAYS].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = RequiredConstraint::class, validatedBy = [RequiredValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Required(
	val allowEmpty: Boolean = false,
	val dependentField: String = "",
	val condition: RequirementCondition = RequirementCondition.ALWAYS,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
) {
	
	/**
	 * Defines the execution trigger logic for relative property validation rules.
	 */
	enum class RequirementCondition {
		
		/** The annotated property is evaluated unconditionally. */
		ALWAYS,
		
		/** The annotated property is treated as required only when the configured [dependentField] evaluates to a missing or null state. */
		IF_DEPENDENT_NULL,
		
		/** The annotated property is treated as required only when the configured [dependentField] evaluates to a present or non-null state. */
		IF_DEPENDENT_NOT_NULL,
	}
}