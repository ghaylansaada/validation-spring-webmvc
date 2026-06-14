package io.ghaylan.validation.accessor

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.reflect.Field

/**
 * Precomputed, high‑performance accessor bound directly to a field's JVM getter slot via [MethodHandle].
 *
 * This is a **lower‑level primitive** than [FieldAccessor]:
 * - It always targets a specific **field** (not getter, not Map).
 * - It does **not** carry container type metadata.
 * - It is not aware of validation schema; you use it when you're already holding the reflected [Field].
 *
 * In most validation flows, prefer [AccessorFactory] → [FieldAccessor]; use this when you need
 * advanced or bulk reflection optimization (e.g., custom mapping, serialization tools, metrics scrapers).
 *
 * ### Thread Safety
 * Immutable; safe to reuse across threads.
 *
 * ### Example
 * ```kotlin
 * val field = UserDto::class.java.getDeclaredField("age")
 * val pa = field.buildFieldAccessor()
 * val age = pa.get(userInstance)
 * ```
 */
data class PropertyAccessor(
	val realName: String,
	val resolvedName: String,
	val handle: MethodHandle,
	val type: Class<*>
) {
	
	/**
	 * Cast the method handle type to accept a generic Object/Any instance
	 * to prevent WrongMethodTypeException during polymorphic invoke.
	 */
	private val genericHandle = handle.asType(MethodType.methodType(Any::class.java, Any::class.java))
	
	/**
	 * Retrieve the current value of the bound field from [instance].
	 *
	 * @param instance An object whose class (or superclass) declares the field associated with this accessor.
	 * @return The field's value (boxed if primitive, nullable).
	 * @throws Throwable If the underlying `MethodHandle` rejects the call (rare under normal usage).
	 */
	fun get(instance: Any): Any? = genericHandle.invokeExact(instance)
}