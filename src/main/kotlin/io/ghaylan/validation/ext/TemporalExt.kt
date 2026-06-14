package io.ghaylan.validation.ext

import java.time.*
import java.time.temporal.Temporal
import kotlin.reflect.KClass

/** Returns `true` if this temporal is strictly before [other]. Both must be the same type. */
fun Temporal.isBefore(other: Temporal): Boolean = compareTemporal(this, other) < 0

/** Returns `true` if this temporal is strictly after [other]. Both must be the same type. */
fun Temporal.isAfter(other: Temporal): Boolean = compareTemporal(this, other) > 0

/** Returns `true` if this temporal equals [other] by chronological comparison. */
fun Temporal.isEqual(other: Temporal): Boolean = compareTemporal(this, other) == 0

/** Returns `true` if this temporal is before or equal to [other]. */
fun Temporal.isBeforeOrEqual(other: Temporal): Boolean = compareTemporal(this, other) <= 0

/** Returns `true` if this temporal is after or equal to [other]. */
fun Temporal.isAfterOrEqual(other: Temporal): Boolean = compareTemporal(this, other) >= 0

/** Returns the current moment as the same concrete [Temporal] type as the receiver. */
internal fun Temporal.now(): Temporal {
	return when (this) {
		is LocalDate -> LocalDate.now()
		is LocalTime -> LocalTime.now()
		is OffsetTime -> OffsetTime.now()
		is LocalDateTime -> LocalDateTime.now()
		is ZonedDateTime -> ZonedDateTime.now()
		is OffsetDateTime -> OffsetDateTime.now()
		else -> Instant.now()
	}
}

/**
 * Converts a [String] into a [Temporal] instance of the specified [clazz].
 *
 * Supported types: LocalDate, LocalTime, OffsetTime, LocalDateTime, ZonedDateTime, OffsetDateTime, Instant.
 *
 * @receiver The string representation of the temporal.
 * @param clazz The target temporal class.
 * @return A temporal instance of type [clazz].
 * @throws IllegalArgumentException If the temporal type is unsupported.
 */
internal fun String.toTemporal(clazz: KClass<out Temporal>): Temporal {
	return when (clazz) {
		LocalDate::class -> LocalDate.parse(this)
		LocalTime::class -> LocalTime.parse(this)
		OffsetTime::class -> OffsetTime.parse(this)
		LocalDateTime::class -> LocalDateTime.parse(this)
		ZonedDateTime::class -> ZonedDateTime.parse(this)
		OffsetDateTime::class -> OffsetDateTime.parse(this)
		Instant::class -> Instant.parse(this)
		else -> throw IllegalArgumentException("Unsupported temporal type: ${clazz.java.name}")
	}
}

/**
 * Compares two [Temporal] instances of the same type.
 *
 * @param value1 First temporal instance.
 * @param value2 Second temporal instance.
 * @return Negative if value1 < value2, zero if equal, positive if value1 > value2.
 * @throws IllegalArgumentException If the temporal types are unsupported or mismatched.
 */
private fun compareTemporal(
	value1: Temporal,
	value2: Temporal
): Int {
	return when (value1) {
		is LocalDate if value2 is LocalDate -> value1.compareTo(value2)
		is LocalTime if value2 is LocalTime -> value1.compareTo(value2)
		is LocalDateTime if value2 is LocalDateTime -> value1.compareTo(value2)
		is ZonedDateTime if value2 is ZonedDateTime -> value1.compareTo(value2)
		is OffsetDateTime if value2 is OffsetDateTime -> value1.compareTo(value2)
		is OffsetTime if value2 is OffsetTime -> value1.compareTo(value2)
		is Instant if value2 is Instant -> value1.compareTo(value2)
		else -> throw IllegalArgumentException("Unsupported or mismatched Temporal types: ${value1::class.simpleName} vs ${value2::class.simpleName}")
	}
}