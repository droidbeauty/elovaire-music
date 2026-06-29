package elovaire.music.droidbeauty.app.data.library

import android.os.SystemClock

internal class ScannerProgressEmitter(
    private val onProgress: ((current: Int, total: Int) -> Unit)?,
) {
    private val throttler = ScannerProgressThrottler()

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
    private val minStep: Float = 0.01f,
    private val minIntervalMs: Long = 80L,
) {
    private var lastProgress = -1f
    private var lastEmitMs = 0L

    fun shouldEmit(progress: Float): Boolean {
        val now = SystemClock.elapsedRealtime()
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
