package elovaire.music.droidbeauty.app.data.playback

import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener

internal class PlaybackPlayerGenerationGate<T : Any> {
    private var currentTarget: T? = null
    private var currentGeneration = 0L

    fun activate(target: T): Long {
        currentGeneration = if (currentGeneration == Long.MAX_VALUE) 1L else currentGeneration + 1L
        currentTarget = target
        return currentGeneration
    }

    fun isCurrent(target: T, generation: Long): Boolean {
        return currentTarget === target && currentGeneration == generation
    }

    fun invalidate(target: T) {
        if (currentTarget === target) {
            currentTarget = null
            currentGeneration = if (currentGeneration == Long.MAX_VALUE) 1L else currentGeneration + 1L
        }
    }
}

internal class GuardedPlaybackPlayerListener(
    private val target: ExoPlayer,
    private val generation: Long,
    private val gate: PlaybackPlayerGenerationGate<ExoPlayer>,
    private val delegate: Player.Listener,
    private val isAuthoritative: () -> Boolean,
) : Player.Listener {
    private inline fun ifCurrent(block: () -> Unit) {
        if (isAuthoritative() && gate.isCurrent(target, generation)) block()
    }

    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) =
        ifCurrent { delegate.onMediaItemTransition(mediaItem, reason) }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) = ifCurrent { delegate.onPositionDiscontinuity(oldPosition, newPosition, reason) }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) =
        ifCurrent { delegate.onPlayWhenReadyChanged(playWhenReady, reason) }

    override fun onIsPlayingChanged(isPlaying: Boolean) =
        ifCurrent { delegate.onIsPlayingChanged(isPlaying) }

    override fun onPlaybackStateChanged(playbackState: Int) =
        ifCurrent { delegate.onPlaybackStateChanged(playbackState) }

    override fun onPlayerError(error: PlaybackException) =
        ifCurrent { delegate.onPlayerError(error) }

    override fun onEvents(player: Player, events: Player.Events) =
        ifCurrent { delegate.onEvents(target, events) }
}

@OptIn(UnstableApi::class)
internal class GuardedPlaybackAnalyticsListener(
    private val target: ExoPlayer,
    private val generation: Long,
    private val gate: PlaybackPlayerGenerationGate<ExoPlayer>,
    private val delegate: AnalyticsListener,
    private val isAuthoritative: () -> Boolean,
) : AnalyticsListener {
    override fun onAudioTrackInitialized(
        eventTime: AnalyticsListener.EventTime,
        audioTrackConfig: androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig,
    ) {
        if (isAuthoritative() && gate.isCurrent(target, generation)) {
            delegate.onAudioTrackInitialized(eventTime, audioTrackConfig)
        }
    }

    override fun onAudioSinkError(
        eventTime: AnalyticsListener.EventTime,
        audioSinkError: Exception,
    ) {
        if (isAuthoritative() && gate.isCurrent(target, generation)) {
            delegate.onAudioSinkError(eventTime, audioSinkError)
        }
    }
}
