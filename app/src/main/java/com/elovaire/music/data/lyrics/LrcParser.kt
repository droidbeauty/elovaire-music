package elovaire.music.app.data.lyrics

import java.nio.charset.Charset
import java.util.Locale
import kotlin.math.min

private val TIMESTAMP_REGEX = Regex("""\[(?:(\d{1,2}):)?(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")
private val METADATA_LINE_REGEX = Regex("""^\[([a-zA-Z]+):(.*)]$""")
private val METADATA_ONLY_LINE_REGEX = Regex("""^\s*\[?\s*(by|ar|ti|al|offset|length)\s*[:：].*\]?\s*$""", RegexOption.IGNORE_CASE)

internal fun parseSyncedLyrics(rawLyrics: String?): List<LyricsLine>? {
    if (rawLyrics.isNullOrBlank()) return null

    var offsetMs = 0L
    val parsedLines = mutableListOf<LyricsLine>()
    val fallbackPlainLines = mutableListOf<String>()

    rawLyrics
        .normalizeLyricBreaks()
        .lineSequence()
        .forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            parseMetadataLine(line)?.let { metadata ->
                if (metadata.first == "offset") {
                    offsetMs = metadata.second.toLongOrNull() ?: offsetMs
                }
                return@forEach
            }

            val timeTags = TIMESTAMP_REGEX.findAll(line).toList()
            if (timeTags.isEmpty()) {
                sanitizeLyricLine(line)?.let(fallbackPlainLines::add)
                return@forEach
            }

            val lyricText = sanitizeLyricLine(line.substring(timeTags.last().range.last + 1)).orEmpty()
            if (lyricText.isBlank()) return@forEach

            timeTags.forEach { match ->
                val startTimeMs = parseTimestampMatch(match)
                    ?.plus(offsetMs)
                    ?.coerceAtLeast(0L)
                    ?: return@forEach
                parsedLines += LyricsLine(
                    text = lyricText,
                    startTimeMs = startTimeMs,
                )
            }
        }

    if (parsedLines.isEmpty()) {
        return parsePlainLyrics(fallbackPlainLines.joinToString("\n"))
    }

    val finalized = finalizeSyncedLyrics(parsedLines.sortedBy { it.startTimeMs ?: Long.MAX_VALUE })
    return finalized.takeIf { it.isNotEmpty() }
}

internal fun parsePlainLyrics(rawLyrics: String?): List<LyricsLine>? {
    if (rawLyrics.isNullOrBlank()) return null
    val lines = rawLyrics
        .normalizeLyricBreaks()
        .lineSequence()
        .mapNotNull(::sanitizeLyricLine)
        .mapIndexed { index, line ->
            LyricsLine(
                text = line,
                startTimeMs = null,
                index = index,
            )
        }
        .toList()

    if (lines.isEmpty()) return null
    val nonMetadataCount = lines.count { !METADATA_ONLY_LINE_REGEX.matches(it.text) }
    return lines.takeIf { nonMetadataCount > 0 }
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
        .trim('-', '–', '—')

    if (cleaned.isBlank()) return null
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized == "embed") return null
    if (normalized.startsWith("translations")) return null
    if (normalized.startsWith("you might also like")) return null
    if (normalized.startsWith("submit corrections")) return null
    if (normalized.startsWith("contributors")) return null
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
    return if (replacementCount > min(6, utf8.length / 16)) {
        bytes.toString(Charset.forName("windows-1252"))
    } else {
        utf8
    }
}

private fun parseMetadataLine(line: String): Pair<String, String>? {
    val match = METADATA_LINE_REGEX.matchEntire(line) ?: return null
    return match.groupValues[1].lowercase(Locale.US) to match.groupValues[2].trim()
}

private fun parseTimestampMatch(match: MatchResult): Long? {
    val hours = match.groups[1]?.value?.toLongOrNull() ?: 0L
    val minutes = match.groups[2]?.value?.toLongOrNull() ?: return null
    val seconds = match.groups[3]?.value?.toLongOrNull() ?: return null
    val fractional = match.groups[4]?.value.orEmpty()
    val millis = when (fractional.length) {
        0 -> 0L
        1 -> fractional.toLongOrNull()?.times(100L)
        2 -> fractional.toLongOrNull()?.times(10L)
        else -> fractional.take(3).toLongOrNull()
    } ?: 0L
    return hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + millis
}

private fun finalizeSyncedLyrics(lines: List<LyricsLine>): List<LyricsLine> {
    val merged = mergeContinuationLyricLines(lines)
    return merged.mapIndexed { index, line ->
        line.copy(
            endTimeMs = merged.getOrNull(index + 1)?.startTimeMs,
            index = index,
        )
    }
}

private fun mergeContinuationLyricLines(lines: List<LyricsLine>): List<LyricsLine> {
    if (lines.isEmpty()) return emptyList()
    val merged = mutableListOf<LyricsLine>()
    var index = 0
    while (index < lines.size) {
        var current = lines[index]
        while (index + 1 < lines.size && shouldMergeLyricLines(current, lines[index + 1])) {
            val next = lines[index + 1]
            current = current.copy(text = joinLyricSegments(current.text, next.text))
            index += 1
        }
        merged += current
        index += 1
    }
    return merged
}

private fun shouldMergeLyricLines(current: LyricsLine, next: LyricsLine): Boolean {
    val currentStart = current.startTimeMs ?: return false
    val nextStart = next.startTimeMs ?: return false
    val gapMs = (nextStart - currentStart).coerceAtLeast(0L)
    if (gapMs > 2_600L) return false
    if (current.text.length + next.text.length > 150) return false
    if (current.text.trimEnd().endsWithAny('.', '!', '?')) return false

    val currentTrimmed = current.text.trim()
    val nextTrimmed = next.text.trim()
    if (currentTrimmed.endsWith(",") || currentTrimmed.endsWith(":") || currentTrimmed.endsWith(";")) {
        return true
    }

    val nextLead = nextTrimmed.trimStart('(', '[', '"', '\'')
    val nextFirst = nextLead.firstOrNull() ?: return false
    if (nextFirst.isLowerCase()) return true

    val normalizedLead = nextLead.lowercase(Locale.US)
    return normalizedLead.startsWith("and ") ||
        normalizedLead.startsWith("or ") ||
        normalizedLead.startsWith("but ") ||
        normalizedLead.startsWith("so ") ||
        normalizedLead.startsWith("because ") ||
        normalizedLead.startsWith("of ") ||
        normalizedLead.startsWith("to ") ||
        normalizedLead.startsWith("for ") ||
        normalizedLead.startsWith("with ") ||
        normalizedLead.startsWith("in ") ||
        normalizedLead.startsWith("on ") ||
        normalizedLead.startsWith("at ")
}

private fun joinLyricSegments(first: String, second: String): String {
    val left = first.trimEnd()
    val right = second.trimStart()
    if (left.isBlank()) return right
    if (right.isBlank()) return left
    return when {
        left.endsWith("-") || left.endsWith("—") || left.endsWith("–") -> left + right
        else -> "$left $right"
    }
}

private fun String.endsWithAny(vararg chars: Char): Boolean = chars.any { trimEnd().endsWith(it) }

private fun ByteArray.startsWith(other: ByteArray): Boolean {
    if (size < other.size) return false
    return other.indices.all { index -> this[index] == other[index] }
}

private fun String.removeBom(): String = removePrefix("\uFEFF")
