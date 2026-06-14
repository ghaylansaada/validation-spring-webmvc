package io.ghaylan.validation.constraint.validator.string.regex

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Regex
import kotlin.reflect.KClass

/** Constraint metadata for [@Regex][Regex]. */
data class RegexConstraint(
	val pattern: String,
	val name: String,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()