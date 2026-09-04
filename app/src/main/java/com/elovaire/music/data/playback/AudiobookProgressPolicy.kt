package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song

data class AudiobookPlaybackContext(
    val bookKey: String,
    val orderedSongIds: List<Long>,
    val bookDurationMs: Long,
    val orderedSongDurationsMs: List<Long> = emptyList(),
) {
    val normalizedSongIds: List<Long> = orderedSongIds.filter { it > 0L }.distinct()
    val normalizedSongDurationsMs: List<Long> = if (orderedSongDurationsMs.size == orderedSongIds.size) {
        buildList {
            val durationByPosition = orderedSongIds.withIndex()
                .associate { (index, songId) -> songId to orderedSongDurationsMs[index].orZero() }
            normalizedSongIds.forEach { songId -> add(durationByPosition[songId].orZero()) }
        }
    } else {
        emptyList()
    }
    private val normalizedPrefixDurationsMs: LongArray by lazy(LazyThreadSafetyMode.NONE) {
        LongArray(normalizedSongDurationsMs.size).also { prefixDurations ->
            var elapsedMs = 0L
            normalizedSongDurationsMs.forEachIndexed { index, durationMs ->
                prefixDurations[index] = elapsedMs
                elapsedMs += durationMs
            }
        }
    }

    fun elapsedBefore(index: Int): Long? {
        return normalizedPrefixDurationsMs.getOrNull(index)
            ?.takeIf { normalizedSongDurationsMs.size == normalizedSongIds.size }
    }

    fun durationAt(index: Int): Long? {
        return normalizedSongDurationsMs.getOrNull(index)
            ?.takeIf { normalizedSongDurationsMs.size == normalizedSongIds.size }
    }
}

internal data class ResolvedAudiobookProgress(
    val songId: Long?,
    val positionMs: Long,
    val partIndex: Int?,
    val bookElapsedMs: Long,
    val bookDurationMs: Long,
    val completed: Boolean,
    val updatedAtMs: Long?,
) {
    val progressFraction: Float
        get() = if (bookDurationMs > 0L) {
            (bookElapsedMs.toFloat() / bookDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

internal fun audiobookPartPrefixDurations(parts: List<AudiobookPart>): LongArray {
    val prefixDurations = LongArray(parts.size)
    var elapsedMs = 0L
    parts.forEachIndexed { index, part ->
        prefixDurations[index] = elapsedMs
        elapsedMs += part.durationMs.coerceAtLeast(0L)
    }
    return prefixDurations
}

internal fun resolveAudiobookProgress(
    book: Audiobook,
    savedProgress: AudiobookProgress?,
    currentSongId: Long?,
    currentPositionMs: Long,
    partPrefixDurations: LongArray? = null,
): ResolvedAudiobookProgress {
    val activeSongId = currentSongId?.takeIf { id -> book.parts.any { it.song.id == id } }
        ?: savedProgress?.songId?.takeIf { id -> book.parts.any { it.song.id == id } }
    val positionMs = if (activeSongId != null && activeSongId == currentSongId) {
        currentPositionMs
    } else {
        savedProgress?.positionMs ?: 0L
    }
    val partIndex = book.parts.indexOfActivePart(activeSongId, positionMs)
    val part = partIndex?.let(book.parts::get)
    val normalizedPositionMs = part?.let { partPositionMs(it, positionMs) } ?: positionMs.coerceAtLeast(0L)
    val offsetMs = part?.let { partOffsetMs(it, normalizedPositionMs) } ?: 0L
    val elapsedMs = partIndex?.let { index ->
        (partPrefixDurations?.getOrNull(index)
            ?: audiobookPartPrefixDurations(book.parts).getOrNull(index)
            ?: 0L) + offsetMs
    } ?: activeSongId?.let { savedProgress?.bookElapsedMs?.coerceAtLeast(0L) } ?: 0L
    val durationMs = book.durationMs.coerceAtLeast(0L)
    return ResolvedAudiobookProgress(
        songId = activeSongId,
        positionMs = normalizedPositionMs,
        partIndex = partIndex,
        bookElapsedMs = elapsedMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE),
        bookDurationMs = durationMs,
        completed = durationMs > 0L && durationMs - elapsedMs <= COMPLETION_THRESHOLD_MS,
        updatedAtMs = savedProgress?.updatedAtMs,
    )
}

internal fun resolveAudiobookBookElapsed(
    context: AudiobookPlaybackContext,
    queue: List<Song>,
    songId: Long,
    positionMs: Long,
): Long {
    val currentIndex = context.normalizedSongIds.indexOf(songId)
    if (currentIndex < 0) return 0L
    val songsById = if (context.durationAt(currentIndex) == null) queue.associateBy(Song::id) else emptyMap()
    val previousDurationMs = context.elapsedBefore(currentIndex)
        ?: context.normalizedSongIds.take(currentIndex)
            .sumOf { songsById[it]?.durationMs?.coerceAtLeast(0L) ?: 0L }
    val currentDurationMs = context.durationAt(currentIndex)
        ?: songsById[songId]?.durationMs?.coerceAtLeast(0L)
        ?: 0L
    return (previousDurationMs + positionMs.coerceAtLeast(0L).coerceAtMost(currentDurationMs))
        .coerceAtMost(context.bookDurationMs.coerceAtLeast(0L).takeIf { it > 0L } ?: Long.MAX_VALUE)
}

private fun Long?.orZero(): Long = this?.coerceAtLeast(0L) ?: 0L

private fun List<AudiobookPart>.indexOfActivePart(songId: Long?, positionMs: Long): Int? {
    if (songId == null) return null
    return indexOfFirst { part ->
        if (part.song.id != songId) return@indexOfFirst false
        val startMs = part.startMs
        val endMs = part.endMs
        if (startMs != null && endMs != null) {
            positionMs.coerceAtLeast(0L) in startMs..<(endMs.coerceAtLeast(startMs))
        } else {
            true
        }
    }.takeIf { it >= 0 }
        ?: indexOfLast { it.song.id == songId }.takeIf { it >= 0 }
}

private fun partOffsetMs(part: AudiobookPart, positionMs: Long): Long {
    val startMs = part.startMs ?: 0L
    return (positionMs - startMs).coerceAtLeast(0L).coerceAtMost(part.durationMs.coerceAtLeast(0L))
}

private fun partPositionMs(part: AudiobookPart, positionMs: Long): Long {
    return positionMs.coerceAtLeast(0L).coerceAtMost(part.song.durationMs.coerceAtLeast(0L))
}

private const val COMPLETION_THRESHOLD_MS = 10_000L
