package elovaire.music.droidbeauty.app.data.library

/** MediaStore, SAF, local-file and NAS identities share a non-zero signed ID domain. */
internal fun isValidMediaId(id: Long): Boolean = id != 0L

/** Returns a flattened acyclic relocation map, or null when the input is malformed. */
internal fun canonicalizeMediaIdRelocations(
    replacements: Map<Long, Long>,
): Map<Long, Long>? {
    if (replacements.isEmpty()) return emptyMap()
    if (replacements.any { (before, after) -> !isValidMediaId(before) || !isValidMediaId(after) }) {
        return null
    }
    val canonical = LinkedHashMap<Long, Long>(replacements.size)
    replacements.keys.forEach { source ->
        val visited = HashSet<Long>()
        var current = source
        while (true) {
            val next = replacements[current] ?: break
            if (next == current || !visited.add(current)) return null
            current = next
        }
        if (current != source) canonical[source] = current
    }
    return canonical
}
