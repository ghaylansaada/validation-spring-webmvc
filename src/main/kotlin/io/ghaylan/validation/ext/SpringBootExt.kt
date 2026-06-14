package io.ghaylan.validation.ext

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * Resolves the effective query parameter name for this [Parameter].
 * * The resolution order checks [RequestParam.name], falling back to [RequestParam.value]
 * if the name is blank, and finally defaulting to the parameter's declared variable name.
 *
 * @return The resolved query parameter name string.
 */
internal fun Parameter.requestParamName(): String {
	val annotation = getAnnotation(RequestParam::class.java)
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Resolves the effective HTTP header name for this [Parameter].
 * * The resolution order checks [RequestHeader.name], falling back to [RequestHeader.value]
 * if the name is blank, and finally defaulting to the parameter's declared variable name.
 *
 * @return The resolved HTTP header name string.
 */
internal fun Parameter.requestHeaderName(): String {
	val annotation = getAnnotation(RequestHeader::class.java)
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Resolves the effective URL path variable name for this [Parameter].
 * * The resolution order checks [PathVariable.name], falling back to [PathVariable.value]
 * if the name is blank, and finally defaulting to the parameter's declared variable name.
 *
 * @return The resolved path variable name string.
 */
internal fun Parameter.pathVariableName(): String {
	val annotation = getAnnotation(PathVariable::class.java)
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/** * Resolves the serialized field name for a JSON request body mapping.
 * * Checks for a [JsonProperty] annotation on this [Field] to extract its configured value.
 * If the annotation is absent or blank, falls back to the field's raw declared name.
 * * @return The effective JSON property name.
 */
internal fun Field.bodyFieldName(): String {
	val annotation = getAnnotation(JsonProperty::class.java)
	return annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Generates a unique, fully-qualified identifier for this [Method].
 * * The identifier is structured as `package.ClassName#methodName(ParamType1,ParamType2)`,
 * which safely distinguishes overloaded methods within the same class context by appending
 * simple parameter type signatures.
 * * @return A unique string token representing the specific method signature.
 */
fun Method.getUniqueIdentifier(): String {
	val clazz = this.declaringClass.name
	val method = this.name
	val params = this.parameterTypes.joinToString(",") { it.simpleName }
	return "$clazz#$method($params)"
}