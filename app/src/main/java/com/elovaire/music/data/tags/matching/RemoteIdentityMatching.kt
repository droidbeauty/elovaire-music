package elovaire.music.droidbeauty.app.data.tags.matching

import java.util.Locale

internal fun normalizeRemoteIdentity(value: String, stripQualifiers: Boolean = false): String {
    val source = if (stripQualifiers) value.replace(REMOTE_QUALIFIER_REGEX, " ") else value
    return source.lowercase(Locale.ROOT)
        .replace(REMOTE_PUNCTUATION_REGEX, " ")
        .trim()
        .replace(REMOTE_WHITESPACE_REGEX, " ")
}

internal fun remoteIdentitySimilarity(left: String, right: String): Float {
    val normalizedLeft = normalizeRemoteIdentity(left, stripQualifiers = true)
    val normalizedRight = normalizeRemoteIdentity(right, stripQualifiers = true)
    if (normalizedLeft.isBlank() || normalizedRight.isBlank()) return 0f
    if (normalizedLeft == normalizedRight) return 1f
    if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) return 0.85f
    val leftTokens = normalizedLeft.splitToSequence(' ').toSet()
    val rightTokens = normalizedRight.splitToSequence(' ').toSet()
    return (leftTokens.intersect(rightTokens).size.toFloat() /
        leftTokens.union(rightTokens).size.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

private val REMOTE_QUALIFIER_REGEX = Regex("""\([^)]*\)|\[[^]]*]""")
private val REMOTE_PUNCTUATION_REGEX = Regex("""[^\p{L}\p{N}]+""")
private val REMOTE_WHITESPACE_REGEX = Regex("""\s+""")
