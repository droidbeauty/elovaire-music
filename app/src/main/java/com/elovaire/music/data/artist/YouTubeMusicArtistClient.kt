package elovaire.music.droidbeauty.app.data.artist

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class YouTubeMusicArtistClient {
    suspend fun findArtistImage(artistName: String): String? = withContext(Dispatchers.IO) {
        val query = artistName.trim().takeIf { it.isNotBlank() } ?: return@withContext null
        val connection = (URL(SEARCH_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        try {
            connection.outputStream.use { it.write(request(query).toString().toByteArray()) }
            if (connection.responseCode !in 200..299) return@withContext null
            findThumbnail(JSONObject(connection.inputStream.bufferedReader().use { it.readText() }), query)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun request(query: String) = JSONObject().apply {
        put("context", JSONObject().put("client", JSONObject().put("clientName", "WEB_REMIX").put("clientVersion", "1.20240801.01.00")))
        put("query", query)
    }

    private fun findThumbnail(value: Any?, query: String): String? = when (value) {
        is JSONObject -> {
            val text = value.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text").orEmpty()
            val thumbnails = value.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
            val match = if (text.equals(query, ignoreCase = true)) thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url") else null
            match?.takeIf { it.startsWith("https://") }
                ?: value.keys().asSequence().mapNotNull { findThumbnail(value.opt(it), query) }.firstOrNull()
        }
        is JSONArray -> (0 until value.length()).asSequence().mapNotNull { findThumbnail(value.opt(it), query) }.firstOrNull()
        else -> null
    }

    private companion object {
        const val SEARCH_URL = "https://music.youtube.com/youtubei/v1/search?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    }
}
