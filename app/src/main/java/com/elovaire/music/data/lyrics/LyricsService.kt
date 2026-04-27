package elovaire.music.app.data.lyrics

import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.CoroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max

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

class LyricsService {
    private val cacheLock = Any()
    private val cache = object : LinkedHashMap<String, CachedLyricsEntry>(MAX_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, CachedLyricsEntry>?,
        ): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<LyricsResult>>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    suspend fun fetchLyrics(song: Song): LyricsResult = coroutineScope {
        val cacheKey = buildCacheKey(song)
        cachedLyrics(song)?.let { return@coroutineScope it }

        val existingRequest = inFlightRequests[cacheKey]
        if (existingRequest != null) {
            return@coroutineScope existingRequest.await()
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song)
            }.getOrDefault(LyricsResult.NotFound)
        }
        val activeRequest = inFlightRequests.putIfAbsent(cacheKey, request) ?: request
        if (activeRequest !== request) {
            request.cancel()
        }

        try {
            val result = activeRequest.await()
            synchronized(cacheLock) {
                cache[cacheKey] = CachedLyricsEntry(
                    result = result,
                    expiresAtMillis = when (result) {
                        is LyricsResult.Found -> Long.MAX_VALUE
                        LyricsResult.NotFound -> System.currentTimeMillis() + NOT_FOUND_CACHE_TTL_MS
                    },
                )
            }
            result
        } finally {
            inFlightRequests.remove(cacheKey, activeRequest)
        }
    }

    suspend fun prefetchLyrics(song: Song) {
        fetchLyrics(song)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun resolveLyrics(song: Song): LyricsResult = coroutineScope {
        fetchPrimaryDirectMatch(song)?.let { return@coroutineScope LyricsResult.Found(it) }

        val fallbackDirectDeferred = async { fetchFallbackDirectMatch(song) }
        val searchMatchDeferred = async { searchBestMatch(song) }

        val fallbackDirectMatch = select<LyricsPayload?> {
            fallbackDirectDeferred.onAwait { it }
            onTimeout(DIRECT_MATCH_FAST_PATH_TIMEOUT_MS) { null }
        } ?: run {
            val searchMatch = searchMatchDeferred.await()
            if (searchMatch != null) {
                fallbackDirectDeferred.cancel()
                return@coroutineScope LyricsResult.Found(searchMatch)
            }
            fallbackDirectDeferred.await()
        }
        if (fallbackDirectMatch != null) {
            searchMatchDeferred.cancel()
            return@coroutineScope LyricsResult.Found(fallbackDirectMatch)
        }

        val searchMatch = searchMatchDeferred.await()
        if (searchMatch != null) {
            LyricsResult.Found(searchMatch)
        } else {
            LyricsResult.NotFound
        }
    }

    private suspend fun searchBestMatch(song: Song): LyricsPayload? {
        return searchLrcLib(song).bestLyricsPayloadFor(song)
    }

    private fun fetchPrimaryDirectMatch(song: Song): LyricsPayload? {
        val durationSeconds = durationSeconds(song.durationMs)
        return listOf(
            Triple(song.title, song.artist, song.album.takeIf { it.isNotBlank() }),
            Triple(song.title, song.artist, null),
        ).mapNotNull { (trackName, artistName, albumName) ->
            fetchLrcLibEntry(
                trackName = trackName,
                artistName = artistName,
                albumName = albumName,
                durationSeconds = durationSeconds,
            )
        }.bestLyricsPayloadFor(song)
    }

    private suspend fun fetchFallbackDirectMatch(song: Song): LyricsPayload? = coroutineScope {
        val normalizedTitle = normalizeTrackTitle(song.title)
        val normalizedArtist = normalizeArtistName(song.artist)
        val normalizedAlbum = song.album.takeIf { it.isNotBlank() }
        val durationSeconds = durationSeconds(song.durationMs)

        val requests = buildList {
            if (normalizedTitle.isNotBlank() && normalizedArtist.isNotBlank()) {
                add(Triple(normalizedTitle, normalizedArtist, normalizedAlbum))
                add(Triple(normalizedTitle, normalizedArtist, null))
            }
        }.distinct()

        requests
            .map { (trackName, artistName, albumName) ->
                async {
                    fetchLrcLibEntry(
                        trackName = trackName,
                        artistName = artistName,
                        albumName = albumName,
                        durationSeconds = durationSeconds,
                    )
                }
            }
            .mapNotNull { it.await() }
            .bestLyricsPayloadFor(song)
    }

    private fun fetchLrcLibEntry(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationSeconds: Long?,
    ): LrcLibCandidate? {
        val query = buildString {
            append("track_name=").append(trackName.urlEncode())
            append("&artist_name=").append(artistName.urlEncode())
            if (!albumName.isNullOrBlank()) {
                append("&album_name=").append(albumName.urlEncode())
            }
            if (durationSeconds != null && durationSeconds > 0L) {
                append("&duration=").append(durationSeconds)
            }
        }
        return getJsonObject("https://lrclib.net/api/get?$query")?.toCandidate()
    }

    private suspend fun searchLrcLib(song: Song): List<LrcLibCandidate> = coroutineScope {
        val normalizedArtist = normalizeArtistName(song.artist)
        val normalizedTitle = normalizeTrackTitle(song.title)
        val requests = buildList {
            add(
                buildString {
                    append("artist_name=").append(song.artist.urlEncode())
                    append("&track_name=").append(song.title.urlEncode())
                },
            )
            if (
                normalizedArtist.isNotBlank() &&
                normalizedTitle.isNotBlank() &&
                (normalizedArtist != song.artist.normalizeForMatch() || normalizedTitle != normalizeTrackTitle(song.title))
            ) {
                add(
                    buildString {
                        append("artist_name=").append(normalizedArtist.urlEncode())
                        append("&track_name=").append(normalizedTitle.urlEncode())
                    },
                )
            }
        }.distinct()

        return@coroutineScope requests
            .map { request ->
                async {
                    val url = "https://lrclib.net/api/search?$request"
                    getJsonArray(url).toCandidates()
                }
            }
            .flatMap { it.await().asSequence() }
            .distinctBy { candidate ->
                listOf(candidate.trackName, candidate.artistName, candidate.albumName).joinToString("::").lowercase()
            }
            .toList()
    }

    private fun JSONObject.toCandidate(): LrcLibCandidate {
        return LrcLibCandidate(
            trackName = optString("trackName").ifBlank { optString("name") },
            artistName = optString("artistName"),
            albumName = optString("albumName"),
            durationSeconds = optDouble("duration").takeIf { it > 0.0 }?.toLong(),
            plainLyrics = optNullableString("plainLyrics"),
            syncedLyrics = optNullableString("syncedLyrics"),
            instrumental = optBoolean("instrumental", false),
        )
    }

    private fun JSONObject.optNullableString(name: String): String {
        return if (isNull(name)) "" else optString(name)
    }

    private fun JSONArray?.toCandidates(): List<LrcLibCandidate> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(item.toCandidate())
            }
        }
    }

    private fun LrcLibCandidate.toLyricsPayload(): LyricsPayload? {
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

    private fun List<LrcLibCandidate>.bestLyricsPayloadFor(song: Song): LyricsPayload? {
        return asSequence()
            .mapNotNull { candidate ->
                val payload = candidate.toLyricsPayload() ?: return@mapNotNull null
                if (!candidate.hasUsableTimingFor(song)) return@mapNotNull null
                val score = candidate.scoreAgainst(song)
                if (!candidate.isAcceptableMatchFor(song, score)) return@mapNotNull null
                Triple(candidate, payload, score)
            }
            .maxByOrNull { (_, _, score) -> score }
            ?.second
    }

    private fun parseSyncedLyrics(rawLyrics: String?): List<LyricsLine>? {
        if (rawLyrics.isNullOrBlank()) return null
        val timeTagRegex = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]([^\n]*)""")
        val lines = rawLyrics.lineSequence()
            .map { it.trim() }
            .mapNotNull { line ->
                val match = timeTagRegex.matchEntire(line) ?: return@mapNotNull null
                val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
                val fractional = match.groupValues[3]
                val text = sanitizeLyricLine(match.groupValues[4]) ?: return@mapNotNull null
                val millis = when (fractional.length) {
                    0 -> 0L
                    1 -> fractional.toLong() * 100L
                    2 -> fractional.toLong() * 10L
                    else -> fractional.take(3).toLong()
                }
                LyricsLine(
                    text = text,
                    startTimeMs = minutes * 60_000L + seconds * 1_000L + millis,
                )
            }
            .sortedBy { it.startTimeMs ?: Long.MAX_VALUE }
            .toList()

        return lines.takeIf { it.isNotEmpty() }
    }

    private fun parsePlainLyrics(rawLyrics: String?): List<LyricsLine>? {
        if (rawLyrics.isNullOrBlank()) return null
        return rawLyrics.lineSequence()
            .map { it.trim() }
            .mapNotNull { line ->
                sanitizeLyricLine(line)?.let { LyricsLine(text = it, startTimeMs = null) }
            }
            .toList()
            .takeIf { it.isNotEmpty() }
    }

    private fun sanitizeLyricLine(line: String): String? {
        val cleaned = line
            .replace('\u00A0', ' ')
            .replace(Regex("""\s{2,}"""), " ")
            .trim()

        if (cleaned.isBlank()) return null
        val normalized = cleaned.lowercase()
        if (normalized.startsWith("translations")) return null
        if (normalized == "embed") return null
        if (normalized.startsWith("you might also like")) return null
        return cleaned
    }

    private fun getJsonObject(url: String): JSONObject? {
        return getText(url)?.let(::JSONObject)
    }

    private fun getJsonArray(url: String): JSONArray? {
        return getText(url)?.let(::JSONArray)
    }

    private fun getText(url: String): String? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return runCatching {
            connection.requestMethod = "GET"
            connection.connectTimeout = NETWORK_CONNECT_TIMEOUT_MS
            connection.readTimeout = NETWORK_READ_TIMEOUT_MS
            connection.setRequestProperty(
                "User-Agent",
                "Elovaire/1.0 (Android; Offline Music Player)",
            )
            connection.setRequestProperty("Accept", "application/json")
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull().also {
            connection.disconnect()
        }
    }

    private fun buildCacheKey(song: Song): String {
        return listOf(
            normalizeArtistName(song.artist),
            song.album.normalizeForMatch(),
            normalizeTrackTitle(song.title),
        ).joinToString("::")
    }

    private fun durationSeconds(durationMs: Long): Long? {
        return (durationMs.takeIf { it > 0L } ?: return null) / 1000L
    }

    private companion object {
        const val MAX_CACHE_ENTRIES = 96
        const val DIRECT_MATCH_FAST_PATH_TIMEOUT_MS = 650L
        const val NETWORK_CONNECT_TIMEOUT_MS = 3_500
        const val NETWORK_READ_TIMEOUT_MS = 6_500
        const val NOT_FOUND_CACHE_TTL_MS = 15 * 60 * 1000L
    }
}

private data class CachedLyricsEntry(
    val result: LyricsResult,
    val expiresAtMillis: Long,
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean = nowMillis >= expiresAtMillis
}

private data class LrcLibCandidate(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Long?,
    val plainLyrics: String,
    val syncedLyrics: String,
    val instrumental: Boolean,
) {
    fun hasUsableTimingFor(song: Song): Boolean {
        if (song.durationMs <= 0L || durationSeconds == null || durationSeconds <= 0L) return true
        val songDurationSeconds = song.durationMs / 1000L
        val allowedDelta = max(12L, (songDurationSeconds * 0.08f).toLong())
        return abs(durationSeconds - songDurationSeconds) <= allowedDelta
    }

    fun scoreAgainst(song: Song): Int {
        val songTitle = normalizeTrackTitle(song.title)
        val songArtist = normalizeArtistName(song.artist)
        val songAlbum = song.album.normalizeForMatch()
        val candidateTitle = normalizeTrackTitle(trackName)
        val candidateArtist = normalizeArtistName(artistName)
        val candidateAlbum = albumName.normalizeForMatch()

        var score = 0
        if (candidateTitle == songTitle) score += 16
        if (candidateArtist == songArtist) score += 16
        if (candidateAlbum.isNotBlank() && candidateAlbum == songAlbum) score += 8
        if (songTitle.isNotBlank() && candidateTitle.isNotBlank() && (candidateTitle.contains(songTitle) || songTitle.contains(candidateTitle))) score += 5
        if (songArtist.isNotBlank() && candidateArtist.isNotBlank() && (candidateArtist.contains(songArtist) || songArtist.contains(candidateArtist))) score += 5
        if (!syncedLyrics.isNullOrBlank()) score += 6
        durationSeconds?.let { candidateDuration ->
            val songDurationSeconds = song.durationMs / 1000L
            val delta = abs(candidateDuration - songDurationSeconds)
            when {
                delta <= 1L -> score += 10
                delta <= 3L -> score += 8
                delta <= 7L -> score += 4
                delta <= 15L -> score += 2
                songDurationSeconds > 0L -> score -= 8
            }
        }
        return score
    }

    fun isAcceptableMatchFor(
        song: Song,
        score: Int,
    ): Boolean {
        val songTitle = normalizeTrackTitle(song.title)
        val songArtist = normalizeArtistName(song.artist)
        val candidateTitle = normalizeTrackTitle(trackName)
        val candidateArtist = normalizeArtistName(artistName)
        val exactTitle = candidateTitle.isNotBlank() && candidateTitle == songTitle
        val exactArtist = candidateArtist.isNotBlank() && candidateArtist == songArtist
        val titleOverlap = candidateTitle.isNotBlank() &&
            songTitle.isNotBlank() &&
            (candidateTitle.contains(songTitle) || songTitle.contains(candidateTitle))
        val artistOverlap = candidateArtist.isNotBlank() &&
            songArtist.isNotBlank() &&
            (candidateArtist.contains(songArtist) || songArtist.contains(candidateArtist))

        return when {
            exactTitle && exactArtist -> score >= 22
            exactTitle && artistOverlap -> score >= 26
            exactArtist && titleOverlap -> score >= 26
            else -> false
        }
    }
}

private fun normalizeTrackTitle(value: String): String {
    return value
        .lowercase()
        .replace("&", "and")
        .replace(Regex("""(?i)\b(feat|ft|featuring)\b.*$"""), "")
        .replace(Regex("""(?i)\b(remaster(ed)?|live|mono|stereo|version)\b"""), "")
        .replace(Regex("""\([^)]*\)|\[[^]]*]"""), "")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()
}

private fun normalizeArtistName(value: String): String {
    return value.normalizeForMatch()
}

private fun String.normalizeForMatch(): String {
    return lowercase()
        .replace("&", "and")
        .replace(Regex("""[^a-z0-9]+"""), " ")
        .trim()
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
