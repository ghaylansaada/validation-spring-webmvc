package io.ghaylan.validation.integration

import io.ghaylan.validation.exception.InvalidRequestException
import io.ghaylan.validation.groups.OnCreate
import io.ghaylan.validation.groups.OnDefault
import io.ghaylan.validation.groups.OnUpdate
import io.ghaylan.validation.model.ConstraintError
import kotlin.reflect.KClass

/**
 * Declares an interceptor boundary that triggers automated, schema-driven validation on incoming HTTP request components.
 *
 * When applied to a Spring MVC or WebFlux controller handler method, this annotation commands the web validation pipeline
 * to intercept the incoming request, resolve the pre-compiled schema graph for the endpoint, and assert invariants
 * across four distinct HTTP payload regions before delegation to the controller method logic.
 *
 * ---
 *
 * ### 1. Evaluation Targets
 * - **HTTP Request Body:** Controlled by [validateBody]. Recursively validates the deserialized object graph payload.
 * - **URI Query Parameters:** Controlled by [validateQuery]. Evaluates incoming flat or array-structured query string key-values.
 * - **URI Path Variables:** Controlled by [validatePath]. Asserts constraints on parameters extracted straight from the route template match.
 * - **HTTP Request Headers:** Controlled by [validateHeaders]. Inspects targeted metadata transport values present in the incoming header collection.
 *
 * ### 2. Architectural Execution Lifecycle
 * 1. **Schema Resolution:** At request inception, the web pipeline handler queries the [io.ghaylan.validation.schema.ValidationRegistry] via the unique method signature coordinate.
 * 2. **Context Filtering:** Active validation constraints are filtered out if their associated targeting boundaries do not match the runtime [groups] configuration profile.
 * 3. **Recursive Graph Traversal:** The engine structurally inspects all nested sub-properties and collection boundaries, extracting raw runtime values via optimized, non-reflective field accessors.
 * 4. **Exception Signaling:** If any constraint fails, evaluation behavior halts according to [singleErrorPerField], collects cumulative violations as a list of [ConstraintError] specifications, and aborts processing by throwing an [InvalidRequestException].
 *
 * ### 3. Contextual Validation Groups
 * Group profiling allows a single data object layout to satisfy different constraint metrics across separate workflow boundaries.
 * For instance, a property might be marked optional during patch sequences but remain mandatory during resource provisioning.
 * - [OnDefault]: The fallback execution profile. Constraints without explicit group bounds are categorized here automatically.
 * - [OnCreate]: Marker interface indicating strict validation profiles applied exclusively during entity instantiation phases.
 * - [OnUpdate]: Marker interface specifying partial or patch-safe validation rules during mutable modification phases.
 *
 * *Note: System engineers can expand this behavior by declaring custom domain interfaces or classes as marker tokens directly within the [groups] parameter.*
 *
 * ---
 *
 * ### Example Usage
 *
 * ```kotlin
 * @RestController
 * @RequestMapping("/api/v1/users")
 * class UserController {
 *
 *    @PostMapping
 *    @ValidateRequest
 *    fun createUser(@RequestBody dto: UserDTO): ResponseEntity<UserResponse> { }
 * }
 * ```
 *
 * @property validateBody Toggles validation tracking across the HTTP Request Body payload graph. Defaults to `true`.
 * @property validateQuery Toggles validation tracking across incoming URI query string parameters. Defaults to `true`.
 * @property validatePath Toggles validation tracking across mapped path segments extracted from the request URI route template. Defaults to `true`.
 * @property validateHeaders Toggles validation tracking across incoming HTTP request headers. Defaults to `true`.
 * @property singleErrorPerField Fail-fast property boundary controller.
 *          If `true`, the engine immediately short-circuits execution on an isolated field after its first constraint failure to decrease noisy telemetry outputs.
 *          If `false`, evaluates all available constraints assigned to the property. Defaults to `true`.
 * @property groups An array of marker types ([KClass]) specifying the active validation boundaries.
 *          The framework skips evaluating constraints whose declared group tags do not intersect with this array. Defaults to `[OnDefault::class]`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateRequest(
	val validateBody: Boolean = true,
	val validateQuery: Boolean = true,
	val validatePath: Boolean = true,
	val validateHeaders: Boolean = true,
	val singleErrorPerField: Boolean = true,
	val groups: Array<KClass<*>> = [OnDefault::class]
)