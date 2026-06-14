package io.ghaylan.validation.utils

import com.fasterxml.jackson.annotation.JsonProperty
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

/**
 * Resolves the effective serialized name of a [Field] or [KProperty], honoring [JsonProperty] when present.
 * Results are cached by (declaring class, member name) to avoid repeated reflection.
 */
object PropNameAccessor {
	
	/** Cache key: (declaring class, member name). Ensures uniqueness across classes with same-named fields. */
	private data class Key(
		val clazz: Class<*>,
		val memberName: String
	)
	
	/** Thread-safe cache of resolved property names. */
	private val cache = ConcurrentHashMap<Key, String>()
	
	/** Returns the [JsonProperty] value if present, otherwise the field's declared name. */
	fun getName(field: Field): String {
		val key = Key(field.declaringClass, field.name)
		return cache.computeIfAbsent(key) {
			field.getAnnotation(JsonProperty::class.java)?.value
				?: field.name
		}
	}
	
	/**
	 * Returns the [JsonProperty] value if present, otherwise the property's declared name.
	 * Falls back to the [KProperty]'s own class when there is no backing Java field.
	 */
	fun getName(property: KProperty<*>): String {
		val declaringClass = property.javaField?.declaringClass
			?: property::class.java
		val key = Key(declaringClass, property.name)
		return cache.computeIfAbsent(key) {
			property.findAnnotation<JsonProperty>()?.value
				?: property.name
		}
	}
}