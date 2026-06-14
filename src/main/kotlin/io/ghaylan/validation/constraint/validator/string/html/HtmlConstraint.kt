package io.ghaylan.validation.constraint.validator.string.html

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Html
import kotlin.reflect.KClass

/** Constraint metadata for [@Html][Html]. */
data class HtmlConstraint(
	val allowedTags: Set<String>,
	val allowedAttrs: Set<String>,
	val allowedProtocols: Set<String>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()
