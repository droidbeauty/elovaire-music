package elovaire.music.droidbeauty.app.data.playback

internal data class AudioSinkRecoveryKey(
    val playbackRevision: Long,
    val routeGeneration: Long,
    val failureCategory: String,
)

internal class AudioSinkRecoveryGuard {
    private var claimedKey: AudioSinkRecoveryKey? = null
    private var softwareFallbackRouteGeneration: Long? = null

    fun claim(key: AudioSinkRecoveryKey): Boolean {
        if (claimedKey == key) return false
        claimedKey = key
        return true
    }

    fun reset() {
        claimedKey = null
    }

    fun activateSoftwareFallback(routeGeneration: Long) {
        softwareFallbackRouteGeneration = routeGeneration
    }

    fun isSoftwareFallbackActive(routeGeneration: Long): Boolean {
        return softwareFallbackRouteGeneration == routeGeneration
    }
}
