package io.ghaylan.validation.constraint.validator.temporal.max

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.TemporalMax
import kotlin.reflect.KClass

/** Constraint metadata for [@TemporalMax][TemporalMax]. */
data class TemporalMaxConstraint(
	val value: String,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()