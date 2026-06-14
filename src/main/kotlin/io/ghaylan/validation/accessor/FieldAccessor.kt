package io.ghaylan.validation.accessor

/**
 * Lightweight, precomputed value extractor for a *single logical property* on a container type [T].
 *
 * A `FieldAccessor` shields framework code from the mechanics of **how** a value is read:
 *
 * - Java/Kotlin **getter** method (`getX()` / `isX()`).
 * - **VarHandle** direct field read (fast; JVM 9+).
 * - **MethodHandle** fallback (`unreflectGetter`) when VarHandle blocked.
 * - **Reflective** `Field.get()` last resort.
 * - **Map lookup** for header/query/param bags or JSON‑like payloads.
 *
 * Accessors are resolved **once** (at schema build / first use) and cached in [AccessorRegistry].
 * Runtime validation is therefore just a cheap virtual call.
 *
 * ---
 * ## Type Safety Model
 * The type parameter [T] is the **container type** (DTO class, Map implementation, etc.) you will
 * pass to [get]. It is **not** the property value type.
 *
 * - Use [get] when you *know* you have an instance of [T].
 * - Use [getFromAny] in schema traversal code that only receives `Any?` at runtime; it checks [containerClass].
 *
 * ---
 * ## Thread Safety
 * Implementations must be immutable and thread‑safe; the registry shares them across requests.
 *
 * ---
 * ## Example
 * ```kotlin
 * val acc = AccessorRegistry.getOrCreate(UserDto::class.java, "email")
 * val email = acc.get(userDto)              // type‑safe
 *
 * // Generic path (engine code walking schema; has Any?):
 * val value = acc.getFromAny(anyObject)     // runtime checked; null if wrong type
 * ```
 */
interface FieldAccessor<T: Any> {
	
	/**
	 * Runtime container class this accessor was built for (DTO, Map subtype, etc.).
	 * Used by [getFromAny] to validate generic calls.
	 */
	val containerClass: Class<T>
	
	/**
	 * Type-safe access when you *know* [instance] is (assignable to) [containerClass].
	 *
	 * @throws Throwable strategy-specific errors (reflection, invocation, access).
	 */
	fun get(instance: T): Any?
	
	/**
	 * Safe, *untyped* bridge for framework code that only has [Any].
	 *
	 * Behavior:
	 * - Returns `null` if [instance] is `null`.
	 * - If [instance] is not assignable to [containerClass]:
	 *   - Return `null` when [strict] = false (default).
	 *   - Throw `IllegalStateException` when [strict] = true.
	 * - Otherwise cast and delegate to [get].
	 *
	 * @param instance The runtime container object (DTO, Map, etc.), or `null`.
	 * @param strict If `true`, throw on container type mismatch (useful in development).
	 */
	@Suppress("UNCHECKED_CAST")
	fun getFromAny(
		instance: Any?,
		strict: Boolean = false
	): Any? {
		if (instance == null) return null
		
		return if (!containerClass.isInstance(instance)) {
			if (strict) {
				error("FieldAccessor expected container of type ${containerClass.name}, but received ${instance::class.java.name}.")
			}
			else null
		}
		else get(instance as T)
	}
}
