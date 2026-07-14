package elovaire.music.droidbeauty.app.data.update

internal object AppVersionPolicy {
    fun normalize(raw: String): String = raw.trim().removePrefix("v").removePrefix("V")

    fun resolve(
        tagName: String,
        releaseName: String,
        assetFileName: String,
    ): String {
        val normalizedTag = normalize(tagName)
        VERSION_REGEX.find(normalizedTag)?.value?.let { return it }

        val normalizedName = normalize(releaseName)
        VERSION_REGEX.find(normalizedName)?.value?.let { return it }

        return VERSION_REGEX.find(assetFileName)?.value?.let(::normalize).orEmpty()
    }

    fun isNewer(candidate: String, installed: String): Boolean = compare(candidate, installed) > 0

    fun compare(left: String, right: String): Int {
        val leftParts = left.versionParts()
        val rightParts = right.versionParts()
        val maxSize = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until maxSize) {
            val difference = leftParts.getOrElse(index) { 0 }
                .compareTo(rightParts.getOrElse(index) { 0 })
            if (difference != 0) return difference
        }
        return 0
    }

    private fun String.versionParts(): List<Int> {
        return normalize(this)
            .split('.', '-', '_')
            .mapNotNull(String::toIntOrNull)
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
    val successAge = nowWallTimeMs - lastSuccessfulWallTimeMs
    return successAge !in 0 until successIntervalMs
}
