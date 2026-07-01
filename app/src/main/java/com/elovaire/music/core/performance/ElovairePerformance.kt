package elovaire.music.droidbeauty.app.core.performance

internal object ElovairePerformance {
    private var monitor: ElovaireJankMonitor? = null

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
}
