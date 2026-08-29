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

    internal fun recordSafSummary(
        treeCount: Int,
        validGrantCount: Int,
        discoveredSongCount: Int,
        incompleteTreeCount: Int,
        mergedSongCount: Int,
    ) {
        if (!BuildConfig.DEBUG) return
        latestSnapshot = latestSnapshot?.copy(
            safTreeCount = treeCount,
            safValidGrantCount = validGrantCount,
            safDiscoveredSongCount = discoveredSongCount,
            safIncompleteTreeCount = incompleteTreeCount,
            mergedSongCount = mergedSongCount,
        )
        Log.d(
            TAG,
            "SAF scan: trees=$treeCount, validGrants=$validGrantCount, " +
                "discovered=$discoveredSongCount, incomplete=$incompleteTreeCount, " +
                "merged=$mergedSongCount",
        )
    }

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
        val indexRefresh: String?,
        val folderMetadataUnavailable: Boolean,
        val safTreeCount: Int,
        val safValidGrantCount: Int,
        val safDiscoveredSongCount: Int,
        val safIncompleteTreeCount: Int,
        val mergedSongCount: Int,
        val rawRows: Int,
        val durationZeroRows: Int,
        val isMusicTrueRows: Int,
        val isMusicFalseRows: Int,
        val isMusicUnknownRows: Int,
        val defaultMusicRelativeRows: Int,
        val missingRelativePathRows: Int,
        val missingAbsolutePathRows: Int,
        val missingExtensionRows: Int,
        val preflightPassedRows: Int,
        val mediaStoreIncluded: Int,
        val mediaStoreGenreLookups: Int,
        val finalSongs: Int,
        val excludedByReason: Map<String, Int>,
    )

    internal class ScannerDecisionMap(
        private val enabled: Boolean,
    ) {
        private var mediaStoreRows = 0
        private var projection: String? = null
        private var indexRefresh: String? = null
        private var folderMetadataUnavailable = false
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
        private var mediaStoreGenreLookups = 0
        private var safIncluded = 0
        private var duplicateSafSongs = 0
        private var finalSongs = 0
        private val excludedByReason = linkedMapOf<String, Int>()

        fun recordProjection(kind: String) {
            if (enabled) projection = kind
        }

        fun recordIndexRefresh(result: MediaStoreIndexRefreshResult) {
            if (!enabled) return
            indexRefresh = when (result) {
                MediaStoreIndexRefreshResult.Complete -> "Complete"
                is MediaStoreIndexRefreshResult.Partial -> "Partial:${result.timedOutChunks}"
                is MediaStoreIndexRefreshResult.Unavailable ->
                    "Unavailable:${result.failure::class.simpleName ?: "Unknown"}"
            }
        }

        fun recordIndexRefreshFailure(failure: Throwable) {
            if (enabled) indexRefresh = "Failure:${failure::class.simpleName ?: "Unknown"}"
        }

        fun recordFolderMetadataUnavailable() {
            if (enabled) folderMetadataUnavailable = true
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

        fun recordMediaStoreGenreLookup() {
            if (enabled) mediaStoreGenreLookups += 1
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
                indexRefresh = indexRefresh,
                folderMetadataUnavailable = folderMetadataUnavailable,
                safTreeCount = 0,
                safValidGrantCount = 0,
                safDiscoveredSongCount = 0,
                safIncompleteTreeCount = 0,
                mergedSongCount = finalSongCount,
                rawRows = mediaStoreRows,
                durationZeroRows = durationZeroRows,
                isMusicTrueRows = isMusicTrueRows,
                isMusicFalseRows = isMusicFalseRows,
                isMusicUnknownRows = isMusicUnknownRows,
                defaultMusicRelativeRows = defaultMusicRelativeRows,
                missingRelativePathRows = missingRelativePathRows,
                missingAbsolutePathRows = missingAbsolutePathRows,
                missingExtensionRows = missingExtensionRows,
                preflightPassedRows = preflightPassedRows,
                mediaStoreIncluded = mediaStoreIncluded,
                mediaStoreGenreLookups = mediaStoreGenreLookups,
                finalSongs = finalSongCount,
                excludedByReason = excludedByReason.toMap(),
            )
            val reasons = excludedByReason.entries.joinToString(
                prefix = "{",
                postfix = "}",
            ) { (reason, count) -> "$reason=$count" }
            Log.d(
                TAG,
                "Scan decision map: projection=$projection, indexRefresh=$indexRefresh, " +
                    "folderMetadataUnavailable=$folderMetadataUnavailable, " +
                    "mediaStoreRows=$mediaStoreRows, " +
                    "durationZeroRows=$durationZeroRows, isMusic=true:$isMusicTrueRows/" +
                    "false:$isMusicFalseRows/unknown:$isMusicUnknownRows, " +
                    "defaultMusicRelativeRows=$defaultMusicRelativeRows, " +
                    "missingRelativePathRows=$missingRelativePathRows, " +
                    "missingAbsolutePathRows=$missingAbsolutePathRows, " +
                    "missingExtensionRows=$missingExtensionRows, " +
                    "preflightPassedRows=$preflightPassedRows, " +
                    "mediaStoreIncluded=$mediaStoreIncluded, " +
                    "mediaStoreGenreLookups=$mediaStoreGenreLookups, " +
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
