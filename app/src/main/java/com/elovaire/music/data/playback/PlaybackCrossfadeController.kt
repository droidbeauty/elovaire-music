package elovaire.music.droidbeauty.app.data.playback

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal enum class CrossfadeState {
    Idle,
    Analyzing,
    WaitingForPrewarm,
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
    const val SETTINGS_MIN_DURATION_MS = 2_000L
    const val SETTINGS_MAX_DURATION_MS = 5_000L
    const val SETTINGS_STEP_MS = 500L

    fun sanitize(durationMs: Long): Long = durationMs.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)

    fun sanitizeSettingsDuration(durationMs: Long): Long {
        val bounded = durationMs.coerceIn(SETTINGS_MIN_DURATION_MS, SETTINGS_MAX_DURATION_MS)
        return ((bounded - SETTINGS_MIN_DURATION_MS + SETTINGS_STEP_MS / 2L) / SETTINGS_STEP_MS * SETTINGS_STEP_MS + SETTINGS_MIN_DURATION_MS)
            .coerceIn(SETTINGS_MIN_DURATION_MS, SETTINGS_MAX_DURATION_MS)
    }
}

@UnstableApi
internal class PlaybackCrossfadeController(
    private val handler: Handler,
    private val scope: CoroutineScope,
    private val cueAnalyzer: CrossfadeCueAnalyzer,
    private val createPlayer: () -> ExoPlayer,
    private val onPromote: (outgoing: ExoPlayer, incoming: ExoPlayer) -> Unit,
    private val onFailed: () -> Unit,
) {
    var state: CrossfadeState = CrossfadeState.Idle
        private set

    private var outgoing: ExoPlayer? = null
    private var incoming: ExoPlayer? = null
    private var incomingReady = false
    private var outgoingGain = 1f
    private var incomingGain = 1f
    private var queue: List<Song> = emptyList()
    private var nextQueueIndex = -1
    private var outgoingSong: Song? = null
    private var incomingSong: Song? = null
    private var outgoingDurationMs = 0L
    private var currentCue: CrossfadeCue? = null
    private var configuredFadeDurationMs = CrossfadeDurationPolicy.DEFAULT_DURATION_MS
    private var configuredSilenceLevelDb = CrossfadeSilencePolicy.BASE_LEVEL_DB
    private var plan: CrossfadeTransitionPlan? = null
    private var fadeDurationMs = CrossfadeDurationPolicy.DEFAULT_DURATION_MS
    private var fadeStartedAtElapsedMs = 0L
    private var transitionToken = 0L
    private var analysisJob: Job? = null

    private val prewarmRunnable = Runnable { prewarmIncoming(transitionToken) }
    private val startRunnable = Runnable { startFade(transitionToken) }
    private val frameRunnable = object : Runnable {
        override fun run() {
            val outgoingPlayer = outgoing ?: return cancel()
            val incomingPlayer = incoming ?: return cancel()
            val elapsed = SystemClock.elapsedRealtime() - fadeStartedAtElapsedMs
            val progress = (elapsed.toFloat() / fadeDurationMs.coerceAtLeast(1L))
                .coerceIn(0f, 1f)
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

    @Suppress("TooGenericExceptionCaught")
    fun prepare(
        primary: ExoPlayer,
        queue: List<Song>,
        nextQueueIndex: Int,
        outgoingSong: Song,
        incomingSong: Song,
        outgoingGain: Float,
        incomingGain: Float,
        fadeDurationMs: Long = CrossfadeDurationPolicy.DEFAULT_DURATION_MS,
        silenceLevelDb: Float = CrossfadeSilencePolicy.BASE_LEVEL_DB,
    ) {
        if (state != CrossfadeState.Idle || queue.isEmpty() || nextQueueIndex !in queue.indices) return
        val durationMs = primary.duration.takeIf { it > 0L } ?: outgoingSong.durationMs
        if (durationMs <= 0L || primary.getPauseAtEndOfMediaItems()) return

        configuredFadeDurationMs = CrossfadeDurationPolicy.sanitize(fadeDurationMs)
        configuredSilenceLevelDb = CrossfadeSilencePolicy.sanitizeLevelDb(silenceLevelDb)
        val token = ++transitionToken
        state = CrossfadeState.Analyzing
        outgoing = primary
        this.queue = queue.toList()
        this.nextQueueIndex = nextQueueIndex
        this.outgoingSong = outgoingSong
        this.incomingSong = incomingSong
        this.outgoingDurationMs = durationMs
        this.outgoingGain = outgoingGain.coerceIn(0f, 1f)
        this.incomingGain = incomingGain.coerceIn(0f, 1f)
        currentCue = null
        logDebug(
            "prepare crossfade_enabled=true configured_duration_ms=$configuredFadeDurationMs " +
                "configured_silence_db=$configuredSilenceLevelDb",
        )
        analysisJob = scope.launch {
            val cue = try {
                cueAnalyzer.analyzePair(
                    outgoing = outgoingSong,
                    incoming = incomingSong,
                    silenceLevelDb = configuredSilenceLevelDb,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val reason = "controller_analysis_exception:${error.javaClass.simpleName}"
                logDebug("analysis_fallback reason=$reason")
                CrossfadeCue.fallback(durationMs).copy(
                    outgoingFallbackReason = reason,
                    incomingFallbackReason = reason,
                )
            }
            handler.post {
                if (token != transitionToken || state != CrossfadeState.Analyzing) return@post
                currentCue = cue
                plan = CrossfadeTransitionPlan.from(
                    cue = cue,
                    outgoingDurationMs = durationMs,
                    incomingDurationMs = incomingSong.durationMs,
                    fadeDurationMs = configuredFadeDurationMs,
                )
                state = CrossfadeState.WaitingForPrewarm
                logDebug(
                        "cue configured_duration_ms=$configuredFadeDurationMs " +
                        "analyzer_silence_db=$configuredSilenceLevelDb duration=$durationMs " +
                        "detected_mix_out_ms=${cue.outgoingMixOutMs} " +
                        "detected_mix_in_ms=${cue.incomingMixInMs} " +
                        "trailing_silence_ms=${cue.outgoingTrailingSilenceMs} " +
                        "leading_silence_ms=${cue.incomingLeadingSilenceMs} " +
                        "planned_fade_ms=${plan?.fadeDurationMs} planned_fade_start_ms=${plan?.fadeStartMs} " +
                        "analysis_success=${cue.outgoingAnalysisSucceeded}/${cue.incomingAnalysisSucceeded} " +
                        "fallback_reason=${cue.outgoingFallbackReason ?: cue.incomingFallbackReason ?: "none"}",
                )
                schedulePrewarm(token)
            }
        }
    }

    fun updateSettings(
        primary: ExoPlayer,
        fadeDurationMs: Long,
        silenceLevelDb: Float,
    ) {
        if (state == CrossfadeState.Released) return
        val normalizedDuration = CrossfadeDurationPolicy.sanitize(fadeDurationMs)
        val normalizedSilence = CrossfadeSilencePolicy.sanitizeLevelDb(silenceLevelDb)
        val durationChanged = configuredFadeDurationMs != normalizedDuration
        val silenceChanged = configuredSilenceLevelDb != normalizedSilence
        configuredFadeDurationMs = normalizedDuration
        configuredSilenceLevelDb = normalizedSilence
        if (!durationChanged && !silenceChanged) return
        if (state == CrossfadeState.Fading || state == CrossfadeState.PromotingNext) return

        if (silenceChanged && state != CrossfadeState.Idle) {
            val currentQueue = queue
            val currentNextQueueIndex = nextQueueIndex
            val currentOutgoingSong = outgoingSong
            val currentIncomingSong = incomingSong
            val currentOutgoingGain = outgoingGain
            val currentIncomingGain = incomingGain
            cancel()
            if (
                currentOutgoingSong != null &&
                currentIncomingSong != null &&
                currentQueue.isNotEmpty() &&
                currentNextQueueIndex in currentQueue.indices
            ) {
                prepare(
                    primary = primary,
                    queue = currentQueue,
                    nextQueueIndex = currentNextQueueIndex,
                    outgoingSong = currentOutgoingSong,
                    incomingSong = currentIncomingSong,
                    outgoingGain = currentOutgoingGain,
                    incomingGain = currentIncomingGain,
                    fadeDurationMs = configuredFadeDurationMs,
                    silenceLevelDb = configuredSilenceLevelDb,
                )
            }
        } else if (durationChanged && currentCue != null) {
            replanForDuration(primary)
        }
    }

    fun refresh(primary: ExoPlayer, outgoingGain: Float) {
        if (primary !== outgoing || state !in READY_STATES) return
        this.outgoingGain = outgoingGain.coerceIn(0f, 1f)
    }

    fun cancel() {
        if (state == CrossfadeState.Released) return
        transitionToken += 1L
        analysisJob?.cancel()
        analysisJob = null
        handler.removeCallbacks(prewarmRunnable)
        handler.removeCallbacks(startRunnable)
        handler.removeCallbacks(frameRunnable)
        outgoing?.volume = outgoingGain
        incoming?.let { it.release() }
        outgoing = null
        incoming = null
        incomingReady = false
        queue = emptyList()
        nextQueueIndex = -1
        outgoingSong = null
        incomingSong = null
        outgoingDurationMs = 0L
        currentCue = null
        plan = null
        state = CrossfadeState.Cancelled
        state = CrossfadeState.Idle
    }

    fun release() {
        cancel()
        state = CrossfadeState.Released
    }

    private fun replanForDuration(primary: ExoPlayer) {
        val cue = currentCue ?: return
        val incomingTrack = incomingSong ?: return
        val durationMs = primary.duration.takeIf { it > 0L } ?: outgoingDurationMs
        if (durationMs <= 0L) return
        plan = CrossfadeTransitionPlan.from(
            cue = cue,
            outgoingDurationMs = durationMs,
            incomingDurationMs = incomingTrack.durationMs,
            fadeDurationMs = configuredFadeDurationMs,
        )
        handler.removeCallbacks(prewarmRunnable)
        handler.removeCallbacks(startRunnable)
        when (state) {
            CrossfadeState.WaitingForPrewarm -> schedulePrewarm(transitionToken)
            CrossfadeState.PreparingNext -> if (incomingReady) scheduleFadeWhenReady(transitionToken)
            CrossfadeState.Ready -> {
                state = CrossfadeState.PreparingNext
                scheduleFadeWhenReady(transitionToken)
            }
            else -> Unit
        }
        logDebug(
            "duration_replan configured_duration_ms=$configuredFadeDurationMs " +
                "planned_fade_ms=${plan?.fadeDurationMs} planned_fade_start_ms=${plan?.fadeStartMs}",
        )
    }

    private fun schedulePrewarm(token: Long) {
        val outgoingPlayer = outgoing ?: return cancel()
        val transitionPlan = plan ?: return cancel()
        val currentPositionMs = outgoingPlayer.currentPosition.coerceAtLeast(0L)
        val prewarmPositionMs = (transitionPlan.fadeStartMs - CrossfadeCuePolicy.PREWARM_LEAD_MS)
            .coerceAtLeast(currentPositionMs)
        val delayMs = (prewarmPositionMs - currentPositionMs).coerceAtLeast(0L)
        handler.postDelayed(prewarmRunnable, delayMs)
        if (token != transitionToken) handler.removeCallbacks(prewarmRunnable)
    }

    private fun prewarmIncoming(token: Long) {
        if (token != transitionToken || state != CrossfadeState.WaitingForPrewarm) return
        val outgoingPlayer = outgoing ?: return cancel()
        val transitionPlan = plan ?: return cancel()
        state = CrossfadeState.PreparingNext
        logDebug("prewarm position=${outgoingPlayer.currentPosition} fadeStart=${transitionPlan.fadeStartMs}")
        val secondary = try {
            createPlayer()
        } catch (_: RuntimeException) {
            fail("secondary_player_creation_failure")
            return
        }
        incoming = secondary
        secondary.volume = 0f
        secondary.repeatMode = outgoingPlayer.repeatMode
        secondary.shuffleModeEnabled = outgoingPlayer.shuffleModeEnabled
        try {
            secondary.setMediaItems(
                queue.map(Song::toPlaybackMediaItem),
                nextQueueIndex,
                transitionPlan.incomingMixInMs,
            )
        } catch (_: RuntimeException) {
            try {
                secondary.setMediaItems(queue.map(Song::toPlaybackMediaItem), nextQueueIndex, 0L)
            } catch (_: RuntimeException) {
                fail("secondary_media_item_setup_failure")
                return
            }
        }
        secondary.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (secondary !== incoming || token != transitionToken) return
                if (playbackState == Player.STATE_READY) {
                    incomingReady = true
                    scheduleFadeWhenReady(token)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (secondary === incoming && token == transitionToken) fail("secondary_player_error")
            }
        })
        secondary.prepare()
    }

    private fun scheduleFadeWhenReady(token: Long) {
        if (token != transitionToken || !incomingReady || state != CrossfadeState.PreparingNext) return
        val outgoingPlayer = outgoing ?: return cancel()
        val transitionPlan = plan ?: return cancel()
        val currentPositionMs = outgoingPlayer.currentPosition.coerceAtLeast(0L)
        val remainingMs = transitionPlan.outgoingMixOutMs - currentPositionMs
        if (remainingMs <= 0L) {
            fadeDurationMs = 1L
            state = CrossfadeState.Ready
            handler.post(startRunnable)
            return
        }
        fadeDurationMs = min(transitionPlan.fadeDurationMs.coerceAtLeast(1L), remainingMs)
        val delayMs = (remainingMs - fadeDurationMs).coerceAtLeast(0L)
        state = CrossfadeState.Ready
        handler.postDelayed(startRunnable, delayMs)
    }

    private fun startFade(token: Long) {
        val outgoingPlayer = outgoing ?: return cancel()
        val incomingPlayer = incoming ?: return cancel()
        if (
            token != transitionToken ||
            !incomingReady ||
            state != CrossfadeState.Ready ||
            !outgoingPlayer.isPlaying
        ) {
            return cancel()
        }
        state = CrossfadeState.Fading
        logDebug("fade duration=$fadeDurationMs position=${outgoingPlayer.currentPosition}")
        incomingPlayer.playWhenReady = true
        incomingPlayer.play()
        fadeStartedAtElapsedMs = SystemClock.elapsedRealtime()
        frameRunnable.run()
    }

    private fun promote() {
        val outgoingPlayer = outgoing ?: return cancel()
        val incomingPlayer = incoming ?: return cancel()
        state = CrossfadeState.PromotingNext
        transitionToken += 1L
        analysisJob = null
        handler.removeCallbacks(prewarmRunnable)
        handler.removeCallbacks(startRunnable)
        handler.removeCallbacks(frameRunnable)
        incomingPlayer.volume = incomingGain
        outgoing = null
        incoming = null
        incomingReady = false
        queue = emptyList()
        nextQueueIndex = -1
        outgoingSong = null
        incomingSong = null
        outgoingDurationMs = 0L
        currentCue = null
        plan = null
        logDebug("promote")
        onPromote(outgoingPlayer, incomingPlayer)
        state = CrossfadeState.Idle
    }

    private fun fail(reason: String) {
        if (state == CrossfadeState.Released) return
        state = CrossfadeState.Failed
        logDebug("fallback normal transition fallback_reason=$reason")
        cancel()
        onFailed()
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private companion object {
        const val FRAME_INTERVAL_MS = 16L
        const val TAG = "PlaybackCrossfade"
        val READY_STATES = setOf(
            CrossfadeState.Ready,
            CrossfadeState.Fading,
            CrossfadeState.PromotingNext,
        )
    }
}
