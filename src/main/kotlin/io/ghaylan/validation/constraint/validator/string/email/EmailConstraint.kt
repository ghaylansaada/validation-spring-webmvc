package io.ghaylan.validation.constraint.validator.string.email

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Email
import kotlin.reflect.KClass

/** Constraint metadata for [@Email][Email]. */
data class EmailConstraint(
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()