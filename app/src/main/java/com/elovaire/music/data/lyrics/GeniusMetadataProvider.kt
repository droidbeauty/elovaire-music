package elovaire.music.app.data.lyrics

import android.util.Log
import elovaire.music.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class GeniusMetadataProvider(
    private val accessToken: String = BuildConfig.GENIUS_ACCESS_TOKEN,
) {
    fun isConfigured(): Boolean = accessToken.isNotBlank()

    fun bestCanonicalVariant(
        identity: LyricsIdentity,
        variants: List<LyricsQueryVariant>,
    ): LyricsQueryVariant? {
        if (!isConfigured()) return null

        val searchQueries = buildList {
            variants.firstOrNull()?.let { add(listOf(it.artist, it.title).filter(String::isNotBlank).joinToString(" ")) }
            val primaryArtist = extractPrimaryArtist(identity.artist)
            add(listOf(primaryArtist, simplifyLookupTitle(identity.title)).filter(String::isNotBlank).joinToString(" "))
            add(listOf(identity.artist, identity.title).filter(String::isNotBlank).joinToString(" "))
        }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(MAX_QUERY_STRINGS)

        val hits = searchQueries
            .flatMap(::searchHits)
            .distinctBy { it.providerId }

        val best = hits
            .map { candidate -> candidate to candidate.scoreAgainst(identity) }
            .filter { (candidate, score) -> candidate.isAcceptableMatchFor(identity, score) || score >= MINIMUM_SCORE }
            .maxByOrNull { (_, score) -> score }
            ?.first
            ?: return null

        return LyricsQueryVariant(
            artist = best.artist.ifBlank { identity.artist },
            title = best.title.ifBlank { identity.title },
            album = best.album.takeIf { it.isNotBlank() } ?: identity.album.takeIf { it.isNotBlank() },
        )
    }

    private fun searchHits(query: String): List<LyricsCandidate> {
        val response = getJsonObject(buildUrl(query)) ?: return emptyList()
        val hits = response.optJSONObject("response")?.optJSONArray("hits") ?: return emptyList()
        return buildList {
            repeat(hits.length()) { index ->
                hits.optJSONObject(index)
                    ?.optJSONObject("result")
                    ?.toLyricsCandidate()
                    ?.let(::add)
            }
        }
    }

    private fun buildUrl(query: String): String {
        return "$BASE_URL?q=${URLEncoder.encode(query, StandardCharsets.UTF_8.name())}"
    }

    private fun getJsonObject(url: String): JSONObject? = getText(url)?.let(::JSONObject)

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
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            val code = connection.responseCode
            if (code !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        }.getOrElse { throwable ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "genius metadata request failed for $url", throwable)
            }
            null
        }.also {
            connection.disconnect()
        }
    }

    private fun JSONObject.toLyricsCandidate(): LyricsCandidate? {
        val id = optFlexibleLong("id")?.takeIf { it > 0L } ?: return null
        val primaryArtist = optJSONObject("primary_artist")
        val albumObject = optJSONObject("album")
        return LyricsCandidate(
            providerId = "genius::$id",
            title = optNullableString("title"),
            artist = primaryArtist?.optNullableString("name").orEmpty(),
            album = albumObject?.optNullableString("name").orEmpty(),
            durationMs = null,
            instrumental = false,
            plainLyrics = "",
            syncedLyrics = "",
        )
    }

    private fun JSONObject.optNullableString(name: String): String {
        return optString(name).trim()
    }

    private fun JSONObject.optFlexibleLong(name: String): Long? {
        val value = opt(name) ?: return null
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private companion object {
        const val TAG = "GeniusMetadata"
        const val BASE_URL = "https://api.genius.com/search"
        const val CONNECT_TIMEOUT_MS = 250
        const val READ_TIMEOUT_MS = 350
        const val MAX_QUERY_STRINGS = 3
        const val MINIMUM_SCORE = 62
    }
}
