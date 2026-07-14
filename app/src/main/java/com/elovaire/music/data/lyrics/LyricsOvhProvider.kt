package elovaire.music.droidbeauty.app.data.lyrics

import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.network.HttpRequest
import elovaire.music.droidbeauty.app.data.network.HttpTransport
import elovaire.music.droidbeauty.app.domain.model.Song
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class LyricsOvhProvider : LyricsProvider {
    override val providerName: String = "lyrics.ovh"
    private val httpTransport = HttpTransport()

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
                sourceTextForEmbedding = plainLyrics.canonicalEmbeddedLyricsText(),
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
        return runCatching {
            httpTransport.getText(
                request = HttpRequest(
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
        }.getOrElse { throwable ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "lyrics.ovh request failed", throwable)
            }
            null
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
