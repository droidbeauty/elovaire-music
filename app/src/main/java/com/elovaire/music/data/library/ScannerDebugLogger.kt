package elovaire.music.droidbeauty.app.data.library

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import java.util.Locale

internal object ScannerDebugLogger {
    @Volatile
    private var latestSnapshot: ScannerDiagnosticSnapshot? = null

    fun newDecisionMap(): ScannerDecisionMap {
        return ScannerDecisionMap(enabled = BuildConfig.DEBUG)
    }

    internal fun latestDiagnosticSnapshot(): ScannerDiagnosticSnapshot? = latestSnapshot

    fun logSourceFailure(failure: Throwable) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "Library source unavailable: type=${failure::class.simpleName ?: "Unknown"}")
    }

    fun logPlatformDependentCandidate(candidate: AudioScanCandidate) {
        if (!BuildConfig.DEBUG) return
        val capability = AudioFormatPolicy.capabilityForExtension(candidate.extension) ?: return
        if (capability.playbackSupport != PlaybackSupport.PlatformDependent) return
        Log.d(TAG, "Included platform-dependent audio candidate (${capability.displayName})")
    }

    private const val TAG = "LibraryAudioFilter"

    internal data class ScannerDiagnosticSnapshot(
        val projection: String?,
        val rawRows: Int,
        val durationZeroRows: Int,
        val isMusicTrueRows: Int,
        val isMusicFalseRows: Int,
        val isMusicUnknownRows: Int,
        val missingRelativePathRows: Int,
        val missingAbsolutePathRows: Int,
        val missingExtensionRows: Int,
        val preflightPassedRows: Int,
        val mediaStoreIncluded: Int,
        val finalSongs: Int,
        val excludedByReason: Map<String, Int>,
    )

    internal class ScannerDecisionMap(
        private val enabled: Boolean,
    ) {
        private var mediaStoreRows = 0
        private var projection: String? = null
        private var durationZeroRows = 0
        private var isMusicTrueRows = 0
        private var isMusicFalseRows = 0
        private var isMusicUnknownRows = 0
        private var defaultMusicRelativeRows = 0
        private var missingRelativePathRows = 0
        private var missingAbsolutePathRows = 0
        private var missingExtensionRows = 0
        private var preflightPassedRows = 0
        private var mediaStoreIncluded = 0
        private var safIncluded = 0
        private var duplicateSafSongs = 0
        private var finalSongs = 0
        private val excludedByReason = linkedMapOf<String, Int>()

        fun recordProjection(kind: String) {
            if (enabled) projection = kind
        }

        fun recordMediaStoreRow(candidate: AudioScanCandidate, rawDurationMs: Long) {
            if (!enabled) return
            mediaStoreRows += 1
            if (rawDurationMs <= 0L) durationZeroRows += 1
            when (candidate.isMusic) {
                true -> isMusicTrueRows += 1
                false -> isMusicFalseRows += 1
                null -> isMusicUnknownRows += 1
            }
            if (candidate.extension.isNullOrBlank()) missingExtensionRows += 1
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

        fun recordPreflightPassed() {
            if (enabled) preflightPassedRows += 1
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

        fun logSummary(finalSongCount: Int) {
            if (!enabled) return
            latestSnapshot = ScannerDiagnosticSnapshot(
                projection = projection,
                rawRows = mediaStoreRows,
                durationZeroRows = durationZeroRows,
                isMusicTrueRows = isMusicTrueRows,
                isMusicFalseRows = isMusicFalseRows,
                isMusicUnknownRows = isMusicUnknownRows,
                missingRelativePathRows = missingRelativePathRows,
                missingAbsolutePathRows = missingAbsolutePathRows,
                missingExtensionRows = missingExtensionRows,
                preflightPassedRows = preflightPassedRows,
                mediaStoreIncluded = mediaStoreIncluded,
                finalSongs = finalSongCount,
                excludedByReason = excludedByReason.toMap(),
            )
            val reasons = excludedByReason.entries.joinToString(
                prefix = "{",
                postfix = "}",
            ) { (reason, count) -> "$reason=$count" }
            Log.d(
                TAG,
                "Scan decision map: projection=$projection, mediaStoreRows=$mediaStoreRows, " +
                    "durationZeroRows=$durationZeroRows, isMusic=true:$isMusicTrueRows/" +
                    "false:$isMusicFalseRows/unknown:$isMusicUnknownRows, " +
                    "defaultMusicRelativeRows=$defaultMusicRelativeRows, " +
                    "missingRelativePathRows=$missingRelativePathRows, " +
                    "missingAbsolutePathRows=$missingAbsolutePathRows, " +
                    "missingExtensionRows=$missingExtensionRows, " +
                    "preflightPassedRows=$preflightPassedRows, " +
                    "mediaStoreIncluded=$mediaStoreIncluded, " +
                    "safIncluded=$safIncluded, duplicateSafSongs=$duplicateSafSongs, " +
                    "finalSongs=$finalSongCount, excludedByReason=$reasons",
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
