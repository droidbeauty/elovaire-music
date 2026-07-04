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
        Log.d(TAG, "Excluded audio candidate: $reason")
    }

    fun logPlatformDependentCandidate(candidate: AudioScanCandidate) {
        if (!BuildConfig.DEBUG) return
        val capability = AudioFormatPolicy.capabilityForExtension(candidate.extension) ?: return
        if (capability.playbackSupport != PlaybackSupport.PlatformDependent) return
        Log.d(TAG, "Included platform-dependent audio candidate (${capability.displayName})")
    }

    private const val TAG = "LibraryAudioFilter"
}
