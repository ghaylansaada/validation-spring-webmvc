package io.ghaylan.validation.accessor

import io.ghaylan.validation.ext.bodyFieldName
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

/**
 * Build a [PropertyAccessor] for this reflective [Field].
 *
 * ### Steps
 * 1. `trySetAccessible()` (best-effort; ignored if not permitted).
 * 2. Use `MethodHandles.lookup().unreflectGetter(this)` to obtain a JVM getter [java.lang.invoke.MethodHandle].
 * 3. Wrap in [PropertyAccessor].
 *
 * ### When It Can Fail
 * - `IllegalAccessException`: access denied (JPMS/module restrictions).
 * - Security restrictions preventing deep reflection.
 *
 * In production, call this from code that can fall back (e.g., to reflection or a getter).
 *
 * @throws IllegalAccessException if the lookup fails.
 */
@Throws(IllegalAccessException::class)
fun Field.buildFieldAccessor(): PropertyAccessor {
    // Best-effort attempt to relax reflective access checks.
	// This does not bypass JPMS module boundaries and may be ignored by the JVM.
    this.trySetAccessible()

    // Create a MethodHandle directly backed by the field's JVM getter.
    val handle = MethodHandles.lookup()
	    .unreflectGetter(this)

    return PropertyAccessor(
        realName = this.name,
        resolvedName = this.bodyFieldName(),
        handle = handle,
        type = this.type)
}