package elovaire.music.droidbeauty.app.data.playback

import android.os.Handler
import android.os.SystemClock
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal enum class CrossfadeState {
    Idle,
    PreparingNext,
    Ready,
    Fading,
    PromotingNext,
    Cancelled,
    Failed,
    Released,
}

internal fun equalPowerCrossfadeEnvelope(progress: Float): Pair<Float, Float> {
    val angle = progress.coerceIn(0f, 1f) * (PI.toFloat() / 2f)
    return cos(angle) to sin(angle)
}

internal object CrossfadeDurationPolicy {
    const val MIN_DURATION_MS = 1_000L
    const val DEFAULT_DURATION_MS = 2_500L
    const val MAX_DURATION_MS = 12_000L

    fun sanitize(durationMs: Long): Long = durationMs.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)
}

@UnstableApi
internal class PlaybackCrossfadeController(
    private val handler: Handler,
    private val createPlayer: () -> ExoPlayer,
    private val onPromote: (outgoing: ExoPlayer, incoming: ExoPlayer) -> Unit,
    private val onFailed: () -> Unit,
    private val onIncomingReleased: (ExoPlayer) -> Unit = {},
) {
    var state: CrossfadeState = CrossfadeState.Idle
        private set
    private var outgoing: ExoPlayer? = null
    private var incoming: ExoPlayer? = null
    private var incomingReady = false
    private var outgoingGain = 1f
    private var incomingGain = 1f
    private var outgoingSilenceDetector: CrossfadeSilenceDetector? = null
    private var startAtElapsedMs = 0L
    private var fadeStartedAtElapsedMs = 0L
    private var outgoingDurationMs = 0L

    private val startRunnable = Runnable { startFade() }
    private val silencePollRunnable = Runnable { pollForTrailingSilence() }
    private val frameRunnable = object : Runnable {
        override fun run() {
            val outgoingPlayer = outgoing ?: return cancel()
            val incomingPlayer = incoming ?: return cancel()
            val elapsed = SystemClock.elapsedRealtime() - fadeStartedAtElapsedMs
            val progress = (elapsed.toFloat() / CrossfadeDurationPolicy.DEFAULT_DURATION_MS).coerceIn(0f, 1f)
            val (outgoingEnvelope, incomingEnvelope) = equalPowerCrossfadeEnvelope(progress)
            outgoingPlayer.volume = (outgoingGain * outgoingEnvelope).coerceIn(0f, 1f)
            incomingPlayer.volume = (incomingGain * incomingEnvelope).coerceIn(0f, 1f)
            if (progress >= 1f) {
                promote()
            } else {
                handler.postDelayed(this, FRAME_INTERVAL_MS)
            }
        }
    }

    fun prepare(
        primary: ExoPlayer,
        queue: List<Song>,
        nextQueueIndex: Int,
        outgoingGain: Float,
        incomingGain: Float,
        outgoingSilenceDetector: CrossfadeSilenceDetector? = null,
    ) {
        if (state != CrossfadeState.Idle || queue.isEmpty() || nextQueueIndex !in queue.indices) return
        if (!primary.isCurrentMediaItemSeekable) return
        val durationMs = primary.duration.takeIf { it > 0L } ?: return
        val remainingMs = durationMs - primary.currentPosition.coerceAtLeast(0L)
        if (remainingMs <= CrossfadeDurationPolicy.DEFAULT_DURATION_MS || primary.getPauseAtEndOfMediaItems()) return
        state = CrossfadeState.PreparingNext
        outgoing = primary
        this.outgoingGain = outgoingGain.coerceIn(0f, 1f)
        this.incomingGain = incomingGain.coerceIn(0f, 1f)
        this.outgoingSilenceDetector = outgoingSilenceDetector
        outgoingDurationMs = durationMs
        val secondary = try {
            createPlayer()
        } catch (_: RuntimeException) {
            fail()
            return
        }
        incoming = secondary
        secondary.volume = 0f
        secondary.repeatMode = primary.repeatMode
        secondary.shuffleModeEnabled = primary.shuffleModeEnabled
        secondary.setMediaItems(queue.map(Song::toPlaybackMediaItem), nextQueueIndex, 0L)
        secondary.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (secondary !== incoming) return
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (!secondary.isCurrentMediaItemSeekable) {
                            cancel()
                            return
                        }
                        incomingReady = true
                        scheduleFade(durationMs, primary.currentPosition)
                    }
                    Player.STATE_IDLE -> fail()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) = fail()
        })
        secondary.prepare()
    }

    fun refresh(primary: ExoPlayer, outgoingGain: Float) {
        if (primary !== outgoing || state != CrossfadeState.Ready) return
        this.outgoingGain = outgoingGain.coerceIn(0f, 1f)
    }

    fun cancel() {
        if (state == CrossfadeState.Released) return
        handler.removeCallbacks(startRunnable)
        handler.removeCallbacks(silencePollRunnable)
        handler.removeCallbacks(frameRunnable)
        outgoing?.getPauseAtEndOfMediaItems()?.let { pausedAtEnd ->
            if (pausedAtEnd) outgoing?.setPauseAtEndOfMediaItems(false)
        }
        outgoing?.volume = outgoingGain
        incoming?.let { incomingPlayer ->
            incomingPlayer.release()
            onIncomingReleased(incomingPlayer)
        }
        outgoing = null
        incoming = null
        incomingReady = false
        outgoingSilenceDetector = null
        outgoingDurationMs = 0L
        state = CrossfadeState.Cancelled
        state = CrossfadeState.Idle
    }

    fun release() {
        cancel()
        state = CrossfadeState.Released
    }

    private fun scheduleFade(durationMs: Long, positionMs: Long) {
        if (!incomingReady || state != CrossfadeState.PreparingNext) return
        val delayMs = (durationMs - positionMs - CrossfadeDurationPolicy.DEFAULT_DURATION_MS).coerceAtLeast(0L)
        startAtElapsedMs = SystemClock.elapsedRealtime() + delayMs
        state = CrossfadeState.Ready
        handler.postAtTime(startRunnable, startAtElapsedMs)
        if (outgoingSilenceDetector != null && delayMs > CrossfadeSilencePolicy.MAX_EARLY_START_MS) {
            handler.postAtTime(
                silencePollRunnable,
                (startAtElapsedMs - CrossfadeSilencePolicy.MAX_EARLY_START_MS)
                    .coerceAtLeast(SystemClock.elapsedRealtime()),
            )
        }
    }

    private fun pollForTrailingSilence() {
        val outgoingPlayer = outgoing ?: return cancel()
        val detector = outgoingSilenceDetector ?: return
        if (state != CrossfadeState.Ready) return
        val remainingMs = outgoingDurationMs - outgoingPlayer.currentPosition.coerceAtLeast(0L)
        if (
            remainingMs <= CrossfadeDurationPolicy.DEFAULT_DURATION_MS + CrossfadeSilencePolicy.MAX_EARLY_START_MS &&
            detector.isSilentAt(
                outgoingPlayer.currentPosition.coerceAtLeast(0L) * 1_000L,
                CrossfadeSilencePolicy.MIN_SILENCE_DURATION_MS,
            )
        ) {
            startFade()
        } else if (remainingMs > CrossfadeDurationPolicy.DEFAULT_DURATION_MS) {
            handler.postDelayed(silencePollRunnable, SILENCE_POLL_INTERVAL_MS)
        }
    }

    private fun startFade() {
        val outgoingPlayer = outgoing ?: return cancel()
        val incomingPlayer = incoming ?: return cancel()
        if (!incomingReady || !outgoingPlayer.isPlaying) return cancel()
        state = CrossfadeState.Fading
        handler.removeCallbacks(silencePollRunnable)
        outgoingPlayer.setPauseAtEndOfMediaItems(true)
        incomingPlayer.playWhenReady = true
        incomingPlayer.play()
        fadeStartedAtElapsedMs = SystemClock.elapsedRealtime()
        frameRunnable.run()
    }

    private fun promote() {
        val outgoingPlayer = outgoing ?: return cancel()
        val incomingPlayer = incoming ?: return cancel()
        state = CrossfadeState.PromotingNext
        handler.removeCallbacks(frameRunnable)
        incomingPlayer.volume = incomingGain
        outgoing = null
        incoming = null
        incomingReady = false
        onPromote(outgoingPlayer, incomingPlayer)
        state = CrossfadeState.Idle
    }

    private fun fail() {
        if (state == CrossfadeState.Released) return
        state = CrossfadeState.Failed
        cancel()
        onFailed()
    }

    private companion object {
        const val FRAME_INTERVAL_MS = 16L
        const val SILENCE_POLL_INTERVAL_MS = 50L
    }
}
