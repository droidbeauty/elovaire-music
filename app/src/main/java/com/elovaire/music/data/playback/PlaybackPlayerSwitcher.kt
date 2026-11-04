package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.exoplayer.ExoPlayer
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal class PlaybackPlayerSwitcher(
    private val createPlayer: (Boolean) -> ExoPlayer,
    private val releasePlayer: (ExoPlayer) -> Unit,
    private val attachPlayerObservers: (ExoPlayer) -> Unit,
    private val detachPlayerObservers: (ExoPlayer) -> Unit,
    private val onPlayerReplaced: (ExoPlayer) -> Unit,
    private val applyPreferredAudioDevice: (Boolean) -> Unit,
    private val targetPlayerOutputGain: () -> Float,
    private val language: () -> AppLanguage = { AppLanguage.English },
) {
    fun switchPlayerAudioPath(
        currentPlayer: ExoPlayer,
        queueSnapshot: List<Song>,
        useDirectPlayback: Boolean,
        playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot.from(currentPlayer),
    ): ExoPlayer {
        var replacementPlayer: ExoPlayer? = null
        var replacementObserversAttached = false
        var currentObserversDetached = false
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
                    queueSnapshot.map { it.toPlaybackMediaItem(language()) },
                    playbackSnapshot.currentIndex.coerceIn(0, queueSnapshot.lastIndex),
                    playbackSnapshot.positionMs,
                )
                replacement.prepare()
                replacement.playWhenReady = playbackSnapshot.playWhenReady
                if (playbackSnapshot.playWhenReady) {
                    replacement.play()
                }
            }
            replacement.volume = targetPlayerOutputGain()
            detachPlayerObservers(currentPlayer)
            currentObserversDetached = true
            onPlayerReplaced(replacement)
            applyPreferredAudioDevice(true)
            releasePlayer(currentPlayer)
            replacement
        } catch (_: RuntimeException) {
            if (replacementObserversAttached) {
                replacementPlayer?.let { runCatching { detachPlayerObservers(it) } }
            }
            replacementPlayer?.let(releasePlayer)
            if (currentObserversDetached) {
                runCatching { attachPlayerObservers(currentPlayer) }
            }
            currentPlayer
        }
    }
}
