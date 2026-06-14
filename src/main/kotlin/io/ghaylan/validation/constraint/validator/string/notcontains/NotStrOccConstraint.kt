package io.ghaylan.validation.constraint.validator.string.notcontains

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Contain.StrOccMode
import io.ghaylan.validation.constraint.annotation.NotContain
import kotlin.reflect.KClass

/** Constraint metadata for [@NotStrOcc][NotContain]. */
data class NotStrOccConstraint(
	val value: String,
	val ignoreCase: Boolean,
	val mode: StrOccMode,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()