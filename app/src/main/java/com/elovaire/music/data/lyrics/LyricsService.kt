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
import kotlinx.coroutines.awaitAll
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

data class LyricsLine(
    val text: String,
    val startTimeMs: Long?,
)

data class LyricsPayload(
    val lines: List<LyricsLine>,
    val isSynced: Boolean,
)

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
    RemoteDirect,
    RemoteSearch,
    RemoteFallback,
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

    fun cachedLyrics(song: Song): LyricsResult? = synchronized(cacheLock) {
        val key = buildCacheKey(song)
        val entry = cache[key] ?: return@synchronized null
        if (entry.isExpired()) {
            cache.remove(key)
            null
        } else {
            entry.result
        }
    }

    fun prefetchLyrics(song: Song) {
        if (cachedLyrics(song) != null || inFlightRequests.containsKey(buildCacheKey(song))) return
        serviceScope.launch {
            fetchLyrics(song)
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

    suspend fun fetchLyrics(song: Song): LyricsResult = coroutineScope {
        val cacheKey = buildCacheKey(song)
        cachedLyrics(song)?.let {
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
        val directQueries = buildLyricsQueryVariants(song).take(MAX_REMOTE_QUERY_VARIANTS)
        val directCandidates = directQueries.map { query ->
            async(ioDispatcher) {
                withTimeoutOrNull(LRCLIB_DIRECT_LOOKUP_TIMEOUT_MS) {
                    fetchLrcLibEntry(
                        trackName = query.title,
                        artistName = query.artist,
                        albumName = query.album,
                        durationSeconds = durationSeconds(song.durationMs),
                    )
                }
            }
        }.awaitAll().filterNotNull()

        val directMatch = directCandidates.bestRemoteMatchFor(
            song = song,
            defaultSource = LyricsSourceKind.RemoteDirect,
        )
        if (directMatch != null && directMatch.confidence >= PREFERRED_DIRECT_CONFIDENCE) {
            return@coroutineScope directMatch
        }

        val structuredSearchMatchDeferred = async(ioDispatcher) {
            searchLrcLib(directQueries).bestRemoteMatchFor(
                song = song,
                defaultSource = LyricsSourceKind.RemoteSearch,
            )
        }
        val fallbackMatchDeferred = async(ioDispatcher) {
            fetchLyricsOvhFallback(song, directQueries)
        }

        listOfNotNull(
            directMatch,
            withTimeoutOrNull(REMOTE_SEARCH_PHASE_TIMEOUT_MS) { structuredSearchMatchDeferred.await() },
            withTimeoutOrNull(REMOTE_FALLBACK_PHASE_TIMEOUT_MS) { fallbackMatchDeferred.await() },
        ).maxWithOrNull(
            compareBy<RemoteLyricsMatch> { it.confidence }
                .thenBy { if (it.payload.isSynced) 1 else 0 },
        )
    }

    private suspend fun resolveLocalLyrics(song: Song): LocalLyricsMatch? = coroutineScope {
        val embeddedDeferred = async(ioDispatcher) { readEmbeddedLyrics(song) }
        val sidecarDeferred = async(ioDispatcher) { readSidecarLyrics(song) }

        val embedded = embeddedDeferred.await()
        embedded?.let { return@coroutineScope it }

        sidecarDeferred.await()
    }

    private suspend fun searchLrcLib(
        queries: List<LyricsQueryVariant>,
    ): List<LrcLibCandidate> = coroutineScope {
        return@coroutineScope queries
            .map { query ->
                async(ioDispatcher) {
                    withTimeoutOrNull(LRCLIB_SEARCH_LOOKUP_TIMEOUT_MS) {
                        val url = buildString {
                            append("https://lrclib.net/api/search?")
                            append("artist_name=").append(query.artist.urlEncode())
                            append("&track_name=").append(query.title.urlEncode())
                        }
                        getJsonArray(
                            url = url,
                            connectTimeoutMs = SEARCH_REQUEST_CONNECT_TIMEOUT_MS,
                            readTimeoutMs = SEARCH_REQUEST_READ_TIMEOUT_MS,
                        ).toCandidates()
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .asSequence()
            .flatten()
            .distinctBy(::candidateDistinctKey)
            .toList()
    }

    private suspend fun fetchLrcLibEntry(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationSeconds: Long?,
    ): LrcLibCandidate? = coroutineScope {
        val attempts = buildList {
            add(
                buildString {
                    append("track_name=").append(trackName.urlEncode())
                    append("&artist_name=").append(artistName.urlEncode())
                    if (!albumName.isNullOrBlank()) {
                        append("&album_name=").append(albumName.urlEncode())
                    }
                    if (durationSeconds != null && durationSeconds > 0L) {
                        append("&duration=").append(durationSeconds)
                    }
                },
            )
            add(
                buildString {
                    append("track_name=").append(trackName.urlEncode())
                    append("&artist_name=").append(artistName.urlEncode())
                    if (durationSeconds != null && durationSeconds > 0L) {
                        append("&duration=").append(durationSeconds)
                    }
                },
            )
            add(
                buildString {
                    append("track_name=").append(trackName.urlEncode())
                    append("&artist_name=").append(artistName.urlEncode())
                    if (!albumName.isNullOrBlank()) {
                        append("&album_name=").append(albumName.urlEncode())
                    }
                },
            )
            add(
                buildString {
                    append("track_name=").append(trackName.urlEncode())
                    append("&artist_name=").append(artistName.urlEncode())
                },
            )
        }.distinct()

        return@coroutineScope attempts
            .map { query ->
                async(ioDispatcher) {
                    withTimeoutOrNull(LRCLIB_DIRECT_REQUEST_TIMEOUT_MS) {
                        getJsonObject(
                            url = "https://lrclib.net/api/get?$query",
                            connectTimeoutMs = DIRECT_REQUEST_CONNECT_TIMEOUT_MS,
                            readTimeoutMs = DIRECT_REQUEST_READ_TIMEOUT_MS,
                        )?.toCandidate()
                    }
                }
            }
            .awaitAll()
            .firstOrNull()
    }

    private suspend fun fetchLyricsOvhFallback(
        song: Song,
        queries: List<LyricsQueryVariant>,
    ): RemoteLyricsMatch? = coroutineScope {
        return@coroutineScope queries
            .map { query ->
                async(ioDispatcher) {
                    withTimeoutOrNull(FALLBACK_REQUEST_TIMEOUT_MS) {
                        val response = getJsonObject(
                            url = "https://api.lyrics.ovh/v1/${query.artist.urlPathEncode()}/${query.title.urlPathEncode()}",
                            connectTimeoutMs = FALLBACK_REQUEST_CONNECT_TIMEOUT_MS,
                            readTimeoutMs = FALLBACK_REQUEST_READ_TIMEOUT_MS,
                        ) ?: return@withTimeoutOrNull null
                        val lines = parsePlainLyrics(response.optNullableString("lyrics")) ?: return@withTimeoutOrNull null
                        val confidence = fallbackConfidenceFor(song, query)
                        if (confidence < MIN_FALLBACK_CONFIDENCE) return@withTimeoutOrNull null
                        RemoteLyricsMatch(
                            payload = LyricsPayload(lines = lines, isSynced = false),
                            source = LyricsSourceKind.RemoteFallback,
                            confidence = confidence,
                        )
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .maxByOrNull { it.confidence }
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
    ): JSONObject? {
        return getText(url, connectTimeoutMs, readTimeoutMs)?.let(::JSONObject)
    }

    private fun getJsonArray(
        url: String,
        connectTimeoutMs: Int = NETWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = NETWORK_READ_TIMEOUT_MS,
    ): JSONArray? {
        return getText(url, connectTimeoutMs, readTimeoutMs)?.let(::JSONArray)
    }

    private fun getText(
        url: String,
        connectTimeoutMs: Int = NETWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = NETWORK_READ_TIMEOUT_MS,
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
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull().also {
            connection.disconnect()
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
        const val NOT_FOUND_CACHE_TTL_MS = 2 * 60 * 1000L
        const val OFFLINE_NOT_FOUND_CACHE_TTL_MS = 90 * 1000L
        const val TIMEOUT_NOT_FOUND_CACHE_TTL_MS = 20 * 1000L
        const val ERROR_CACHE_TTL_MS = 2 * 60 * 1000L
        const val NETWORK_CONNECT_TIMEOUT_MS = 900
        const val NETWORK_READ_TIMEOUT_MS = 2_400
        const val REMOTE_LOOKUP_TOTAL_TIMEOUT_MS = 2_800L
        const val LRCLIB_DIRECT_LOOKUP_TIMEOUT_MS = 2_000L
        const val LRCLIB_SEARCH_LOOKUP_TIMEOUT_MS = 1_500L
        const val REMOTE_SEARCH_PHASE_TIMEOUT_MS = 1_600L
        const val REMOTE_FALLBACK_PHASE_TIMEOUT_MS = 800L
        const val LRCLIB_DIRECT_REQUEST_TIMEOUT_MS = 1_050L
        const val FALLBACK_REQUEST_TIMEOUT_MS = 850L
        const val DIRECT_REQUEST_CONNECT_TIMEOUT_MS = 450
        const val DIRECT_REQUEST_READ_TIMEOUT_MS = 900
        const val SEARCH_REQUEST_CONNECT_TIMEOUT_MS = 500
        const val SEARCH_REQUEST_READ_TIMEOUT_MS = 1_100
        const val FALLBACK_REQUEST_CONNECT_TIMEOUT_MS = 500
        const val FALLBACK_REQUEST_READ_TIMEOUT_MS = 900
        const val MAX_REMOTE_QUERY_VARIANTS = 4
        const val MIN_FALLBACK_CONFIDENCE = 23
        const val PREFERRED_DIRECT_CONFIDENCE = 28
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

internal data class LrcLibCandidate(
    val id: Long?,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Long?,
    val plainLyrics: String,
    val syncedLyrics: String,
    val instrumental: Boolean,
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
        .removeBom()
        .replace("\r\n", "\n")
        .replace('\r', '\n')
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

            val lyricText = sanitizeLyricLine(line.substring(timeTags.last().range.last + 1))
            if (lyricText == null) {
                return@forEach
            }

            timeTags.forEach { match ->
                val startTimeMs = parseTimestampMatch(match)?.plus(offsetMs)?.coerceAtLeast(0L) ?: return@forEach
                parsedLines += LyricsLine(
                    text = lyricText,
                    startTimeMs = startTimeMs,
                )
            }
        }

    val sortedLines = parsedLines
        .sortedBy { it.startTimeMs ?: Long.MAX_VALUE }
        .takeIf { it.isNotEmpty() }
    if (!sortedLines.isNullOrEmpty()) {
        return sortedLines
    }

    return parsePlainLyrics(fallbackPlainLines.joinToString("\n"))
}

internal fun parsePlainLyrics(rawLyrics: String?): List<LyricsLine>? {
    if (rawLyrics.isNullOrBlank()) return null
    val lines = rawLyrics
        .removeBom()
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .lineSequence()
        .map { it.trim() }
        .mapNotNull { line ->
            sanitizeLyricLine(line)?.let { LyricsLine(text = it, startTimeMs = null) }
        }
        .toList()
    if (lines.isEmpty()) return null
    val nonMetadataCount = lines.count { line ->
        !METADATA_ONLY_LINE_REGEX.matches(line.text)
    }
    return lines.takeIf { nonMetadataCount > 0 }
}

internal fun sanitizeLyricLine(line: String): String? {
    val cleaned = line
        .replace('\u00A0', ' ')
        .replace(Regex("""\s{2,}"""), " ")
        .trim()

    if (cleaned.isBlank()) return null
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized.startsWith("translations")) return null
    if (normalized == "embed") return null
    if (normalized.startsWith("you might also like")) return null
    if (normalized.startsWith("submit corrections")) return null
    if (normalized.startsWith("contributors")) return null
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

internal fun buildLyricsSearchQueries(song: Song): List<String> {
    val primaryArtist = extractPrimaryArtist(song.artist)
    val normalizedArtist = normalizeArtistName(song.artist)
    val normalizedTitle = normalizeTrackTitle(song.title)
    val simplifiedTitle = simplifyLookupTitle(song.title)
    return buildList {
        add("${song.artist} ${song.title}")
        add("${primaryArtist} ${song.title}")
        if (song.album.isNotBlank()) add("${song.artist} ${song.title} ${song.album}")
        if (simplifiedTitle.isNotBlank() && simplifiedTitle != song.title) {
            add("${song.artist} $simplifiedTitle")
            add("${primaryArtist} $simplifiedTitle")
        }
        if (normalizedArtist.isNotBlank() && normalizedTitle.isNotBlank()) {
            add("$normalizedArtist $normalizedTitle")
        }
    }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

internal fun List<LrcLibCandidate>.bestRemoteMatchFor(
    song: Song,
    defaultSource: LyricsSourceKind,
): RemoteLyricsMatch? {
    return asSequence()
        .mapNotNull { candidate ->
            val payload = candidate.toLyricsPayload() ?: return@mapNotNull null
            if (!candidate.hasUsableTimingFor(song)) return@mapNotNull null
            val score = candidate.scoreAgainst(song)
            if (!candidate.isAcceptableMatchFor(song, score)) return@mapNotNull null
            RemoteLyricsMatch(
                payload = payload,
                source = defaultSource,
                confidence = score,
            )
        }
        .maxByOrNull { it.confidence }
}

internal fun LrcLibCandidate.toLyricsPayload(): LyricsPayload? {
    if (instrumental) return null

    parseSyncedLyrics(syncedLyrics)?.takeIf { it.isNotEmpty() }?.let { syncedLines ->
        return LyricsPayload(
            lines = syncedLines,
            isSynced = true,
        )
    }

    parsePlainLyrics(plainLyrics)?.takeIf { it.isNotEmpty() }?.let { plainLines ->
        return LyricsPayload(
            lines = plainLines,
            isSynced = false,
        )
    }

    return null
}

internal fun LrcLibCandidate.hasUsableTimingFor(song: Song): Boolean {
    if (song.durationMs <= 0L || durationSeconds == null || durationSeconds <= 0L) return true
    val songDurationSeconds = song.durationMs / 1000L
    val allowedDelta = max(12L, (songDurationSeconds * 0.08f).toLong())
    return abs(durationSeconds - songDurationSeconds) <= allowedDelta
}

internal fun LrcLibCandidate.scoreAgainst(song: Song): Int {
    val songTitle = normalizeTrackTitle(song.title)
    val songArtist = normalizeArtistName(song.artist)
    val songPrimaryArtist = normalizeArtistName(extractPrimaryArtist(song.artist))
    val songAlbum = song.album.normalizeForMatch()
    val candidateTitle = normalizeTrackTitle(trackName)
    val candidateArtist = normalizeArtistName(artistName)
    val candidateAlbum = albumName.normalizeForMatch()

    var score = 0
    if (candidateTitle == songTitle) score += 28
    if (candidateArtist == songArtist || candidateArtist == songPrimaryArtist) score += 26
    if (candidateAlbum.isNotBlank() && candidateAlbum == songAlbum) score += 9
    if (songTitle.isNotBlank() && candidateTitle.isNotBlank() && (candidateTitle.contains(songTitle) || songTitle.contains(candidateTitle))) score += 8
    if (
        songArtist.isNotBlank() &&
        candidateArtist.isNotBlank() &&
        (
            candidateArtist.contains(songArtist) || songArtist.contains(candidateArtist) ||
                candidateArtist.contains(songPrimaryArtist) || songPrimaryArtist.contains(candidateArtist)
            )
    ) {
        score += 8
    }
    if (syncedLyrics.isNotBlank()) score += 10
    score += tokenOverlapBonus(songTitle, candidateTitle, maxBonus = 10)
    score += tokenOverlapBonus(songArtist.ifBlank { songPrimaryArtist }, candidateArtist, maxBonus = 8)
    if (looksLikeAlternateVersion(song.title, trackName)) score -= 8
    durationSeconds?.let { candidateDuration ->
        val songDurationSeconds = song.durationMs / 1000L
        val delta = abs(candidateDuration - songDurationSeconds)
        when {
            delta <= 1L -> score += 12
            delta <= 3L -> score += 9
            delta <= 7L -> score += 5
            delta <= 15L -> score += 2
            songDurationSeconds > 0L -> score -= 7
        }
    }
    return score
}

internal fun LrcLibCandidate.isAcceptableMatchFor(
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

internal fun JSONObject.toCandidate(): LrcLibCandidate {
    return LrcLibCandidate(
        id = optLong("id").takeIf { it > 0L },
        trackName = optString("trackName").ifBlank { optString("name") },
        artistName = optString("artistName"),
        albumName = optString("albumName"),
        durationSeconds = optDouble("duration").takeIf { it > 0.0 }?.toLong(),
        plainLyrics = optNullableString("plainLyrics"),
        syncedLyrics = optNullableString("syncedLyrics"),
        instrumental = optBoolean("instrumental", false),
    )
}

internal fun JSONObject.optNullableString(name: String): String {
    return if (isNull(name)) "" else optString(name)
}

internal fun JSONArray?.toCandidates(): List<LrcLibCandidate> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(item.toCandidate())
        }
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

private fun fallbackConfidenceFor(
    song: Song,
    query: LyricsQueryVariant,
): Int {
    val exactTitle = normalizeTrackTitle(query.title) == normalizeTrackTitle(song.title)
    val exactArtist = normalizeArtistName(query.artist) == normalizeArtistName(song.artist)
    val primaryArtistMatch = normalizeArtistName(query.artist) == normalizeArtistName(extractPrimaryArtist(song.artist))
    return when {
        exactTitle && exactArtist -> 30
        exactTitle && primaryArtistMatch -> 27
        exactTitle -> 23
        else -> 0
    }
}

private fun candidateDistinctKey(candidate: LrcLibCandidate): String {
    candidate.id?.let { return "id::$it" }
    return listOf(
        normalizeTrackTitle(candidate.trackName),
        normalizeArtistName(candidate.artistName),
        candidate.albumName.normalizeForMatch(),
        candidate.durationSeconds.toString(),
    ).joinToString("::")
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

private fun String.urlPathEncode(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")

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
        LyricsPayload(lines = it, isSynced = true)
    }
}

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
