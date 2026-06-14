package io.ghaylan.validation.accessor

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread‑safe cache of [FieldAccessor]s keyed by `(containerClass, fieldName)`.
 *
 * ### Why Cache?
 * Building accessors involves reflection, JPMS checks, VarHandle/MethodHandle linking.
 * We do that **once**, then reuse a cheap virtual call for every request validation.
 *
 * ### Classloader Safety
 * Keys use actual `Class<?>` references (not names) to avoid collisions across shaded JARs
 * and hot reload classloaders. Call [clear] when the app reloads in dev mode.
 *
 * ### Example
 * ```kotlin
 * val emailAcc = AccessorRegistry.getOrCreate(UserDto::class.java, "email")
 * val email = emailAcc.get(userDto)
 * ```
 */
object AccessorRegistry {
	
	/** Internal key: container class + field name. */
	private data class Key(
		val clazz: Class<*>,
		val fieldRealName: String,
		val fieldResolvedName: String
	)
	
	/** Global cache: safe for concurrent read/write. */
	private val cache = ConcurrentHashMap<Key, FieldAccessor<*>>()
	
	/**
	 * Get (or build & cache) an accessor for [fieldRealName] on [containerClass].
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T: Any> getOrCreate(
		containerClass: Class<T>,
		fieldRealName: String,
		fieldResolvedName: String
	): FieldAccessor<T> {
		return cache.computeIfAbsent(Key(containerClass, fieldRealName, fieldResolvedName)) {
			AccessorFactory.build(containerClass, fieldRealName)
		} as FieldAccessor<T>
	}
	
	/**
	 * Remove all cached accessors.
	 * Use in dev mode when classloaders bounce (Spring DevTools).
	 */
	fun clear() = cache.clear()
}