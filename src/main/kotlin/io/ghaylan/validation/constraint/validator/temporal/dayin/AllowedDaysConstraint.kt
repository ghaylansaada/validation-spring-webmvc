package io.ghaylan.validation.constraint.validator.temporal.dayin

import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.DayIn
import java.time.DayOfWeek
import kotlin.reflect.KClass

/** Constraint metadata for [@AllowedDays][DayIn]. */
data class AllowedDaysConstraint(
	val days: Set<DayOfWeek>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()