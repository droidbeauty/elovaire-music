package elovaire.music.droidbeauty.app.data.lyrics

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class LrcLibLyricsProvider : LyricsProvider {
    override val providerName: String = "LRCLIB"

    override suspend fun search(query: LyricsSearchQuery): List<LyricsCandidate> {
        val variants = query.variants.take(MAX_QUERY_VARIANTS)
        if (variants.isEmpty()) return emptyList()

        val exactMatches = coroutineScope {
            variants
                .map { variant ->
                    async { fetchExactCandidate(variant, query.identity) }
                }
                .awaitAll()
        }
            .mapNotNull { it }
            .distinctBy(LyricsCandidate::providerId)

        if (exactMatches.containsSyncedLyrics()) {
            return exactMatches
        }

        val searchMatches = coroutineScope {
            variants
                .map { variant ->
                    async { searchCandidates(variant) }
                }
                .awaitAll()
                .flatten()
        }.distinctBy(LyricsCandidate::providerId)
        val mergedSearchMatches = (exactMatches + searchMatches)
            .distinctBy(LyricsCandidate::providerId)
        if (mergedSearchMatches.containsSyncedLyrics()) {
            return mergedSearchMatches
        }

        val freeTextMatches = coroutineScope {
            variants
                .take(MAX_FREE_TEXT_QUERY_VARIANTS)
                .map { variant ->
                    async { searchCandidatesByFreeText(variant) }
                }
                .awaitAll()
                .flatten()
        }.distinctBy(LyricsCandidate::providerId)

        return (mergedSearchMatches + freeTextMatches)
            .distinctBy(LyricsCandidate::providerId)
    }

    override suspend fun getLyrics(
        candidate: LyricsCandidate,
        identity: LyricsIdentity,
    ): ProviderLyricsMatch? {
        if (candidate.instrumental) return null

        val syncedLines = parseSyncedLyrics(candidate.syncedLyrics)
            ?.takeIf { lines -> lines.any { it.startTimeMs != null } }
        val plainLines = parsePlainLyrics(candidate.plainLyrics)
        val score = candidate.scoreAgainst(identity)
        val timingCorrection = syncedLines?.let { lines ->
            estimateRemoteTimingCorrection(
                lines = lines,
                durationMs = candidate.durationMs ?: identity.durationMs,
            )
        } ?: TimingCorrection.None

        val payload = when {
            !syncedLines.isNullOrEmpty() -> LyricsPayload(
                lines = syncedLines,
                isSynced = true,
                displayTimingOffsetMs = timingCorrection.displayTimingOffsetMs,
                timingScale = timingCorrection.timingScale,
                providerName = providerName,
                confidence = score,
            )
            !plainLines.isNullOrEmpty() -> LyricsPayload(
                lines = plainLines,
                isSynced = false,
                providerName = providerName,
                confidence = score,
            )
            else -> null
        } ?: return null

        return ProviderLyricsMatch(
            payload = payload,
            confidence = score,
            providerName = providerName,
        )
    }

    private fun fetchExactCandidate(
        variant: LyricsQueryVariant,
        identity: LyricsIdentity,
    ): LyricsCandidate? {
        val response = getJsonObject(
            buildUrl(
                LRCLIB_GET_URL,
                linkedMapOf<String, String>().apply {
                    put("track_name", variant.title)
                    put("artist_name", variant.artist)
                    variant.album?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
                    identity.durationMs.takeIf { it > 0L }?.let { put("duration", (it / 1000L).toString()) }
                },
            ),
        ) ?: return null
        return response.toLyricsCandidate()
    }

    private fun searchCandidates(variant: LyricsQueryVariant): List<LyricsCandidate> {
        val response = getJsonArray(
            buildUrl(
                LRCLIB_SEARCH_URL,
                linkedMapOf<String, String>().apply {
                    put("track_name", variant.title)
                    put("artist_name", variant.artist)
                    variant.album?.takeIf { it.isNotBlank() }?.let { put("album_name", it) }
                },
            ),
        ) ?: return emptyList()
        return buildList {
            repeat(response.length()) { index ->
                response.optJSONObject(index)?.toLyricsCandidate()?.let(::add)
            }
        }
    }

    private fun searchCandidatesByFreeText(variant: LyricsQueryVariant): List<LyricsCandidate> {
        val response = getJsonArray(
            buildUrl(
                LRCLIB_SEARCH_URL,
                linkedMapOf(
                    "q" to listOf(variant.artist, variant.title, variant.album.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                ),
            ),
        ) ?: return emptyList()
        return buildList {
            repeat(response.length()) { index ->
                response.optJSONObject(index)?.toLyricsCandidate()?.let(::add)
            }
        }
    }

    private fun estimateRemoteTimingCorrection(
        lines: List<LyricsLine>,
        durationMs: Long,
    ): TimingCorrection {
        val timedLines = lines.mapNotNull(LyricsLine::startTimeMs).sorted()
        if (timedLines.size < 4) return TimingCorrection.None

        val firstTimestampMs = timedLines.first()
        val fourthTimestampMs = timedLines.getOrNull(3)
        val sixthTimestampMs = timedLines.getOrNull(5)
        val lastTimestampMs = timedLines.last()
        val linesInFirst15Seconds = timedLines.count { it <= 15_000L }

        var displayTimingOffsetMs = 0L
        var timingScale = 1f

        if (durationMs >= 90_000L && firstTimestampMs <= 1_500L) {
            displayTimingOffsetMs += when {
                sixthTimestampMs != null && sixthTimestampMs <= 18_000L -> 6_500L
                fourthTimestampMs != null && fourthTimestampMs <= 12_000L -> 4_500L
                linesInFirst15Seconds >= 6 -> 3_000L
                linesInFirst15Seconds >= 4 -> 1_800L
                else -> 0L
            }
        }

        if (durationMs >= 90_000L && lastTimestampMs > 30_000L) {
            val expectedLyricEndMs = (durationMs - 8_000L).coerceAtLeast(1L)
            val ratio = expectedLyricEndMs.toFloat() / lastTimestampMs.toFloat()
            if (kotlin.math.abs(ratio - 1f) >= 0.03f) {
                timingScale = ratio.coerceIn(0.95f, 1.05f)
            }
        }

        return TimingCorrection(
            displayTimingOffsetMs = displayTimingOffsetMs.coerceIn(0L, 12_000L),
            timingScale = timingScale,
        )
    }

    private data class TimingCorrection(
        val displayTimingOffsetMs: Long = 0L,
        val timingScale: Float = 1f,
    ) {
        companion object {
            val None = TimingCorrection()
        }
    }

    private fun getJsonObject(url: String): JSONObject? = getText(url)?.let(::JSONObject)

    private fun getJsonArray(url: String): JSONArray? = getText(url)?.let(::JSONArray)

    private fun getText(url: String): String? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return runCatching {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty(
                "User-Agent",
                "Elovaire/${BuildConfig.VERSION_NAME} (Android; Offline Music Player)",
            )
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        }.getOrElse { throwable ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "lyrics request failed for $url", throwable)
            }
            null
        }.also {
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

    private fun JSONObject.toLyricsCandidate(): LyricsCandidate? {
        val id = optFlexibleLong("id")?.takeIf { it > 0L } ?: return null
        return LyricsCandidate(
            providerId = "lrclib::$id",
            title = optNullableString("trackName").ifBlank { optNullableString("name") },
            artist = optNullableString("artistName"),
            album = optNullableString("albumName"),
            durationMs = optFlexibleDouble("duration")?.times(1000.0)?.toLong(),
            instrumental = optFlexibleFlag("instrumental"),
            plainLyrics = optNullableString("plainLyrics"),
            syncedLyrics = optNullableString("syncedLyrics"),
        )
    }

    private fun JSONObject.optNullableString(name: String): String {
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
                val normalized = value.trim().lowercase()
                normalized == "1" || normalized == "true" || normalized == "yes"
            }
            else -> false
        }
    }

    private fun List<LyricsCandidate>.containsSyncedLyrics(): Boolean {
        return any { it.syncedLyrics.isNotBlank() }
    }

    private companion object {
        const val TAG = "LyricsProvider"
        const val CONNECT_TIMEOUT_MS = 450
        const val READ_TIMEOUT_MS = 700
        const val MAX_QUERY_VARIANTS = 7
        const val MAX_FREE_TEXT_QUERY_VARIANTS = 4
        const val LRCLIB_BASE_URL = "https://lrclib.net/api/"
        const val LRCLIB_GET_URL = "${LRCLIB_BASE_URL}get"
        const val LRCLIB_SEARCH_URL = "${LRCLIB_BASE_URL}search"
    }
}
