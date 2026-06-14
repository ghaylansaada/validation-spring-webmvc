package io.ghaylan.validation.constraint.validator.string.url

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.annotation.Url
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import java.net.URI
import java.net.URISyntaxException

/**
 * Validates URLs using structural configurations built out via [UrlConstraint] metadata.
 */
object UrlValidator : ConstraintValidator<CharSequence, UrlConstraint>() {

    override fun validate(
        value: CharSequence?,
        constraint: UrlConstraint,
        context: ValidationContext
    ): ConstraintError<*>? {
        if (value.isNullOrBlank()) return null
        val rawUrl = value.toString()

        // 1. URL_TOO_LONG Check
        if (rawUrl.length > constraint.maxLength) {
            return ConstraintError(
                code = ConstraintErrorCode.URL_TOO_LONG,
                metadata = buildMap {
                    put("max_length", constraint.maxLength)
                }
            )
        }

        // 2. URL_FORMAT_INVALID Check
        val uri = try {
            URI(rawUrl)
        } catch (e: URISyntaxException) {
            return ConstraintError(
                code = ConstraintErrorCode.URL_FORMAT_INVALID,
                metadata = buildMap {
                    put("reason", e.reason)
                }
            )
        }

        // 3. URL_HOST_MISSING Check (Enforced if type is WEBSITE)
        if (constraint.type == Url.UrlType.WEBSITE && uri.host.isNullOrBlank()) {
            return ConstraintError(code = ConstraintErrorCode.URL_HOST_MISSING)
        }

        // 4. URL_PROTOCOL_NOT_ALLOWED Check
        val scheme = uri.scheme?.lowercase()
        val allowedProtocols = constraint.allowedProtocols
        if (!allowedProtocols.contains("*") && !allowedProtocols.map { it.lowercase() }.contains(scheme)) {
            return ConstraintError(
                code = ConstraintErrorCode.URL_PROTOCOL_NOT_ALLOWED,
                metadata = buildMap {
                    put("allowed_protocols", allowedProtocols)
                }
            )
        }

        // 5. URL_PORT_NOT_ALLOWED Check
        val portString = uri.port.toString()
        val allowedPorts = constraint.allowedPorts
        if (uri.port != -1 && !allowedPorts.contains("*") && !allowedPorts.contains(portString)) {
            return ConstraintError(
                code = ConstraintErrorCode.URL_PORT_NOT_ALLOWED,
                metadata = buildMap {
                    put("allowed_ports", allowedPorts.toList())
                }
            )
        }

        // 6. URL_QUERY_NOT_ALLOWED Check
        if (!uri.query.isNullOrEmpty()) {
            val allowedParams = constraint.allowedParams
            if (allowedParams.isEmpty() || (allowedParams.size == 1 && allowedParams.contains(""))) {
                return ConstraintError(
                    code = ConstraintErrorCode.URL_QUERY_NOT_ALLOWED,
                    metadata = buildMap {
                        put("actual_query", uri.query)
                    }
                )
            } else if (!allowedParams.contains("*")) {
                val queryKeys = uri.query.split("&").map { it.substringBefore("=") }.toSet()
                val unpermittedKeys = queryKeys.filter { !allowedParams.contains(it) }
                
                if (unpermittedKeys.isNotEmpty()) {
                    return ConstraintError(
                        code = ConstraintErrorCode.URL_QUERY_NOT_ALLOWED,
                        metadata = buildMap {
                            put("allowed_parameters", allowedParams.toList())
                        }
                    )
                }
            }
        }

        // 7. URL_EXTENSION_NOT_ALLOWED Check
        val extension = uri.path.orEmpty().substringAfterLast('.', "").lowercase()
        val hasCustomExtensions = !constraint.allowedExtensions.contains("*")

        if (constraint.type.isMedia || hasCustomExtensions) {
            val finalAllowedExtensions = if (hasCustomExtensions) {
                constraint.allowedExtensions.map { it.lowercase() }.toSet()
            } else {
                constraint.type.extensions.toSet()
            }

            if (extension.isBlank() || !finalAllowedExtensions.contains(extension)) {
                return ConstraintError(
                    code = ConstraintErrorCode.URL_EXTENSION_NOT_ALLOWED,
                    metadata = buildMap {
                        put("allowed_extensions", finalAllowedExtensions.toList())
                    }
                )
            }
        }

        return null
    }
}