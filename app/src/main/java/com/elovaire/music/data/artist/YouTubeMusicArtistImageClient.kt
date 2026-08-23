package elovaire.music.droidbeauty.app.data.artist

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import java.util.Locale
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

internal class YouTubeMusicArtistImageClient(
    private val transport: BoundedHttpTransport = BoundedHttpTransport(
        connectTimeoutMs = 6_000,
        readTimeoutMs = 8_000,
    ),
) {
    suspend fun findArtistImage(artistName: String): String? {
        val query = artistName.trim().takeIf { it.isNotEmpty() } ?: return null
        val requestBody = runCatching {
            JSONObject()
                .put(
                    "context",
                    JSONObject().put(
                        "client",
                        JSONObject()
                            .put("clientName", "WEB_REMIX")
                            .put("clientVersion", CLIENT_VERSION)
                            .put("hl", "en")
                            .put("gl", "US"),
                    ),
                )
                .put("query", query)
                .toString()
                .toByteArray(Charsets.UTF_8)
        }.getOrNull() ?: return null
        val response = try {
            transport.post(
                rawUrl = SEARCH_URL,
                body = requestBody,
                headers = mapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json",
                    "User-Agent" to USER_AGENT,
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return null
        }
        if (response.statusCode !in 200..299 || response.body.isEmpty()) return null
        return runCatching {
            findArtistThumbnail(JSONObject(response.body.toString(Charsets.UTF_8)), query)
        }.getOrNull()
    }

    private companion object {
        const val SEARCH_URL = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"
        const val CLIENT_VERSION = "1.20250101.01.00"
        const val MAX_RESPONSE_BYTES = 768 * 1024
        const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"

        fun findArtistThumbnail(response: JSONObject, query: String): String? {
            val sections = response
                .optJSONObject("contents")
                ?.optJSONObject("tabbedSearchResultsRenderer")
                ?.optJSONArray("tabs")
                ?.let(::firstSearchSections)
                ?: return null
            val normalizedQuery = normalize(query)
            var firstArtistImage: String? = null
            for (sectionIndex in 0 until sections.length()) {
                val section = sections.optJSONObject(sectionIndex) ?: continue
                val contents = section
                    .optJSONObject("itemSectionRenderer")
                    ?.optJSONArray("contents")
                    ?: continue
                for (itemIndex in 0 until contents.length()) {
                    val card = contents
                        .optJSONObject(itemIndex)
                        ?.optJSONObject("musicCardShelfRenderer")
                        ?: continue
                    val subtitle = textOf(card.optJSONObject("subtitle"))
                    if (!subtitle.contains("artist", ignoreCase = true)) continue
                    val image = thumbnailUrl(card) ?: continue
                    if (firstArtistImage == null) firstArtistImage = image
                    if (normalize(textOf(card.optJSONObject("title"))) == normalizedQuery) {
                        return image
                    }
                }
            }
            return firstArtistImage
        }

        fun firstSearchSections(tabs: JSONArray): JSONArray? {
            for (index in 0 until tabs.length()) {
                val tab = tabs.optJSONObject(index)
                    ?.optJSONObject("tabRenderer")
                    ?: continue
                val sections = tab
                    .optJSONObject("content")
                    ?.optJSONObject("sectionListRenderer")
                    ?.optJSONArray("contents")
                if (sections != null) return sections
            }
            return null
        }

        fun thumbnailUrl(card: JSONObject): String? {
            val thumbnails = card
                .optJSONObject("thumbnail")
                ?.optJSONObject("musicThumbnailRenderer")
                ?.optJSONObject("thumbnail")
                ?.optJSONArray("thumbnails")
                ?: return null
            var bestUrl: String? = null
            var bestSize = -1
            for (index in 0 until thumbnails.length()) {
                val thumbnail = thumbnails.optJSONObject(index) ?: continue
                val url = thumbnail.optString("url").takeIf(String::isNotBlank) ?: continue
                val size = thumbnail.optInt("width", 0) * thumbnail.optInt("height", 0)
                if (size > bestSize) {
                    bestSize = size
                    bestUrl = url
                }
            }
            return bestUrl?.replace(Regex("=w\\d+-h\\d+"), "=w544-h544")
        }

        fun textOf(node: JSONObject?): String {
            if (node == null) return ""
            val simpleText = node.optString("simpleText").takeIf(String::isNotBlank)
            if (simpleText != null) return simpleText
            val runs = node.optJSONArray("runs") ?: return ""
            return buildString {
                for (index in 0 until runs.length()) {
                    append(runs.optJSONObject(index)?.optString("text").orEmpty())
                }
            }
        }

        fun normalize(value: String): String = value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
    }
}
