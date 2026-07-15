package elovaire.music.droidbeauty.app.data.lyrics

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.network.HttpFailureKind
import elovaire.music.droidbeauty.app.data.network.HttpRequest
import elovaire.music.droidbeauty.app.data.network.HttpTransport
import elovaire.music.droidbeauty.app.data.network.HttpTransportException
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Collections

internal interface LrcLibApi {
    suspend fun get(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationSeconds: Int?,
    ): LrcLibResponse?

    suspend fun search(
        query: String? = null,
        trackName: String? = null,
        artistName: String? = null,
        albumName: String? = null,
    ): List<LrcLibResponse>
}

internal class LrcLibLyricsProvider(
    private val api: LrcLibApi = DefaultLrcLibApi(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LyricsProvider {
    override val providerName: String = "LRCLIB"

    override suspend fun findBestLyrics(
        song: Song,
        identity: LyricsIdentity,
        lookupMode: LyricsLookupMode,
    ): ProviderLyricsMatch? = withContext(ioDispatcher) {
        val primaryArtist = extractPrimaryArtist(song.artist)
        val weakArtist = isWeakLyricsArtist(song.artist)
        val baseTitle = baseTitleForLyricsMatch(song.title).ifBlank { song.title }
        val cleanedTitle = cleanedTitleForFallback(song.title).ifBlank { baseTitle }
        val durationSeconds = song.durationMs.takeIf { it > 0L }?.div(1000L)?.toInt()
        val variants = buildLyricsQueryVariants(identity)
        val exactVariant = variants.firstOrNull() ?: LyricsQueryVariant(
            artist = song.artist,
            title = song.title,
            album = song.album.takeIf { it.isNotBlank() },
        )

        val strategies = buildList {
            if (!weakArtist) {
                add(
                    LyricsSearchStrategy(name = "exact_get") {
                        listOfNotNull(
                            api.get(
                                trackName = exactVariant.title,
                                artistName = exactVariant.artist,
                                albumName = exactVariant.album,
                                durationSeconds = durationSeconds,
                            ),
                        )
                    },
                )
                add(
                    LyricsSearchStrategy(name = "artist_track_search") {
                        api.search(
                            trackName = exactVariant.title,
                            artistName = exactVariant.artist,
                            albumName = exactVariant.album,
                        )
                    },
                )
                add(
                    LyricsSearchStrategy(name = "combined_query") {
                        api.search(query = "$primaryArtist $baseTitle".trim())
                    },
                )
            }
            if (!weakArtist && (baseTitle != exactVariant.title || primaryArtist != exactVariant.artist)) {
                add(
                    LyricsSearchStrategy(name = "simplified_variant") {
                        api.search(
                            trackName = baseTitle,
                            artistName = primaryArtist,
                            albumName = exactVariant.album,
                        )
                    },
                )
            }
            if (weakArtist && baseTitle.isNotBlank()) {
                add(
                    LyricsSearchStrategy(name = "title_only_base") {
                        api.search(trackName = baseTitle)
                    },
                )
            }
            if (cleanedTitle.isNotBlank() && cleanedTitle != baseTitle) {
                add(
                    LyricsSearchStrategy(name = "title_only_fallback") {
                        api.search(trackName = cleanedTitle)
                    },
                )
            }
        }

        val responses = runFastStrategies(
            strategies = strategies,
            timeoutMs = if (lookupMode == LyricsLookupMode.Full) FULL_LOOKUP_TIMEOUT_MS else FAST_LOOKUP_TIMEOUT_MS,
        )
        val ranked = rankLrcLibMatches(song, responses)
        val best = ranked.firstOrNull() ?: run {
            logDebug("no acceptable candidate song=${song.id} candidates=${responses.size}")
            return@withContext null
        }
        logDebug(
            "selected song=${song.id} candidate=${best.response.id} score=${best.score} " +
                "synced=${!best.response.syncedLyrics.isNullOrBlank()}",
        )
        return@withContext best.response.toProviderLyricsMatch(best.score, providerName)
    }

    private fun LrcLibResponse.toProviderLyricsMatch(
        confidence: Int,
        providerName: String,
    ): ProviderLyricsMatch? {
        if (instrumental) return null

        val syncedPayload = syncedLyrics
            ?.takeIf { it.isNotBlank() }
            ?.let { parseLrcOrPlain(it, providerName, confidence) }
            ?.takeIf { it.isSynced && it.lines.isNotEmpty() }

        val payload = syncedPayload ?: plainLyrics
            ?.takeIf { it.isNotBlank() }
            ?.let { parseLrcOrPlain(it, providerName, confidence) }
            ?.takeIf { it.lines.isNotEmpty() }
            ?: syncedLyrics
                ?.takeIf { it.isNotBlank() }
                ?.let { parseLrcOrPlain(it, providerName, confidence) }
                ?.takeIf { it.lines.isNotEmpty() }

        return payload?.let {
            ProviderLyricsMatch(
                payload = it,
                confidence = confidence,
                providerName = providerName,
            )
        }
    }

    private data class LyricsSearchStrategy(
        val name: String,
        val block: suspend () -> List<LrcLibResponse>,
    )

    private suspend fun runFastStrategies(
        strategies: List<LyricsSearchStrategy>,
        timeoutMs: Long,
    ): List<LrcLibResponse> = coroutineScope {
        val collected = Collections.synchronizedList(mutableListOf<LrcLibResponse>())

        val jobs = strategies.map { strategy ->
            async(ioDispatcher) {
                val results = runCatching { strategy.block() }
                    .getOrElse { throwable ->
                        if (throwable is CancellationException) throw throwable
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "strategy ${strategy.name} failed", throwable)
                        }
                        emptyList()
                    }
                    .filter { it.hasLyrics() }
                    .distinctBy { it.id }
                collected.addAll(results)
                results
            }
        }

        withTimeoutOrNull(timeoutMs) { jobs.joinAll() }
        jobs.forEach { it.cancelAndJoin() }
        synchronized(collected) { collected.toList() }
            .filter { it.hasLyrics() }
            .distinctBy { it.id }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "LrcLibLyrics"
        const val FULL_LOOKUP_TIMEOUT_MS = 6_500L
        const val FAST_LOOKUP_TIMEOUT_MS = 1_500L
    }
}

private class DefaultLrcLibApi(
    private val clock: AppClock = AndroidAppClock,
) : LrcLibApi {
    private val httpTransport = HttpTransport()
    private val rateLimitMutex = Mutex()
    private val requestTimestampsMs = ArrayDeque<Long>()

    override suspend fun get(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationSeconds: Int?,
    ): LrcLibResponse? {
        val query = linkedMapOf(
            "track_name" to trackName,
            "artist_name" to artistName,
        ).apply {
            albumName?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
            durationSeconds?.takeIf { it > 0 }?.let { put("duration", it.toString()) }
        }
        return requestObject("$BASE_URL/get", query)?.toLrcLibResponse()
    }

    override suspend fun search(
        query: String?,
        trackName: String?,
        artistName: String?,
        albumName: String?,
    ): List<LrcLibResponse> {
        val params = linkedMapOf<String, String>().apply {
            query?.takeIf { it.isNotBlank() }?.let { put("q", it) }
            trackName?.takeIf { it.isNotBlank() }?.let { put("track_name", it) }
            artistName?.takeIf { it.isNotBlank() }?.let { put("artist_name", it) }
            albumName?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
        }
        val response = requestArray("$BASE_URL/search", params) ?: return emptyList()
        return buildList {
            repeat(response.length()) { index ->
                response.optJSONObject(index)?.toLrcLibResponse()?.let(::add)
            }
        }
    }

    private suspend fun requestObject(
        baseUrl: String,
        queryParameters: Map<String, String>,
    ): JSONObject? = requestText(baseUrl, queryParameters)?.let(::JSONObject)

    private suspend fun requestArray(
        baseUrl: String,
        queryParameters: Map<String, String>,
    ): JSONArray? = requestText(baseUrl, queryParameters)?.let(::JSONArray)

    private suspend fun requestText(
        baseUrl: String,
        queryParameters: Map<String, String>,
    ): String? {
        val url = buildUrl(baseUrl, queryParameters)
        repeat(NETWORK_RETRY_ATTEMPTS) { attempt ->
            awaitRateLimitSlot()
            try {
                return httpTransport.getText(
                    HttpRequest(
                        url = url,
                        accept = "application/json",
                        headers = mapOf(
                            "User-Agent" to "Elovaire/${BuildConfig.VERSION_NAME} (Android; Music Player)",
                        ),
                        connectTimeoutMs = CONNECT_TIMEOUT_MS,
                        readTimeoutMs = READ_TIMEOUT_MS,
                    ),
                    maxBytes = MAX_RESPONSE_BYTES,
                )
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                if (throwable is HttpTransportException && throwable.statusCode == 404) return null
                if (!shouldRetry(throwable) || attempt == NETWORK_RETRY_ATTEMPTS - 1) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "lrclib request failed", throwable)
                    }
                    return null
                }
                delay(NETWORK_RETRY_INITIAL_DELAY_MS * (attempt + 1))
            }
        }
        return null
    }

    private suspend fun awaitRateLimitSlot() {
        while (true) {
            val waitMs = rateLimitMutex.withLock {
                val now = clock.elapsedTimeMs()
                while (requestTimestampsMs.isNotEmpty() && now - requestTimestampsMs.first() >= 60_000L) {
                    requestTimestampsMs.removeFirst()
                }
                val lastCallDelayMs = requestTimestampsMs.lastOrNull()
                    ?.let { lastCall -> (LRCLIB_MIN_DELAY_MS - (now - lastCall)).coerceAtLeast(0L) }
                    ?: 0L
                val windowDelayMs = if (requestTimestampsMs.size >= LRCLIB_MAX_CALLS_PER_MINUTE) {
                    (60_000L - (now - requestTimestampsMs.first())).coerceAtLeast(0L)
                } else {
                    0L
                }
                val requiredDelayMs = maxOf(lastCallDelayMs, windowDelayMs)
                if (requiredDelayMs <= 0L) {
                    requestTimestampsMs.addLast(now)
                }
                requiredDelayMs
            }
            if (waitMs <= 0L) return
            delay(waitMs)
        }
    }

    private fun shouldRetry(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.util.concurrent.CancellationException -> false
            is HttpTransportException -> throwable.kind == HttpFailureKind.Transport ||
                throwable.statusCode?.let { it == 429 || it in 500..599 } == true
            is IOException -> true
            else -> false
        }
    }

    private fun JSONObject.toLrcLibResponse(): LrcLibResponse? {
        val id = optFlexibleInt("id") ?: return null
        val name = optNullableString("trackName").ifBlank { optNullableString("name") }
        val artistName = optNullableString("artistName")
        if (name.isBlank() || artistName.isBlank()) return null
        return LrcLibResponse(
            id = id,
            name = name,
            artistName = artistName,
            albumName = optNullableString("albumName").ifBlank { null },
            duration = optFlexibleDouble("duration") ?: 0.0,
            plainLyrics = optNullableString("plainLyrics").ifBlank { null },
            syncedLyrics = optNullableString("syncedLyrics").ifBlank { null },
            instrumental = optFlexibleBoolean("instrumental"),
        )
    }

    private fun buildUrl(
        baseUrl: String,
        queryParameters: Map<String, String>,
    ): String {
        if (queryParameters.isEmpty()) return baseUrl
        return buildString {
            append(baseUrl)
            append('?')
            queryParameters
                .filterValues { it.isNotBlank() }
                .entries
                .forEachIndexed { index, (name, value) ->
                    if (index > 0) append('&')
                    append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()))
                    append('=')
                    append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                }
        }
    }

    private fun JSONObject.optNullableString(name: String): String {
        return if (isNull(name)) "" else optString(name).trim()
    }

    private fun JSONObject.optFlexibleInt(name: String): Int? {
        val value = opt(name) ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull()
            else -> null
        }
    }

    private fun JSONObject.optFlexibleDouble(name: String): Double? {
        val value = opt(name) ?: return null
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.trim().toDoubleOrNull()
            else -> null
        }
    }

    private fun JSONObject.optFlexibleBoolean(name: String): Boolean {
        val value = opt(name) ?: return false
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", ignoreCase = true) || value == "1"
            else -> false
        }
    }

    private companion object {
        const val TAG = "LrcLibApi"
        const val BASE_URL = "https://lrclib.net/api"
        const val CONNECT_TIMEOUT_MS = 2_500
        const val READ_TIMEOUT_MS = 3_500
        const val LRCLIB_MIN_DELAY_MS = 100L
        const val LRCLIB_MAX_CALLS_PER_MINUTE = 30
        const val MAX_RESPONSE_BYTES = 1 * 1024 * 1024
        const val NETWORK_RETRY_ATTEMPTS = 2
        const val NETWORK_RETRY_INITIAL_DELAY_MS = 350L
    }
}
