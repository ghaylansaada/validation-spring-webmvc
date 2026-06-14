package io.ghaylan.validation.integration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Declares that a Spring [ConfigurationProperties] class is targeted for advanced, schema-driven validation
 * during the application context bootstrapping phase.
 *
 * While [ValidateRequest] handles the HTTP transport layer, this annotation targets the configuration management
 * layer. It signals the framework's schema builder to introspect strongly-typed configuration beans—such as database pools,
 * security rules, or JWT signature profiles—ensuring that the application fails fast during startup if externalized
 * configurations (from `application.yml`, environment variables, or cloud config servers) violate domain constraints.
 *
 * ---
 * * ### Architectural Role
 * - **Fail-Fast Configuration Bootstrapping:** Works alongside Spring's lifecycle to intercept initialized `@ConfigurationProperties`
 * beans, verifying their structural correctness before the application begins accepting traffic.
 * - **Complex Property Graph Parsing:** Enables deep structural verification of nested properties, structural maps,
 * and collections inside configuration objects (e.g., verifying lists of allowed origins or nested credential shapes).
 * - **Bypasses Standard JSR-380 Overheads:** Provides highly optimized, pre-compiled schema validation paths to replace or
 * supplement standard Spring Boot `@Validated` processing routines for configuration properties.
 *
 * ---
 *
 * ### Example Usage
 *
 * ```kotlin
 * @ValidateConfig
 * @ConfigurationProperties(prefix = "app")
 * data class AppProperties()
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateConfig