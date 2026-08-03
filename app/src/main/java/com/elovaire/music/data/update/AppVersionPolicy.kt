package elovaire.music.droidbeauty.app.data.update

internal object AppVersionPolicy {
    fun normalize(raw: String): String = raw.trim().removePrefix("v").removePrefix("V")

    fun resolve(tagName: String, releaseName: String, assetFileName: String): String {
        sequenceOf(tagName, releaseName, assetFileName)
            .map { VERSION_REGEX.find(normalize(it))?.value }
            .firstOrNull { !it.isNullOrBlank() }
            ?.let(::normalize)
            ?.let { return it }
        return ""
    }

    fun isNewer(candidate: String, installed: String): Boolean = compare(candidate, installed) > 0

    fun compare(left: String, right: String): Int {
        val leftParts = normalize(left).split('.', '-', '_').mapNotNull(String::toIntOrNull)
        val rightParts = normalize(right).split('.', '-', '_').mapNotNull(String::toIntOrNull)
        return (0 until maxOf(leftParts.size, rightParts.size))
            .asSequence()
            .map { index -> leftParts.getOrElse(index) { 0 }.compareTo(rightParts.getOrElse(index) { 0 }) }
            .firstOrNull { it != 0 }
            ?: 0
    }

    private val VERSION_REGEX = Regex("""\d+(?:\.\d+)+""")
}

internal fun shouldRunAutomaticUpdateCheck(
    lastSuccessfulWallTimeMs: Long,
    nowWallTimeMs: Long,
    lastFailureElapsedTimeMs: Long?,
    nowElapsedTimeMs: Long,
    successIntervalMs: Long,
    failureBackoffMs: Long,
): Boolean {
    val failureAge = lastFailureElapsedTimeMs?.let { nowElapsedTimeMs - it }
    if (failureAge != null && failureAge in 0 until failureBackoffMs) return false
    return nowWallTimeMs - lastSuccessfulWallTimeMs !in 0 until successIntervalMs
}
