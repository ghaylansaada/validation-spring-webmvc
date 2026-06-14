package io.ghaylan.validation.constraint.validator.required

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Required
import io.ghaylan.validation.constraint.annotation.Required.RequirementCondition
import kotlin.reflect.KClass

/** Constraint metadata for [@Required][Required]. */
data class RequiredConstraint(
	val allowEmpty: Boolean,
	val dependentField: String,
	val condition: RequirementCondition,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()