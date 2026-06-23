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
        detachPlayerObservers(currentPlayer)
        val replacementPlayer = createPlayer(!useDirectPlayback)
        attachPlayerObservers(replacementPlayer)
        replacementPlayer.repeatMode = currentPlayer.repeatMode
        replacementPlayer.shuffleModeEnabled = currentPlayer.shuffleModeEnabled
        if (queueSnapshot.isNotEmpty()) {
            replacementPlayer.setMediaItems(
                queueSnapshot.map(Song::toPlaybackMediaItem),
                playbackSnapshot.currentIndex.coerceIn(0, queueSnapshot.lastIndex),
                playbackSnapshot.positionMs,
            )
            replacementPlayer.prepare()
            replacementPlayer.playWhenReady = playbackSnapshot.playWhenReady
            if (playbackSnapshot.playWhenReady) {
                replacementPlayer.play()
            }
        }
        onPlayerReplaced(replacementPlayer)
        applyPreferredAudioDevice(true)
        replacementPlayer.volume = targetPlayerOutputGain()
        currentPlayer.release()
        return replacementPlayer
    }
}
