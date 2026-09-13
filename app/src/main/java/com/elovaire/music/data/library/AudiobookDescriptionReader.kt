package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import java.io.IOException
import java.net.URLEncoder
import java.util.LinkedHashMap
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
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
    private sealed interface DescriptionClaim {
        data class Cached(val value: String?) : DescriptionClaim
        data class Await(val deferred: CompletableDeferred<String?>) : DescriptionClaim
        data class Owner(val deferred: CompletableDeferred<String?>) : DescriptionClaim
    }

    private val cacheLock = Any()
    private val descriptionCache = LinkedHashMap<String, String?>(CACHE_CAPACITY, 0.75f, true)
    private val inFlight = mutableMapOf<String, CompletableDeferred<String?>>()

    override suspend fun description(book: Audiobook): String? {
        val title = book.title.trim()
        val author = book.author.trim()
        val key = descriptionKey(title, author)
        val claim = synchronized(cacheLock) {
            if (descriptionCache.containsKey(key)) {
                DescriptionClaim.Cached(descriptionCache[key])
            } else {
                inFlight[key]?.let(DescriptionClaim::Await)
                    ?: CompletableDeferred<String?>().let { deferred ->
                        inFlight[key] = deferred
                        DescriptionClaim.Owner(deferred)
                    }
            }
        }
        when (claim) {
            is DescriptionClaim.Cached -> return claim.value
            is DescriptionClaim.Await -> return claim.deferred.await()
            is DescriptionClaim.Owner -> Unit
        }
        val deferred = claim.deferred
        currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause == null) return@invokeOnCompletion
            synchronized(cacheLock) { if (inFlight[key] === deferred) inFlight.remove(key) }
            if (cause is CancellationException) deferred.cancel(cause) else deferred.completeExceptionally(cause)
        }
        return try {
            val result = fetchDescription(title, author)
            synchronized(cacheLock) {
                descriptionCache[key] = result
                while (descriptionCache.size > CACHE_CAPACITY) {
                    val oldestKey = descriptionCache.entries.iterator().next().key
                    descriptionCache.remove(oldestKey)
                }
                if (inFlight[key] === deferred) inFlight.remove(key)
            }
            deferred.complete(result)
            result
        } finally {
            synchronized(cacheLock) { if (inFlight[key] === deferred) inFlight.remove(key) }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchDescription(title: String, author: String): String? {
        return try {
            val query = buildQuery(title, author)
            val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
            val response = transport.get(
                rawUrl = "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=5&printType=books",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Elovaire/1.0",
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
            if (response.statusCode !in 200..299) null else {
                parseGoogleBooksDescription(String(response.body, Charsets.UTF_8), title)
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
    }

    private fun buildQuery(title: String, author: String): String = buildString {
        append("intitle:").append(title)
        if (author.isNotBlank()) {
            append(" inauthor:").append(author)
        }
    }

    private fun descriptionKey(title: String, author: String): String =
        "${title.lowercase(Locale.ROOT)}|${author.lowercase(Locale.ROOT)}"

    private companion object {
        const val CACHE_CAPACITY = 32
        const val MAX_RESPONSE_BYTES = 512 * 1024
    }
}

internal fun parseGoogleBooksDescription(body: String, book: Audiobook): String? {
    return parseGoogleBooksDescription(body, book.title.trim())
}

private fun parseGoogleBooksDescription(body: String, title: String): String? {
    val items = JSONObject(body).optJSONArray("items") ?: return null
    val normalizedTitle = title.lowercase(Locale.ROOT)
    var firstDescription: String? = null
    for (index in 0 until items.length()) {
        val volumeInfo = items.optJSONObject(index)?.optJSONObject("volumeInfo") ?: continue
        val description = normalizeBookDescription(volumeInfo.optString("description")) ?: continue
        if (firstDescription == null) firstDescription = description
        val resultTitle = volumeInfo.optString("title")
        if (titlesMatch(resultTitle, normalizedTitle)) return description
    }
    return firstDescription
}

private fun normalizeBookDescription(value: String): String? {
    val normalized = value
        .replace(HTML_TAG_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
        .take(MAX_DESCRIPTION_CHARACTERS)
    return normalized.takeIf(String::isNotBlank)
}

private fun titlesMatch(first: String, normalizedSecond: String): Boolean {
    val normalizedFirst = first.trim().lowercase(Locale.ROOT)
    return normalizedFirst.isNotBlank() && normalizedSecond.isNotBlank() &&
        (normalizedFirst == normalizedSecond ||
            normalizedFirst.contains(normalizedSecond) || normalizedSecond.contains(normalizedFirst))
}

private const val MAX_DESCRIPTION_CHARACTERS = 20_000
private val HTML_TAG_REGEX = Regex("<[^>]*>")
private val WHITESPACE_REGEX = Regex("\\s+")
