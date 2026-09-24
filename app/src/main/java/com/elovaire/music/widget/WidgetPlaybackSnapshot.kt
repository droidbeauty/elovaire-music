package elovaire.music.droidbeauty.app.widget

import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState

internal enum class WidgetRepeatMode {
    Off,
    One,
    All,
}

internal data class WidgetArtworkIdentity(
    val songId: Long,
    val mediaRevisionSeconds: Long,
)

internal data class WidgetPlaybackSnapshot(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val contentRevision: Long,
    val currentSongId: Long?,
    val title: String,
    val artist: String,
    val album: String?,
    val artworkIdentity: WidgetArtworkIdentity?,
    val isPlaying: Boolean,
    val transportShowsPause: Boolean,
    val repeatMode: WidgetRepeatMode,
    val shuffleEnabled: Boolean,
    val durationMs: Long?,
    val capturedAtElapsedRealtimeMs: Long,
) {
    internal fun hasSameWidgetContent(other: WidgetPlaybackSnapshot): Boolean =
        schemaVersion == other.schemaVersion &&
            currentSongId == other.currentSongId &&
            title == other.title &&
            artist == other.artist &&
            album == other.album &&
            artworkIdentity == other.artworkIdentity &&
            isPlaying == other.isPlaying &&
            transportShowsPause == other.transportShowsPause &&
            repeatMode == other.repeatMode &&
            shuffleEnabled == other.shuffleEnabled &&
            durationMs == other.durationMs

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        const val MAX_TEXT_LENGTH = 4_096
    }
}

internal fun projectWidgetPlaybackSnapshot(
    nowPlaying: PlaybackNowPlayingState,
    transport: PlaybackTransportState,
    queue: PlaybackQueueState,
    previous: WidgetPlaybackSnapshot?,
    elapsedRealtimeMs: Long,
): WidgetPlaybackSnapshot {
    val queuedSong = queue.queue.getOrNull(queue.currentIndex)
    val song = when {
        queuedSong?.id == nowPlaying.currentSong?.id -> nowPlaying.currentSong
        nowPlaying.currentSong != null -> nowPlaying.currentSong
        else -> queuedSong
    }
    val candidate = WidgetPlaybackSnapshot(
        contentRevision = previous?.contentRevision ?: 0L,
        currentSongId = song?.id,
        title = song?.title.orEmpty().take(WidgetPlaybackSnapshot.MAX_TEXT_LENGTH),
        artist = song?.artist.orEmpty().take(WidgetPlaybackSnapshot.MAX_TEXT_LENGTH),
        album = song?.album?.take(WidgetPlaybackSnapshot.MAX_TEXT_LENGTH),
        artworkIdentity = song?.let {
            WidgetArtworkIdentity(
                songId = it.id,
                mediaRevisionSeconds = it.dateModifiedSeconds ?: 0L,
            )
        },
        isPlaying = transport.isPlaying,
        transportShowsPause = transport.transportShowsPause,
        repeatMode = transport.repeatMode.toWidgetRepeatMode(),
        shuffleEnabled = transport.shuffleEnabled,
        durationMs = song?.durationMs?.coerceAtLeast(0L),
        capturedAtElapsedRealtimeMs = elapsedRealtimeMs.coerceAtLeast(0L),
    )

    if (previous?.hasSameWidgetContent(candidate) == true) {
        return previous
    }
    return candidate.copy(contentRevision = (previous?.contentRevision ?: 0L) + 1L)
}

private fun PlaybackRepeatMode.toWidgetRepeatMode(): WidgetRepeatMode = when (this) {
    PlaybackRepeatMode.Off -> WidgetRepeatMode.Off
    PlaybackRepeatMode.One -> WidgetRepeatMode.One
    PlaybackRepeatMode.All -> WidgetRepeatMode.All
}
