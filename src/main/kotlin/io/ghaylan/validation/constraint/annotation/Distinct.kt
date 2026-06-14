package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.distinct.DistinctConstraint
import io.ghaylan.validation.constraint.validator.distinct.DistinctValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert that elements within a collection or array are unique.
 *
 * Uniqueness evaluations can be applied directly to the whole element values or scoped to specific
 * top-level fields using properties extracted reflectionally. This annotation can be applied multiple
 * times to the same container element to enforce distinct, independent uniqueness boundaries.
 *
 * @property by An array of top-level field paths or names to evaluate. If left empty, whole-element
 * object equality (`equals`/`hashCode`) is utilized.
 * @property mode The strategy employed when multiple extraction fields are declared in [by]. Defaults to [DistinctMode.PER_FIELD].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = DistinctConstraint::class, validatedBy = [DistinctValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
annotation class Distinct(
	val by: Array<String> = [],
	val mode: DistinctMode = DistinctMode.PER_FIELD,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
) {
	
	/** Strategy for multi-field uniqueness checks. */
	enum class DistinctMode {
		
		/** The combination of all [by] fields must be unique across elements. */
		COMBINATION,
		
		/** Each field in [by] must be unique independently. */
		PER_FIELD
	}
}