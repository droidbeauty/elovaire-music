package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import java.io.IOException
import java.net.URLEncoder
import java.util.LinkedHashMap
import java.util.Locale
import kotlinx.coroutines.CancellationException
import org.json.JSONException
import org.json.JSONObject

internal interface AudiobookDescriptionReader {
    suspend fun description(book: Audiobook): String?
}

/** Fetches a bounded public book description and keeps the result in a small process cache. */
internal class GoogleBooksAudiobookDescriptionReader(
    private val transport: BoundedHttpTransport = BoundedHttpTransport(
        connectTimeoutMs = 6_000,
        readTimeoutMs = 8_000,
    ),
) : AudiobookDescriptionReader {
    private val cacheLock = Any()
    private val descriptionCache = LinkedHashMap<String, String?>(CACHE_CAPACITY, 0.75f, true)

    override suspend fun description(book: Audiobook): String? {
        val key = descriptionKey(book)
        synchronized(cacheLock) {
            if (descriptionCache.containsKey(key)) return descriptionCache[key]
        }
        val result = try {
            val query = buildQuery(book)
            val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
            val response = transport.get(
                rawUrl = "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=5&printType=books",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Elovaire/1.0",
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
            if (response.statusCode !in 200..299) {
                null
            } else {
                parseGoogleBooksDescription(String(response.body, Charsets.UTF_8), book)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            null
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
        synchronized(cacheLock) {
            descriptionCache[key] = result
            while (descriptionCache.size > CACHE_CAPACITY) {
                val oldestKey = descriptionCache.entries.iterator().next().key
                descriptionCache.remove(oldestKey)
            }
        }
        return result
    }

    private fun buildQuery(book: Audiobook): String = buildString {
        append("intitle:").append(book.title.trim())
        if (book.author.isNotBlank()) {
            append(" inauthor:").append(book.author.trim())
        }
    }

    private fun descriptionKey(book: Audiobook): String =
        "${book.title.trim().lowercase(Locale.ROOT)}|${book.author.trim().lowercase(Locale.ROOT)}"

    private companion object {
        const val CACHE_CAPACITY = 32
        const val MAX_RESPONSE_BYTES = 512 * 1024
    }
}

internal fun parseGoogleBooksDescription(body: String, book: Audiobook): String? {
    val items = JSONObject(body).optJSONArray("items") ?: return null
    var firstDescription: String? = null
    for (index in 0 until items.length()) {
        val volumeInfo = items.optJSONObject(index)?.optJSONObject("volumeInfo") ?: continue
        val description = normalizeBookDescription(volumeInfo.optString("description")) ?: continue
        if (firstDescription == null) firstDescription = description
        val resultTitle = volumeInfo.optString("title")
        if (titlesMatch(resultTitle, book.title)) return description
    }
    return firstDescription
}

private fun normalizeBookDescription(value: String): String? {
    val normalized = value
        .replace(Regex("<[^>]*>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(MAX_DESCRIPTION_CHARACTERS)
    return normalized.takeIf(String::isNotBlank)
}

private fun titlesMatch(first: String, second: String): Boolean {
    val normalizedFirst = first.trim().lowercase(Locale.ROOT)
    val normalizedSecond = second.trim().lowercase(Locale.ROOT)
    return normalizedFirst.isNotBlank() && normalizedSecond.isNotBlank() &&
        (normalizedFirst == normalizedSecond ||
            normalizedFirst.contains(normalizedSecond) || normalizedSecond.contains(normalizedFirst))
}

private const val MAX_DESCRIPTION_CHARACTERS = 20_000
