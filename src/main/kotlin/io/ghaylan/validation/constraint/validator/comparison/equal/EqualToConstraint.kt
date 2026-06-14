package io.ghaylan.validation.constraint.validator.comparison.equal

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.EqualTo
import kotlin.reflect.KClass


/** Constraint metadata for [@EqualTo][EqualTo]. */
data class EqualToConstraint(
	val property: String,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()