package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.domain.model.Song
import java.net.URLEncoder
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

internal class LrclibClient(
    private val transport: BoundedHttpTransport = BoundedHttpTransport(),
) {
    suspend fun fetch(song: Song): LyricsResult {
        return try {
            val query = listOf(
                "track_name" to song.title,
                "artist_name" to song.artist,
                "album_name" to song.album,
                "duration" to (song.durationMs / 1_000L).toString(),
            ).joinToString("&") { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }
            val response = transport.get(
                rawUrl = "https://lrclib.net/api/get?$query",
                headers = mapOf(
                    "User-Agent" to USER_AGENT,
                    "Accept" to "application/json",
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
            return when (response.statusCode) {
                200 -> parse(response.body)
                404 -> LyricsResult.NotFound
                408, 429, 500, 502, 503, 504 -> LyricsResult.Timeout
                else -> LyricsResult.Timeout
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            LyricsResult.Timeout
        }
    }

    private fun parse(bytes: ByteArray): LyricsResult {
        if (bytes.isEmpty() || bytes.size > MAX_RESPONSE_BYTES) return LyricsResult.Timeout
        val response = runCatching { JSONObject(bytes.toString(Charsets.UTF_8)) }.getOrNull() ?: return LyricsResult.Timeout
        val raw = response.optString("syncedLyrics").takeIf(String::isNotBlank)
            ?: response.optString("plainLyrics").takeIf(String::isNotBlank)
            ?: return LyricsResult.NotFound
        return parseLrcOrPlain(raw)?.takeIf { it.lines.isNotEmpty() }?.let(LyricsResult::Found)
            ?: LyricsResult.NotFound
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 512 * 1024
        const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"
    }
}
