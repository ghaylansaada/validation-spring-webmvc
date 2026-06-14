package io.ghaylan.validation.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber

/**
 * Utility class for handling phone number validation, formatting, and extraction.
 */
object PhoneNumberUtils {
	
	private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
	
	/**
	 * Checks if the given number is a valid phone number (mobile or landline).
	 *
	 * @param internationalPhoneNumber The international phone number.
	 * @return `true` if valid, `false` otherwise.
	 */
	fun isValidNumber(internationalPhoneNumber: CharSequence): Boolean {
		val phone: PhoneNumber = getPhoneNumber(internationalPhoneNumber)
			?: return false
		return phoneNumberUtil.isValidNumber(phone)
	}
	
	/**
	 * Gets the number type (MOBILE, FIXED_LINE, etc.) for the given phone number.
	 *
	 * @param internationalPhoneNumber The international phone number.
	 * @return The [PhoneNumberType] or `null` if invalid.
	 */
	fun getNumberType(internationalPhoneNumber: CharSequence): PhoneNumberType? {
		val phone: PhoneNumber = getPhoneNumber(internationalPhoneNumber)
			?: return null
		return phoneNumberUtil.getNumberType(phone)
	}
	
	/**
	 * Retrieves the country ISO code (e.g., "US", "FR") from an international phone number.
	 *
	 * @param internationalPhoneNumber The international phone number.
	 * @return The country ISO code or `null` if invalid.
	 */
	fun getCountryISOCode(internationalPhoneNumber: CharSequence): String? {
		val phone: PhoneNumber? = getPhoneNumber(internationalPhoneNumber)
		return phoneNumberUtil.getRegionCodeForCountryCode(phone?.countryCode
			?: 0)
	}
	
	/**
	 * Parses an international phone number into a [PhoneNumber] object.
	 *
	 * @param internationalPhoneNumber The international phone number.
	 * @return A [PhoneNumber] object or `null` if invalid.
	 */
	private fun getPhoneNumber(internationalPhoneNumber: CharSequence): PhoneNumber? {
		return runCatching {
			val cleanNumber: String = getCleanNumber(internationalPhoneNumber)
			phoneNumberUtil.parse("+$cleanNumber", "")
		}.getOrNull()
	}
	
	/**
	 * Cleans a phone number string, keeping only digits.
	 *
	 * @param internationalPhoneNumber The phone number to clean.
	 * @return A sanitized phone number containing only digits.
	 */
	fun getCleanNumber(internationalPhoneNumber: CharSequence): String {
		val number: String = internationalPhoneNumber.replace("[^-?0-9]+".toRegex(), "")
		return when {
			number.startsWith("+00") -> number.replaceFirst("+00", "")
			number.startsWith("00") -> number.replaceFirst("00", "")
			number.startsWith("+") -> number.replaceFirst("+", "")
			else -> number
		}
	}
}