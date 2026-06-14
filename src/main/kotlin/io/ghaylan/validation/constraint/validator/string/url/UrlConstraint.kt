package io.ghaylan.validation.constraint.validator.string.url

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Url
import io.ghaylan.validation.constraint.annotation.Url.UrlType
import kotlin.reflect.KClass

/** Constraint metadata for [@Url][Url]. */
data class UrlConstraint(
	val type: UrlType,
	val maxLength: Int,
	val allowedPorts: Set<String>,
	val allowedParams: Set<String>,
	val allowedProtocols: Set<String>,
	val allowedExtensions: Set<String>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()