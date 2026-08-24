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
    private var totalFrames = 0
    private var jankyFrames = 0
    private var worstFrameDurationMs = 0L
    private var lastJankState = ""
    private var currentStates: Map<String, String> = emptyMap()

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
        performanceState.putState(key, value)
    }

    fun removeState(key: String) {
        performanceState.removeState(key)
    }

    fun release() {
        setTrackingEnabled(false)
        ElovairePerformance.detach(this)
    }

    private fun onFrame(frameData: FrameData) {
        totalFrames += 1
        currentStates = frameData.states.associate { state -> state.key to state.value }
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
                    screen = currentStates["screen"],
                    interaction = currentStates["interaction"],
                    playbackState = currentStates["playback_state"],
                    libraryWork = currentStates["library_work"],
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
        currentStates = emptyMap()
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
