package io.ghaylan.validation.utils

import java.lang.reflect.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * A utility object for extracting structured type metadata from Java/Kotlin reflection elements.
 *
 * This extractor is capable of analyzing raw classes, generic types, Kotlin reflection types,
 * fields, method return types, parameters, and more. It returns a normalized [TypeInfo] structure
 * that includes the raw root abstraction, full resolved type, a [TypeKind] classification, and
 * nested type details (e.g., generics).
 *
 * This utility supports:
 * - Java and Kotlin interop
 * - Generic types and wildcards
 * - Primitive and boxed types
 * - Arrays and collections
 * - Enums and data classes
 * - Cyclic type graphs (prevents infinite recursion)
 */
object ReflectionUtils {
	
	private val wildcardType = TypeInfo(Any::class, Any::class, TypeKind.ANY)
	
    private val primitiveArrayTypes = setOf(
	    ByteArray::class,
	    ShortArray::class,
	    IntArray::class,
	    LongArray::class,
        FloatArray::class,
        DoubleArray::class,
        BooleanArray::class,
        CharArray::class)
	
	private val primitiveBoxedMap = mapOf(
		Byte::class to java.lang.Byte::class,
		Short::class to java.lang.Short::class,
		Int::class to Integer::class,
		Long::class to java.lang.Long::class,
		Float::class to java.lang.Float::class,
		Double::class to java.lang.Double::class,
		Boolean::class to java.lang.Boolean::class,
		Char::class to Character::class)


    /**
     * Builds [TypeInfo] from a raw Java [Type] (e.g., field or method return type).
     *
     * @param type The Java type to analyze.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromType(type: Type): TypeInfo = buildTypeInfo(type)

    /**
     * Builds [TypeInfo] from a Kotlin [KType] (e.g., obtained via a property return type).
     *
     * @param kType The Kotlin type to analyze.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromKType(kType: KType): TypeInfo = buildTypeInfo(kType.javaType)

    /**
     * Builds [TypeInfo] from a raw Java [Class], using its generic superclass if available.
     *
     * @param clazz The class to analyze.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromClass(clazz: Class<*>): TypeInfo = buildTypeInfo(clazz)

    /**
     * Builds [TypeInfo] from a Java [Field].
     *
     * @param field The field whose generic type will be analyzed.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromField(field: Field): TypeInfo = buildTypeInfo(field.genericType)

    /**
     * Builds [TypeInfo] from a Java method [Parameter].
     *
     * @param parameter The method parameter to analyze.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromParameter(parameter: Parameter): TypeInfo = buildTypeInfo(parameter.parameterizedType)

    /**
     * Builds [TypeInfo] from the return type of Java [Method].
     *
     * @param method The method whose return type will be analyzed.
     * @return A structured [TypeInfo] representation.
     */
    fun infoFromMethodReturn(method: Method): TypeInfo = buildTypeInfo(method.genericReturnType)

    /**
     * Checks if the given type is assignable from a [Map]-like structure.
     *
     * @param type The Java class to check.
     * @return `true` if the type is a map or subclass of a map.
     */
    fun isMapLike(type: Class<*>): Boolean = Map::class.java.isAssignableFrom(type)

    /**
     * Determines if a class is a structured data object (DTO-like).
     *
     * Rejects primitives, collections, enums, interfaces, synthetic classes, etc.
     * Accepts Kotlin data classes, Java records, and concrete classes with real instance fields.
     *
     * @param type The class to inspect.
     * @return `true` if it is likely to represent a data object.
     */
    fun isObjectLike(type: Class<*>): Boolean {
		
        // Early rejection for non-object types
        if (
            type == Unit::class.java ||
            type == Any::class.java ||
            type == Void::class.java ||
            type.isEnum ||
            type.isArray ||
            type.isPrimitive ||
            type.isInterface ||
            type.isSynthetic ||
            Modifier.isAbstract(type.modifiers) ||
            Map::class.java.isAssignableFrom(type) ||
            Collection::class.java.isAssignableFrom(type) ||
            CharSequence::class.java.isAssignableFrom(type) ||
            Number::class.java.isAssignableFrom(type) ||
            Temporal::class.java.isAssignableFrom(type)
        ) return false
	    
        // Accept Java records
        if (runCatching { type.isRecord }.getOrDefault(false)) return true

        // Accept Kotlin data classes (safely)
        if (runCatching { type.kotlin.isData }.getOrDefault(false)) return true

        // Reject non-concrete or anonymous inner classes
        if (type.isAnonymousClass || type.isLocalClass) return false

        // Check for meaningful instance fields
        var current: Class<*>? = type

        // Accept only classes that have at least one real (non-static, non-synthetic) field
	    while (current != null && current != Any::class.java) {
            val hasRealFields = current.declaredFields.any {
                !it.isSynthetic && !Modifier.isStatic(it.modifiers)
            }

            if (hasRealFields) return true

            current = current.superclass
        }

        // Fallback: no meaningful fields found, don't assume it's an object
        return false
    }

    /**
     * Checks if a class is an array or a [Collection].
     *
     * @param type The class to inspect.
     * @return `true` if the type is array-like.
     */
    fun isCollectionLike(type: Class<*>): Boolean {
        return type.isArray || Collection::class.java.isAssignableFrom(type)
    }
	
	/**
	 * Returns `true` if [value] is any array or collection type (including primitive arrays).
	 */
	fun isCollection(value: Any): Boolean {
        return isCollectionLike(value.javaClass)
    }
	
	fun isTypeInfoCollectionLike(type: TypeInfo): Boolean {
		return Collection::class.java.isAssignableFrom(type.concreteType.java)
	}
	
	fun isTypeInfoMapLike(type: TypeInfo): Boolean {
		return Map::class.java.isAssignableFrom(type.concreteType.java)
	}
	
	fun isWildcard(type: TypeInfo): Boolean {
		return type.concreteType == Any::class || type == wildcardType
	}
	
	fun primitiveOrBoxedMatch(a: KClass<*>, b: KClass<*>): Boolean {
		return a == b || primitiveBoxedMap[a] == b || primitiveBoxedMap[b] == a
	}
	
	fun isNumericType(type: KClass<*>): Boolean {
		return Number::class.java.isAssignableFrom(type.java)
	}
	
	fun isComparableNumeric(type: KClass<*>): Boolean {
		return isNumericType(type) && Comparable::class.java.isAssignableFrom(type.java)
	}
	
	/**
	 * Returns `true` if [value] is a scalar (string, char, number, temporal, boolean, enum, or Date).
	 */
	fun isScalar(value: Any): Boolean {
        return !isCollection(value) && !isObjectLike(value.javaClass)
    }
	
    /**
     * Retrieves all declared fields in the given class and its superclasses.
     *
     * This includes private and protected fields, which are made accessible.
     *
     * @param clazz The class to inspect.
     * @return A list of all non-static, declared fields across the inheritance chain.
     */
    fun getFields(clazz: Class<*>): List<Field> {
        return generateSequence(clazz) { it.superclass }
            .flatMap { it.declaredFields.asSequence().onEach { field -> field.trySetAccessible() } }
            .toList()
    }

    /**
     * Recursively builds [TypeInfo] from the given Java [Type], using a visited set
     * to avoid infinite loops in recursive/cyclic generic type hierarchies.
     *
     * @param type The type to inspect.
     * @param visited A mutable set to track seen types (for cycle prevention).
     * @return Fully resolved [TypeInfo] tree.
     */
    private fun buildTypeInfo(
	    type: Type,
	    visited: MutableSet<Type> = mutableSetOf()
    ): TypeInfo {
        if (!visited.add(type)) {
            // Type already seen: prevent infinite loop by returning fallback
            return TypeInfo(
                rawRootType = Any::class,
                concreteType = Any::class,
                kind = TypeKind.ANY,
                typeArguments = emptyList())
        }
	    
	    return when (type) {
            // 🔷 Case 1: If it's a parameterized type like List<String> or Map<String, Int>
            is ParameterizedType -> {

                // Extract raw class (e.g., List from List<String>), fallback to Any if not Class
                val raw = (type.rawType as? Class<*>)?.kotlin ?: Any::class

                // Recursively extract type info for each actual type argument (e.g., String in List<String>)
                val nestedTypes = type.actualTypeArguments.map { buildTypeInfo(it, visited) }

                // Build TypeInfo with full type, root abstraction (like Collection for List), and nested generic types
                TypeInfo(
                    rawRootType = extractRootAbstraction(raw),
                    concreteType = raw,
                    kind = determineTypeKind(raw, nestedTypes),
                    typeArguments = nestedTypes)
            }

            // 🔷 Case 2: If it's a generic array type (e.g., T[] where T is a generic type)
            is GenericArrayType -> {

                // Recursively build the type info of the component type
                val componentType = buildTypeInfo(type.genericComponentType, visited)

                // Build TypeInfo for the array, using helper to find a primitive version if needed
                TypeInfo(
                    rawRootType = Array::class,
                    concreteType = getPrimitiveArrayClass(componentType.concreteType),
                    kind = determineTypeKind(Array::class, listOf(componentType)),
                    typeArguments = listOf(componentType))
            }

            // 🔷 Case 3: If it's a regular class type (could be a primitive, array, or object)
            is Class<*> -> {

                val kClass = type.kotlin

                // ❗ Short-circuit enums
                if (type.isEnum) {
                    return TypeInfo(
                        rawRootType = kClass,
                        concreteType = kClass,
                        kind = TypeKind.ENUM,
                        typeArguments = emptyList())
                }

                when {

                    // 🔹 Sub-case: Raw Java array type (like IntArray, String[])
                    type.isArray -> {
						
                        // Recursively extract type info of the component
                        val componentType = buildTypeInfo(type.componentType, visited)

                        // Build array TypeInfo including primitive conversion if applicable
                        TypeInfo(
                            rawRootType = Array::class,
                            concreteType = getPrimitiveArrayClass(componentType.concreteType),
                            kind = determineTypeKind(Array::class, listOf(componentType)),
                            typeArguments = listOf(componentType))
                    }

                    // 🔹 Sub-case: It's a standard object (non-primitive, non-array)
                    isObjectLike(type) -> {

                        TypeInfo(
                            rawRootType = extractRootAbstraction(type.kotlin),
                            concreteType = type.kotlin,
                            kind = determineTypeKind(type.kotlin, emptyList()),
                            typeArguments = emptyList())
                    }

                    // 🔹 Sub-case: Try to fallback to its generic superclass if present (e.g., CustomList : ArrayList<String>)
                    else -> {
                        val genericSuper = type.genericSuperclass

                        // 🔸 Try to extract generic information from the superclass if it is a parameterized type
                        if (genericSuper is ParameterizedType) {
                            return buildTypeInfo(genericSuper, visited)
                        }

                        // 🔸 Otherwise, treat it as a regular non-generic class
                        val kClass = type.kotlin
                        TypeInfo(
                            rawRootType = extractRootAbstraction(kClass),
                            concreteType = kClass,
                            kind = determineTypeKind(kClass, emptyList()),
                            typeArguments = emptyList()
                        )
                    }
                }
            }

            // 🔷 Case 4: Handle wildcards (e.g., `? extends Number`), fallback to upper bound
            is WildcardType -> {
                buildTypeInfo(type.upperBounds.firstOrNull() ?: Any::class.java, visited)
            }

            // 🔷 Case 5: Handle type variables (e.g., T in <T : String>), fallback to its bound
            is TypeVariable<*> -> {
                buildTypeInfo(type.bounds.firstOrNull() ?: Any::class.java, visited)
            }

            // 🔷 Case 6: Unknown type, fallback to "Any"
            else -> {
                TypeInfo(Any::class, Any::class, TypeKind.ANY)
            }
        }
    }

    /**
     * Returns the appropriate Kotlin primitive array class for the given scalar type.
     *
     * For example, `Int::class` → `IntArray::class`, `String::class` → `Array<Any>::class`
     *
     * @param component The element type.
     * @return A Kotlin array type.
     */
    private fun getPrimitiveArrayClass(component: KClass<*>?): KClass<*> {
        return when (component) {
            Float::class -> FloatArray::class
            Double::class -> DoubleArray::class
            Int::class -> IntArray::class
            Long::class -> LongArray::class
            Short::class -> ShortArray::class
            Boolean::class -> BooleanArray::class
            Char::class -> CharArray::class
            Byte::class -> ByteArray::class
            else -> Array<Any>::class
        }
    }

    /**
     * Extracts the root abstraction of the type: Array, Collection, Map, or itself.
     *
     * Used to simplify `TypeInfo` representation for common wrappers.
     *
     * @param type The raw Kotlin type.
     * @return The root type abstraction.
     */
    private fun extractRootAbstraction(type: KClass<*>): KClass<*> {
        return when {
            type in primitiveArrayTypes -> Array::class
            type.java.isArray -> Array::class
            Collection::class.java.isAssignableFrom(type.java) -> Collection::class
            Map::class.java.isAssignableFrom(type.java) -> Map::class
            else -> type
        }
    }

    /**
     * Infers the semantic [TypeKind] of a given Kotlin type and its nested type arguments.
     *
     * This is used to normalize primitive vs array vs object vs time categories.
     *
     * @param type The target Kotlin class.
     * @param nested The list of nested [TypeInfo] if generics exist.
     * @return A corresponding [TypeKind] value.
     */
    private fun determineTypeKind(
        type: KClass<*>,
        nested: List<TypeInfo>
    ): TypeKind {
        val nestedKind = nested.firstOrNull()?.kind

        return when {
            type == Nothing::class || type == Void::class -> {
                TypeKind.NOTHING
            }
            type == BooleanArray::class || type == Boolean::class -> {
                if (type.java.isArray) TypeKind.BOOLEAN_ARRAY else TypeKind.BOOLEAN
            }
            type == CharArray::class || type == Char::class -> {
                if (type.java.isArray) TypeKind.CHAR_ARRAY else TypeKind.CHAR
            }
            type == ByteArray::class || type == Byte::class -> {
                if (type.java.isArray) TypeKind.BYTE_ARRAY else TypeKind.BYTE
            }
            type == String::class -> {
                TypeKind.STRING
            }
            type == LocalDate::class -> {
                TypeKind.DATE
            }
            type in arrayOf(LocalTime::class, OffsetTime::class) -> {
                TypeKind.TIME
            }
            type in arrayOf(Date::class, LocalDateTime::class, ZonedDateTime::class, Instant::class) -> {
                TypeKind.DATETIME
            }
            type in setOf(Int::class, Long::class, Short::class, BigInteger::class) -> {
                TypeKind.NUMERIC
            }
            type in setOf(Float::class, Double::class, BigDecimal::class) -> {
                TypeKind.DECIMAL
            }
            type in setOf(IntArray::class, LongArray::class, ShortArray::class) -> {
                TypeKind.NUMERIC_ARRAY
            }
            type in setOf(FloatArray::class, DoubleArray::class) -> {
                TypeKind.DECIMAL_ARRAY
            }
            type.java.isEnum -> {
                TypeKind.ENUM
            }
            Map::class.java.isAssignableFrom(type.java) -> {
                TypeKind.MAP
            }
            isCollectionLike(type.java) -> {

                when {
                    nestedKind != null && nestedKind.isArray -> TypeKind.ARRAY_ARRAY
                    nestedKind == TypeKind.BOOLEAN -> TypeKind.BOOLEAN_ARRAY
                    nestedKind == TypeKind.BYTE -> TypeKind.BYTE_ARRAY
                    nestedKind == TypeKind.CHAR -> TypeKind.CHAR_ARRAY
                    nestedKind == TypeKind.ENUM -> TypeKind.ENUM_ARRAY
                    nestedKind == TypeKind.STRING -> TypeKind.STRING_ARRAY
                    nestedKind == TypeKind.NUMERIC -> TypeKind.NUMERIC_ARRAY
                    nestedKind == TypeKind.DECIMAL -> TypeKind.DECIMAL_ARRAY
                    nestedKind == TypeKind.DATE -> TypeKind.DATE_ARRAY
                    nestedKind == TypeKind.TIME -> TypeKind.TIME_ARRAY
                    nestedKind == TypeKind.DATETIME -> TypeKind.DATETIME_ARRAY
                    nestedKind == TypeKind.MAP -> TypeKind.MAP_ARRAY
                    nestedKind == TypeKind.OBJECT -> TypeKind.OBJECT_ARRAY
                    nestedKind == TypeKind.ANY -> TypeKind.ANY_ARRAY
                    else -> TypeKind.OBJECT_ARRAY
                }
            }
            isObjectLike(type.java) -> TypeKind.OBJECT
            else -> TypeKind.ANY
        }
    }


    /**
     * A structured representation of a resolved Java/Kotlin type, including its classification,
     * generic type arguments, and array/object semantics.
     *
     * This abstraction is designed for schema-driven reflection and validation logic.
     */
    data class TypeInfo(

        /**
         * The generalized root type abstraction (e.g., `List` → `Collection`, `IntArray` → `Array`).
         * Useful for grouping similar concrete types under a shared conceptual category.
         */
        val rawRootType: KClass<*>,

        /**
         * The fully resolved Kotlin class of the actual runtime type.
         * For example: `ArrayList::class`, `Int::class`, `User::class`, etc.
         */
        val concreteType: KClass<*>,

        /**
         * A categorized [TypeKind] enum that distinguishes between primitive, object, array, date/time,
         * enum, and collection types in a consistent way across Kotlin/Java interop.
         */
        val kind: TypeKind,

        /**
         * Generic type parameters or component types of this type.
         *
         * This list captures **nested generic types recursively**. For example:
         * - For `List<String>`, this will contain a single `TypeInfo` representing `String`.
         * - For `Map<String, List<Int>>`, this will contain:
         *   - A `TypeInfo` for `String`
         *   - A `TypeInfo` for `List<Int>`, which itself contains a `TypeInfo` for `Int`.
         * - For arrays, it contains the element type recursively as well (e.g., `Array<List<User>>` → List → User).
         *
         * This structure is **recursive**: each `TypeInfo` may itself have `typeArguments`,
         * allowing deep traversal of arbitrarily nested types until a final leaf type is reached
         * (e.g., primitive, object, or enum).
         */
        val typeArguments: List<TypeInfo> = emptyList()
	) {

        /**
         * True if this type represents a plain object or data class (non-primitive, non-collection, non-array).
         */
        val isObject : Boolean get() {
            return kind == TypeKind.OBJECT
        }

        /**
         * True if this type represents an array of objects (e.g., `Array<User>`).
         */
        val isArrayOfObjects : Boolean get() {
            return kind == TypeKind.OBJECT_ARRAY
        }

        /**
         * True if this type represents an array of maps (e.g., `Array<Map<*,*>>`).
         */
        val isArrayOfMaps : Boolean get() {
            return kind == TypeKind.MAP_ARRAY
        }
	    
	    val isArrayOfNonScalar: Boolean get() {
			return kind == TypeKind.MAP_ARRAY || kind == TypeKind.OBJECT_ARRAY || kind == TypeKind.ARRAY_ARRAY
		}

        /**
         * True if this type represents a Map (e.g., `Map<*,*>`).
         */
        val isMap : Boolean get() {
            return kind == TypeKind.MAP
        }

        /**
         * True if this is a simple, scalar type such as `String`, `Boolean`, `Int`, `Double`, enums, or date/time types.
         * Excludes collections and objects.
         */
        val isScalar : Boolean get() {
            return kind.isScalar && !kind.isArray
        }

        /**
         * True if this type is an array of simple scalar types (e.g., `IntArray`, `Array<String>`).
         */
        val isArrayOfScalars : Boolean get() {
            return kind.isArray && kind.isScalar
        }

        /**
         * True if this type is a multi-dimensional array (e.g., `Array<Array<String>>`).
         */
        val isArrayOfArrays : Boolean get() {
            return kind == TypeKind.ARRAY_ARRAY
        }

        /**
         * True if this type is any kind of array — whether of objects or simple types.
         */
        val isArray : Boolean get() {
            return kind.isArray
        }

        /**
         * The core type this structure represents:
         * - For arrays, returns the element type (e.g., `User` in `Array<User>`).
         * - Otherwise, returns the concrete type directly.
         */
        val resolveType get() : KClass<*> {
            return if (isArray) typeArguments.firstOrNull()?.concreteType ?: concreteType else concreteType
        }

        /**
         * If this is an array, returns the class of the element type (e.g., `String::class` in `Array<String>`).
         * Returns `null` if this is not an array.
         */
        val arrayElemType : KClass<*>? get() {
            return if (isArray) typeArguments.firstOrNull()?.concreteType else null
        }
    }


    /**
     * Classification of JVM/Kotlin types used in [TypeInfo] for schema-driven validation.
     */
    enum class TypeKind(val isScalar : Boolean, val isArray : Boolean) {
        BOOLEAN(isScalar = true, isArray = false),
        BYTE(isScalar = true, isArray = false),
        CHAR(isScalar = true, isArray = false),
        ENUM(isScalar = true, isArray = false),
        STRING(isScalar = true, isArray = false),
        NUMERIC(isScalar = true, isArray = false),
        DECIMAL(isScalar = true, isArray = false),
        DATE(isScalar = true, isArray = false),
        TIME(isScalar = true, isArray = false),
        DATETIME(isScalar = true, isArray = false),
        OBJECT(isScalar = false, isArray = false),
        MAP(isScalar = false, isArray = false),
        ANY(isScalar = false, isArray = false),

        BOOLEAN_ARRAY(isScalar = true, isArray = true),
        BYTE_ARRAY(isScalar = true, isArray = true),
        CHAR_ARRAY(isScalar = true, isArray = true),
        ENUM_ARRAY(isScalar = true, isArray = true),
        STRING_ARRAY(isScalar = true, isArray = true),
        NUMERIC_ARRAY(isScalar = true, isArray = true),
        DECIMAL_ARRAY(isScalar = true, isArray = true),
        DATE_ARRAY(isScalar = true, isArray = true),
        TIME_ARRAY(isScalar = true, isArray = true),
        DATETIME_ARRAY(isScalar = true, isArray = true),
        OBJECT_ARRAY(isScalar = false, isArray = true),
        MAP_ARRAY(isScalar = false, isArray = true),
        ANY_ARRAY(isScalar = false, isArray = true),
        ARRAY_ARRAY(isScalar = false, isArray = true),

        NOTHING(isScalar = false, isArray = false)
    }
}