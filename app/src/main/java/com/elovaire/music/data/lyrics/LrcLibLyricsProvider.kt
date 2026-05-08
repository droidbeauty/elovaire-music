package elovaire.music.app.data.lyrics

import android.util.Log
import elovaire.music.app.BuildConfig
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

        val exactMatches = variants
            .mapNotNull { variant -> fetchExactCandidate(variant, query.identity) }
            .distinctBy(LyricsCandidate::providerId)

        if (exactMatches.isNotEmpty()) {
            return exactMatches
        }

        return variants
            .flatMap(::searchCandidates)
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

        val payload = when {
            !syncedLines.isNullOrEmpty() -> LyricsPayload(
                lines = syncedLines,
                isSynced = true,
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

    private companion object {
        const val TAG = "LyricsProvider"
        const val CONNECT_TIMEOUT_MS = 900
        const val READ_TIMEOUT_MS = 2200
        const val MAX_QUERY_VARIANTS = 8
        const val LRCLIB_BASE_URL = "https://lrclib.net/api/"
        const val LRCLIB_GET_URL = "${LRCLIB_BASE_URL}get"
        const val LRCLIB_SEARCH_URL = "${LRCLIB_BASE_URL}search"
    }
}
