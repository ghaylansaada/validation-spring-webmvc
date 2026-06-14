package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.language.LanguageConstraint
import io.ghaylan.validation.constraint.validator.string.language.LanguageValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a structurally valid ISO language tag.
 *
 * This annotation evaluates the target string against internationalization standards, confirming compliance with
 * BCP 47 layout conventions. It permits either a standalone base language identifier (such as a two-letter ISO 639-1
 * code like `"en"` or `"fr"`) or a composite locale structure containing a primary language combined with an
 * optional subtag or regional country identifier (such as `"en-US"` or `"fr-FR"`).
 *
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = LanguageConstraint::class, validatedBy = [LanguageValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class LanguageCode(
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)