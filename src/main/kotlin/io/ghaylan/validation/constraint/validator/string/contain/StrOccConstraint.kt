package io.ghaylan.validation.constraint.validator.string.contain

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Contain
import io.ghaylan.validation.constraint.annotation.Contain.StrOccMode
import kotlin.reflect.KClass

/** Constraint metadata for [@StrOcc][Contain]. */
data class StrOccConstraint(
	val value: String,
	val minOccurrences: Int,
	val maxOccurrences: Int,
	val ignoreCase: Boolean,
	val mode: StrOccMode,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()