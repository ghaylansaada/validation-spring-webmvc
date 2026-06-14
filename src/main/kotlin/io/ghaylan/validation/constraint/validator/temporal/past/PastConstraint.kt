package io.ghaylan.validation.constraint.validator.temporal.past

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Past
import kotlin.reflect.KClass

/** Constraint metadata for [@Past][Past]. */
data class PastConstraint(
	val withinSeconds: Long = 0,
	val withinMinutes: Long = 0,
	val withinHours: Long = 0,
	val withinDays: Long = 0,
	val withinWeeks: Long = 0,
	val withinMonths: Long = 0,
	val withinYears: Long = 0,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()