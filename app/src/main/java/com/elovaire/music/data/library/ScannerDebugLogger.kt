package elovaire.music.droidbeauty.app.data.library

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport

internal object ScannerDebugLogger {
    fun logFilteredOutCandidate(
        candidate: AudioScanCandidate,
        reason: String,
    ) {
        if (!BuildConfig.DEBUG) return
        val label = candidate.displayName
            ?.takeIf { it.isNotBlank() }
            ?: candidate.title
            ?.takeIf { it.isNotBlank() }
            ?: "audio-${candidate.id}"
        Log.d(TAG, "Excluded $label: $reason")
    }

    fun logPlatformDependentCandidate(candidate: AudioScanCandidate) {
        if (!BuildConfig.DEBUG) return
        val capability = AudioFormatPolicy.capabilityForExtension(candidate.extension) ?: return
        if (capability.playbackSupport != PlaybackSupport.PlatformDependent) return
        val label = candidate.displayName?.takeIf(String::isNotBlank) ?: "audio-${candidate.id}"
        Log.d(TAG, "Included platform-dependent audio $label (${capability.displayName})")
    }

    private const val TAG = "LibraryAudioFilter"
}
