package io.ghaylan.validation.integration

import io.ghaylan.validation.engine.ValidatorEngine
import io.ghaylan.validation.exception.InvalidRequestException
import io.ghaylan.validation.ext.getUniqueIdentifier
import io.ghaylan.validation.ext.pathVariableName
import io.ghaylan.validation.ext.requestHeaderName
import io.ghaylan.validation.ext.requestParamName
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * A customized [ServletInvocableHandlerMethod] extension that integrates custom, multi-vector request validation
 * directly into the Spring MVC controller method invocation pipeline.
 *
 * By overriding [invokeForRequest], this class optimizes the validation execution order: it allows Spring's native
 * infrastructure (`HandlerMethodArgumentResolver` implementations) to completely execute initial tasks such as
 * raw input extraction, JSON/XML deserialization via Jackson, and parameter type conversion (e.g., parsing raw strings
 * into complex objects like [java.util.UUID], [java.time.Instant], or custom Enums).
 *
 * Once arguments are fully materialized into type-safe instances, this class segregates them by their HTTP transport
 * vectors and routes them through the custom validation pipeline. The request safely proceeds to the underlying controller
 * endpoint logic only if no structural constraint violations occur.
 *
 * @param handlerMethod The source method execution handle describing the specific destination endpoint.
 * @property validatorEngine The core stateless validation runtime engine responsible for enforcing structural schema criteria.
 */
class ValidatingServletInvocableHandlerMethod(
    handlerMethod: HandlerMethod,
    private val validatorEngine: ValidatorEngine
) : ServletInvocableHandlerMethod(handlerMethod) {
	
	/**
	 * Intercepts the request processing sequence to resolve parameter values and enforce validation schemas prior to
	 * invoking the controller endpoint.
	 *
	 * This implementation splits the execution cycle into three discrete structural milestones:
	 * 1. **Argument Resolution:** Delegating to [getMethodArgumentValues] to construct strongly-typed model parameters from the request.
	 * 2. **Validation Verification:** Inspecting metadata markers on the bridged method or its enclosing declaration class for [ValidateRequest] targets.
	 * 3. **Target Invocation:** Forwards processing to the controller via [doInvoke] if—and only if—the input matches schema requirements.
	 *
	 * @param request The current web request container wrapper.
	 * @param mavContainer The model and view tracking context container for the active request scope.
	 * @param providedArgs Variable arguments provided explicitly by internal Spring dispatcher routines.
	 * @return The execution result of the destination endpoint method handler.
	 * @throws InvalidRequestException if any input parameter violates validation schema rules.
	 * @throws Exception if downstream controller method invocation or standard argument binding fails.
	 */
    override fun invokeForRequest(
        request: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        vararg providedArgs: Any?
    ): Any? {
        // 1. Let Spring execute all type conversions and object mapping (Instant, UUID, Enums, etc.)
        val args = getMethodArgumentValues(request, mavContainer, *providedArgs)

        val method = bridgedMethod
        
        // 2. Check if the action or parent class demands custom validation
        val hasAnnotation = method.isAnnotationPresent(ValidateRequest::class.java) ||
		        method.declaringClass.isAnnotationPresent(ValidateRequest::class.java)

        if (hasAnnotation && method.parameterCount > 0) {
            runValidation(method, method.parameters, args)
        }

        // 3. Safe to proceed to the controller method with perfectly validated, type-safe arguments
        return doInvoke(*args)
    }
	
	/**
	 * Extracts, segregates, and bundles materialized argument arrays into distinct HTTP structural maps
	 * before delegating to the verification engine.
	 *
	 * Iterates through the reflection [parameters] array, checking for transport marker annotations
	 * (`@RequestBody`, `@RequestParam`, `@RequestHeader`, `@PathVariable`). It maps resolved names via extension utilities
	 * to group positional arguments correctly.
	 *
	 * Non-empty maps are dispatched directly to [ValidatorEngine.validateRequest]. If the engine logs errors,
	 * the transaction terminates instantly.
	 *
	 * @param method The actual, executable reflection method handle.
	 * @param parameters The ordered collection of parameters defined on the target method signature.
	 * @param args The current, resolved method arguments array matching the order of [parameters].
	 * @throws InvalidRequestException containing collected constraint failure details when violations are caught.
	 */
    private fun runValidation(method: Method, parameters: Array<Parameter>, args: Array<Any?>) {
        var requestBody: Any? = null
        val queryParams = mutableMapOf<String, Any?>()
        val headers = mutableMapOf<String, Any?>()
        val pathVariables = mutableMapOf<String, Any?>()

        // Zip the parameters with their compiled, fully-mapped runtime values
        for (index in parameters.indices) {
            val param = parameters[index]
            val value = args[index]

            when {
                param.isAnnotationPresent(RequestBody::class.java) && requestBody == null -> {
                    requestBody = value
                }
                param.isAnnotationPresent(RequestParam::class.java) -> {
                    queryParams[param.requestParamName()] = value
                }
                param.isAnnotationPresent(RequestHeader::class.java) -> {
                    headers[param.requestHeaderName()] = value
                }
                param.isAnnotationPresent(PathVariable::class.java) -> {
                    pathVariables[param.pathVariableName()] = value
                }
            }
        }

        // 4. Pass the clean, typed infrastructure directly into your engine
        val errors = validatorEngine.validateRequest(
            id = method.getUniqueIdentifier(),
            body = requestBody ?: Any(),
            params = queryParams.ifEmpty { null },
            headers = headers.ifEmpty { null },
            pathVariables = pathVariables.ifEmpty { null })

        if (errors.isNotEmpty()) {
            throw InvalidRequestException(errors = errors)
        }
    }
}