package io.ghaylan.validation.accessor

import io.ghaylan.validation.utils.ReflectionUtils
import java.beans.Introspector
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Builds optimized [FieldAccessor] instances for reading values from **objects** or **maps**.
 *
 * ### Strategy Ladder (fastest viable)
 *
 * 0. Map lookup
 * 1. Public getter
 * 2. VarHandle
 * 3. MethodHandle (via [PropertyAccessor])
 * 4. Reflection
 *
 * ### Inheritance
 * Field lookup walks superclasses. We always **bind the accessor’s runtime [FieldAccessor.containerClass] to the
 * class requested by the schema**, *not* the field’s declaring class. That keeps schema + runtime aligned and makes
 * `getFromAny()` behave sanely for subclass instances.
 *
 * ### Safety
 * Synthetic, static, and transient fields are ignored during field lookup; we validate instance state only.
 *
 * ### Failure
 * If the container is not a Map and no field strategy succeeds, `build()` throws `IllegalStateException`.
 */
object AccessorFactory {

    /**
     * Build a [FieldAccessor] for [fieldName] on [containerClass].
     *
     * @param containerClass The class of the object **or Map** whose values will be read.
     * @param fieldName Logical property name (or Map key).
     * @throws IllegalStateException if no strategy succeeds and the container is not a Map.
     */
    fun <T : Any> build(
        containerClass: Class<T>,
        fieldName: String,
    ): FieldAccessor<T> {

        // Map container? (fast exit; do NOT attempt bean/field resolution on raw maps.)
        buildMapAccessorIfApplicable(containerClass, fieldName)?.let { return it }

        // Public getter (encapsulation-friendly, JPMS-safe).
        buildGetterAccessor(containerClass, fieldName)?.let { return it }

        // Field resolution across class hierarchy.
        val field = findFieldDeep(containerClass, fieldName)
            ?: error("Field '$fieldName' not found in ${containerClass.name} or its superclasses")

        // VarHandle (ultra-fast).
        buildVarHandleAccessor(field, containerClass)?.let { return it }

        // MethodHandle fallback (still fast).
        buildMethodHandleAccessor(containerClass, field)?.let { return it }

        // 5. Reflection fallback.
        return buildReflectiveAccessor(field, containerClass)
    }

    /**
     * If [containerClass] implements [Map], return an accessor that performs key lookup.
     *
     * ### Lookup Rules
     * 1. Direct lookup by exact `fieldName` (as `String` key).
     * 2. If not found, scan keys and compare `key?.toString()` to `fieldName` (covers enum/int keys).
     *
     * ### Performance Note
     * The fallback scan is O(n) in Map size; acceptable for small JSON objects. For large maps, prefer a
     * schema‑driven `KeyNormalizer` or wrap your map before validation.
     */
    private fun <T : Any> buildMapAccessorIfApplicable(
        containerClass: Class<T>,
        fieldName: String,
    ): FieldAccessor<T>? {
        if (!ReflectionUtils.isMapLike(containerClass)) return null
	    
	    return object: FieldAccessor<T> {
            override val containerClass: Class<T> = containerClass

            @Suppress("UNCHECKED_CAST")
            override fun get(instance: T): Any? {
                val map = instance as Map<*, *>
                // Fast path: exact String key
                if (map.containsKey(fieldName)) return map[fieldName]
                // Fallback: tolerant toString compare (enum, int keys)
                val match = map.entries.firstOrNull {
                    it.key?.toString() == fieldName
                }
                return match?.value
            }
        }
    }

    /**
     * Attempt to build an accessor that calls a public getter method.
     *
     * ### Naming Rules
     * - Tries standard `getXxx()`.
     * - Tries boolean `isXxx()`.
     * - Falls back to JavaBeans Introspector scan (handles Kotlin `isActive` → `isActive`, non‑standard cases).
     */
    private fun <T : Any> buildGetterAccessor(
        containerClass: Class<T>,
        fieldName: String
    ): FieldAccessor<T>? {
        val cap = fieldName.replaceFirstChar { it.uppercaseChar() }

        // Fast path reflective lookup
	    val method = runCatching { containerClass.getMethod("get$cap") }
		    .recoverCatching { containerClass.getMethod("is$cap") }
		    .recoverCatching {
			    if (fieldName.startsWith("is") && fieldName.getOrNull(2)?.isUpperCase() == true) {
				    containerClass.getMethod(fieldName)
			    } else throw NoSuchMethodException()
		    }
		    .getOrNull()
		    ?: findGetterViaBeanInfo(containerClass, fieldName)
		    ?: return null
	    
	    return object: FieldAccessor<T> {
            override val containerClass: Class<T> = containerClass
            override fun get(instance: T): Any? = method.invoke(instance)
        }
    }

    /**
     * Locate the first field with the given [fieldName] in [containerClass] or any of its superclasses.
     *
     * @param containerClass Starting point for the search.
     * @param fieldName Name of the field to find (case-sensitive).
     * @return The matching [Field] or `null` if not found.
     */
    private fun findFieldDeep(
        containerClass: Class<*>,
        fieldName: String
    ): Field? {
        var current: Class<*>? = containerClass
	    while (current != null) {
            current.declaredFields.firstOrNull { field ->
	            field.name == fieldName && !field.isSynthetic && !Modifier.isStatic(field.modifiers) && !Modifier.isTransient(field.modifiers)
            }?.let { return it }
            current = current.superclass
        }
        return null
    }

    /**
     * BeanInfo fallback that tolerates non-standard getter names.
     */
    private fun findGetterViaBeanInfo(
	    type: Class<*>,
	    fieldName: String
    ): Method? {
        return runCatching {
            val info = Introspector.getBeanInfo(type)
            info.propertyDescriptors.firstOrNull { it.name == fieldName }?.readMethod
        }.getOrNull()
    }

    /**
     * Attempt to build a VarHandle-based accessor for ultra-fast field access.
     *
     * @return `null` if:
     * * JPMS blocks deep reflection into [Field.declaringClass].
     * * SecurityManager forbids access.
     * * JVM version < 9 (no VarHandle support).
     */
    private fun <T : Any> buildVarHandleAccessor(
        field: Field,
        containerClass: Class<T>
    ): FieldAccessor<T>? {
        return runCatching {
            val declaringClass = field.declaringClass
            // Create a lookup with private access to the declaring class
            val lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())
            // Bind a VarHandle to the field
            val vh: VarHandle = lookup.findVarHandle(declaringClass, field.name, field.type)
            // Return an accessor using VarHandle for near-native performance
            object : FieldAccessor<T> {
                override val containerClass: Class<T> = containerClass
                override fun get(instance: T): Any? = vh.get(instance)
            }
        }.getOrNull()
    }
	
	/**
     * Fast fallback when VarHandle is unavailable but MethodHandle unreflect is allowed.
     * Uses [buildFieldAccessor] and wraps the result in a [FieldAccessor].
     */
    private fun <T : Any> buildMethodHandleAccessor(
        containerClass: Class<T>,
        field: Field
    ): FieldAccessor<T>? {
        return runCatching {
            val pa = field.buildFieldAccessor()
	        object: FieldAccessor<T> {
                override val containerClass: Class<T> = containerClass
                override fun get(instance: T): Any? = pa.get(instance)
            }
        }.getOrNull()
    }

    /**
     * Fallback: Build a reflection-based accessor.
     * Attempts to override accessibility using [Field.trySetAccessible] so non-public fields can be accessed
     * if JVM policy allows it.
     */
    private fun <T : Any> buildReflectiveAccessor(
        field : Field,
        containerClass: Class<T>
    ): FieldAccessor<T> {
        field.trySetAccessible()
	    return object: FieldAccessor<T> {
            override val containerClass: Class<T> = containerClass
            override fun get(instance: T): Any? = field.get(instance)
        }
    }
}