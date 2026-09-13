package elovaire.music.droidbeauty.app.data.playback

internal data class AudioSinkRecoveryKey(
    val playbackRevision: Long,
    val routeGeneration: Long,
    val failureCategory: String,
)

internal class AudioSinkRecoveryGuard {
    private var claimedKey: AudioSinkRecoveryKey? = null

    fun claim(key: AudioSinkRecoveryKey): Boolean {
        if (claimedKey == key) return false
        claimedKey = key
        return true
    }

    fun reset() {
        claimedKey = null
    }
}
