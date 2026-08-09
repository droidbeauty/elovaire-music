package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class LrclibClient {
    suspend fun fetch(song: Song): LyricsResult = withContext(Dispatchers.IO) {
        try {
            val query = listOf(
                "track_name" to song.title,
                "artist_name" to song.artist,
                "album_name" to song.album,
                "duration" to (song.durationMs / 1_000L).toString(),
            ).joinToString("&") { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }
            val connection = (URL("https://lrclib.net/api/get?$query").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }
            try {
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> parse(connection.inputStream.readLimitedBytes(MAX_RESPONSE_BYTES))
                    HttpURLConnection.HTTP_NOT_FOUND -> LyricsResult.NotFound
                    HttpURLConnection.HTTP_CLIENT_TIMEOUT,
                    HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                    HttpURLConnection.HTTP_UNAVAILABLE,
                    429 -> LyricsResult.Timeout
                    else -> LyricsResult.Timeout
                }
            } finally {
                connection.disconnect()
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

    private fun java.io.InputStream.readLimitedBytes(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8_192)
        while (true) {
            val read = read(buffer)
            if (read < 0) return output.toByteArray()
            if (output.size() + read > limit) return ByteArray(0)
            output.write(buffer, 0, read)
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 10_000
        const val MAX_RESPONSE_BYTES = 512 * 1024
        const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"
    }
}
