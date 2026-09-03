package elovaire.music.droidbeauty.app.core.performance

import android.util.Log
import android.view.Window
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import elovaire.music.droidbeauty.app.BuildConfig

internal class ElovaireJankMonitor private constructor(
    private val jankStats: JankStats,
    private val performanceState: PerformanceMetricsState,
) {
    private val attributionState = JankAttributionState()
    private var totalFrames = 0
    private var jankyFrames = 0
    private var worstFrameDurationMs = 0L
    private var lastJankState = ""
    private var currentScreen: String? = null
    private var currentInteraction: String? = null
    private var currentPlaybackState: String? = null
    private var currentLibraryWork: String? = null

    fun setTrackingEnabled(enabled: Boolean) {
        jankStats.isTrackingEnabled = enabled
        if (!enabled) {
            logAndReset("paused")
        }
    }

    fun putState(
        key: String,
        value: String,
    ) {
        if (attributionState.update(key, value)) {
            logAndReset("state_change")
        }
        performanceState.putState(key, value)
    }

    fun removeState(key: String) {
        if (attributionState.remove(key)) {
            logAndReset("state_change")
        }
        performanceState.removeState(key)
    }

    fun release() {
        setTrackingEnabled(false)
        ElovairePerformance.detach(this)
    }

    private fun onFrame(frameData: FrameData) {
        totalFrames += 1
        currentScreen = null
        currentInteraction = null
        currentPlaybackState = null
        currentLibraryWork = null
        frameData.states.forEach { state ->
            when (state.key) {
                "screen" -> currentScreen = state.value
                "interaction" -> currentInteraction = state.value
                "playback_state" -> currentPlaybackState = state.value
                "library_work" -> currentLibraryWork = state.value
            }
        }
        if (frameData.isJank) {
            jankyFrames += 1
            lastJankState = frameData.states
                .joinToString(separator = ",") { state -> "${state.key}=${state.value}" }
                .take(MAX_STATE_LOG_LENGTH)
        }
        worstFrameDurationMs = maxOf(worstFrameDurationMs, frameData.frameDurationUiNanos / NANOS_PER_MILLI)
        if (totalFrames >= LOG_WINDOW_FRAMES) {
            logAndReset("window")
        }
    }

    private fun logAndReset(reason: String) {
        if (totalFrames > 0) {
            ElovairePerformance.recordJankWindow(
                JankWindowSnapshot(
                    reason = reason,
                    screen = currentScreen,
                    interaction = currentInteraction,
                    playbackState = currentPlaybackState,
                    libraryWork = currentLibraryWork,
                    frameCount = totalFrames,
                    jankCount = jankyFrames,
                    worstFrameMs = worstFrameDurationMs,
                ),
            )
        }
        if (BuildConfig.DEBUG && totalFrames > 0 && jankyFrames > 0) {
            Log.d(
                TAG,
                "jank reason=$reason frames=$totalFrames janky=$jankyFrames worstMs=$worstFrameDurationMs state=$lastJankState",
            )
        }
        totalFrames = 0
        jankyFrames = 0
        worstFrameDurationMs = 0L
        lastJankState = ""
        currentScreen = null
        currentInteraction = null
        currentPlaybackState = null
        currentLibraryWork = null
    }

    companion object {
        fun start(window: Window): ElovaireJankMonitor {
            var monitor: ElovaireJankMonitor? = null
            val stats = JankStats.createAndTrack(window) { frameData ->
                monitor?.onFrame(frameData)
            }
            monitor = ElovaireJankMonitor(
                jankStats = stats,
                performanceState = requireNotNull(
                    PerformanceMetricsState.getHolderForHierarchy(window.decorView).state,
                ),
            )
            ElovairePerformance.attach(monitor)
            return monitor
        }

        private const val TAG = "ElovaireJank"
        private const val LOG_WINDOW_FRAMES = 240
        private const val MAX_STATE_LOG_LENGTH = 240
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}

internal class JankAttributionState {
    private val values = HashMap<String, String>()

    fun update(
        key: String,
        value: String,
    ): Boolean {
        if (key !in BOUNDARY_KEYS) return false
        val previous = values.put(key, value)
        return previous != null && previous != value
    }

    fun remove(key: String): Boolean {
        if (key !in BOUNDARY_KEYS) return false
        return values.remove(key) != null
    }

    private companion object {
        val BOUNDARY_KEYS = setOf(
            "screen",
            "interaction",
            "playback_state",
            "library_work",
        )
    }
}
