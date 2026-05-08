package elovaire.music.app.data.lyrics

internal fun resolveActiveLyricLineIndex(
    lines: List<LyricsLine>,
    positionMs: Long,
    timingOffsetMs: Long = 0L,
): Int? {
    if (lines.isEmpty()) return null
    val timedLines = lines.filter { it.startTimeMs != null }
    if (timedLines.isEmpty()) return null

    val correctedPosition = (positionMs - timingOffsetMs).coerceAtLeast(0L)
    val firstStart = timedLines.first().startTimeMs ?: return null
    if (correctedPosition < firstStart) return null

    var low = 0
    var high = timedLines.lastIndex
    var resultIndex = -1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val lineStart = timedLines[mid].startTimeMs ?: Long.MAX_VALUE
        if (lineStart <= correctedPosition) {
            resultIndex = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }

    if (resultIndex < 0) return null
    val resolvedLine = timedLines[resultIndex]
    val resolvedEnd = resolvedLine.endTimeMs
    return if (resolvedEnd != null && correctedPosition >= resolvedEnd && resultIndex < timedLines.lastIndex) {
        timedLines[resultIndex + 1].index
    } else {
        resolvedLine.index
    }
}
