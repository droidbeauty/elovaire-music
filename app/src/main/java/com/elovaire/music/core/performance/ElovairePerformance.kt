package elovaire.music.droidbeauty.app.core.performance

internal object ElovairePerformance {
    private var monitor: ElovaireJankMonitor? = null
    private val jankWindows = ArrayDeque<JankWindowSnapshot>()
    private var jankSequence = 0L

    fun attach(jankMonitor: ElovaireJankMonitor) {
        monitor = jankMonitor
    }

    fun detach(jankMonitor: ElovaireJankMonitor) {
        if (monitor === jankMonitor) {
            monitor = null
        }
    }

    fun putState(
        key: String,
        value: String,
    ) {
        if (key == "interaction") MotionDiagnosticRecorder.recordInteractionState(value)
        monitor?.putState(key, value)
    }

    fun removeState(key: String) {
        if (key == "interaction") MotionDiagnosticRecorder.recordInteractionState(null)
        monitor?.removeState(key)
    }

    @Synchronized
    fun recordJankWindow(window: JankWindowSnapshot) {
        if (jankWindows.size == MAX_JANK_WINDOWS) jankWindows.removeFirst()
        jankWindows.addLast(window.copy(sequence = ++jankSequence))
    }

    @Synchronized
    fun jankWindowSnapshot(): List<JankWindowSnapshot> = jankWindows.toList()

    @Synchronized
    fun jankSequenceMarker(): Long = jankSequence

    @Synchronized
    fun jankWindowsAfter(marker: Long): List<JankWindowSnapshot> =
        jankWindows.filter { it.sequence > marker }

    @Synchronized
    fun clearJankWindows() {
        jankWindows.clear()
    }

    private const val MAX_JANK_WINDOWS = 16
}

internal data class JankWindowSnapshot(
    val reason: String,
    val screen: String?,
    val interaction: String?,
    val playbackState: String?,
    val libraryWork: String?,
    val frameCount: Int,
    val jankCount: Int,
    val worstFrameMs: Long,
    val sequence: Long = 0L,
)
