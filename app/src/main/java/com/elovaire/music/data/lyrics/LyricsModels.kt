package elovaire.music.droidbeauty.app.data.lyrics

enum class SyncedLyricsTimingProfile {
    ExactIntervals,
}

enum class LyricsLookupMode {
    FastPresenceCheck,
    Full,
}

data class LyricsIdentity(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaId: String?,
    val contentUri: String?,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val normalizedLookupKey: String,
    val cacheKeys: List<String>,
)

data class LyricsQueryVariant(
    val artist: String,
    val title: String,
    val album: String? = null,
)

data class LyricsSearchQuery(
    val identity: LyricsIdentity,
    val variants: List<LyricsQueryVariant>,
)

internal data class LrcLibResponse(
    val id: Int,
    val name: String,
    val artistName: String,
    val albumName: String?,
    val duration: Double,
    val plainLyrics: String?,
    val syncedLyrics: String?,
    val instrumental: Boolean = false,
)

internal data class RankedLyricsCandidate(
    val response: LrcLibResponse,
    val score: Int,
)

data class LyricsCandidate(
    val providerId: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long?,
    val instrumental: Boolean,
    val plainLyrics: String,
    val syncedLyrics: String,
    val sourceUrl: String? = null,
)

data class LyricsLine(
    val text: String,
    val startTimeMs: Long?,
    val endTimeMs: Long? = null,
    val index: Int = 0,
)

data class LyricsPayload(
    val lines: List<LyricsLine>,
    val isSynced: Boolean,
    val displayTimingOffsetMs: Long = 0L,
    val timingScale: Float = 1f,
    val timingProfile: SyncedLyricsTimingProfile = SyncedLyricsTimingProfile.ExactIntervals,
    val providerName: String? = null,
    val confidence: Int = 0,
    val sourceTextForEmbedding: String? = null,
) {
    fun currentLineIndexAt(
        positionMs: Long,
        timingOffsetMs: Long = 0L,
        switchGraceMs: Long = 180L,
    ): Int? = currentLineIndexAtFast(positionMs, timingOffsetMs, switchGraceMs)

    fun currentLineIndexAtFast(
        positionMs: Long,
        timingOffsetMs: Long = 0L,
        switchGraceMs: Long = 120L,
    ): Int? {
        if (!isSynced || lines.isEmpty()) return null
        val correctedPositionMs = correctedLyricPositionMs(
            positionMs = positionMs,
            timingOffsetMs = timingOffsetMs,
            switchGraceMs = switchGraceMs,
        )
        if (correctedPositionMs < 0L) return null

        var low = 0
        var high = lines.lastIndex
        var result = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val startTimeMs = lines[mid].startTimeMs ?: Long.MAX_VALUE
            if (startTimeMs <= correctedPositionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        if (result < 0) return null
        val endTimeMs = lines[result].endTimeMs
        if (endTimeMs != null && correctedPositionMs > endTimeMs + 500L) return null
        return result
    }

    fun currentLineAt(
        positionMs: Long,
        timingOffsetMs: Long = 0L,
        switchGraceMs: Long = 180L,
    ): LyricsLine? = currentLineIndexAt(positionMs, timingOffsetMs, switchGraceMs)?.let(lines::get)

    private fun correctedLyricPositionMs(
        positionMs: Long,
        timingOffsetMs: Long,
        switchGraceMs: Long,
    ): Long {
        val delayedPositionMs = positionMs - displayTimingOffsetMs - timingOffsetMs - switchGraceMs
        if (timingScale == 1f) return delayedPositionMs
        return (delayedPositionMs / timingScale).toLong()
    }
}

sealed interface LyricsResult {
    data class Found(val payload: LyricsPayload) : LyricsResult
    data object NotFound : LyricsResult
    data object Timeout : LyricsResult
}

internal enum class LyricsLookupState {
    Idle,
    Loading,
    FoundSynced,
    FoundUnsynced,
    NotFound,
    Error,
}

internal data class LyricsLookupOutcome(
    val result: LyricsResult,
    val cacheTtlMs: Long?,
    val state: LyricsLookupState,
    val providerName: String? = null,
    val confidence: Int = 0,
)

internal data class LyricsCacheEntry(
    val result: LyricsResult,
    val expiresAtMillis: Long,
    val providerName: String? = null,
    val confidence: Int = 0,
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean = nowMillis >= expiresAtMillis
}

internal data class ProviderLyricsMatch(
    val payload: LyricsPayload,
    val confidence: Int,
    val providerName: String,
)

internal data class LocalLyricsMatch(
    val payload: LyricsPayload,
)
