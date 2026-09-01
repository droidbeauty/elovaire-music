package elovaire.music.droidbeauty.app.data.update

internal object AppVersionPolicy {
    fun normalize(raw: String): String = raw.trim().removePrefix("v").removePrefix("V")

    fun resolve(tagName: String, releaseName: String, assetFileName: String): String {
        sequenceOf(tagName, releaseName, assetFileName)
            .map { VERSION_CANDIDATE_REGEX.find(it.trim())?.groups?.get(1)?.value }
            .firstOrNull { !it.isNullOrBlank() }
            ?.let(::normalize)
            ?.let { return it }
        return ""
    }

    fun isNewer(candidate: String, installed: String): Boolean = compare(candidate, installed) > 0

    fun isSame(left: String, right: String): Boolean {
        val leftVersion = parse(left) ?: return false
        val rightVersion = parse(right) ?: return false
        return leftVersion == rightVersion
    }

    fun compare(left: String, right: String): Int {
        val leftVersion = parse(left) ?: return 0
        val rightVersion = parse(right) ?: return 0
        return leftVersion.compareTo(rightVersion)
    }

    private fun parse(raw: String): ComparableVersion? {
        val match = VERSION_REGEX.matchEntire(raw.trim()) ?: return null
        val numbers = match.groupValues[1].split('.')
        val major = numbers[0].trimLeadingZeros()
        val minor = numbers.getOrElse(1) { "0" }.trimLeadingZeros()
        val patch = numbers.getOrElse(2) { "0" }.trimLeadingZeros()
        val preRelease = match.groupValues[2].takeIf(String::isNotBlank)
            ?.split('.')
            ?.takeIf { identifiers -> identifiers.all { it.isNotBlank() } }
            ?: emptyList()
        return ComparableVersion(major, minor, patch, preRelease)
    }

    private fun String.trimLeadingZeros(): String = trimStart('0').ifEmpty { "0" }

    private data class ComparableVersion(
        val major: String,
        val minor: String,
        val patch: String,
        val preRelease: List<String>,
    ) : Comparable<ComparableVersion> {
        override fun compareTo(other: ComparableVersion): Int {
            compareNumeric(major, other.major).takeIf { it != 0 }?.let { return it }
            compareNumeric(minor, other.minor).takeIf { it != 0 }?.let { return it }
            compareNumeric(patch, other.patch).takeIf { it != 0 }?.let { return it }
            if (preRelease.isEmpty() && other.preRelease.isEmpty()) return 0
            if (preRelease.isEmpty()) return 1
            if (other.preRelease.isEmpty()) return -1
            for (index in 0 until maxOf(preRelease.size, other.preRelease.size)) {
                val left = preRelease.getOrNull(index) ?: return -1
                val right = other.preRelease.getOrNull(index) ?: return 1
                if (left == right) continue
                val leftNumeric = left.all(Char::isDigit)
                val rightNumeric = right.all(Char::isDigit)
                if (leftNumeric && rightNumeric) {
                    return compareNumeric(left.trimLeadingZeros(), right.trimLeadingZeros())
                }
                if (leftNumeric != rightNumeric) return if (leftNumeric) -1 else 1
                return left.compareTo(right)
            }
            return 0
        }
    }

    private fun compareNumeric(left: String, right: String): Int = when {
        left.length != right.length -> left.length.compareTo(right.length)
        else -> left.compareTo(right)
    }

    private val VERSION_REGEX = Regex(
        """(?i)^v?(\d+(?:\.\d+){0,2})(?:-([0-9a-z-]+(?:\.[0-9a-z-]+)*))?(?:\+[0-9a-z-]+(?:\.[0-9a-z-]+)*)?$""",
    )
    private val VERSION_CANDIDATE_REGEX = Regex(
        """(?i)(?:^|[^0-9a-z])v?(\d+(?:\.\d+){1,2}(?:-[0-9a-z-]+(?:\.[0-9a-z-]+)*)?)(?=$|[^0-9a-z])""",
    )
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
