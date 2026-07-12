package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.exoplayer.ExoPlayer
import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackPlayerSwitcher(
    private val createPlayer: (Boolean) -> ExoPlayer,
    private val attachPlayerObservers: (ExoPlayer) -> Unit,
    private val detachPlayerObservers: (ExoPlayer) -> Unit,
    private val onPlayerReplaced: (ExoPlayer) -> Unit,
    private val applyPreferredAudioDevice: (Boolean) -> Unit,
    private val targetPlayerOutputGain: () -> Float,
) {
    fun switchPlayerAudioPath(
        currentPlayer: ExoPlayer,
        queueSnapshot: List<Song>,
        useDirectPlayback: Boolean,
        playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot.from(currentPlayer),
    ): ExoPlayer {
        var replacementPlayer: ExoPlayer? = null
        var replacementObserversAttached = false
        return try {
            val replacement = createPlayer(!useDirectPlayback)
            replacementPlayer = replacement
            attachPlayerObservers(replacement)
            replacementObserversAttached = true
            replacement.repeatMode = currentPlayer.repeatMode
            replacement.shuffleModeEnabled = currentPlayer.shuffleModeEnabled
            replacement.playbackParameters = playbackSnapshot.playbackParameters
            if (queueSnapshot.isNotEmpty()) {
                replacement.setMediaItems(
                    queueSnapshot.map(Song::toPlaybackMediaItem),
                    playbackSnapshot.currentIndex.coerceIn(0, queueSnapshot.lastIndex),
                    playbackSnapshot.positionMs,
                )
                replacement.prepare()
                replacement.playWhenReady = playbackSnapshot.playWhenReady
                if (playbackSnapshot.playWhenReady) {
                    replacement.play()
                }
            }
            onPlayerReplaced(replacement)
            applyPreferredAudioDevice(true)
            replacement.volume = targetPlayerOutputGain()
            detachPlayerObservers(currentPlayer)
            currentPlayer.release()
            replacement
        } catch (_: Throwable) {
            if (replacementObserversAttached) {
                replacementPlayer?.let { runCatching { detachPlayerObservers(it) } }
            }
            replacementPlayer?.let { runCatching { it.release() } }
            currentPlayer
        }
    }
}
