package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@UnstableApi
internal class PlaybackExternalCommandGateway(
    delegate: Player,
    private val dispatchPlaybackCommand: (PlaybackCommand, PlaybackCommandOrigin) -> Unit,
    private val seek: (Long) -> Unit,
    private val skipNext: () -> Unit,
    private val skipPrevious: () -> Unit,
) : ForwardingPlayer(delegate) {
    override fun play() {
        dispatchPlaybackCommand(PlaybackCommand.Play, PlaybackCommandOrigin.ExternalController)
    }

    override fun pause() {
        dispatchPlaybackCommand(PlaybackCommand.Pause, PlaybackCommandOrigin.ExternalController)
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        dispatchPlaybackCommand(
            if (playWhenReady) PlaybackCommand.Play else PlaybackCommand.Pause,
            PlaybackCommandOrigin.ExternalController,
        )
    }

    override fun seekTo(positionMs: Long) {
        seek(positionMs)
    }

    override fun seekToNextMediaItem() {
        skipNext()
    }

    override fun seekToPreviousMediaItem() {
        skipPrevious()
    }
}
