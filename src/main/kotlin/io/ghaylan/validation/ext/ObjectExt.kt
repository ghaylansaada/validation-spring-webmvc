package io.ghaylan.validation.ext

import java.util.*
import java.util.stream.BaseStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Returns `true` if the value is semantically absent: `null`, `Unit`, blank string, empty collection,
 * or a collection/map whose every element is itself deeply null or empty.
 *
 * Cycle detection via [visited] prevents infinite recursion on self-referential structures.
 * Enums and all other leaf types return `false` (they are considered present).
 */
@OptIn(ExperimentalContracts::class)
fun Any?.isDeepNullOrEmpty(
	visited: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())
): Boolean {
	contract {
		returns(false) implies (this@isDeepNullOrEmpty != null)
	}
	
	if (this == null) return true
	if (this in visited) return false
	
	visited.add(this)
	
	try {
		return when (this) {
			is Unit -> true
			is Optional<*> -> !this.isPresent
			is Enum<*> -> false
			is Char -> this.isWhitespace() || this == '\u0000'
			is CharSequence -> this.isBlank()
			
			// Handle Collections and General Iterables safely
			is Collection<*> -> this.isEmpty() || this.all { it.isDeepNullOrEmpty(visited) }
			is Iterable<*> -> !this.iterator().hasNext() || this.all { it.isDeepNullOrEmpty(visited) }
			
			// Arrays
	        is Array<*> -> this.isEmpty() || this.all { it.isDeepNullOrEmpty(visited) }
	        is CharArray -> this.isEmpty()
	        is ByteArray -> this.isEmpty()
	        is ShortArray -> this.isEmpty()
	        is IntArray -> this.isEmpty()
	        is LongArray -> this.isEmpty()
	        is FloatArray -> this.isEmpty()
	        is DoubleArray -> this.isEmpty()
	        is BooleanArray -> this.isEmpty()
	
			// Maps and Tuples
	        is Map<*, *> -> this.isEmpty() || this.values.all { it.isDeepNullOrEmpty(visited) }
	        is Pair<*, *> -> this.first.isDeepNullOrEmpty(visited) && this.second.isDeepNullOrEmpty(visited)
	        is Triple<*, *, *> -> {
	            this.first.isDeepNullOrEmpty(visited) &&
	            this.second.isDeepNullOrEmpty(visited) &&
	            this.third.isDeepNullOrEmpty(visited)
	        }
			
			// Sequences / Streams: Treat as present to avoid accidental infinite loops or consumption
			is Sequence<*>, is BaseStream<*, *> -> false
			
			else -> false
		}
	} finally {
		// Backtrack: remove from visited so sibling nodes can be evaluated correctly
        visited.remove(this)
	}
}