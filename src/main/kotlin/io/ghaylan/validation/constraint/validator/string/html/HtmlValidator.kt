package io.ghaylan.validation.constraint.validator.string.html

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist


object HtmlValidator : ConstraintValidator<CharSequence, HtmlConstraint>() {

     override fun validate(
        value: CharSequence?,
        constraint: HtmlConstraint,
        context: ValidationContext
    ): ConstraintError<*>? {
        if (value.isNullOrBlank()) return null

		val rawHtml = value.toString()
		val attrsPerTag = parseAllowedAttributes(constraint.allowedAttrs)
		val protocolsPerTagAttr = parseAllowedProtocols(constraint.allowedProtocols)
		val document = Jsoup.parseBodyFragment(rawHtml)
	     
	     val parser = Parser.htmlParser().setTrackErrors(10)
	     document.parser(parser) // Attach the tracking parser to check errors post-parse
	     
	     // 1. Structural Markup Validity Guard
	    if (parser.errors.isNotEmpty()) {
	        return ConstraintError(
	            code = ConstraintErrorCode.HTML_FORMAT_INVALID,
	            metadata = buildMap {
	                put("syntax_errors", parser.errors.map { it.toString() })
	            }
	        )
	    }

		// 1. Structural Markup Evaluation Guards
		checkTagErrors(document, constraint.allowedTags)?.let { return it }
		checkAttributeErrors(document, attrsPerTag)?.let { return it }
		checkProtocolErrors(document, protocolsPerTagAttr)?.let { return it }

		// 2. Safe-list Modification Parity Check
		val safelist = buildDynamicSafelist(
			allowedTags = constraint.allowedTags,
			allowedAttributes = attrsPerTag,
			allowedProtocols = protocolsPerTagAttr)
		
		val cleanedHtml = Jsoup.clean(rawHtml, safelist)
		if (cleanedHtml != rawHtml) {
			return ConstraintError(code = ConstraintErrorCode.HTML_SANITIZATION_MISMATCH)
		}

		return null
    }
	
	private fun buildDynamicSafelist(
		allowedTags: Set<String>,
		allowedAttributes: Map<String, List<String>>,
		allowedProtocols: Map<String, Map<String, List<String>>>
	): Safelist {
		val safelist = Safelist.none()
		
		allowedTags.forEach { safelist.addTags(it) }
		
		allowedAttributes.forEach { (tag, attrs) ->
			safelist.addAttributes(tag, *attrs.toTypedArray())
		}
		
		allowedProtocols.forEach { (tag, attrProtocols) ->
			attrProtocols.forEach { (attr, protocols) ->
				safelist.addProtocols(tag, attr, *protocols.toTypedArray())
			}
		}
		
		return safelist
	}
	
	private fun parseAllowedAttributes(allowedAttrs: Set<String>): Map<String, List<String>> {
		return allowedAttrs
			.mapNotNull { entry ->
				val parts = entry.split(':', limit = 2)
				if (parts.size == 2) parts[0] to parts[1] else null
			}
			.groupBy({ it.first }, { it.second })
	}
	
	private fun parseAllowedProtocols(
		allowedProtocols: Set<String>
	): Map<String, Map<String, List<String>>> {
		val map = mutableMapOf<String, MutableMap<String, List<String>>>()
		
		allowedProtocols.forEach { entry ->
			val parts = entry.split(':', limit = 3)
			if (parts.size == 3) {
				val (tag, attr, protocolsStr) = parts
				val protocols = protocolsStr.split(',').map { it.lowercase() }
				
				val attrMap = map.getOrPut(tag) { mutableMapOf() }
				attrMap[attr] = protocols
			}
		}
		
		return map
	}
	
	private fun checkTagErrors(
		doc: Document,
		allowedTags: Set<String>
	): ConstraintError<*>? {
		doc.body().allElements.forEach { element ->
			val tag = element.tagName()
			if (tag != "#root" && tag != "body" && !allowedTags.contains(tag)) {
				return ConstraintError(
					code = ConstraintErrorCode.HTML_TAG_NOT_ALLOWED,
					message = "The HTML document contains a prohibited markup tag.",
					metadata = buildMap {
						put("prohibited_tag", tag)
						put("allowed_tags", allowedTags.toList())
					})
			}
		}
		return null
	}
	
	private fun checkAttributeErrors(
		doc: Document,
		allowedAttrs: Map<String, List<String>>
	): ConstraintError<*>? {
		doc.body().allElements.forEach { element ->
			val tag = element.tagName()
			if (tag == "#root" || tag == "body") return@forEach
			
			val allowed = allowedAttrs[tag].orEmpty()
			element.attributes().forEach { attr ->
				if (!allowed.contains(attr.key)) {
					return ConstraintError(
						code = ConstraintErrorCode.HTML_ATTRIBUTE_NOT_ALLOWED,
						message = "The HTML element carries a prohibited attribute.",
						metadata = buildMap {
							put("tag", tag)
							put("prohibited_attribute", attr.key)
							put("allowed_attributes", allowed)
						}
					)
				}
			}
		}
		return null
	}
	
	private fun checkProtocolErrors(
		doc: Document,
		allowedProtocols: Map<String, Map<String, List<String>>>
	): ConstraintError<*>? {
		doc.body().allElements.forEach { element ->
			val tag = element.tagName()
			val attrProtocols = allowedProtocols[tag].orEmpty()
			
			element.attributes().forEach { attr ->
				val allowed = attrProtocols[attr.key].orEmpty()
				if (allowed.isNotEmpty()) {
					val protocol = attr.value.substringBefore(":", "").lowercase()
					if (protocol.isNotEmpty() && !allowed.contains(protocol)) {
						return ConstraintError(
							code = ConstraintErrorCode.HTML_PROTOCOL_NOT_ALLOWED,
							message = "The attribute URI contains a prohibited scheme protocol.",
							metadata = buildMap {
								put("tag", tag)
								put("attribute", attr.key)
								put("prohibited_protocol", protocol)
								put("allowed_protocols", allowed)
							}
						)
					}
				}
			}
		}
		return null
	}
}