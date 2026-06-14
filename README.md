# Spring Boot Schema-Based Validation

A high-performance, schema-based validation framework for Spring Boot **WebMVC** applications — delivering superior
flexibility, performance, and developer experience compared to standard Bean Validation (JSR-380).

> **Note:** This framework targets **Spring WebMVC** (servlet-based). Reactive WebFlux is not supported.

---

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Features](#features)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
    - [Automatic Request Validation](#1-automatic-request-validation-with-validaterequest)
    - [Validation Groups](#validation-groups)
    - [Configuration Properties Validation](#2-configuration-properties-validation-with-validateconfig)
    - [Dynamic Validation](#3-dynamic-validation-runtime-schema)
    - [Manual Validation](#4-manual-validation-with-error-collector)
- [Common Constraint Properties](#common-constraint-properties)
- [Built-in Constraints](#built-in-constraints)
- [Constraint Reference](#constraint-reference)
- [Type-Level Validation](#type-level-validation)
- [Error Handling](#error-handling)
- [Extending the Framework](#extending-the-framework)
- [Performance Considerations](#performance-considerations)
- [Why Use This Framework?](#why-use-this-framework)
- [License](#license)

---

## Overview

This library is a complete validation solution for Spring Boot WebMVC applications that improves upon the built-in
validation mechanisms in several key ways:

- **Schema-based** — Generates optimized validation schemas at startup or on demand at runtime
- **High-performance** — Precomputes field accessors and avoids reflection in the hot path
- **MVC-integrated** — Automatically validates controller inputs via `ValidatingServletInvocableHandlerMethod`
- **API-friendly errors** — Structured `ConstraintError<CodeT>` responses with typed error codes, field paths, and locations
- **Config validation** — Validates `@ConfigurationProperties` beans at startup via `@ValidateConfig`
- **Dynamic validation** — Supports runtime schema generation for dynamic or arbitrary payloads
- **Manual validation** — Powerful fluent error-collection API for custom business logic
- **Type-level validation** — Annotate generic type arguments directly (e.g., `List<@Email String>`)
- **Extensible error codes** — Define custom error codes by implementing `ConstraintErrorDefinition`

Perfect for applications with complex validation requirements, high-performance needs, or API-first designs.

---

## Requirements

| Dependency      | Minimum Version |
|-----------------|-----------------|
| JDK             | 25+             |
| Kotlin          | 2.4+            |
| Spring Boot     | 4.1+            |
| Spring WebMVC   | Required        |

---

## Features

- **Schema-based validation** — Pre-computed validation schemas for maximum throughput
- **Zero-reflection runtime** — Uses compiled accessors (`VarHandle`, `MethodHandle`) for field value retrieval
- **Spring Boot auto-configuration** — Zero-config setup via `@AutoConfiguration`
- **45+ built-in constraints** — Covers strings, numbers, temporals, collections, geographic data, financial formats, and more
- **Type-level validation** — Apply constraints directly to generic type arguments (`List<@Email String>`, `Map<String, @Min(0) Int>`)
- **Flexible error structure** — `ConstraintError<CodeT>` with typed codes, field paths, and error locations
- **Dynamic validation** — First-class `validate<T>()` API for runtime schema generation
- **Manual validation mode** — Fluent error-collector API for business rule enforcement
- **Validation groups** — Context-sensitive rules with `OnCreate`, `OnUpdate`, `OnDefault`, or custom groups
- **Cross-field validation** — Compare fields within the same object (`@EqualTo`, `@GreaterThan`, `@LessThan`, etc.)
- **Conditional requirements** — `@Required` with `IF_DEPENDENT_NULL` / `IF_DEPENDENT_NOT_NULL` conditions
- **Nested & recursive validation** — Deep validation of nested objects, arrays, and multi-dimensional collections
- **Config validation** — Fail-fast startup validation of `@ConfigurationProperties` via `@ValidateConfig`
- **Custom error codes** — Extend the error system by implementing `ConstraintErrorDefinition` on any enum

---

## How It Works

The framework performs all expensive operations (reflection, annotation processing) at startup and uses cached
components during request validation for optimal runtime performance.

### Startup Phase 1 — Discovering Validators

When the application starts:

1. The framework scans the classpath for annotations marked with `@Constraint`.
2. For each constraint found, it:
    - Maps the annotation to its metadata class (specified in the `metadata` property)
    - Collects all validator classes from the `validatedBy` array
    - Resolves validator instances through the following strategy, in order:
        1. Kotlin `object` singleton
        2. Spring-managed bean
        3. Autowiring via `AutowireCapableBeanFactory`
        4. No-arg constructor fallback
3. Validators are cached in `ValidationRegistry` using a nested map:
    - **Outer key** — constraint metadata class (`KClass<out ConstraintMetadata>`)
    - **Inner key** — supported value type (`TypeInfo`)
    - **Value** — validator instance

### Startup Phase 2 — Building Request Schemas

After caching validators, the framework:

1. Finds all controller methods (or classes) annotated with `@ValidateRequest`
2. For each method, analyzes all parameter annotations:
    - `@RequestBody` — request body class and all nested fields
    - `@PathVariable` — path variables
    - `@RequestParam` — query parameters
    - `@RequestHeader` — HTTP headers
3. For each field or parameter:
    - Extracts constraint annotations (including type-use annotations on generic parameters) and converts them to `ConstraintMetadata` via `ConstraintConverter`
    - Matches each constraint with the most compatible validator (exact type → supertype → wildcard → `Any`)
    - Builds or retrieves a cached `FieldAccessor` from `AccessorRegistry`
    - Packages everything into a `PropertySpec`
4. Creates a complete `RequestInputSchema` containing maps of all path variables, headers, query params, and body fields
5. Registers the schema in `ValidationRegistry`, indexed by a unique method identifier

### Runtime Validation

When a request arrives:

1. `ValidatingServletInvocableHandlerMethod` intercepts the method invocation **after** Spring has fully resolved all
   arguments (deserialization, type conversion, etc.)
2. It checks whether `@ValidateRequest` is present on the method or its declaring class
3. `ValidatorEngine` validates each enabled section (body, query, headers, path):
    - Retrieves field values using cached accessors — no reflection
    - Applies each constraint via cached validators
    - Builds precise field paths (e.g., `user.address[0].city`)
    - Collects `ConstraintError` instances with typed codes, paths, and optional messages
4. Deduplicates errors and throws `InvalidRequestException` if any violations exist

### Configuration Validation

`ConfigValidationPostProcessor` is a `BeanPostProcessor` that runs after each bean is initialized. When it finds a
bean annotated with `@ValidateConfig`, it passes it through `ValidatorEngine`. If violations are found, it builds a
detailed startup report and throws `InvalidConfigurationException`, preventing the application from starting.

---

## Getting Started

### Add Dependency

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("io.github.ghaylansaada:validation-spring-webmvc:0.0.1")
}
```

**Maven**

```xml
<dependency>
    <groupId>io.github.ghaylansaada</groupId>
    <artifactId>validation-spring-webmvc</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Auto-Configuration

Adding the dependency automatically registers the following beans via `@AutoConfiguration`:

| Bean                                      | Role                                                                         |
|-------------------------------------------|------------------------------------------------------------------------------|
| `ValidationRegistry`                      | Validator and schema cache                                                   |
| `ValidatorEngine`                         | Validation execution engine                                                  |
| `ValidatingServletInvocableHandlerMethod` | MVC-layer interceptor (replaces the default `ServletInvocableHandlerMethod`) |
| `ConfigValidationPostProcessor`           | Startup `@ConfigurationProperties` validator                                 |

All beans use `@ConditionalOnMissingBean`, so any component can be overridden by providing your own bean.

---

## Usage Examples

### 1. Automatic Request Validation with @ValidateRequest

Place `@ValidateRequest` on a controller method or on the controller class itself to enable automatic validation:

```kotlin
@RestController
class UserController(private val userService: UserService) {

    @ValidateRequest
    @PostMapping("/users")
    fun createUser(@RequestBody user: UserDTO): UserDTO {
        return userService.createUser(user)
    }
}
```

`ValidatingServletInvocableHandlerMethod` intercepts the invocation after Spring has already performed all argument
resolution (Jackson deserialization, type conversion, etc.), so your validators always receive fully typed,
ready-to-use objects.

---

### Validation Groups

Groups allow different validation rules to apply depending on the operation context:

```kotlin
data class UserDTO(
    @field:Required(groups = [OnCreate::class, OnUpdate::class])
    val id: Long?,

    @field:Required(groups = [OnCreate::class])
    @field:Email
    val email: String?,

    @field:Required(groups = [OnCreate::class])
    @field:Password(
        minLength = 8,
        maxLength = 100,
        requireUppercase = true,
        requireDigit = true,
        groups = [OnCreate::class]
    )
    val password: String?,

    @field:EqualTo(property = "password", groups = [OnCreate::class])
    val confirmPassword: String?
)
```

**Built-in groups:**

| Group       | Purpose                                        |
|-------------|------------------------------------------------|
| `OnDefault` | Default group, used when no group is specified |
| `OnCreate`  | Applied during creation operations             |
| `OnUpdate`  | Applied during update operations               |

**Custom groups** are defined as simple marker interfaces:

```kotlin
interface OnAdmin
interface OnPublic
```

---

### 2. Configuration Properties Validation with @ValidateConfig

Annotate any `@ConfigurationProperties` class with `@ValidateConfig` to enable fail-fast startup validation:

```kotlin
@ValidateConfig
@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    @field:Required
    val jwtSecret: String?,

    @field:Size(min = 8)
    val apiKey: String?,

    @field:Required
    val allowedOrigins: List<String>?
)
```

If any constraint is violated at startup, `ConfigValidationPostProcessor` prints a detailed report and throws
`InvalidConfigurationException`, preventing the application from accepting traffic. Example output:

```
Reason: Configuration validation failed for bean 'securityProperties'.
Class : com.example.SecurityProperties
Errors: 1 violation(s) found.

(1) Property : app.security.jwtSecret
    Path     : jwtSecret
    Code     : VALUE_MISSING
    Message  : This field is required and cannot be null or missing.
```

---

### 3. Dynamic Validation (Runtime Schema)

Validate arbitrary objects at runtime — schemas are created and cached on demand:

```kotlin
@Service
class DynamicValidationService(private val validatorEngine: ValidatorEngine) {

    fun validateObject(data: Any) {
        validatorEngine.validate(
            params = data,
            singleErrorPerField = false,
            groups = arrayOf(OnDefault::class)
        )
    }
}
```

---

### 4. Manual Validation with Error Collector

For business rules that cannot be expressed with annotations:

```kotlin
@Service
class OrderService(private val repository: OrderRepository) {

    fun placeOrder(order: OrderDTO): Order {
        val collector = RequestErrorCollector()

        if (order.items.isEmpty()) {
            collector.business(OrderErrorCode.ORDER_EMPTY)
                .field("items")
                .message("Order must contain at least one item")
        }

        if (order.total < 0) {
            collector.body(OrderErrorCode.INVALID_TOTAL)
                .field("total")
                .message("Total cannot be negative")
        }

        // Throws InvalidRequestException if any errors were collected
        collector.throwIfNotEmpty(code = RequestErrorCode.INVALID_ORDER)

        return repository.saveOrder(order)
    }
}
```

The collector supports all error locations: `body()`, `query()`, `header()`, `path()`, `business()`.

Each error builder supports: `.field()`, `.message()`, and `.metadata()`.

---

## Common Constraint Properties

All constraints share the following properties:

### `groups`

Controls when the constraint is applied:

```kotlin
@field:Required(groups = [OnCreate::class, OnUpdate::class])
val name: String?
```

### `message`

Optional human-readable error message override. If not set, the validator provides a sensible default:

```kotlin
@field:Email(message = "Please enter a valid email address")
val email: String?
```

---

## Built-in Constraints

### General

| Constraint  | Description                                                        | Key Properties                              |
|-------------|--------------------------------------------------------------------|---------------------------------------------|
| `@Required` | Ensures a value is not null, empty, or blank                       | `allowEmpty`, `dependentField`, `condition` |
| `@Size`     | Validates array / collection / `Map` / `String` size within bounds | `min`, `max`                                |

### String

| Constraint      | Description                                                | Key Properties                                                                                                                                                                           |
|-----------------|------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Email`        | Valid email address format                                 | —                                                                                                                                                                                        |
| `@Regex`        | Value must match a regular expression                      | `pattern`                                                                                                                                                                                |
| `@StrOcc`       | String contains / equals / starts / ends with a value      | `value`, `mode`, `minOccurrences`, `maxOccurrences`, `ignoreCase`                                                                                                                        |
| `@NotStrOcc`    | String must NOT contain / equal / start / end with a value | `value`, `mode`, `ignoreCase`                                                                                                                                                            |
| `@Url`          | Valid URL with type / protocol / extension validation      | `type`, `maxLength`, `allowedPorts`, `allowedParams`, `allowedProtocols`, `allowedExtensions`                                                                                            |
| `@HexColor`     | Valid hex color code (`#FFF` or `#FFFFFF`)                 | —                                                                                                                                                                                        |
| `@Html`         | Safe HTML with tag / attribute / protocol whitelisting     | `allowedTags`, `allowedAttrs`, `allowedProtocols`                                                                                                                                        |
| `@IBAN`         | Valid International Bank Account Number                    | —                                                                                                                                                                                        |
| `@CountryCode`  | Valid ISO 3166-1 alpha-2 country code                      | —                                                                                                                                                                                        |
| `@CurrencyCode` | Valid ISO 4217 currency code                               | —                                                                                                                                                                                        |
| `@LanguageCode` | Valid ISO 639-1 language code (e.g., `"en"`, `"fr"`)       | —                                                                                                                                                                                        |
| `@Phone`        | Valid international phone number                           | `allowedTypes`, `allowedCountries`                                                                                                                                                       |
| `@CreditCard`   | Valid credit card number (Luhn algorithm)                  | —                                                                                                                                                                                        |
| `@Password`     | Configurable password strength validation                  | `minLength`, `maxLength`, `requireUppercase`, `requireLowercase`, `requireDigit`, `requireSpecialChar`, `allowedSpecialChars`, `minEntropy`, `noSequentialChars`, `noRepetitivePatterns` |

### Number

| Constraint    | Description                          | Key Properties       |
|---------------|--------------------------------------|----------------------|
| `@NumberMin`  | Minimum numeric value                | `value`, `inclusive` |
| `@NumberMax`  | Maximum numeric value                | `value`, `inclusive` |
| `@MultipleOf` | Value must be a multiple of a factor | `factor`             |
| `@Latitude`   | Valid latitude (−90 to 90)           | —                    |
| `@Longitude`  | Valid longitude (−180 to 180)        | —                    |

### Cross-Field Comparison

These constraints compare the annotated field against another field in the same object, using the `property` parameter:

| Constraint     | Description                                           | Key Properties          |
|----------------|-------------------------------------------------------|-------------------------|
| `@EqualTo`     | Must equal the referenced property                    | `property`              |
| `@NotEqualTo`  | Must not equal the referenced property                | `property`              |
| `@GreaterThan` | Must be strictly greater than the referenced property | `property`, `inclusive` |
| `@LessThan`    | Must be strictly less than the referenced property    | `property`, `inclusive` |

```kotlin
data class DateRange(
    val startDate: LocalDate?,

    @field:GreaterThan(property = "startDate", inclusive = false)
    val endDate: LocalDate?
)
```

### Allowed Values

| Constraint    | Description                                                | Key Properties |
|---------------|------------------------------------------------------------|----------------|
| `@ValueIn`    | Value must be one of a set of allowed string values        | `values`       |
| `@ValueNotIn` | Value must not be one of a set of disallowed string values | `values`       |

```kotlin
@field:ValueIn(values = ["ACTIVE", "INACTIVE", "PENDING"])
val status: String?

@field:ValueNotIn(values = ["admin", "root", "system"])
val username: String?
```

### Temporal

| Constraint     | Description                                        | Key Properties                                  |
|----------------|----------------------------------------------------|-------------------------------------------------|
| `@Past`        | Must be in the past (with optional within-range)   | `withinDays`, `withinHours`, `withinMinutes`, … |
| `@Future`      | Must be in the future (with optional within-range) | `withinDays`, `withinHours`, `withinMinutes`, … |
| `@TemporalMin` | Must be after a specified date/time                | `value`, `inclusive`                            |
| `@TemporalMax` | Must be before a specified date/time               | `value`, `inclusive`                            |
| `@AllowedDays` | Must fall on one of the specified days of the week | `days`                                          |

Supported temporal types: `LocalDate`, `LocalTime`, `OffsetTime`, `LocalDateTime`, `ZonedDateTime`,
`OffsetDateTime`, `Instant`.

---

## Constraint Reference

### @Required — Conditional Dependencies

```kotlin
data class ContactDTO(
    val email: String?,

    // Required only when email is not provided
    @field:Required(dependentField = "email", condition = RequirementCondition.IF_DEPENDENT_NULL)
    val phone: String?,

    // Required only when email IS provided
    @field:Required(dependentField = "email", condition = RequirementCondition.IF_DEPENDENT_NOT_NULL)
    val emailVerificationCode: String?
)
```

| Condition               | Meaning                                            |
|-------------------------|----------------------------------------------------|
| `ALWAYS` *(default)*    | Always required                                    |
| `IF_DEPENDENT_NULL`     | Required only when the dependent field is null     |
| `IF_DEPENDENT_NOT_NULL` | Required only when the dependent field is not null |

---

### @Password — Strength Configuration

```kotlin
@field:Password(
    minLength = 12,
    maxLength = 128,
    requireUppercase = true,
    requireLowercase = true,
    requireDigit = true,
    requireSpecialChar = true,
    allowedSpecialChars = "!@#\$%^&*",
    minEntropy = Password.PasswordStrength.STRONG,
    noSequentialChars = true,
    noRepetitivePatterns = true
)
val password: String?
```

**Strength levels** (Shannon entropy bits):

| Level         | Entropy Bits |
|---------------|--------------|
| `VERY_WEAK`   | 0            |
| `WEAK`        | 28           |
| `MODERATE`    | 36           |
| `STRONG`      | 60           |
| `VERY_STRONG` | 128          |

---

### @Url — Type-Specific Validation

```kotlin
@field:Url(type = UrlType.IMAGE, allowedProtocols = ["https"])
val avatarUrl: String?

@field:Url(type = UrlType.WEBSITE, allowedParams = [])
val homepage: String?

@field:Url(type = UrlType.VIDEO, allowedExtensions = ["mp4", "webm"])
val videoUrl: String?
```

URL types: `GENERIC`, `WEBSITE`, `FILE`, `IMAGE`, `VIDEO`, `AUDIO`. Media types enforce valid file extensions.

---

### @Future / @Past — Within-Range Constraints

```kotlin
// Must be in the future, but within the next 30 days
@field:Future(withinDays = 30)
val appointmentDate: LocalDateTime?

// Must be in the past, within the last 1 year
@field:Past(withinYears = 1)
val dateOfBirth: LocalDate?
```

Available range parameters: `withinSeconds`, `withinMinutes`, `withinHours`, `withinDays`, `withinWeeks`,
`withinMonths`, `withinYears`.

---

### @Distinct — Uniqueness Modes

```kotlin
// Scalar uniqueness: no duplicate strings
@field:Distinct
val tags: List<String>?

// Unique by a single field
@field:Distinct(by = ["email"], mode = DistinctMode.PER_FIELD)
val users: List<UserDTO>?

// Unique by a combination of fields
@field:Distinct(by = ["firstName", "lastName"], mode = DistinctMode.COMBINATION)
val employees: List<EmployeeDTO>?
```

| Mode          | Meaning                                                     |
|---------------|-------------------------------------------------------------|
| `PER_FIELD`   | Each specified field must be independently unique           |
| `COMBINATION` | Uniqueness is enforced on the tuple of all specified fields |

---

### @Html — Safe HTML Whitelisting

```kotlin
@field:Html(
    allowedTags = ["b", "i", "u", "p", "a", "ul", "li"],
    allowedAttrs = ["a:href"],
    allowedProtocols = ["https"]
)
val richText: String?
```

---

### @Phone — Type and Country Filtering

```kotlin
@field:Phone(
    allowedTypes = [PhoneNumberUtil.PhoneNumberType.MOBILE],
    allowedCountries = ["US", "FR", "TN"]
)
val mobilePhone: String?
```

---

### @StrOcc — String Occurrence Modes

```kotlin
@field:StrOcc(value = "http", mode = StrOccMode.STARTS_WITH)
val url: String?

@field:StrOcc(value = "@", mode = StrOccMode.CONTAINS, minOccurrences = 1, maxOccurrences = 1)
val email: String?

@field:NotStrOcc(value = "admin", mode = StrOccMode.CONTAINS, ignoreCase = true)
val username: String?
```

Available modes: `EQUALS`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`.

---

## Type-Level Validation

Constraints can be applied directly to **type arguments** within generic types by targeting `AnnotationTarget.TYPE`.
This allows you to validate every element of a collection, every value in a map, or any other parameterized type —
without writing a custom collection-level validator.

### How It Works

When a constraint annotation declares `AnnotationTarget.TYPE` in its `@Target`, the framework inspects the generic
type tree of each field at startup. Any constraint found on a type argument is compiled into the field's
`PropertySpec` and applied to each element during runtime validation, with the element's index automatically
appended to the field path (e.g., `emails[2]`).

### Basic Example — Validating Every Element

```kotlin
// Every string in the list must be a valid email
val emails: List<@Email String>?

// Every integer in the list must be ≥ 0
val scores: List<@NumberMin(0) Int>?

// Every tag must be between 2 and 30 characters
val tags: Set<@Size(min = 2, max = 30) String>?
```

### Map Validation — Keys and Values

Constraints can target map keys, map values, or both:

```kotlin
// Every map value must be a valid URL
val links: Map<String, @Url String>?

// Every map value must be positive
val inventory: Map<String, @NumberMin(1) Int>?
```

### Nested Generics

Type-level annotations compose naturally with nested collections:

```kotlin
// Each inner list must have at most 5 elements, and each element must be a valid email
val emailGroups: List<@Size(max = 5) List<@Email String>>?
```

### Combined with Field-Level Constraints

Type-level annotations work seamlessly alongside field-level annotations:

```kotlin
// The list itself must have 1–10 elements, and every element must be a valid email
@field:Size(min = 1, max = 10)
val emails: List<@Email String>?

// The list must be non-null, each phone must be valid, grouped by context
@field:Required(groups = [OnCreate::class])
val phones: List<@Phone(allowedCountries = ["US", "TN"]) String>?
```

### Making a Constraint Support Type-Level Usage

Add `AnnotationTarget.TYPE` to the `@Target` declaration of any constraint annotation:

```kotlin
@Constraint(metadata = EmailConstraint::class, validatedBy = [EmailValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE             // ← enables use on generic type arguments
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Email(
    val message: String = "",
    val groups: Array<KClass<*>> = [OnDefault::class]
)
```

All built-in constraints already include `AnnotationTarget.TYPE`, so they are ready to use on type arguments
out of the box.

### Error Path for Type-Level Violations

When a type-level constraint is violated, the error path includes the index of the offending element:

| Field declaration                       | Violating element            | Error path        |
|-----------------------------------------|------------------------------|-------------------|
| `val emails: List<@Email String>`       | Third element                | `emails[2]`       |
| `val groups: List<List<@Email String>>` | Outer index 1, inner index 0 | `groups[1][0]`    |
| `val links: Map<String, @Url String>`   | Value at key `"homepage"`    | `links[homepage]` |

---

## Error Handling

### Error Structure

Validation errors are represented as `ConstraintError<CodeT>`, where `CodeT` is an enum implementing
`ConstraintErrorDefinition`:

```kotlin
data class ConstraintError<CodeT>(
    val path: String?,            // Field path, e.g. "user.address[0].city"
    val code: CodeT?,             // Typed error code, e.g. ConstraintErrorCode.VALUE_MISSING
    var message: String?,         // Optional human-readable message
    val location: ErrorLocation?, // BODY, QUERY, HEADER, PATH, or BUSINESS
    val metadata: Any?            // Optional additional context
) where CodeT : Enum<CodeT>, CodeT : ConstraintErrorDefinition
```

### Error Locations

| Location   | Description                                     |
|------------|-------------------------------------------------|
| `BODY`     | Request body field error                        |
| `QUERY`    | Query parameter error                           |
| `HEADER`   | HTTP header error                               |
| `PATH`     | Path variable error                             |
| `BUSINESS` | Business logic error (manual validation)        |

### Field Path Syntax

| Path                 | Meaning                          |
|----------------------|----------------------------------|
| `field`              | Root-level property              |
| `field.nested`       | Nested property                  |
| `field[0]`           | Element at index 0               |
| `field[0].name`      | Property inside an array element |
| `field[0][1].name`   | Property inside a nested array   |
| `[0].name`           | Root-level array element         |

### Built-in Error Codes

All built-in error codes live in the `ConstraintErrorCode` enum, which implements `ConstraintErrorDefinition`:

```
VALUE_MISSING, VALUE_EMPTY, VALUE_DUPLICATE, VALUE_NOT_ALLOWED,
VALUE_NOT_MATCHING_REFERENCE, VALUE_COLLIDING_WITH_REFERENCE,
VALUE_COMPARISON_MISMATCH,
OBJECT_TOO_SMALL, OBJECT_TOO_LARGE,
COLLECTION_TOO_SMALL, COLLECTION_TOO_LARGE,
GEO_LATITUDE_INVALID, GEO_LONGITUDE_INVALID,
DAY_OF_WEEK_NOT_ALLOWED,
INSTANT_NOT_IN_PAST, INSTANT_NOT_IN_FUTURE, INSTANT_TOO_EARLY, INSTANT_TOO_LATE,
COUNTRY_CODE_INVALID, CURRENCY_CODE_INVALID, LANGUAGE_CODE_INVALID,
NUMBER_FORMAT_INVALID, NUMBER_NOT_MULTIPLE, NUMBER_TOO_LARGE, NUMBER_TOO_SMALL,
IBAN_COUNTRY_CODE_INVALID, IBAN_LENGTH_INVALID, IBAN_CHECKSUM_INVALID,
CREDIT_CARD_FORMAT_INVALID, CREDIT_CARD_CHECKSUM_INVALID,
TEXT_TOO_SHORT, TEXT_TOO_LONG, TEXT_PATTERN_MISMATCH,
HTML_FORMAT_INVALID, HTML_TAG_NOT_ALLOWED, HTML_SANITIZATION_MISMATCH,
HTML_ATTRIBUTE_NOT_ALLOWED, HTML_PROTOCOL_NOT_ALLOWED,
HEX_COLOR_INVALID,
URL_FORMAT_INVALID, URL_TOO_LONG, URL_HOST_MISSING, URL_PORT_NOT_ALLOWED,
URL_QUERY_NOT_ALLOWED, URL_PROTOCOL_NOT_ALLOWED, URL_EXTENSION_NOT_ALLOWED,
EMAIL_INVALID_FORMAT,
PHONE_INVALID_FORMAT, PHONE_TYPE_RESTRICTED, PHONE_COUNTRY_RESTRICTED,
PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, PASSWORD_UPPERCASE_MISSING,
PASSWORD_LOWERCASE_MISSING, PASSWORD_DIGIT_MISSING,
PASSWORD_SPECIAL_CHARACTER_MISSING, PASSWORD_TOO_WEAK
```

### Exception Handling

`InvalidRequestException` is thrown when request validation fails. It carries a `code` (implementing
`RequestErrorDefinition`) and the list of `ConstraintError` instances. Handle it in a `@RestControllerAdvice`:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException::class)
    fun handleValidation(ex: InvalidRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = 400,
                message = "Validation failed",
                errors = ex.errors
            )
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<ConstraintError<*>>
)
```

---

## Extending the Framework

### Creating Custom Error Codes

Define project-specific error codes by implementing `ConstraintErrorDefinition` on any enum:

```kotlin
enum class OrderErrorCode(override val message: String) : ConstraintErrorDefinition {
    ORDER_EMPTY("Order must contain at least one item."),
    INVALID_TOTAL("Order total cannot be negative."),
    ITEM_OUT_OF_STOCK("One or more items are currently out of stock.")
}
```

These codes can be used anywhere a `ConstraintError` is constructed or thrown. The `InvalidRequestException.code`
parameter similarly accepts any enum implementing `RequestErrorDefinition`:

```kotlin
enum class AppErrorCode(override val message: String) : RequestErrorDefinition {
    BUSINESS_RULE_VIOLATION("A business rule was violated."),
    CONFLICT("The request conflicts with the current state.")
}

throw InvalidRequestException(code = AppErrorCode.BUSINESS_RULE_VIOLATION, errors = errors)
```

---

### Creating Custom Constraints

#### Step 1 — Define the Annotation

```kotlin
@Constraint(metadata = SlugConstraint::class, validatedBy = [SlugValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slug(
    val maxLength: Int = 100,
    val message: String = "",
    val groups: Array<KClass<*>> = [OnDefault::class]
)
```

#### Step 2 — Create the Metadata Class

Property names must match the annotation properties exactly. The framework uses `ConstraintConverter` to
automatically map annotation values to the metadata constructor.

```kotlin
data class SlugConstraint(
    val maxLength: Int,
    override val message: String,
    override val groups: Set<KClass<*>>
) : ConstraintMetadata()
```

#### Step 3 — Implement the Validator

```kotlin
object SlugValidator : ConstraintValidator<CharSequence, SlugConstraint>() {

    private val SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*\$")

    override fun validate(
        value: CharSequence?,
        constraint: SlugConstraint,
        context: ValidationContext
    ): ConstraintError<*>? {
        value ?: return null

        if (value.length > constraint.maxLength) {
            return ConstraintError(
                code = ConstraintErrorCode.TEXT_TOO_LONG,
                message = "Slug must be at most ${constraint.maxLength} characters"
            )
        }

        if (!SLUG_PATTERN.matcher(value).matches()) {
            return ConstraintError(
                code = ConstraintErrorCode.TEXT_PATTERN_MISMATCH,
                message = "Must be a valid URL slug (lowercase letters and hyphens only)"
            )
        }

        return null
    }
}
```

#### Spring Bean Validators

Validators can be Spring beans to access services or repositories:

```kotlin
@Component
class UniqueEmailValidator(
    private val userRepository: UserRepository
) : ConstraintValidator<CharSequence, UniqueEmailConstraint>() {

    override fun validate(
        value: CharSequence?,
        constraint: UniqueEmailConstraint,
        context: ValidationContext
    ): ConstraintError<*>? {
        value ?: return null
        if (userRepository.existsByEmail(value.toString())) {
            return ConstraintError(
                code = CustomErrorCode.EMAIL_TAKEN,
                message = "This email address is already in use"
            )
        }
        return null
    }
}
```

> **Key rules for custom constraints:**
> - Constraints are auto-discovered at startup — no manual registration is required
> - Validators must be **stateless**, as they are shared across all requests
> - Return `null` from `validate()` for `null` input values; null handling is the caller's responsibility via `@Required`
> - The `ConstraintError` returned from `validate()` only needs `code` — `path` and `location` are set automatically by the framework; `message` is optional
> - Metadata constructor parameter names must match annotation property names exactly
> - Add `AnnotationTarget.TYPE` to `@Target` to allow the constraint on generic type arguments (e.g., `List<@Slug String>`)

---

## Performance Considerations

### Precomputed Schemas

- **One-time analysis** — Field scanning and constraint resolution happen only once at startup
- **Cached schemas** — Validation schemas are built once and reused for every subsequent request
- **No reflection at runtime** — Field access uses precomputed `FieldAccessor` instances

### Fast Field Access — Strategy Ladder

`AccessorFactory` selects the fastest available strategy for each field:

| Priority | Strategy       | Mechanism                                         |
|----------|----------------|---------------------------------------------------|
| 1        | Map lookup     | For header / query / param maps                   |
| 2        | Public getter  | Standard `getX()` / `isX()` methods               |
| 3        | `VarHandle`    | Direct memory access (JVM 9+, near-native)        |
| 4        | `MethodHandle` | Fast reflection alternative via `unreflectGetter` |
| 5        | Reflection     | `Field.get()` — last resort                       |

### Validation Optimizations

- **Fail-fast mode** — `singleErrorPerField = true` stops at the first error per field
- **Constraint ordering** — `@Required` constraints are always evaluated first
- **Zero-copy collections** — `CollectionUtils.normalizeList` avoids element copies when possible
- **Error deduplication** — Removes duplicate errors by `(path, code, location)` tuple
- **Group filtering** — Efficient set-intersection check skips non-matching constraints at zero allocation cost

---

## Why Use This Framework?

### Comparison with Standard Bean Validation

| Feature                   | This Framework                                                  | Bean Validation (JSR-380)                       |
|---------------------------|-----------------------------------------------------------------|-------------------------------------------------|
| **Runtime reflection**    | None — precomputed accessors                                    | Every validation call                           |
| **Schema generation**     | Once at startup, cached                                         | On each validation                              |
| **Error structure**       | API-ready `ConstraintError<CodeT>` with paths, codes, locations | `ConstraintViolation` — requires manual mapping |
| **Error codes**           | Typed enums implementing `ConstraintErrorDefinition`            | String-based message keys                       |
| **Type-level validation** | Native — annotate generic type arguments directly               | Not supported                                   |
| **Dynamic validation**    | First-class `validate<T>()` API                                 | Difficult to implement                          |
| **Error messages**        | Optional per-constraint `message` property                      | Requires `MessageSource` configuration          |
| **Validation groups**     | First-class with `OnCreate` / `OnUpdate` / custom               | Available but verbose                           |
| **Manual validation**     | Fluent error-collector API                                      | Limited programmatic API                        |
| **Cross-field rules**     | `@EqualTo`, `@GreaterThan`, conditional `@Required`             | Requires class-level validator                  |
| **Config validation**     | `@ValidateConfig` + `ConfigValidationPostProcessor`             | `@Validated` on `@ConfigurationProperties`      |
| **Custom error codes**    | Implement `ConstraintErrorDefinition` on any enum               | Not natively supported                          |
| **WebMVC integration**    | Native via `ValidatingServletInvocableHandlerMethod`            | Via `MethodValidationInterceptor` (AOP proxy)   |
| **Collection validation** | `@Distinct`, `@ArraySize`, element-level type annotations       | Basic `@Size` / `@NotEmpty`                     |
| **Custom constraints**    | Annotation + metadata class + validator                         | Annotation + validator + message key            |

### Ideal For

- **API backends** — structured, typed error responses with codes, paths, and locations
- **High-throughput services** — zero-reflection runtime with startup-cached schemas
- **Complex domain models** — cross-field rules, conditional requirements, deeply nested objects
- **WebMVC microservices** — native servlet integration without AOP proxy overhead
- **Dynamic data processing** — runtime schema generation for arbitrary or user-defined payloads

---

## License

MIT License

Copyright (c) 2025 Ghaylan Saada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.