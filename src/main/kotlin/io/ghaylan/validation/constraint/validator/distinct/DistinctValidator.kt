package io.ghaylan.validation.constraint.validator.distinct

import io.ghaylan.validation.constraint.ConstraintValidator
import io.ghaylan.validation.constraint.annotation.Distinct.DistinctMode
import io.ghaylan.validation.engine.ValidationContext
import io.ghaylan.validation.model.ConstraintError
import io.ghaylan.validation.model.ConstraintErrorCode

object DistinctValidator : ConstraintValidator<Any, DistinctConstraint>() {

    override fun validate(
        value: Any?,
        constraint: DistinctConstraint,
        context: ValidationContext
    ): ConstraintError<*>? {
        value ?: return null
        if (context.type?.isArray == true) return null

        val arrayValue = context.array?.value?.takeUnless { it.isEmpty() } ?: return null

        // Strip the index token (e.g., "emails[0]" -> "emails") to create a shared
        // identifier base for all sibling elements in this specific list.
        val collectionKey = context.fieldPath.substringBeforeLast('[')

        return when {
            // 1. Primitive/Scalar lists (Strings, Numbers, etc.)
            context.array.type.isArrayOfScalars -> {
                validateScalarElement(value, arrayValue, collectionKey, context)
            }

            // 2. Key-value Map collections
            context.array.type.isArrayOfMaps && constraint.by.isNotEmpty() -> {
                val extractors = constraint.by.map { key -> key to { item: Any? -> (item as? Map<*, *>?)?.get(key) } }
                executeElementStrategy(value, arrayValue, constraint.mode, extractors, collectionKey, context)
            }

            // 3. Strongly-typed class objects using schema metadata accessors
            context.array.type.isArrayOfObjects -> {
                val fields = constraint.by.ifEmpty { setOf(context.fieldName) }
                val extractors = fields.mapNotNull { field ->
                    val accessor = context.array.schema[field]?.accessor ?: return@mapNotNull null
                    field to { item: Any? -> accessor.getFromAny(item, strict = false) }
                }
                executeElementStrategy(value, arrayValue, constraint.mode, extractors, collectionKey, context)
            }

            else -> null
        }
    }

    /**
     * Mode: Scalar (Element level)
     */
    private fun validateScalarElement(
        currentItem: Any,
        array: List<Any?>,
        collectionKey: String,
        context: ValidationContext
    ): ConstraintError<*>? {
        val cacheKey = "$collectionKey.scalar_duplicates"
        val duplicates = context.getOrComputeAttribute(cacheKey) {
            findDuplicateScalars(array)
        }

        if (currentItem in duplicates) {
            return ConstraintError(
                code = ConstraintErrorCode.VALUE_DUPLICATE,
                message = "The value '$currentItem' is a duplicate. Values in this list must be unique."
            )
        }
        return null
    }

    private fun executeElementStrategy(
        currentItem: Any,
        array: List<Any?>,
        mode: DistinctMode,
        extractors: List<Pair<String, (Any?) -> Any?>>,
        collectionKey: String,
        context: ValidationContext
    ): ConstraintError<*>? {
        return if (mode == DistinctMode.PER_FIELD) {
            validateElementPerField(currentItem, array, extractors, collectionKey, context)
        } else {
            validateElementCombination(currentItem, array, extractors, collectionKey, context)
        }
    }

    /**
     * Mode: PER_FIELD (Element level)
     */
    @Suppress("UNCHECKED_CAST")
    private fun validateElementPerField(
        currentItem: Any,
        array: List<Any?>,
        extractors: List<Pair<String, (Any?) -> Any?>>,
        collectionKey: String,
        context: ValidationContext
    ): ConstraintError<*>? {
        val cacheKey = "$collectionKey.field_duplicates"
        val fieldMap = context.getOrComputeAttribute(cacheKey) {
            extractors.associate { (field, extractor) ->
                field to findDuplicateScalars(array.map(extractor))
            }
        }

        for ((field, extractor) in extractors) {
            val currentValue = extractor(currentItem)
            val duplicateValues = fieldMap[field] ?: emptySet()

            if (currentValue in duplicateValues) {
                return ConstraintError(
                    code = ConstraintErrorCode.VALUE_DUPLICATE,
                    message = "The field '$field' has a duplicate value '$currentValue'. It must be unique across all items.",
                    metadata = buildMap {
                        put("by", listOf(field))
                        put("mode", DistinctMode.PER_FIELD)
                        put("rejectedValue", currentValue)
                    }
                )
            }
        }
        return null
    }

    /**
     * Mode: COMBINATION (Element level)
     */
    private fun validateElementCombination(
        currentItem: Any,
        array: List<Any?>,
        extractors: List<Pair<String, (Any?) -> Any?>>,
        collectionKey: String,
        context: ValidationContext
    ): ConstraintError<*>? {
        val cacheKey = "$collectionKey.combo_duplicates"
        val duplicateCombos = context.getOrComputeAttribute(cacheKey) {
            val comboList = array.map { item -> extractors.map { (_, extractor) -> extractor(item) } }
            findDuplicateScalars(comboList)
        }

        val currentCombo = extractors.map { (_, extractor) -> extractor(currentItem) }
        if (currentCombo in duplicateCombos) {
            val fieldsList = extractors.map { it.first }
            return ConstraintError(
                code = ConstraintErrorCode.VALUE_DUPLICATE,
                message = "The composite keys for fields $fieldsList contain duplicate combinations.",
                metadata = buildMap {
                    put("mode", DistinctMode.COMBINATION)
                    put("by", fieldsList)
                    put("rejectedCombination", currentCombo)
                }
            )
        }
        return null
    }

    private fun findDuplicateScalars(list: List<Any?>): Set<Any?> {
        val seen = HashSet<Any?>(list.size)
        val duplicates = HashSet<Any?>()
        for (item in list) {
            if (!seen.add(item)) {
                duplicates.add(item)
            }
        }
        return duplicates
    }
}