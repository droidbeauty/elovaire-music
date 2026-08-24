package elovaire.music.droidbeauty.app.core.performance

internal object ElovairePerformance {
    private var monitor: ElovaireJankMonitor? = null
    private val jankWindows = ArrayDeque<JankWindowSnapshot>()

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
        monitor?.putState(key, value)
    }

    fun removeState(key: String) {
        monitor?.removeState(key)
    }

    @Synchronized
    fun recordJankWindow(window: JankWindowSnapshot) {
        if (jankWindows.size == MAX_JANK_WINDOWS) jankWindows.removeFirst()
        jankWindows.addLast(window)
    }

    @Synchronized
    fun jankWindowSnapshot(): List<JankWindowSnapshot> = jankWindows.toList()

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
)
