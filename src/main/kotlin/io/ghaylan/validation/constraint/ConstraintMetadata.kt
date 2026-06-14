package io.ghaylan.validation.constraint

import io.ghaylan.validation.utils.ConstraintConverter
import kotlin.reflect.KClass

/**
 * Immutable configuration extracted from a constraint annotation at schema-build time.
 *
 * Subclasses mirror the properties of their corresponding annotation; property names must
 * match exactly so [ConstraintConverter] can map annotation values via reflection.
 *
 * @property groups Active groups; empty means always validate.
 * @property message Optional override for the validator's default error message.
 */
abstract class ConstraintMetadata {
	abstract val message: String
	abstract val groups: Set<KClass<*>>
	open val appliesToContainer: Boolean = false
}