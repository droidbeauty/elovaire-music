package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock

internal class ScannerProgressEmitter(
    private val onProgress: ((current: Int, total: Int) -> Unit)?,
    clock: AppClock = AndroidAppClock,
) {
    private val throttler = ScannerProgressThrottler(clock)

    fun emit(
        current: Int,
        total: Int,
    ) {
        if (onProgress == null) return
        val safeTotal = total.coerceAtLeast(1)
        val progress = (current.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
        if (throttler.shouldEmit(progress)) {
            onProgress.invoke(current, total)
        }
    }
}

private class ScannerProgressThrottler(
    private val clock: AppClock,
    private val minStep: Float = 0.01f,
    private val minIntervalMs: Long = 80L,
) {
    private var lastProgress = -1f
    private var lastEmitMs = 0L

    fun shouldEmit(progress: Float): Boolean {
        val now = clock.elapsedTimeMs()
        if (progress >= 1f) return true
        if (lastProgress < 0f) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        val enoughProgress = progress - lastProgress >= minStep
        val enoughTime = now - lastEmitMs >= minIntervalMs
        if (enoughProgress || enoughTime) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        return false
    }
}
