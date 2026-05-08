package elovaire.music.app.data.lyrics

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.MediaStore
import android.util.Log
import elovaire.music.app.BuildConfig
import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val DEFAULT_LYRIC_SWITCH_GRACE_MS = 350L
private const val MIN_SONG_DURATION_FOR_REMOTE_OPENING_FIX_MS = 90_000L
private const val REMOTE_MIN_SYNCED_TIMELINE_COVERAGE = 0.52f
private const val REMOTE_MIN_HEALTHY_MEDIAN_GAP_MS = 950L

enum class SyncedLyricsTimingProfile {
    Approximate,
    ExactIntervals,
}

data class LyricsLine(
    val text: String,
    val startTimeMs: Long?,
    val endTimeMs: Long? = null,
)

data class LyricsPayload(
    val lines: List<LyricsLine>,
    val isSynced: Boolean,
    /**
     * Positive values intentionally delay lyric highlighting without changing the underlying
     * timestamps used for seeking. This is mainly for remote synced lyrics whose timestamps are
     * consistently earlier than the actual audio in the local file.
     */
    val displayTimingOffsetMs: Long = 0L,
    val timingProfile: SyncedLyricsTimingProfile = SyncedLyricsTimingProfile.Approximate,
) {
    /**
     * Returns the lyric line that should be highlighted at [positionMs].
     *
     * Use this instead of a raw "last timestamp <= position" lookup in the UI. It intentionally
     * applies the provider-specific display delay and waits a tiny grace period before switching
     * lines so the highlight does not run ahead of the vocal. It returns null before the first
     * corrected timed line instead of activating the first lyric during an instrumental intro.
     */
    fun currentLineIndexAt(
        positionMs: Long,
        timingOffsetMs: Long = 0L,
        switchGraceMs: Long = DEFAULT_LYRIC_SWITCH_GRACE_MS,
    ): Int? {
        if (!isSynced || lines.isEmpty()) return null
        val correctedPosition = positionMs - timingOffsetMs - displayTimingOffsetMs
        if (correctedPosition < 0L) return null
        val timedLineIndexes = lines.indices.filter { lines[it].startTimeMs != null }
        if (timedLineIndexes.isEmpty()) return null

        var low = 0
        var high = timedLineIndexes.lastIndex
        var timedResult = -1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val lineIndex = timedLineIndexes[mid]
            val start = lines[lineIndex].startTimeMs ?: Long.MAX_VALUE
            if (start <= correctedPosition) {
                timedResult = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        if (timedResult < 0) return null

        val candidateIndex = timedLineIndexes[timedResult]
        val candidate = lines[candidateIndex]
        val candidateStart = candidate.startTimeMs ?: return null
        val nextTimedIndex = timedLineIndexes.getOrNull(timedResult + 1)
        val nextStart = nextTimedIndex?.let { lines[it].startTimeMs }

        return when (timingProfile) {
            SyncedLyricsTimingProfile.ExactIntervals -> {
                val candidateEnd = candidate.endTimeMs ?: nextStart
                if (candidateEnd != null && correctedPosition >= candidateEnd) {
                    if (nextStart != null && correctedPosition < nextStart) {
                        null
                    } else {
                        nextTimedIndex ?: candidateIndex
                    }
                } else {
                    candidateIndex
                }
            }

            SyncedLyricsTimingProfile.Approximate -> {
                val delayedSwitchBoundary = candidateStart + adaptiveSwitchDelayMs(
                    currentStartMs = candidateStart,
                    nextStartMs = nextStart,
                    fallbackGraceMs = switchGraceMs,
                )
                if (candidateIndex == timedLineIndexes.first() && correctedPosition < delayedSwitchBoundary) {
                    null
                } else if (candidateIndex != timedLineIndexes.first() && correctedPosition < delayedSwitchBoundary) {
                    timedLineIndexes.getOrNull(timedResult - 1)
                } else {
                    candidateIndex
                }
            }
        }
    }

    fun currentLineAt(
        positionMs: Long,
        timingOffsetMs: Long = 0L,
        switchGraceMs: Long = DEFAULT_LYRIC_SWITCH_GRACE_MS,
    ): LyricsLine? = currentLineIndexAt(positionMs, timingOffsetMs, switchGraceMs)?.let(lines::get)
}

private fun adaptiveSwitchDelayMs(
    currentStartMs: Long,
    nextStartMs: Long?,
    fallbackGraceMs: Long,
): Long {
    val gapMs = nextStartMs?.minus(currentStartMs)?.coerceAtLeast(0L) ?: Long.MAX_VALUE
    return when {
        gapMs <= 650L -> 12L
        gapMs <= 1_050L -> 24L
        gapMs <= 1_700L -> 36L
        gapMs <= 2_600L -> 48L
        else -> fallbackGraceMs.coerceIn(24L, 60L)
    }
}

sealed interface LyricsResult {
    data class Found(val payload: LyricsPayload) : LyricsResult
    data object NotFound : LyricsResult
}

internal enum class LyricsLookupState {
    Idle,
    LoadingLocal,
    LoadingRemote,
    FoundSyncedLyrics,
    FoundPlainLyrics,
    NotFound,
    ErrorRecoverable,
    ErrorPermanent,
}

internal enum class LyricsSourceKind {
    Cache,
    EmbeddedSynced,
    EmbeddedPlain,
    SidecarLrc,
    SidecarText,
    RemoteLrcLibSynced,
    RemoteLrcLibLyrics,
    Offline,
    Timeout,
    NotFound,
    Error,
}

internal data class LyricsLookupOutcome(
    val result: LyricsResult,
    val cacheTtlMs: Long?,
    val source: LyricsSourceKind,
    val state: LyricsLookupState,
    val confidence: Int = 0,
)

internal data class LyricsQueryVariant(
    val artist: String,
    val title: String,
    val album: String? = null,
)

internal data class LyricsDebugMetrics(
    val cacheKey: String,
    val localLookupMs: Long,
    val remoteLookupMs: Long,
    val totalLookupMs: Long,
    val source: LyricsSourceKind,
    val cacheHit: Boolean,
)

class LyricsService(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val cacheLock = Any()
    private val cache = object : LinkedHashMap<String, CachedLyricsEntry>(MAX_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, CachedLyricsEntry>?,
        ): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<LyricsLookupOutcome>>()
    private val serviceScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean = true,
    ): LyricsResult? = synchronized(cacheLock) {
        val key = buildCacheKey(song)
        val entry = cache[key] ?: return@synchronized null
        if (entry.isExpired()) {
            cache.remove(key)
            null
        } else if (!includeNotFound && entry.result == LyricsResult.NotFound) {
            null
        } else {
            entry.result
        }
    }

    fun isLookupInFlight(song: Song): Boolean = inFlightRequests.containsKey(buildCacheKey(song))

    fun prefetchLyrics(song: Song) {
        if (cachedLyrics(song, includeNotFound = false) != null || inFlightRequests.containsKey(buildCacheKey(song))) return
        serviceScope.launch {
            fetchLyrics(song, allowCachedNotFound = false)
        }
    }

    fun cancelObsoleteRequests(keepSongs: List<Song?>) {
        val keepKeys = keepSongs.filterNotNull().mapTo(mutableSetOf(), ::buildCacheKey)
        inFlightRequests.entries.removeIf { (key, request) ->
            val obsolete = key !in keepKeys
            if (obsolete) {
                request.cancel()
            }
            obsolete
        }
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean = true,
    ): LyricsResult = coroutineScope {
        val cacheKey = buildCacheKey(song)
        cachedLyrics(song, includeNotFound = allowCachedNotFound)?.let {
            logMetrics(
                LyricsDebugMetrics(
                    cacheKey = cacheKey,
                    localLookupMs = 0L,
                    remoteLookupMs = 0L,
                    totalLookupMs = 0L,
                    source = LyricsSourceKind.Cache,
                    cacheHit = true,
                ),
            )
            return@coroutineScope it
        }

        val existingRequest = inFlightRequests[cacheKey]
        if (existingRequest != null) {
            return@coroutineScope existingRequest.await().result
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song, cacheKey)
            }.getOrElse { throwable ->
                logDebug("Lyrics lookup failed for ${song.artist} - ${song.title}", throwable)
                LyricsLookupOutcome(
                    result = LyricsResult.NotFound,
                    cacheTtlMs = ERROR_CACHE_TTL_MS,
                    source = LyricsSourceKind.Error,
                    state = LyricsLookupState.ErrorRecoverable,
                )
            }
        }
        val activeRequest = inFlightRequests.putIfAbsent(cacheKey, request) ?: request
        if (activeRequest !== request) {
            request.cancel()
        }

        try {
            val outcome = activeRequest.await()
            synchronized(cacheLock) {
                cache[cacheKey] = CachedLyricsEntry(
                    result = outcome.result,
                    expiresAtMillis = outcome.cacheTtlMs?.let { System.currentTimeMillis() + it } ?: Long.MAX_VALUE,
                )
            }
            outcome.result
        } finally {
            inFlightRequests.remove(cacheKey, activeRequest)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun resolveLyrics(
        song: Song,
        cacheKey: String,
    ): LyricsLookupOutcome = coroutineScope {
        val startedAt = System.currentTimeMillis()
        if (!isNetworkAvailable()) {
            val localStartedAt = startedAt
            val local = withContext(ioDispatcher) { resolveLocalLyrics(song) }
            val localLookupMs = System.currentTimeMillis() - localStartedAt
            if (local != null) {
                val totalLookupMs = System.currentTimeMillis() - startedAt
                logMetrics(
                    LyricsDebugMetrics(
                        cacheKey = cacheKey,
                        localLookupMs = localLookupMs,
                        remoteLookupMs = 0L,
                        totalLookupMs = totalLookupMs,
                        source = local.source,
                        cacheHit = false,
                    ),
                )
                return@coroutineScope LyricsLookupOutcome(
                    result = LyricsResult.Found(local.payload),
                    cacheTtlMs = null,
                    source = local.source,
                    state = if (local.payload.isSynced) LyricsLookupState.FoundSyncedLyrics else LyricsLookupState.FoundPlainLyrics,
                    confidence = 100,
                )
            }
            val totalLookupMs = System.currentTimeMillis() - startedAt
            logMetrics(
                LyricsDebugMetrics(
                    cacheKey = cacheKey,
                    localLookupMs = localLookupMs,
                    remoteLookupMs = 0L,
                    totalLookupMs = totalLookupMs,
                    source = LyricsSourceKind.Offline,
                    cacheHit = false,
                ),
            )
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = OFFLINE_NOT_FOUND_CACHE_TTL_MS,
                source = LyricsSourceKind.Offline,
                state = LyricsLookupState.NotFound,
            )
        }

        val localStartedAt = startedAt
        val localDeferred = async(ioDispatcher) { resolveLocalLyrics(song) }
        val remoteStartedAt = System.currentTimeMillis()
        val remote = withTimeoutOrNull(REMOTE_LOOKUP_TOTAL_TIMEOUT_MS) {
            resolveRemoteLyrics(song)
        }
        val remoteLookupMs = System.currentTimeMillis() - remoteStartedAt
        val totalLookupMs = System.currentTimeMillis() - startedAt
        if (remote != null) {
            localDeferred.cancel()
            logMetrics(
                LyricsDebugMetrics(
                    cacheKey = cacheKey,
                    localLookupMs = 0L,
                    remoteLookupMs = remoteLookupMs,
                    totalLookupMs = totalLookupMs,
                    source = remote.source,
                    cacheHit = false,
                ),
            )
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(remote.payload),
                cacheTtlMs = null,
                source = remote.source,
                state = if (remote.payload.isSynced) LyricsLookupState.FoundSyncedLyrics else LyricsLookupState.FoundPlainLyrics,
                confidence = remote.confidence,
            )
        }

        val local = localDeferred.await()
        val localLookupMs = System.currentTimeMillis() - localStartedAt

        if (local != null) {
            logMetrics(
                LyricsDebugMetrics(
                    cacheKey = cacheKey,
                    localLookupMs = localLookupMs,
                    remoteLookupMs = remoteLookupMs,
                    totalLookupMs = totalLookupMs,
                    source = local.source,
                    cacheHit = false,
                ),
            )
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(local.payload),
                cacheTtlMs = null,
                source = local.source,
                state = if (local.payload.isSynced) LyricsLookupState.FoundSyncedLyrics else LyricsLookupState.FoundPlainLyrics,
                confidence = 100,
            )
        }

        val timeoutTriggered = remoteLookupMs >= REMOTE_LOOKUP_TOTAL_TIMEOUT_MS
        val source = if (timeoutTriggered) LyricsSourceKind.Timeout else LyricsSourceKind.NotFound
        logMetrics(
            LyricsDebugMetrics(
                cacheKey = cacheKey,
                localLookupMs = localLookupMs,
                remoteLookupMs = remoteLookupMs,
                totalLookupMs = totalLookupMs,
                source = source,
                cacheHit = false,
            ),
        )
        LyricsLookupOutcome(
            result = LyricsResult.NotFound,
            cacheTtlMs = if (timeoutTriggered) TIMEOUT_NOT_FOUND_CACHE_TTL_MS else NOT_FOUND_CACHE_TTL_MS,
            source = source,
            state = LyricsLookupState.NotFound,
        )
    }

    private suspend fun resolveRemoteLyrics(song: Song): RemoteLyricsMatch? = coroutineScope {
        withTimeoutOrNull(LRCLIB_LOOKUP_TIMEOUT_MS) {
            fetchLrcLibPayload(song)
        }?.let { return@coroutineScope it }
        null
    }

    private fun fetchLrcLibPayload(song: Song): RemoteLyricsMatch? {
        val queryVariants = buildLyricsQueryVariants(song).take(MAX_REMOTE_QUERY_VARIANTS)
        if (queryVariants.isEmpty()) return null

        val exactCandidates = queryVariants
            .mapNotNull { fetchLrcLibExactMatch(it, song) }
            .distinctBy(::lrcLibCandidateDistinctKey)

        val candidates = if (exactCandidates.isNotEmpty()) {
            exactCandidates
        } else {
            queryVariants
                .flatMap(::searchLrcLibTracks)
                .distinctBy(::lrcLibCandidateDistinctKey)
        }

        if (candidates.isEmpty()) return null

        val scoredCandidates = candidates
            .map { candidate -> candidate to candidate.scoreAgainst(song) }
            .sortedByDescending { (_, score) -> score }

        return scoredCandidates
            .filter { (candidate, score) -> candidate.isAcceptableMatchFor(song, score) }
            .ifEmpty {
                scoredCandidates.filter { (_, score) -> score >= LRCLIB_FALLBACK_MATCH_MIN_SCORE }
            }
            .take(MAX_LRCLIB_CANDIDATES)
            .mapNotNull { (candidate, baseScore) ->
                candidate.toRemoteLyricsMatch(song, baseScore)
            }
            .maxWithOrNull(
                compareBy<RemoteLyricsMatch> { it.confidence }
                    .thenBy { if (it.payload.isSynced) 1 else 0 },
            )
    }

    private fun fetchLrcLibExactMatch(
        query: LyricsQueryVariant,
        song: Song,
    ): LrcLibTrackCandidate? {
        val response = getJsonObject(
            url = buildUrl(
                LRCLIB_GET_URL,
                linkedMapOf<String, String>().apply {
                    put("track_name", query.title)
                    put("artist_name", query.artist)
                    query.album?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
                    durationSeconds(song.durationMs)?.takeIf { it > 0L }?.let { put("duration", it.toString()) }
                },
            ),
            connectTimeoutMs = LRCLIB_CONNECT_TIMEOUT_MS,
            readTimeoutMs = LRCLIB_READ_TIMEOUT_MS,
        ) ?: return null
        return response.toLrcLibCandidate()
    }

    private fun searchLrcLibTracks(query: LyricsQueryVariant): List<LrcLibTrackCandidate> {
        val response = getJsonArray(
            url = buildUrl(
                LRCLIB_SEARCH_URL,
                linkedMapOf<String, String>().apply {
                    put("track_name", query.title)
                    put("artist_name", query.artist)
                    query.album?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
                },
            ),
            connectTimeoutMs = LRCLIB_CONNECT_TIMEOUT_MS,
            readTimeoutMs = LRCLIB_READ_TIMEOUT_MS,
        ) ?: return emptyList()

        return buildList {
            for (index in 0 until response.length()) {
                response.optJSONObject(index)?.toLrcLibCandidate()?.let(::add)
            }
        }
    }

    private suspend fun resolveLocalLyrics(song: Song): LocalLyricsMatch? = coroutineScope {
        val embeddedDeferred = async(ioDispatcher) { readEmbeddedLyrics(song) }
        val sidecarDeferred = async(ioDispatcher) { readSidecarLyrics(song) }

        val embedded = embeddedDeferred.await()
        embedded?.let { return@coroutineScope it }

        sidecarDeferred.await()
    }

    private fun readEmbeddedLyrics(song: Song): LocalLyricsMatch? {
        val headerBytes = contentResolver.openInputStream(song.uri)?.use { input ->
            input.readNBytes(4)
        } ?: return null

        return when {
            headerBytes.startsWithAscii("ID3") -> readId3Lyrics(song)
            headerBytes.startsWithAscii("fLaC") -> readFlacLyrics(song)
            else -> null
        }
    }

    private fun readId3Lyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val header = input.readNBytes(10)
            if (header.size < 10 || !header.copyOfRange(0, 3).startsWithAscii("ID3")) {
                return@use null
            }
            val majorVersion = header[3].toInt() and 0xFF
            val flags = header[5].toInt() and 0xFF
            val tagSize = synchsafeInt(header, 6)
            if (tagSize <= 0 || tagSize > MAX_EMBEDDED_TAG_BYTES) {
                return@use null
            }
            val tagData = input.readNBytes(tagSize)
            if (tagData.size < tagSize) {
                return@use null
            }
            val normalizedTagData = if ((flags and ID3_UNSYNCHRONIZATION_FLAG) != 0) {
                removeId3Unsynchronization(tagData)
            } else {
                tagData
            }
            parseId3Frames(normalizedTagData, majorVersion)
        }
    }

    private fun parseId3Frames(
        tagData: ByteArray,
        majorVersion: Int,
    ): LocalLyricsMatch? {
        var position = 0
        val headerSize = if (majorVersion == 2) 6 else 10
        val syncedLines = mutableListOf<LyricsLine>()
        val plainLyrics = mutableListOf<String>()

        while (position + headerSize <= tagData.size) {
            val (frameId, frameSize, nextPosition) = parseId3FrameHeader(tagData, position, majorVersion) ?: break
            if (frameId.isBlank() || frameSize <= 0) break
            if (nextPosition + frameSize > tagData.size) break
            val frameData = tagData.copyOfRange(nextPosition, nextPosition + frameSize)
            when (frameId) {
                "USLT", "ULT" -> parseUsltFrame(frameData)?.let(plainLyrics::add)
                "SYLT", "SLT" -> syncedLines += parseSyltFrame(frameData)
            }
            position = nextPosition + frameSize
        }

        parseSyncedLines(syncedLines)?.let { payload ->
            return LocalLyricsMatch(payload = payload, source = LyricsSourceKind.EmbeddedSynced)
        }
        parsePlainLyrics(plainLyrics.joinToString("\n"))?.let { lines ->
            return LocalLyricsMatch(
                payload = LyricsPayload(lines = lines, isSynced = false),
                source = LyricsSourceKind.EmbeddedPlain,
            )
        }
        return null
    }

    private fun parseId3FrameHeader(
        tagData: ByteArray,
        position: Int,
        majorVersion: Int,
    ): Triple<String, Int, Int>? {
        return when (majorVersion) {
            2 -> {
                val frameId = String(tagData, position, 3, Charsets.ISO_8859_1)
                val size = ((tagData[position + 3].toInt() and 0xFF) shl 16) or
                    ((tagData[position + 4].toInt() and 0xFF) shl 8) or
                    (tagData[position + 5].toInt() and 0xFF)
                Triple(frameId, size, position + 6)
            }
            3 -> {
                val frameId = String(tagData, position, 4, Charsets.ISO_8859_1)
                val size = ByteBuffer.wrap(tagData, position + 4, 4).int
                Triple(frameId, size, position + 10)
            }
            4 -> {
                val frameId = String(tagData, position, 4, Charsets.ISO_8859_1)
                val size = synchsafeInt(tagData, position + 4)
                Triple(frameId, size, position + 10)
            }
            else -> null
        }
    }

    private fun parseUsltFrame(frameData: ByteArray): String? {
        if (frameData.size < 5) return null
        val encoding = frameData[0].toInt() and 0xFF
        val descriptorStart = 4
        val descriptorEnd = findEncodedTerminator(frameData, descriptorStart, encoding)
        val lyricsStart = descriptorEnd + terminatorLengthForEncoding(encoding)
        if (lyricsStart !in 0..frameData.size) return null
        return decodeTextPayload(frameData.copyOfRange(lyricsStart, frameData.size), encoding)
            ?.removeBom()
            ?.takeIf { it.isNotBlank() }
    }

    private fun parseSyltFrame(frameData: ByteArray): List<LyricsLine> {
        if (frameData.size < 7) return emptyList()
        val encoding = frameData[0].toInt() and 0xFF
        val timestampFormat = frameData[4].toInt() and 0xFF
        if (timestampFormat != ID3_TIMESTAMP_MILLISECONDS) {
            return emptyList()
        }
        val descriptorStart = 6
        val descriptorEnd = findEncodedTerminator(frameData, descriptorStart, encoding)
        var position = descriptorEnd + terminatorLengthForEncoding(encoding)
        val lines = mutableListOf<LyricsLine>()
        while (position < frameData.size) {
            val textEnd = findEncodedTerminator(frameData, position, encoding).coerceAtMost(frameData.size)
            val textBytes = frameData.copyOfRange(position, textEnd)
            val text = decodeTextPayload(textBytes, encoding)?.let(::sanitizeLyricLine)
            val timestampStart = textEnd + terminatorLengthForEncoding(encoding)
            if (timestampStart + 4 > frameData.size) break
            val timestamp = ByteBuffer.wrap(frameData, timestampStart, 4).order(ByteOrder.BIG_ENDIAN).int.toLong()
            if (text != null) {
                lines += LyricsLine(
                    text = text,
                    startTimeMs = timestamp.coerceAtLeast(0L),
                )
            }
            position = timestampStart + 4
        }
        return lines
    }

    private fun readFlacLyrics(song: Song): LocalLyricsMatch? {
        return contentResolver.openInputStream(song.uri)?.use { rawInput ->
            val input = BufferedInputStream(rawInput, EMBEDDED_TAG_BUFFER_BYTES)
            val magic = input.readNBytes(4)
            if (magic.size < 4 || !magic.startsWithAscii("fLaC")) {
                return@use null
            }
            var isLastBlock = false
            while (!isLastBlock) {
                val header = input.readNBytes(4)
                if (header.size < 4) break
                isLastBlock = (header[0].toInt() and 0x80) != 0
                val blockType = header[0].toInt() and 0x7F
                val blockSize = ((header[1].toInt() and 0xFF) shl 16) or
                    ((header[2].toInt() and 0xFF) shl 8) or
                    (header[3].toInt() and 0xFF)
                if (blockSize < 0 || blockSize > MAX_VORBIS_COMMENT_BYTES) {
                    input.skip(blockSize.toLong())
                    continue
                }
                val blockData = input.readNBytes(blockSize)
                if (blockData.size < blockSize) break
                if (blockType == FLAC_BLOCK_VORBIS_COMMENT) {
                    parseFlacVorbisLyrics(blockData)?.let { return@use it }
                }
            }
            null
        }
    }

    private fun parseFlacVorbisLyrics(blockData: ByteArray): LocalLyricsMatch? {
        val buffer = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.remaining() < 8) return null
        val vendorLength = buffer.int.coerceAtLeast(0)
        if (vendorLength > buffer.remaining()) return null
        buffer.position(buffer.position() + vendorLength)
        if (buffer.remaining() < 4) return null
        val commentCount = buffer.int.coerceAtLeast(0)
        var syncedPayload: LyricsPayload? = null
        var plainPayload: LyricsPayload? = null
        repeat(commentCount) {
            if (buffer.remaining() < 4) return@repeat
            val commentLength = buffer.int.coerceAtLeast(0)
            if (commentLength <= 0 || commentLength > buffer.remaining()) return@repeat
            val commentBytes = ByteArray(commentLength)
            buffer.get(commentBytes)
            val comment = commentBytes.toString(Charsets.UTF_8)
            val separatorIndex = comment.indexOf('=')
            if (separatorIndex <= 0) return@repeat
            val key = comment.substring(0, separatorIndex)
                .uppercase(Locale.US)
                .replace(" ", "")
                .replace("_", "")
            val value = comment.substring(separatorIndex + 1).removeBom()
            when {
                key in FLAC_SYNCED_KEYS || looksLikeTimedLyrics(value) -> {
                    val lines = parseSyncedLyrics(value)
                    if (!lines.isNullOrEmpty()) {
                        syncedPayload = LyricsPayload(lines = lines, isSynced = true)
                    }
                }
                key in FLAC_PLAIN_KEYS -> {
                    val lines = parsePlainLyrics(value)
                    if (!lines.isNullOrEmpty()) {
                        plainPayload = LyricsPayload(lines = lines, isSynced = false)
                    }
                }
            }
        }
        syncedPayload?.let { return LocalLyricsMatch(it, LyricsSourceKind.EmbeddedSynced) }
        plainPayload?.let { return LocalLyricsMatch(it, LyricsSourceKind.EmbeddedPlain) }
        return null
    }

    private fun readSidecarLyrics(song: Song): LocalLyricsMatch? {
        val localFile = resolveSongFile(song) ?: return null
        val parent = localFile.parentFile ?: return null
        if (!parent.isDirectory) return null

        val baseNames = linkedSetOf(
            localFile.nameWithoutExtension,
            song.fileName.substringBeforeLast('.', song.fileName),
            sanitizeFileStem(song.title),
        ).filter { it.isNotBlank() }

        baseNames.forEach { baseName ->
            val lrcFile = File(parent, "$baseName.lrc")
            if (lrcFile.isFile) {
                parseSyncedLyrics(readTextFile(lrcFile))?.let { lines ->
                    if (lines.isNotEmpty()) {
                        return LocalLyricsMatch(
                            payload = LyricsPayload(lines = lines, isSynced = true),
                            source = LyricsSourceKind.SidecarLrc,
                        )
                    }
                }
            }
            val txtFile = File(parent, "$baseName.txt")
            if (txtFile.isFile) {
                parsePlainLyrics(readTextFile(txtFile))?.let { lines ->
                    if (lines.isNotEmpty()) {
                        return LocalLyricsMatch(
                            payload = LyricsPayload(lines = lines, isSynced = false),
                            source = LyricsSourceKind.SidecarText,
                        )
                    }
                }
            }
        }

        return null
    }

    @SuppressLint("Range")
    private fun resolveSongFile(song: Song): File? {
        if (song.uri.scheme == "file") {
            return song.uri.path?.let(::File)?.takeIf(File::exists)
        }
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val resolvedPath = runCatching {
            contentResolver.query(song.uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            }
        }.getOrNull()
        return resolvedPath?.let(::File)?.takeIf(File::exists)
    }

    private fun getJsonObject(
        url: String,
        connectTimeoutMs: Int = NETWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = NETWORK_READ_TIMEOUT_MS,
        requestHeaders: Map<String, String> = emptyMap(),
    ): JSONObject? {
        return getText(url, connectTimeoutMs, readTimeoutMs, requestHeaders)?.let(::JSONObject)
    }

    private fun getJsonArray(
        url: String,
        connectTimeoutMs: Int = NETWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = NETWORK_READ_TIMEOUT_MS,
        requestHeaders: Map<String, String> = emptyMap(),
    ): JSONArray? {
        return getText(url, connectTimeoutMs, readTimeoutMs, requestHeaders)?.let(::JSONArray)
    }

    private fun getText(
        url: String,
        connectTimeoutMs: Int = NETWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = NETWORK_READ_TIMEOUT_MS,
        requestHeaders: Map<String, String> = emptyMap(),
    ): String? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return runCatching {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty(
                "User-Agent",
                "Elovaire/${BuildConfig.VERSION_NAME} (Android; Offline Music Player)",
            )
            connection.setRequestProperty("Accept", "application/json")
            requestHeaders.forEach { (name, value) ->
                connection.setRequestProperty(name, value)
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull().also {
            connection.disconnect()
        }
    }

    private fun buildUrl(
        baseUrl: String,
        queryParameters: Map<String, String>,
    ): String {
        if (queryParameters.isEmpty()) return baseUrl
        return buildString {
            append(baseUrl)
            append('?')
            queryParameters.entries
                .filter { it.value.isNotBlank() }
                .forEachIndexed { index, (name, value) ->
                    if (index > 0) append('&')
                    append(name.urlEncode())
                    append('=')
                    append(value.urlEncode())
                }
        }
    }

    private fun buildCacheKey(song: Song): String {
        return listOf(
            song.id.toString(),
            song.uri.toString(),
            durationSeconds(song.durationMs).toString(),
            normalizeArtistName(song.artist),
            normalizeTrackTitle(song.title),
        ).joinToString("::")
    }

    private fun durationSeconds(durationMs: Long): Long? {
        return (durationMs.takeIf { it > 0L } ?: return null) / 1000L
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun logMetrics(metrics: LyricsDebugMetrics) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG,
            "lyrics cacheKey=${metrics.cacheKey} source=${metrics.source} cacheHit=${metrics.cacheHit} " +
                "local=${metrics.localLookupMs}ms remote=${metrics.remoteLookupMs}ms total=${metrics.totalLookupMs}ms",
        )
    }

    private fun logDebug(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        if (throwable == null) {
            Log.d(TAG, message)
        } else {
            Log.d(TAG, message, throwable)
        }
    }

    private companion object {
        const val TAG = "LyricsService"
        const val MAX_CACHE_ENTRIES = 128
        const val NOT_FOUND_CACHE_TTL_MS = 15 * 1000L
        const val OFFLINE_NOT_FOUND_CACHE_TTL_MS = 20 * 1000L
        const val TIMEOUT_NOT_FOUND_CACHE_TTL_MS = 3 * 1000L
        const val ERROR_CACHE_TTL_MS = 5 * 1000L
        const val NETWORK_CONNECT_TIMEOUT_MS = 900
        const val NETWORK_READ_TIMEOUT_MS = 2_400
        const val REMOTE_LOOKUP_TOTAL_TIMEOUT_MS = 5_800L
        const val LRCLIB_LOOKUP_TIMEOUT_MS = 1_950L
        const val LRCLIB_CONNECT_TIMEOUT_MS = 650
        const val LRCLIB_READ_TIMEOUT_MS = 1_300
        const val MAX_REMOTE_QUERY_VARIANTS = 8
        const val MAX_LRCLIB_CANDIDATES = 5
        const val LRCLIB_BASE_URL = "https://lrclib.net/api/"
        const val LRCLIB_GET_URL = "${LRCLIB_BASE_URL}get"
        const val LRCLIB_SEARCH_URL = "${LRCLIB_BASE_URL}search"
        const val LRCLIB_FALLBACK_MATCH_MIN_SCORE = 32
        const val EMBEDDED_TAG_BUFFER_BYTES = 64 * 1024
        const val MAX_EMBEDDED_TAG_BYTES = 1_500_000
        const val MAX_SIDECAR_BYTES = 256 * 1024
        const val MAX_VORBIS_COMMENT_BYTES = 1_000_000
        const val FLAC_BLOCK_VORBIS_COMMENT = 4
        const val ID3_UNSYNCHRONIZATION_FLAG = 0x80
        const val ID3_TIMESTAMP_MILLISECONDS = 0x02
        val FLAC_SYNCED_KEYS = setOf("SYNCEDLYRICS", "LRC", "LYRICSTIMED")
        val FLAC_PLAIN_KEYS = setOf("LYRICS", "UNSYNCEDLYRICS", "UNSYNCEDTEXT", "TEXT")
    }
}

internal data class CachedLyricsEntry(
    val result: LyricsResult,
    val expiresAtMillis: Long,
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean = nowMillis >= expiresAtMillis
}

internal data class LrcLibTrackCandidate(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Double?,
    val instrumental: Boolean,
    val plainLyrics: String,
    val syncedLyrics: String,
)

internal data class LocalLyricsMatch(
    val payload: LyricsPayload,
    val source: LyricsSourceKind,
)

internal data class RemoteLyricsMatch(
    val payload: LyricsPayload,
    val source: LyricsSourceKind,
    val confidence: Int,
)

internal fun parseSyncedLyrics(rawLyrics: String?): List<LyricsLine>? {
    if (rawLyrics.isNullOrBlank()) return null
    var offsetMs = 0L
    val parsedLines = mutableListOf<LyricsLine>()
    val fallbackPlainLines = mutableListOf<String>()

    rawLyrics
        .normalizeLyricBreaks()
        .lineSequence()
        .forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach
            parseMetadataLine(line)?.let { metadata ->
                if (metadata.first == "offset") {
                    offsetMs = metadata.second.toLongOrNull() ?: offsetMs
                }
                return@forEach
            }

            val timeTags = TIMESTAMP_REGEX.findAll(line).toList()
            if (timeTags.isEmpty()) {
                fallbackPlainLines += line
                return@forEach
            }

            val lyricFragments = splitLyricDisplayText(line.substring(timeTags.last().range.last + 1))
            if (lyricFragments.isEmpty()) {
                return@forEach
            }

            timeTags.forEach { match ->
                val startTimeMs = parseTimestampMatch(match)?.plus(offsetMs)?.coerceAtLeast(0L) ?: return@forEach
                parsedLines += LyricsLine(
                    text = lyricFragments.joinToString("\n"),
                    startTimeMs = startTimeMs,
                )
            }
        }

    val sortedLines = parsedLines
        .sortedBy { it.startTimeMs ?: Long.MAX_VALUE }
        .takeIf { it.isNotEmpty() }
    if (!sortedLines.isNullOrEmpty()) {
        return finalizeSyncedLyrics(sortedLines)
    }

    return parsePlainLyrics(fallbackPlainLines.joinToString("\n"))
}

internal fun parsePlainLyrics(rawLyrics: String?): List<LyricsLine>? {
    if (rawLyrics.isNullOrBlank()) return null
    val lines = rawLyrics
        .normalizeLyricBreaks()
        .lineSequence()
        .flatMap { splitLyricDisplayText(it).asSequence() }
        .map { LyricsLine(text = it, startTimeMs = null) }
        .toList()
    if (lines.isEmpty()) return null
    val nonMetadataCount = lines.count { line ->
        !METADATA_ONLY_LINE_REGEX.matches(line.text)
    }
    return lines.takeIf { nonMetadataCount > 0 }
}

private fun String.normalizeLyricBreaks(): String {
    return removeBom()
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace(Regex("""(?i)<\s*br\s*/?\s*>"""), "\n")
        .replace(Regex("""(?i)</\s*p\s*>"""), "\n")
        .replace(Regex("""(?i)<\s*/?\s*(div|p|span)[^>]*>"""), "\n")
        .replace(Regex("""\[(verse|chorus|bridge|intro|outro|pre-chorus|post-chorus|hook|refrain)[^]]*]""", RegexOption.IGNORE_CASE), "\n")
}

private fun splitLyricDisplayText(rawLine: String): List<String> {
    val sanitized = sanitizeLyricLine(rawLine) ?: return emptyList()
    return listOf(sanitized)
}

internal fun sanitizeLyricLine(line: String): String? {
    val withoutTags = line
        .replace(Regex("""<[^>]+>"""), " ")
        .replace(Regex("""&amp;""", RegexOption.IGNORE_CASE), "&")
        .replace(Regex("""&quot;""", RegexOption.IGNORE_CASE), "\"")
        .replace(Regex("""&#39;|&apos;""", RegexOption.IGNORE_CASE), "'")

    val cleaned = withoutTags
        .replace('\u00A0', ' ')
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
        .trim('-', '–', '—')

    if (cleaned.isBlank()) return null
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized.startsWith("translations")) return null
    if (normalized == "embed") return null
    if (normalized.startsWith("you might also like")) return null
    if (normalized.startsWith("submit corrections")) return null
    if (normalized.startsWith("contributors")) return null
    if (METADATA_ONLY_LINE_REGEX.matches(cleaned)) return null
    return cleaned
}

internal fun normalizeTrackTitle(value: String): String {
    return value
        .normalizeDiacritics()
        .lowercase(Locale.US)
        .replace("&", "and")
        .replace(Regex("""(?i)\b(feat|ft|featuring)\b.*$"""), "")
        .replace(Regex("""(?i)\b(remaster(ed)?|live|mono|stereo|version|edit|mix|deluxe|bonus track)\b"""), "")
        .replace(Regex("""\([^)]*\)|\[[^]]*]"""), "")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()
}

internal fun normalizeArtistName(value: String): String {
    return value.normalizeForMatch()
}

internal fun buildLyricsQueryVariants(song: Song): List<LyricsQueryVariant> {
    val primaryArtist = extractPrimaryArtist(song.artist)
    val normalizedArtist = normalizeArtistName(song.artist)
    val normalizedTitle = normalizeTrackTitle(song.title)
    val simplifiedTitle = simplifyLookupTitle(song.title)
    val originalAlbum = song.album.takeIf { it.isNotBlank() }
    val albumWithoutDecorations = song.album.takeIf { it.isNotBlank() }?.let(::normalizeAlbumTitle)?.takeIf { it.isNotBlank() }

    return buildList {
        add(LyricsQueryVariant(song.artist, song.title, originalAlbum))
        add(LyricsQueryVariant(song.artist, song.title, null))
        if (primaryArtist != song.artist) {
            add(LyricsQueryVariant(primaryArtist, song.title, originalAlbum))
            add(LyricsQueryVariant(primaryArtist, song.title, null))
        }
        if (normalizedArtist.isNotBlank() && normalizedTitle.isNotBlank()) {
            add(LyricsQueryVariant(normalizedArtist, normalizedTitle, albumWithoutDecorations))
            add(LyricsQueryVariant(normalizedArtist, normalizedTitle, null))
        }
        if (simplifiedTitle.isNotBlank() && simplifiedTitle != song.title) {
            add(LyricsQueryVariant(song.artist, simplifiedTitle, originalAlbum))
            add(LyricsQueryVariant(song.artist, simplifiedTitle, null))
            if (primaryArtist != song.artist) {
                add(LyricsQueryVariant(primaryArtist, simplifiedTitle, originalAlbum))
                add(LyricsQueryVariant(primaryArtist, simplifiedTitle, null))
            }
        }
        if (primaryArtist != song.artist && normalizedTitle.isNotBlank()) {
            add(LyricsQueryVariant(primaryArtist.normalizeForMatch(), normalizedTitle, null))
        }
    }.distinct()
}

private fun estimateRemoteDisplayTimingOffsetMs(
    lines: List<LyricsLine>,
    song: Song?,
): Long {
    if (song == null || song.durationMs < 60_000L) return 0L
    val timedLines = lines.mapNotNull { it.startTimeMs }.sorted()
    if (timedLines.size < 4) return 0L

    val first = timedLines.first()
    val second = timedLines.getOrNull(1) ?: first
    val third = timedLines.getOrNull(2) ?: second
    val fifth = timedLines.getOrNull(4) ?: third
    val eighth = timedLines.getOrNull(7) ?: fifth
    val last = timedLines.last()
    val gaps = timedLines
        .zipWithNext { current, next -> next - current }
        .filter { it > 0L }
        .sorted()
    val medianGap = gaps.getOrNull(gaps.size / 2) ?: Long.MAX_VALUE
    val coverage = if (song.durationMs > 0L) last.toFloat() / song.durationMs.toFloat() else 1f

    // LRCLIB entries often match the written lyric text correctly but are timed for another cut
    // of the song, or their first vocal line is timestamped at 0:00. Correct only at display time
    // so the original LRC timestamps stay available for diagnostics and seeking.
    val openingDelay = when {
        first <= 700L && fifth <= 16_000L -> 16_000L
        first <= 1_500L && fifth <= 20_000L -> 13_000L
        first <= 3_500L && fifth <= 24_000L -> 9_000L
        first <= 6_000L && eighth <= 34_000L -> 6_000L
        first <= 3_000L -> 4_000L
        else -> 0L
    }

    val densityDelay = when {
        medianGap in 1L..1_250L && timedLines.size >= 18 -> 5_000L
        medianGap in 1_251L..1_650L && timedLines.size >= 18 -> 2_500L
        else -> 0L
    }

    val coverageDelay = when {
        coverage in 0f..0.55f && timedLines.size >= 12 -> 5_000L
        coverage in 0.55f..0.68f && timedLines.size >= 12 -> 2_500L
        else -> 0L
    }

    return max(openingDelay, max(densityDelay, coverageDelay)).coerceIn(0L, 18_000L)
}

private fun LyricsPayload.hasHealthyRemoteTimeline(song: Song): Boolean {
    if (!isSynced || song.durationMs <= 0L) return true
    val timedLines = lines.mapNotNull { it.startTimeMs }.sorted()
    if (timedLines.size < 8 || song.durationMs < MIN_SONG_DURATION_FOR_REMOTE_OPENING_FIX_MS) return true

    val first = timedLines.first()
    val last = timedLines.last()
    val coverage = last.toFloat() / song.durationMs.toFloat()
    val gaps = timedLines
        .zipWithNext { current, next -> next - current }
        .filter { it > 0L }
        .sorted()
    val medianGap = gaps.getOrNull(gaps.size / 2) ?: Long.MAX_VALUE
    val duplicateRatio = 1f - (timedLines.distinct().size.toFloat() / timedLines.size.toFloat())

    val timelineEndsFarTooEarly = coverage < REMOTE_MIN_SYNCED_TIMELINE_COVERAGE
    val linesRaceTooFast = medianGap < REMOTE_MIN_HEALTHY_MEDIAN_GAP_MS && timedLines.size >= 16
    val hasTooManyDuplicateTimestamps = duplicateRatio > 0.18f
    return !timelineEndsFarTooEarly &&
        !linesRaceTooFast &&
        !hasTooManyDuplicateTimestamps
}

internal fun LrcLibTrackCandidate.scoreAgainst(song: Song): Int {
    val songTitle = normalizeTrackTitle(song.title)
    val songArtist = normalizeArtistName(song.artist)
    val primaryArtist = normalizeArtistName(extractPrimaryArtist(song.artist))
    val songAlbum = song.album.normalizeForMatch()
    val candidateTitle = normalizeTrackTitle(trackName)
    val candidateArtist = normalizeArtistName(artistName)
    val candidateAlbum = albumName.normalizeForMatch()

    var score = 0
    if (candidateTitle == songTitle) score += 28
    if (candidateArtist == songArtist || candidateArtist == primaryArtist) score += 26
    if (candidateAlbum.isNotBlank() && candidateAlbum == songAlbum) score += 9
    if (songTitle.isNotBlank() && candidateTitle.isNotBlank() && (candidateTitle.contains(songTitle) || songTitle.contains(candidateTitle))) score += 8
    if (
        songArtist.isNotBlank() &&
        candidateArtist.isNotBlank() &&
        (
            candidateArtist.contains(songArtist) || songArtist.contains(candidateArtist) ||
                candidateArtist.contains(primaryArtist) || primaryArtist.contains(candidateArtist)
            )
    ) {
        score += 8
    }
    if (syncedLyrics.isNotBlank()) score += 10
    if (plainLyrics.isNotBlank()) score += 4
    score += tokenOverlapBonus(songTitle, candidateTitle, maxBonus = 10)
    score += tokenOverlapBonus(songArtist.ifBlank { primaryArtist }, candidateArtist, maxBonus = 8)
    if (looksLikeAlternateVersion(song.title, trackName)) score -= 8
    durationSeconds?.let { candidateDuration ->
        val songDurationSeconds = song.durationMs / 1000.0
        val delta = abs(candidateDuration - songDurationSeconds)
        when {
            delta <= 1.0 -> score += 12
            delta <= 3.0 -> score += 9
            delta <= 7.0 -> score += 5
            delta <= 15.0 -> score += 2
            songDurationSeconds > 0.0 -> score -= 7
        }
    }
    return score
}

internal fun LrcLibTrackCandidate.isAcceptableMatchFor(
    song: Song,
    score: Int,
): Boolean {
    val songTitle = normalizeTrackTitle(song.title)
    val songArtist = normalizeArtistName(song.artist)
    val primaryArtist = normalizeArtistName(extractPrimaryArtist(song.artist))
    val candidateTitle = normalizeTrackTitle(trackName)
    val candidateArtist = normalizeArtistName(artistName)
    val exactTitle = candidateTitle.isNotBlank() && candidateTitle == songTitle
    val exactArtist = candidateArtist.isNotBlank() && (candidateArtist == songArtist || candidateArtist == primaryArtist)
    val titleOverlap = candidateTitle.isNotBlank() &&
        songTitle.isNotBlank() &&
        (candidateTitle.contains(songTitle) || songTitle.contains(candidateTitle))
    val artistOverlap = candidateArtist.isNotBlank() &&
        (candidateArtist.contains(songArtist) || songArtist.contains(candidateArtist) ||
            candidateArtist.contains(primaryArtist) || primaryArtist.contains(candidateArtist))

    return when {
        exactTitle && exactArtist -> score >= 20
        exactTitle && artistOverlap -> score >= 20
        exactArtist && titleOverlap -> score >= 20
        else -> false
    }
}

internal fun JSONObject.toLrcLibCandidate(): LrcLibTrackCandidate? {
    val id = optFlexibleLong("id")?.takeIf { it > 0L } ?: return null
    return LrcLibTrackCandidate(
        id = id,
        trackName = optNullableString("trackName").ifBlank { optNullableString("name") },
        artistName = optNullableString("artistName"),
        albumName = optNullableString("albumName"),
        durationSeconds = optFlexibleDouble("duration"),
        instrumental = optFlexibleFlag("instrumental"),
        plainLyrics = optNullableString("plainLyrics"),
        syncedLyrics = optNullableString("syncedLyrics"),
    )
}

internal fun JSONObject.optNullableString(name: String): String {
    return if (isNull(name)) "" else optString(name)
}

private fun JSONObject.optFlexibleLong(name: String): Long? {
    if (isNull(name)) return null
    val value = opt(name) ?: return null
    return when (value) {
        is Number -> value.toLong()
        is String -> value.trim().toLongOrNull()
        else -> null
    }
}

private fun JSONObject.optFlexibleDouble(name: String): Double? {
    if (isNull(name)) return null
    val value = opt(name) ?: return null
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }
}

private fun JSONObject.optFlexibleFlag(name: String): Boolean {
    if (isNull(name)) return false
    val value = opt(name) ?: return false
    return when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> {
            val normalized = value.trim().lowercase(Locale.US)
            normalized == "1" || normalized == "true" || normalized == "yes"
        }
        else -> false
    }
}

internal fun String.normalizeForMatch(): String {
    return normalizeDiacritics()
        .lowercase(Locale.US)
        .replace("&", "and")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()
}

private fun normalizeAlbumTitle(value: String): String {
    return value
        .normalizeDiacritics()
        .lowercase(Locale.US)
        .replace(Regex("""\([^)]*\)|\[[^]]*]"""), "")
        .replace(Regex("""(?i)\b(deluxe|expanded|edition|remaster(ed)?|version)\b"""), "")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()
}

private fun simplifyLookupTitle(value: String): String {
    return value
        .replace(Regex("""(?i)\s+-\s+.*$"""), "")
        .replace(Regex("""(?i)\s*/\s+.*$"""), "")
        .replace(Regex("""(?i)\b(remaster(ed)?|live|version|edit|mix|deluxe)\b.*$"""), "")
        .trim()
}

private fun extractPrimaryArtist(value: String): String {
    return value.split(Regex("""(?i)\b(feat\.?|ft\.?|featuring|with)\b|,|&|;|/"""))
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        ?: value
}

private fun lrcLibCandidateDistinctKey(candidate: LrcLibTrackCandidate): String {
    return "lrclib::${candidate.id}::${normalizeTrackTitle(candidate.trackName)}::${normalizeArtistName(candidate.artistName)}"
}

private fun LrcLibTrackCandidate.toRemoteLyricsMatch(
    song: Song,
    baseScore: Int,
): RemoteLyricsMatch? {
    if (instrumental) return null

    val plainLines = parsePlainLyrics(plainLyrics)
    val syncedLines = parseSyncedLyrics(syncedLyrics)
        ?.takeIf { lines -> lines.any { it.startTimeMs != null } }

    if (syncedLines != null) {
        val syncedPayload = LyricsPayload(
            lines = syncedLines,
            isSynced = true,
            displayTimingOffsetMs = estimateRemoteDisplayTimingOffsetMs(syncedLines, song),
        )
        if (syncedPayload.hasHealthyRemoteTimeline(song) || plainLines.isNullOrEmpty()) {
            return RemoteLyricsMatch(
                payload = syncedPayload,
                source = LyricsSourceKind.RemoteLrcLibSynced,
                confidence = (baseScore + 8).coerceAtMost(100),
            )
        }
    }

    if (!plainLines.isNullOrEmpty()) {
        return RemoteLyricsMatch(
            payload = LyricsPayload(lines = plainLines, isSynced = false),
            source = LyricsSourceKind.RemoteLrcLibLyrics,
            confidence = (baseScore + 3).coerceAtMost(100),
        )
    }

    if (syncedLines != null) {
        return RemoteLyricsMatch(
            payload = LyricsPayload(
                lines = syncedLines,
                isSynced = true,
                displayTimingOffsetMs = estimateRemoteDisplayTimingOffsetMs(syncedLines, song),
            ),
            source = LyricsSourceKind.RemoteLrcLibSynced,
            confidence = (baseScore + 3).coerceAtMost(100),
        )
    }

    return null
}

private fun tokenOverlapBonus(
    left: String,
    right: String,
    maxBonus: Int,
): Int {
    if (left.isBlank() || right.isBlank()) return 0
    val leftTokens = left.split(' ').filter { it.isNotBlank() }.toSet()
    val rightTokens = right.split(' ').filter { it.isNotBlank() }.toSet()
    if (leftTokens.isEmpty() || rightTokens.isEmpty()) return 0
    val intersection = leftTokens.intersect(rightTokens).size
    val union = leftTokens.union(rightTokens).size.coerceAtLeast(1)
    return ((intersection.toFloat() / union.toFloat()) * maxBonus).toInt()
}

private fun looksLikeAlternateVersion(
    originalTitle: String,
    candidateTitle: String,
): Boolean {
    val originalDecorators = lookupDecorators(originalTitle)
    val candidateDecorators = lookupDecorators(candidateTitle)
    return candidateDecorators.isNotEmpty() && candidateDecorators != originalDecorators
}

private fun lookupDecorators(value: String): Set<String> {
    return VERSION_DECORATOR_REGEX.findAll(value)
        .map { it.value.lowercase(Locale.US) }
        .toSet()
}

private fun sanitizeFileStem(value: String): String {
    return value.replace(Regex("""[\\/:*?"<>|]"""), "").trim()
}

private fun readTextFile(file: File): String? {
    val bytes = runCatching { file.takeIf { it.length() in 1..MAX_SIDECAR_FILE_BYTES }?.readBytes() }.getOrNull() ?: return null
    return decodeBestEffortText(bytes)
}

private fun parseMetadataLine(line: String): Pair<String, String>? {
    val match = METADATA_LINE_REGEX.matchEntire(line) ?: return null
    return match.groupValues[1].lowercase(Locale.US) to match.groupValues[2].trim()
}

private fun parseTimestampMatch(match: MatchResult): Long? {
    val hours = match.groups[1]?.value?.toLongOrNull() ?: 0L
    val minutes = match.groups[2]?.value?.toLongOrNull() ?: return null
    val seconds = match.groups[3]?.value?.toLongOrNull() ?: return null
    val fractional = match.groups[4]?.value.orEmpty()
    val millis = when (fractional.length) {
        0 -> 0L
        1 -> fractional.toLongOrNull()?.times(100L)
        2 -> fractional.toLongOrNull()?.times(10L)
        else -> fractional.take(3).toLongOrNull()
    } ?: 0L
    return hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + millis
}

private fun parseSyncedLines(lines: List<LyricsLine>): LyricsPayload? {
    val validLines = lines
        .filter { !it.text.isBlank() && it.startTimeMs != null }
        .sortedBy { it.startTimeMs }
    return validLines.takeIf { it.isNotEmpty() }?.let {
        LyricsPayload(lines = finalizeSyncedLyrics(it), isSynced = true)
    }
}

private fun finalizeSyncedLyrics(lines: List<LyricsLine>): List<LyricsLine> {
    val merged = mergeContinuationLyricLines(lines)
    return merged.mapIndexed { index, line ->
        line.copy(endTimeMs = merged.getOrNull(index + 1)?.startTimeMs)
    }
}

private fun mergeContinuationLyricLines(lines: List<LyricsLine>): List<LyricsLine> {
    if (lines.isEmpty()) return emptyList()
    val merged = mutableListOf<LyricsLine>()
    var index = 0
    while (index < lines.size) {
        var current = lines[index]
        while (index + 1 < lines.size && shouldMergeLyricLines(current, lines[index + 1])) {
            val next = lines[index + 1]
            current = current.copy(text = joinLyricSegments(current.text, next.text))
            index += 1
        }
        merged += current
        index += 1
    }
    return merged
}

private fun shouldMergeLyricLines(current: LyricsLine, next: LyricsLine): Boolean {
    val currentStart = current.startTimeMs ?: return false
    val nextStart = next.startTimeMs ?: return false
    val gapMs = (nextStart - currentStart).coerceAtLeast(0L)
    if (gapMs > 2_600L) return false
    if (current.text.length + next.text.length > 140) return false
    if (current.text.trimEnd().endsWithAny('.', '!', '?')) return false

    val currentTrimmed = current.text.trim()
    val nextTrimmed = next.text.trim()
    if (currentTrimmed.endsWith(",") || currentTrimmed.endsWith(":") || currentTrimmed.endsWith(";")) {
        return true
    }

    val nextLead = nextTrimmed.trimStart('(', '[', '"', '\'')
    val nextFirst = nextLead.firstOrNull() ?: return false
    if (nextFirst.isLowerCase()) return true

    val normalizedLead = nextLead.lowercase(Locale.US)
    return normalizedLead.startsWith("and ") ||
        normalizedLead.startsWith("or ") ||
        normalizedLead.startsWith("but ") ||
        normalizedLead.startsWith("so ") ||
        normalizedLead.startsWith("because ") ||
        normalizedLead.startsWith("of ") ||
        normalizedLead.startsWith("to ") ||
        normalizedLead.startsWith("for ") ||
        normalizedLead.startsWith("with ") ||
        normalizedLead.startsWith("in ") ||
        normalizedLead.startsWith("on ") ||
        normalizedLead.startsWith("at ")
}

private fun joinLyricSegments(first: String, second: String): String {
    val left = first.trimEnd()
    val right = second.trimStart()
    if (left.isBlank()) return right
    if (right.isBlank()) return left
    return when {
        left.endsWith("-") || left.endsWith("—") || left.endsWith("–") -> left + right
        else -> "$left $right"
    }
}

private fun String.endsWithAny(vararg chars: Char): Boolean = chars.any { trimEnd().endsWith(it) }

private fun looksLikeTimedLyrics(value: String): Boolean = TIMESTAMP_REGEX.containsMatchIn(value)

private fun decodeBestEffortText(bytes: ByteArray): String {
    if (bytes.isEmpty()) return ""
    if (bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))) {
        return bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
    }
    if (bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte()))) {
        return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16LE)
    }
    if (bytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte()))) {
        return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16BE)
    }
    val utf8 = bytes.toString(Charsets.UTF_8)
    val replacementCount = utf8.count { it == '\uFFFD' }
    return if (replacementCount > min(6, utf8.length / 16)) {
        bytes.toString(Charset.forName("windows-1252"))
    } else {
        utf8
    }
}

private fun String.normalizeDiacritics(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(DIACRITIC_REGEX, "")
}

private fun decodeTextPayload(
    bytes: ByteArray,
    encoding: Int,
): String? {
    if (bytes.isEmpty()) return null
    return when (encoding) {
        0 -> bytes.toString(Charsets.ISO_8859_1)
        1 -> decodeUtf16(bytes)
        2 -> bytes.toString(Charsets.UTF_16BE)
        3 -> bytes.toString(Charsets.UTF_8)
        else -> bytes.toString(Charsets.UTF_8)
    }.removeBom()
}

private fun decodeUtf16(bytes: ByteArray): String {
    return when {
        bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte())) -> bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16LE)
        bytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte())) -> bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16BE)
        else -> bytes.toString(Charsets.UTF_16)
    }
}

private fun findEncodedTerminator(
    bytes: ByteArray,
    startIndex: Int,
    encoding: Int,
): Int {
    val delimiterLength = terminatorLengthForEncoding(encoding)
    var index = startIndex
    while (index + delimiterLength <= bytes.size) {
        val terminated = if (delimiterLength == 1) {
            bytes[index] == 0.toByte()
        } else {
            bytes[index] == 0.toByte() && bytes.getOrNull(index + 1) == 0.toByte()
        }
        if (terminated) {
            return index
        }
        index += delimiterLength
    }
    return bytes.size
}

private fun terminatorLengthForEncoding(encoding: Int): Int {
    return when (encoding) {
        1, 2 -> 2
        else -> 1
    }
}

private fun synchsafeInt(bytes: ByteArray, offset: Int): Int {
    if (offset + 4 > bytes.size) return 0
    return ((bytes[offset].toInt() and 0x7F) shl 21) or
        ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
        ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
        (bytes[offset + 3].toInt() and 0x7F)
}

private fun removeId3Unsynchronization(data: ByteArray): ByteArray {
    val output = ArrayList<Byte>(data.size)
    var index = 0
    while (index < data.size) {
        val current = data[index]
        if (
            current == 0xFF.toByte() &&
            index + 1 < data.size &&
            data[index + 1] == 0x00.toByte()
        ) {
            output += current
            index += 2
        } else {
            output += current
            index += 1
        }
    }
    return output.toByteArray()
}

private fun ByteArray.startsWithAscii(prefix: String): Boolean {
    if (size < prefix.length) return false
    return prefix.indices.all { index -> this[index].toInt().toChar() == prefix[index] }
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    return prefix.indices.all { index -> this[index] == prefix[index] }
}

private fun String.removeBom(): String = removePrefix("\uFEFF")

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private val METADATA_LINE_REGEX = Regex("""^\[([A-Za-z]+):(.*)]$""")
private val METADATA_ONLY_LINE_REGEX = Regex("""^\[(ar|ti|al|by|offset):.*]$""", RegexOption.IGNORE_CASE)
private val TIMESTAMP_REGEX = Regex("""\[(?:(\d{1,2}):)?(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")
private val VERSION_DECORATOR_REGEX = Regex("""(?i)\b(live|acoustic|remaster(?:ed)?|mono|stereo|demo|karaoke|instrumental|reprise|edit|mix|version)\b""")
private val DIACRITIC_REGEX = Regex("\\p{Mn}+")
private const val MAX_SIDECAR_FILE_BYTES = 256 * 1024L
