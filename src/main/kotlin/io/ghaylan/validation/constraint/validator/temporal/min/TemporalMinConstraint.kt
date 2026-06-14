package io.ghaylan.validation.constraint.validator.temporal.min

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.TemporalMin
import kotlin.reflect.KClass

/** Constraint metadata for [@TemporalMin][TemporalMin]. */
data class TemporalMinConstraint(
	val value: String,
	val inclusive: Boolean,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()