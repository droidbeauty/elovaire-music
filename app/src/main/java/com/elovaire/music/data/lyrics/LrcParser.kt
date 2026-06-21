package elovaire.music.droidbeauty.app.data.lyrics

import java.nio.charset.Charset
import java.util.Locale

private val LRC_TIME_REGEX = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
private val LRC_METADATA_REGEX = Regex("""^\s*\[([a-zA-Z]+):(.*)]\s*$""")
private val METADATA_ONLY_LINE_REGEX = Regex("""^\s*\[?\s*(by|ar|ti|al|offset|length)\s*[:：].*\]?\s*$""", RegexOption.IGNORE_CASE)
private val SECTION_HEADER_REGEX = Regex("""^\s*\[[^\]]+]\s*$""")

internal fun parseLrcOrPlain(
    raw: String,
    providerName: String?,
    confidence: Int,
): LyricsPayload? {
    if (raw.isBlank()) return null

    val normalizedRaw = raw.normalizeLyricBreaks()
    val offsetMs = normalizedRaw
        .lineSequence()
        .mapNotNull { line ->
            LRC_METADATA_REGEX.matchEntire(line.trim())
                ?.takeIf { it.groupValues[1].equals("offset", ignoreCase = true) }
                ?.groupValues
                ?.get(2)
                ?.trim()
                ?.toLongOrNull()
        }
        .lastOrNull()
        ?: 0L
    val timed = mutableListOf<LyricsLine>()
    val plain = mutableListOf<String>()

    normalizedRaw
        .lineSequence()
        .forEach { rawLine ->
            val metadataMatch = LRC_METADATA_REGEX.matchEntire(rawLine.trim())
            if (metadataMatch != null) {
                return@forEach
            }

            val matches = LRC_TIME_REGEX.findAll(rawLine).toList()
            val text = sanitizeLyricLine(rawLine.replace(LRC_TIME_REGEX, "").trim()).orEmpty()

            if (matches.isEmpty()) {
                if (text.isNotBlank() && !METADATA_ONLY_LINE_REGEX.matches(text)) {
                    plain += text
                }
                return@forEach
            }

            if (text.isBlank()) return@forEach

            matches.forEach { match ->
                timed += LyricsLine(
                    text = text,
                    startTimeMs = match.toTimeMs().plus(offsetMs).coerceAtLeast(0L),
                )
            }
        }

    if (timed.isEmpty()) {
        val plainLines = plain
            .mapIndexed { index, text -> LyricsLine(text = text, startTimeMs = null, index = index) }
        return plainLines.takeIf { it.isNotEmpty() }?.let { lines ->
            LyricsPayload(
                lines = lines,
                isSynced = false,
                providerName = providerName,
                confidence = confidence,
            )
        }
    }

    val sorted = timed.sortedBy { it.startTimeMs ?: Long.MAX_VALUE }
    val indexed = sorted.mapIndexed { index, line ->
        line.copy(
            index = index,
            endTimeMs = sorted.nextDistinctStartTimeAfter(index)?.minus(1L),
        )
    }

    return LyricsPayload(
        lines = indexed,
        isSynced = true,
        providerName = providerName,
        confidence = confidence,
    )
}

internal fun parseSyncedLyrics(rawLyrics: String?): List<LyricsLine>? {
    return rawLyrics
        ?.takeIf { it.isNotBlank() }
        ?.let { parseLrcOrPlain(it, providerName = null, confidence = 0) }
        ?.takeIf { it.isSynced }
        ?.lines
}

internal fun parsePlainLyrics(rawLyrics: String?): List<LyricsLine>? {
    return rawLyrics
        ?.takeIf { it.isNotBlank() }
        ?.let { parseLrcOrPlain(it, providerName = null, confidence = 0) }
        ?.lines
        ?.takeIf { lines -> lines.any { it.text.isNotBlank() } }
}

internal fun sanitizeLyricLine(line: String): String? {
    val withoutTags = line
        .replace(Regex("""<[^>]+>"""), " ")
        .replace(Regex("""&amp;""", RegexOption.IGNORE_CASE), "&")
        .replace(Regex("""&quot;""", RegexOption.IGNORE_CASE), "\"")
        .replace(Regex("""&#39;|&apos;""", RegexOption.IGNORE_CASE), "'")

    val cleaned = withoutTags
        .replace('\u00A0', ' ')
        .replace(Regex("""\s{2,}"""), " ")
        .trim()

    if (cleaned.isBlank()) return null
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized == "embed") return null
    if (normalized.startsWith("translations")) return null
    if (normalized.startsWith("you might also like")) return null
    if (normalized.startsWith("submit corrections")) return null
    if (normalized.startsWith("contributors")) return null
    if (SECTION_HEADER_REGEX.matches(cleaned)) return null
    if (METADATA_ONLY_LINE_REGEX.matches(cleaned)) return null
    return cleaned
}

internal fun String.normalizeLyricBreaks(): String {
    return removeBom()
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace(Regex("""(?i)<\s*br\s*/?\s*>"""), "\n")
        .replace(Regex("""(?i)</\s*p\s*>"""), "\n")
        .replace(Regex("""(?i)<\s*/?\s*(div|p|span)[^>]*>"""), "\n")
}

internal fun decodeBestEffortText(bytes: ByteArray): String {
    if (bytes.isEmpty()) return ""
    if (bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))) {
        return bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
    }
    if (bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte()))) {
        return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16LE)
    }
    if (bytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte()))) {
        return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16BE)
    }
    val utf8 = bytes.toString(Charsets.UTF_8)
    val replacementCount = utf8.count { it == '\uFFFD' }
    return if (replacementCount > maxOf(6, utf8.length / 10)) {
        bytes.toString(Charset.forName("windows-1252"))
    } else {
        utf8
    }
}

private fun MatchResult.toTimeMs(): Long {
    val min = groupValues[1].toLong()
    val sec = groupValues[2].toLong()
    val frac = groupValues.getOrNull(3).orEmpty()
    val ms = when (frac.length) {
        0 -> 0L
        1 -> frac.toLong() * 100L
        2 -> frac.toLong() * 10L
        else -> frac.take(3).toLong()
    }
    return min * 60_000L + sec * 1_000L + ms
}

private fun List<LyricsLine>.nextDistinctStartTimeAfter(index: Int): Long? {
    val currentStart = getOrNull(index)?.startTimeMs ?: return null
    return asSequence()
        .drop(index + 1)
        .mapNotNull(LyricsLine::startTimeMs)
        .firstOrNull { it > currentStart }
}

private fun ByteArray.startsWith(other: ByteArray): Boolean {
    if (size < other.size) return false
    return other.indices.all { index -> this[index] == other[index] }
}

private fun String.removeBom(): String = removePrefix("\uFEFF")
