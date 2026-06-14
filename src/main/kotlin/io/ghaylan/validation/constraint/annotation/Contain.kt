package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.contain.StrOccConstraint
import io.ghaylan.validation.constraint.validator.string.contain.StrOccValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to assert substring containment characteristics within a [String].
 *
 * This annotation evaluates whether a target character sequence satisfies specific presence, placement,
 * and occurrence count boundaries. It supports multiple matching modes and case-sensitivity toggles,
 * and can be applied multiple times to the same target element.
 *
 * @property value The target substring or sequence required to match against the validated text.
 * @property minOccurrences The minimum allowed occurrence count of the [value] (inclusive). Defaults to `1`.
 * @property maxOccurrences The maximum allowed occurrence count of the [value] (inclusive). Defaults to [Int.MAX_VALUE].
 * @property ignoreCase Toggles whether character case boundaries are disregarded during evaluation. Defaults to `false`.
 * @property mode The matching strategy indicating where or how the substring must appear. Defaults to [StrOccMode.EQUALS].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = StrOccConstraint::class, validatedBy = [StrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Contain(
	val value: String,
	val minOccurrences: Int = 1,
	val maxOccurrences: Int = Int.MAX_VALUE,
	val ignoreCase: Boolean = false,
	val mode: StrOccMode = StrOccMode.EQUALS,
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
) {
	
	enum class StrOccMode { EQUALS,
		CONTAINS,
		STARTS_WITH,
		ENDS_WITH
	}
}