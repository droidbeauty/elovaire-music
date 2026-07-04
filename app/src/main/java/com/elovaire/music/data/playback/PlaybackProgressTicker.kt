package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class PlaybackProgressTicker(
    private val scope: CoroutineScope,
    private val intervalMs: () -> Long,
    private val onTick: () -> Boolean,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            try {
                while (isActive) {
                    val shouldContinue = onTick()
                    if (!shouldContinue) break
                    delay(intervalMs().coerceAtLeast(MIN_INTERVAL_MS))
                }
            } finally {
                if (job === this) {
                    job = null
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun release() {
        stop()
    }

    private companion object {
        const val MIN_INTERVAL_MS = 50L
    }
}
