package elovaire.music.droidbeauty.app.data.lyrics

import java.nio.charset.Charset
import java.util.Locale

private val LRC_TIME_REGEX = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
private val LRC_METADATA_REGEX = Regex("""^\s*\[([a-zA-Z]+):(.*)]\s*$""")
private val METADATA_ONLY_LINE_REGEX = Regex("""^\s*\[?\s*(by|ar|ti|al|offset|length)\s*[:：].*\]?\s*$""", RegexOption.IGNORE_CASE)
private val SECTION_HEADER_REGEX = Regex("""^\s*\[[^\]]+]\s*$""")
private val HTML_TAG_REGEX = Regex("""<[^>]+>""")
private val HTML_AMP_REGEX = Regex("""&amp;""", RegexOption.IGNORE_CASE)
private val HTML_QUOTE_REGEX = Regex("""&quot;""", RegexOption.IGNORE_CASE)
private val HTML_APOSTROPHE_REGEX = Regex("""&#39;|&apos;""", RegexOption.IGNORE_CASE)
private val REPEATED_WHITESPACE_REGEX = Regex("""\s{2,}""")
private val ESCAPED_BREAK_REGEX = Regex("""(?i)<\s*br\s*/?\s*>""")
private val CLOSING_PARAGRAPH_REGEX = Regex("""(?i)</\s*p\s*>""")
private val BLOCK_TAG_REGEX = Regex("""(?i)<\s*/?\s*(div|p|span)[^>]*>""")
private val EXCESS_BREAKS_REGEX = Regex("""\n{3,}""")

internal fun parseLrcOrPlain(
    raw: String,
    providerName: String?,
    confidence: Int,
): LyricsPayload? {
    if (raw.isBlank() || raw.length > MAX_LYRICS_CHARACTERS) return null

    val normalizedRaw = raw.normalizeLyricBreaks()
    if (!normalizedRaw.isLyricsTextWithinBounds()) return null
    val rawLines = normalizedRaw.lineSequence().take(MAX_LYRICS_LINES + 1).toList()
    val offsetMs = rawLines
        .asSequence()
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

    rawLines.forEach { rawLine ->
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
                sourceTextForEmbedding = normalizedRaw.canonicalEmbeddedLyricsText(),
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
        sourceTextForEmbedding = normalizedRaw.canonicalEmbeddedLyricsText(),
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
        .replace(HTML_TAG_REGEX, " ")
        .replace(HTML_AMP_REGEX, "&")
        .replace(HTML_QUOTE_REGEX, "\"")
        .replace(HTML_APOSTROPHE_REGEX, "'")

    val cleaned = withoutTags
        .replace('\u00A0', ' ')
        .replace(REPEATED_WHITESPACE_REGEX, " ")
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
        .replace(ESCAPED_BREAK_REGEX, "\n")
        .replace(CLOSING_PARAGRAPH_REGEX, "\n")
        .replace(BLOCK_TAG_REGEX, "\n")
}

internal fun String.canonicalEmbeddedLyricsText(): String =
    normalizeLyricBreaks()
        .replace(EXCESS_BREAKS_REGEX, "\n\n")
        .trimEnd()

internal fun LyricsPayload.toEmbeddedLyricsText(): String =
    (sourceTextForEmbedding ?: lines.joinToString("\n") { it.text })
        .canonicalEmbeddedLyricsText()

internal fun LyricsPayload.toDisplayPayload(): LyricsPayload {
    val displayLines = lines
        .mapNotNull { line ->
            sanitizeLyricLine(line.text.replace(LRC_TIME_REGEX, "").trim())
                ?.let { displayText -> line.copy(text = displayText) }
        }
    return copy(lines = displayLines)
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

internal fun String.isLyricsTextWithinBounds(): Boolean {
    if (length > MAX_LYRICS_CHARACTERS) return false
    var lineCount = 0
    for (line in lineSequence()) {
        lineCount += 1
        if (lineCount > MAX_LYRICS_LINES || line.length > MAX_LYRIC_LINE_CHARACTERS) return false
        if (LRC_TIME_REGEX.findAll(line).take(MAX_TIMESTAMPS_PER_LINE + 1).count() > MAX_TIMESTAMPS_PER_LINE) {
            return false
        }
    }
    return true
}

internal const val MAX_LYRICS_CHARACTERS = 512 * 1024
internal const val MAX_LYRICS_LINES = 20_000
private const val MAX_LYRIC_LINE_CHARACTERS = 16 * 1024
private const val MAX_TIMESTAMPS_PER_LINE = 64
