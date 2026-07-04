package elovaire.music.droidbeauty.app.data.lyrics

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.network.readUtf8Bounded
import elovaire.music.droidbeauty.app.domain.model.Song
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class LyricsOvhProvider : LyricsProvider {
    override val providerName: String = "lyrics.ovh"

    override suspend fun findBestLyrics(
        song: Song,
        identity: LyricsIdentity,
        lookupMode: LyricsLookupMode,
    ): ProviderLyricsMatch? {
        if (isWeakLyricsArtist(identity.artist)) return null
        val candidates = buildLyricsQueryVariants(identity)
            .take(MAX_QUERY_VARIANTS)
            .mapIndexed { index, variant ->
                LyricsCandidate(
                    providerId = "lyrics.ovh::$index",
                    title = variant.title,
                    artist = variant.artist,
                    album = variant.album.orEmpty(),
                    durationMs = song.durationMs.takeIf { it > 0L },
                    instrumental = false,
                    plainLyrics = "",
                    syncedLyrics = "",
                    sourceUrl = null,
                )
            }
        return candidates
            .mapNotNull { candidate -> getLyrics(candidate, identity) }
            .filter { it.confidence >= MIN_ACCEPTED_CONFIDENCE }
            .maxByOrNull { it.confidence }
    }

    override suspend fun search(query: LyricsSearchQuery): List<LyricsCandidate> {
        return query.variants
            .take(MAX_QUERY_VARIANTS)
            .mapIndexed { index, variant ->
                LyricsCandidate(
                    providerId = "lyrics.ovh::$index",
                    title = variant.title,
                    artist = variant.artist,
                    album = variant.album.orEmpty(),
                    durationMs = query.identity.durationMs.takeIf { it > 0L },
                    instrumental = false,
                    plainLyrics = "",
                    syncedLyrics = "",
                    sourceUrl = null,
                )
            }
    }

    override suspend fun getLyrics(
        candidate: LyricsCandidate,
        identity: LyricsIdentity,
    ): ProviderLyricsMatch? {
        val response = getJsonObject(buildLyricsUrl(candidate.artist, candidate.title)) ?: return null
        val plainLyrics = response.optString("lyrics").orEmpty()
        val lines = parsePlainLyrics(plainLyrics).orEmpty()
        if (lines.isEmpty()) return null
        val score = candidate.scoreAgainst(identity)
        return ProviderLyricsMatch(
            payload = LyricsPayload(
                lines = lines,
                isSynced = false,
                providerName = providerName,
                confidence = score,
            ),
            confidence = score,
            providerName = providerName,
        )
    }

    private fun buildLyricsUrl(
        artist: String,
        title: String,
    ): String {
        val encodedArtist = URLEncoder.encode(artist.trim(), StandardCharsets.UTF_8.name())
        val encodedTitle = URLEncoder.encode(title.trim(), StandardCharsets.UTF_8.name())
        return "$BASE_URL/$encodedArtist/$encodedTitle"
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
                "Elovaire/${BuildConfig.VERSION_NAME} (Android; Music Player)",
            )
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "lyrics.ovh status=$code")
            }
            if (code !in 200..299) return@runCatching null
            connection.inputStream.use { input ->
                input.readUtf8Bounded(MAX_RESPONSE_BYTES, connection.contentLengthLong)
            }
        }.getOrElse { throwable ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "lyrics.ovh request failed", throwable)
            }
            null
        }.also {
            connection.disconnect()
        }
    }

    private companion object {
        const val TAG = "LyricsOvhProvider"
        const val BASE_URL = "https://api.lyrics.ovh/v1"
        const val CONNECT_TIMEOUT_MS = 1_500
        const val READ_TIMEOUT_MS = 2_000
        const val MAX_QUERY_VARIANTS = 2
        const val MAX_RESPONSE_BYTES = 1 * 1024 * 1024
        const val MIN_ACCEPTED_CONFIDENCE = 62
    }
}
