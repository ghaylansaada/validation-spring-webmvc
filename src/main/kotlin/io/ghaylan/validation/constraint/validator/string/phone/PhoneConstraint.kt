package io.ghaylan.validation.constraint.validator.string.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import io.ghaylan.validation.constraint.ConstraintMetadata
import io.ghaylan.validation.constraint.annotation.Phone
import kotlin.reflect.KClass

/** Constraint metadata for [@Phone][Phone]. */
data class PhoneConstraint(
	val allowedTypes: Set<PhoneNumberType>,
	val allowedCountries: Set<String>,
	override val message: String,
	override val groups: Set<KClass<*>>
): ConstraintMetadata()