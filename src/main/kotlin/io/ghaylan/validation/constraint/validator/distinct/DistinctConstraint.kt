package io.ghaylan.validation.constraint.validator.distinct

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Distinct
import io.ghaylan.validation.constraint.annotation.Distinct.DistinctMode
import kotlin.reflect.KClass

/** Constraint metadata for [@Distinct][Distinct]. */
data class DistinctConstraint(
	val by: Set<String>,
	val mode: DistinctMode,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()