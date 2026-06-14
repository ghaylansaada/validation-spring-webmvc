package io.ghaylan.validation.engine

import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorDefinition
import io.ghaylan.validation.utils.PropNameAccessor
import java.lang.reflect.Field
import kotlin.reflect.KProperty

/**
 * Fluent builder for constructing [ConstraintError] instances.
 *
 * @property location Request section where the error originated.
 * @property code Error code representing the validation or business error type.
 */
class ConstraintErrorBuilder<CodeT>(
	private val location: ConstraintError.ErrorLocation,
	private val code: CodeT
) where CodeT: Enum<CodeT>, CodeT: ConstraintErrorDefinition {
	
	private var field: String? = null
	private var message: String? = null
	private var data: Any? = null
	private var dataMap: HashMap<String, Any?>? = null
	
	/**
	 * Sets the field path for the error using a dot-notation string.
	 *
	 * @param field Dot-notation path to the invalid field
	 *              e.g., "user.name" or "items[0].price"
	 * @return This builder instance for chaining
	 */
	fun field(field: String): ConstraintErrorBuilder<CodeT> {
		this.field = field
		return this
	}
	
	/**
	 * Sets the field path for the error using a Kotlin property reference.
	 *
	 * @param property KProperty of the invalid field (e.g., User::name, Item::price)
	 * @return This builder instance for chaining
	 */
	fun field(property: KProperty<*>): ConstraintErrorBuilder<CodeT> {
		this.field = PropNameAccessor.getName(property)
		return this
	}
	
	/**
	 * Sets the field path for the error using a Java reflection field.
	 *
	 * @param field The Java [Field] representing the invalid property
	 * @return This builder instance for chaining
	 */
	fun field(field: Field): ConstraintErrorBuilder<CodeT> {
		this.field = PropNameAccessor.getName(field)
		return this
	}
	
	/**
	 * Sets a generic data object associated with this error.
	 *
	 * @param value Arbitrary data object to include in the error
	 * @return This builder instance for chaining
	 */
	fun data(value: Any?): ConstraintErrorBuilder<CodeT> {
		this.data = value
		return this
	}
	
	/**
	 * Adds a single key-value pair to the data map associated with this error.
	 *
	 * @param key The key identifying the piece of data
	 * @param value The value associated with the key
	 * @return This builder instance for chaining
	 */
	fun data(
		key: String,
		value: Any?
	): ConstraintErrorBuilder<CodeT> {
		if (dataMap == null) dataMap = HashMap()
		dataMap!![key] = value
		return this
	}
	
	/**
	 * Sets the message for this error.
	 *
	 * @param message The error message
	 * @return This builder instance for chaining
	 */
	fun message(message: String): ConstraintErrorBuilder<CodeT> {
		this.message = message
		return this
	}
	
	/**
	 * Builds and returns the configured [ConstraintError] instance.
	 * @return The constructed ApiError with all configured properties
	 */
	internal fun build(): ConstraintError<CodeT> = ConstraintError(path = field,
		code = code,
		message = message,
		location = location,
		metadata = dataMap?.ifEmpty { null }
			?: data)
}