package elovaire.music.droidbeauty.app.domain.kernel

internal data class NormalizedPlaybackQueue(
    val currentIndex: Int,
    val sourcePlaylistId: Long?,
)

internal fun normalizePlaybackQueue(
    queueSize: Int,
    currentIndex: Int,
    sourcePlaylistId: Long?,
): NormalizedPlaybackQueue {
    if (queueSize <= 0) {
        return NormalizedPlaybackQueue(currentIndex = -1, sourcePlaylistId = null)
    }
    return NormalizedPlaybackQueue(
        currentIndex = currentIndex.takeIf { it in 0 until queueSize } ?: 0,
        sourcePlaylistId = sourcePlaylistId,
    )
}
