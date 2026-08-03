package elovaire.music.droidbeauty.app.data.update

internal class UpdateDownloadProgressThrottler(
    private val minimumProgressDelta: Float = 0.01f,
    private val minimumIntervalMs: Long = 150L,
) {
    private var lastProgress = 0f
    private var lastUpdateMs = -minimumIntervalMs

    fun shouldEmit(progress: Float, nowMs: Long): Boolean {
        val normalized = progress.coerceIn(0f, 1f)
        if (normalized < lastProgress) return false
        if (normalized >= 1f || normalized - lastProgress >= minimumProgressDelta || nowMs - lastUpdateMs >= minimumIntervalMs) {
            lastProgress = normalized
            lastUpdateMs = nowMs
            return true
        }
        return false
    }
}
