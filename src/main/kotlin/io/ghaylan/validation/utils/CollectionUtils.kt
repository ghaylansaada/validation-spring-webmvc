package io.ghaylan.validation.utils

/**
 * High-performance data normalization utility designed to flatten, scrub, and stabilize multi-dimensional
 * array structures and collection aggregates during validation loop traversals.
 *
 * This component protects the validation engine from memory thrashing. When processing heavily nested
 * JSON arrays, it avoids the allocation of short-lived iterator objects or duplicated heap list graphs.
 * Instead, it yields a standardized, read-only `List<Any>` view using specialized allocation strategies.
 *
 * ---
 *
 * ### Allocation Strategy Profile
 * - **Zero-Copy Views:** For incoming arrays or lists that are already fully populated (containing no null elements),
 * this class skips duplicating data segments. It wraps the native structure in an anonymous, read-only [AbstractList] view.
 * - **Conditional Compaction:** Allocations occur only if data cleanup is explicitly required—such as when filtering
 * null elements out of standard structures or copying un-indexed sets into predictable arrays to secure position tracking.
 * - **Boxed Primitive Views:** Primitive arrays (such as [IntArray] or [BooleanArray]) are adapted via Kotlin's primitive
 * `.asList()` extension handlers. This wraps data segments inside a boxed view without triggering element array copy sequences.
 */
object CollectionUtils {
	
	/**
	 * Normalizes incoming data structures into a flat, non-null, index-stable `List<Any>` tracker while
	 * optimizing for zero-allocation bypasses wherever possible.
	 *
	 * Processing vectors adapt to incoming type architectures:
	 * - `null` $\rightarrow$ Evaluates instantly to [emptyList].
	 * - `Array<*>` / `List<*>` $\rightarrow$ Delegates to specialized null-scanning check loops to determine if a zero-copy wrapper can be used.
	 * - `Collection<*>` $\rightarrow$ Compiles to an sequential array list to provide safe positional tracking across sets or queues.
	 * - `Primitive Arrays` $\rightarrow$ Adapted using zero-copy primitive wrapper views.
	 *
	 * @param value The raw, unknown structural dataset or collection extracted from a runtime property field.
	 * @return A non-null, immutable, index-stable list instance representing the scrubbed input dataset.
	 */
	fun normalizeList(value: Any?): List<Any> {
		if (value == null) return emptyList()
		
		return when (value) {
			is Array<*> -> normalizeArray(value)
			is List<*> -> normalizeListValue(value)
			is Collection<*> -> normalizeCollection(value)
			is BooleanArray -> value.asList()
			is ByteArray -> value.asList()
			is ShortArray -> value.asList()
			is IntArray -> value.asList()
			is LongArray -> value.asList()
			is FloatArray -> value.asList()
			is DoubleArray -> value.asList()
			else -> emptyList()
		}
	}
	
	/**
	 * Processes standard reference arrays by evaluating null footprint density.
	 *
	 * @param array The reference array instance to analyze.
	 * @return A zero-copy array wrapper view if the structure is completely filled; otherwise a new list containing filtered elements.
	 */
	private fun normalizeArray(array: Array<*>): List<Any> {
		return if (!arrayHasNulls(array)) arrayReadOnlyView(array)
		else array.filterNotNull()
	}
	
	/**
	 * Processes standard lists by evaluating null footprint density.
	 *
	 * @param list The list wrapper instance to analyze.
	 * @return A zero-copy list wrapper view if the structure is completely filled; otherwise a new list containing filtered elements.
	 */
	private fun normalizeListValue(list: List<*>): List<Any> {
		return when {
			list.isEmpty() -> emptyList()
			!listHasNulls(list) -> listReadOnlyView(list)
			else -> list.filterNotNull()
		}
	}
	
	/**
	 * Stabilizes tracking indices over non-indexed collections like sets or queues.
	 *
	 * @param collection The collection instance to process.
	 * @return An [emptyList] if the collection is empty; otherwise a new index-stable, null-filtered [ArrayList].
	 */
	private fun normalizeCollection(collection: Collection<*>): List<Any> {
		return if (collection.isEmpty()) emptyList()
		else collection.filterNotNull()
	}
	
	/**
	 * Executes a zero-allocation sequential scan over a reference array to look for null entries.
	 *
	 * @param array The reference array to evaluate.
	 * @return `true` if a null element is found; `false` if the array contains only fully populated instances.
	 */
	private fun arrayHasNulls(array: Array<*>): Boolean {
		for (e in array) if (e == null) return true
		return false
	}
	
	/**
	 * Executes a zero-allocation index-based traversal loop over a list to look for null entries.
	 *
	 * This method utilizes an explicit sequential loop over indices to prevent the instantiation of
	 * temporary iterator tracker tokens on the heap.
	 *
	 * @param list The list target to evaluate.
	 * @return `true` if a null reference is encountered; `false` if the list contains only fully populated instances.
	 */
	private fun listHasNulls(list: List<*>): Boolean {
		for (i in list.indices) if (list[i] == null) return true
		return false
	}
	
	/**
	 * Synthesizes an anonymous, read-only [AbstractList] wrapper view around a reference array without
	 * triggering element duplication or allocation sequences.
	 *
	 * **Safety Invariant:** The caller must guarantee that the referenced array is verified to be completely free
	 * of null entries before wrapping. This allows the internal fetch method to use non-null assertion operators (`!!`) safely.
	 *
	 * @param array The source reference array to encapsulate.
	 * @return A zero-copy [AbstractList] wrapper proxying requests directly down to the source array.
	 */
	private fun arrayReadOnlyView(array: Array<*>): List<Any> {
		return object: AbstractList<Any>() {
			override val size: Int = array.size
			override fun get(index: Int): Any = array[index]!!  // safe due to "no nulls" precheck
		}
	}
	
	/**
	 * Synthesizes an anonymous, read-only [AbstractList] wrapper view around an existing list instance
	 * without triggering element duplication or allocation sequences.
	 *
	 * This structural pattern protects the framework's internal loops by preventing downstream steps
	 * from accidentally executing mutability operations (like `add` or `clear`) against the shared input lists.
	 *
	 * **Safety Invariant:** The caller must guarantee that the target list is verified to be completely free
	 * of null entries before wrapping. This allows the internal fetch method to use non-null assertion operators (`!!`) safely.
	 *
	 * @param list The source list structure to encapsulate.
	 * @return A zero-copy [AbstractList] wrapper proxying requests directly down to the source list.
	 */
	private fun listReadOnlyView(list: List<*>): List<Any> {
		return object: AbstractList<Any>() {
			override val size: Int = list.size
			override fun get(index: Int): Any = list[index]!!   // safe due to "no nulls" precheck
		}
	}
}