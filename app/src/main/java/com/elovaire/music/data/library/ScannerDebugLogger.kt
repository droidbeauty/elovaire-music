package elovaire.music.droidbeauty.app.data.library

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import java.util.Locale

internal object ScannerDebugLogger {
    fun newDecisionMap(): ScannerDecisionMap {
        return ScannerDecisionMap(enabled = BuildConfig.DEBUG)
    }

    fun logPlatformDependentCandidate(candidate: AudioScanCandidate) {
        if (!BuildConfig.DEBUG) return
        val capability = AudioFormatPolicy.capabilityForExtension(candidate.extension) ?: return
        if (capability.playbackSupport != PlaybackSupport.PlatformDependent) return
        Log.d(TAG, "Included platform-dependent audio candidate (${capability.displayName})")
    }

    private const val TAG = "LibraryAudioFilter"

    internal class ScannerDecisionMap(
        private val enabled: Boolean,
    ) {
        private var mediaStoreRows = 0
        private var defaultMusicRelativeRows = 0
        private var missingRelativePathRows = 0
        private var missingAbsolutePathRows = 0
        private var mediaStoreIncluded = 0
        private var safIncluded = 0
        private var duplicateSafSongs = 0
        private var finalSongs = 0
        private val excludedByReason = linkedMapOf<String, Int>()

        fun recordMediaStoreRow(candidate: AudioScanCandidate) {
            if (!enabled) return
            mediaStoreRows += 1
            val relativePath = candidate.relativePath.normalizeRelativePath()
            if (relativePath == null) {
                missingRelativePathRows += 1
            } else if (relativePath == "music" || relativePath.startsWith("music/")) {
                defaultMusicRelativeRows += 1
            }
            if (candidate.absolutePath.isNullOrBlank()) {
                missingAbsolutePathRows += 1
            }
        }

        fun recordMediaStoreInclude() {
            if (enabled) mediaStoreIncluded += 1
        }

        fun recordMediaStoreExclude(reason: String) {
            if (!enabled) return
            excludedByReason[reason] = (excludedByReason[reason] ?: 0) + 1
        }

        fun recordSafIncluded(count: Int) {
            if (enabled) safIncluded = count
        }

        fun recordMerge(
            mediaStoreSongCount: Int,
            safSongCount: Int,
            mergedSongCount: Int,
        ) {
            if (!enabled) return
            duplicateSafSongs = (mediaStoreSongCount + safSongCount - mergedSongCount).coerceAtLeast(0)
            finalSongs = mergedSongCount
        }

        fun logSummary() {
            if (!enabled) return
            val reasons = excludedByReason.entries.joinToString(
                prefix = "{",
                postfix = "}",
            ) { (reason, count) -> "$reason=$count" }
            Log.d(
                TAG,
                "Scan decision map: mediaStoreRows=$mediaStoreRows, " +
                    "defaultMusicRelativeRows=$defaultMusicRelativeRows, " +
                    "missingRelativePathRows=$missingRelativePathRows, " +
                    "missingAbsolutePathRows=$missingAbsolutePathRows, " +
                    "mediaStoreIncluded=$mediaStoreIncluded, " +
                    "safIncluded=$safIncluded, duplicateSafSongs=$duplicateSafSongs, " +
                    "finalSongs=$finalSongs, excludedByReason=$reasons",
            )
        }

        private fun String?.normalizeRelativePath(): String? {
            return this
                ?.trim()
                ?.replace('\\', '/')
                ?.trim('/')
                ?.lowercase(Locale.ROOT)
                ?.takeIf { it.isNotBlank() }
        }
    }
}
