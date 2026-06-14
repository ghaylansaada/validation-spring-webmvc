package io.ghaylan.validation.constraint.validator.string.language

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.LanguageCode
import kotlin.reflect.KClass

/** Constraint metadata for [@LanguageCode][LanguageCode]. */
data class LanguageConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()
