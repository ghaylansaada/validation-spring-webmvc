package io.ghaylan.validation.constraint.annotation

import io.ghaylan.validation.constraint.Constraint
import io.ghaylan.validation.constraint.validator.string.url.UrlConstraint
import io.ghaylan.validation.constraint.validator.string.url.UrlValidator
import io.ghaylan.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validation constraint annotation used to verify that a character sequence represents a valid, well-formed
 * Uniform Resource Identifier (URI) or Uniform Resource Locator (URL) matching specific operational criteria.
 *
 * This annotation enforces architectural integrity on web-accessible endpoints and resource identifiers. It supports
 * infrastructure profiling including network port filtering, protocol/scheme white-listing, query parameter inspection,
 * and media content type validation.
 *
 * @property type The structural or semantic classification category of the URL via [UrlType]. Dictates whether the resource
 * represents a general web endpoint or a specific media category enforcing standard file extensions. Defaults to [UrlType.ANY].
 * @property maxLength The maximum permissible character length of the raw URL string. Protects against buffer overflows or
 * DOS vectors via oversized query string patterns. Defaults to `2048`.
 * @property allowedPorts An array of allowed network port numbers. Defaults to `["*"]` to allow any valid port identifier.
 * @property allowedParams An array of allowed query parameter keys. Defaults to `["*"]` to permit any structured query parameters.
 * @property allowedProtocols An array of permitted URI scheme protocols (e.g., `"https"`, `"http"`). Defaults to `["*"]`.
 * @property allowedExtensions Overrides or supplements the file extensions associated with the configured [type].
 * Defaults to `["*"]` which signifies that any extension (or no extension) is allowed unless restricted by the underlying [type].
 * @property message A custom template string or static message returned when the constraint is violated. Defaults to blank.
 * @property groups The validation groups this constraint belongs to, dictating when the rule is evaluated. Defaults to [OnDefault].
 */
@MustBeDocumented
@Constraint(metadata = UrlConstraint::class, validatedBy = [UrlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class Url(
	val type: UrlType = UrlType.ANY,
	val maxLength: Int = 2048,
	val allowedPorts: Array<String> = ["*"],
	val allowedParams: Array<String> = ["*"],
	val allowedProtocols: Array<String> = ["*"],
	val allowedExtensions: Array<String> = ["*"],
	val message: String = "",
	val groups: Array<KClass<*>> = [OnDefault::class]
) {
	
	/**
	 * Defines the target resource classifications and their associated standard file extension restrictions.
	 *
	 * @property isMedia Indicates whether the category represents a binary media resource requiring file extension extraction.
	 * @property extensions The standard array of default permissible lowercase file extensions linked to this category profile.
	 */
	enum class UrlType(
		val isMedia: Boolean,
		val extensions: Array<String>
	) {
		
		/** Any valid structural URI format; imposes no default domain or path extension constraints. */
		ANY(false, emptyArray()),
		
		/** A standard web address structure; typically enforces web-centric protocol schemes (HTTP/HTTPS) and domain routing. */
		WEBSITE(false, emptyArray()),
		
		/** Digital documents, archives, and executable binaries. */
		DOCUMENT(true, arrayOf("pdf", "zip", "rar", "tar", "exe", "doc", "docx", "ppt", "pptx", "xls", "xlsx")),
		
		/** Static or animated graphic layout resources. */
		IMAGE(true, arrayOf("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "tiff", "ico")),
		
		/** Digital video media profiles. */
		VIDEO(true, arrayOf("mp4", "avi", "mov", "mkv", "flv", "wmv", "webm", "mpeg")),
		
		/** Digital audio encoding profiles. */
		AUDIO(true, arrayOf("mp3", "wav", "ogg", "flac", "aac", "wma", "m4a", "opus"))
	}
}