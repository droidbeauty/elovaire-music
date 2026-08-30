package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.IOException
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
            return if (response.statusCode == 200) {
                parse(response.body)
            } else {
                lrclibResponseResult(response.statusCode, response.retryAfterMs)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            LyricsResult.Unavailable
        } catch (_: SecurityException) {
            LyricsResult.Unavailable
        } catch (_: IllegalArgumentException) {
            LyricsResult.Unavailable
        } catch (_: IllegalStateException) {
            LyricsResult.Unavailable
        }
    }

    private fun parse(bytes: ByteArray): LyricsResult {
        if (bytes.isEmpty() || bytes.size > MAX_RESPONSE_BYTES) return LyricsResult.MalformedResponse
        val response = runCatching { JSONObject(bytes.toString(Charsets.UTF_8)) }.getOrNull()
            ?: return LyricsResult.MalformedResponse
        val raw = response.optNullableString("syncedLyrics")
            ?: response.optNullableString("plainLyrics")
            ?: return LyricsResult.NotFound
        return parseLrcOrPlain(raw)?.takeIf { it.lines.isNotEmpty() }?.let(LyricsResult::Found)
            ?: LyricsResult.NotFound
    }

    private fun JSONObject.optNullableString(name: String): String? {
        return opt(name)
            ?.takeUnless { it == JSONObject.NULL }
            ?.toString()
            ?.takeIf(String::isNotBlank)
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 512 * 1024
        const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"
    }
}
