package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.html.HtmlConstraint
import io.ghaylan.validation.constraint.validator.string.html.HtmlValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to ensure an HTML markup string complies with strict security safelists.
 *
 * This annotation protects application boundary layers against Cross-Site Scripting (XSS) and malformed markup injection.
 * It utilizes an underlying Jsoup-backed HTML sanitizer strategy to compare the raw input against a structurally clean
 * and safe output version. If any untrusted tags, attributes, or unapproved protocol endpoints are discovered during
 * this evaluation, the validation fails.
 *
 * @property allowedTags The collection of permitted HTML element tags. Elements omitted from this array are stripped
 * during evaluation. Defaults to a standard safe layout set including inline and block structural tags.
 * @property allowedAttrs Explicitly allowed tag-to-attribute mappings declared as `"tag:attribute"` string pairs
 * (for example, `"a:href"` or `"img:src"`). Unlisted attributes are treated as violations. Defaults to permitting links.
 * @property allowedProtocols Approved communication protocols targeted at URL-based properties, defined via
 * a list of standard schemes (such as `"http"` or `"https"`).
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [[OnDefault]].
 */
@MustBeDocumented
@Constraint(metadata = HtmlConstraint::class, validatedBy = [HtmlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Html(
	val allowedTags: Array<String> = ["b", "i", "u", "p", "ul", "li", "a", "span", "strong"],
	val allowedAttrs: Array<String> = ["a:href"],
	val allowedProtocols: Array<String> = ["http", "https"],
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
)