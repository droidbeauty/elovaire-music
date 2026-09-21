package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.data.audio.EmbeddedDescriptionMetadata
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer
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

internal data class AudiobookLookupMetadata(
    val title: String,
    val author: String?,
    val releaseYear: Int?,
    val identifiers: Set<String> = emptySet(),
)

internal data class AudiobookDescriptionCandidate(
    val title: String,
    val subtitle: String? = null,
    val authors: List<String> = emptyList(),
    val releaseYear: Int? = null,
    val description: String? = null,
    val identifier: String? = null,
    val providerKey: String? = null,
)

internal sealed interface AudiobookDescriptionProviderResult {
    data class Found(val description: String) : AudiobookDescriptionProviderResult
    data object NoMatch : AudiobookDescriptionProviderResult
    data object TemporaryFailure : AudiobookDescriptionProviderResult
}

internal interface AudiobookDescriptionProvider {
    suspend fun lookup(metadata: AudiobookLookupMetadata): AudiobookDescriptionProviderResult
}

/** Fetches bounded public book descriptions after conservative metadata matching. */
internal class GoogleBooksAudiobookDescriptionReader internal constructor(
    private val googleBooks: AudiobookDescriptionProvider,
    private val openLibrary: AudiobookDescriptionProvider,
) : AudiobookDescriptionReader {
    constructor(transport: BoundedHttpTransport = BoundedHttpTransport(
        connectTimeoutMs = 6_000,
        readTimeoutMs = 8_000,
    )) : this(
        googleBooks = GoogleBooksDescriptionProvider(transport),
        openLibrary = OpenLibraryDescriptionProvider(transport),
    )

    private sealed interface DescriptionClaim {
        data class Cached(val value: String?) : DescriptionClaim
        data class Await(val deferred: CompletableDeferred<String?>) : DescriptionClaim
        data class Owner(val deferred: CompletableDeferred<String?>) : DescriptionClaim
    }

    private sealed interface CacheEntry {
        data class Found(val value: String) : CacheEntry
        data object NoMatch : CacheEntry
    }

    private data class FetchResult(
        val value: String?,
        val cacheable: Boolean,
    )

    private val cacheLock = Any()
    private val descriptionCache = LinkedHashMap<String, CacheEntry>(CACHE_CAPACITY, 0.75f, true)
    private val inFlight = mutableMapOf<String, CompletableDeferred<String?>>()

    override suspend fun description(book: Audiobook): String? {
        book.description
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { return it }

        val metadata = book.lookupMetadata()
        if (metadata.title.isBlank()) return null
        val key = descriptionKey(metadata)
        val claim = synchronized(cacheLock) {
            descriptionCache[key]?.let { entry ->
                DescriptionClaim.Cached((entry as? CacheEntry.Found)?.value)
            } ?: inFlight[key]?.let(DescriptionClaim::Await)
                ?: CompletableDeferred<String?>().let { deferred ->
                    inFlight[key] = deferred
                    DescriptionClaim.Owner(deferred)
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
            val fetched = fetchDescription(metadata)
            if (fetched.cacheable) {
                synchronized(cacheLock) {
                    descriptionCache[key] = if (fetched.value == null) {
                        CacheEntry.NoMatch
                    } else {
                        CacheEntry.Found(fetched.value)
                    }
                    while (descriptionCache.size > CACHE_CAPACITY) {
                        descriptionCache.remove(descriptionCache.entries.iterator().next().key)
                    }
                }
            }
            synchronized(cacheLock) { if (inFlight[key] === deferred) inFlight.remove(key) }
            deferred.complete(fetched.value)
            fetched.value
        } finally {
            synchronized(cacheLock) { if (inFlight[key] === deferred) inFlight.remove(key) }
        }
    }

    private suspend fun fetchDescription(metadata: AudiobookLookupMetadata): FetchResult {
        val google = googleBooks.lookup(metadata)
        if (google is AudiobookDescriptionProviderResult.Found) {
            return FetchResult(google.description, cacheable = true)
        }
        val openLibraryResult = openLibrary.lookup(metadata)
        return when (openLibraryResult) {
            is AudiobookDescriptionProviderResult.Found -> FetchResult(openLibraryResult.description, true)
            AudiobookDescriptionProviderResult.NoMatch -> FetchResult(
                value = null,
                cacheable = google is AudiobookDescriptionProviderResult.NoMatch,
            )
            AudiobookDescriptionProviderResult.TemporaryFailure -> FetchResult(null, cacheable = false)
        }
    }

    private fun Audiobook.lookupMetadata(): AudiobookLookupMetadata {
        val years = parts.mapNotNull { it.song.releaseYear }.distinct()
        return AudiobookLookupMetadata(
            title = title.trim(),
            author = author.trim().takeIf { it.isMeaningfulBookAuthor() },
            releaseYear = years.singleOrNull(),
        )
    }

    private fun descriptionKey(metadata: AudiobookLookupMetadata): String = buildString {
        append(normalizeAudiobookMetadataText(metadata.title))
        append('|')
        append(normalizeAudiobookMetadataText(metadata.author.orEmpty()))
        append('|')
        append(metadata.releaseYear ?: 0)
        append('|')
        append(metadata.identifiers.sorted().joinToString(","))
    }

    private companion object {
        const val CACHE_CAPACITY = 32
    }
}

internal class GoogleBooksDescriptionProvider(
    private val transport: BoundedHttpTransport,
) : AudiobookDescriptionProvider {
    override suspend fun lookup(metadata: AudiobookLookupMetadata): AudiobookDescriptionProviderResult {
        val query = buildString {
            val isbn = metadata.identifiers.firstNotNullOfOrNull(::normalizeIsbn)
            if (isbn != null) {
                append("isbn:").append(isbn)
            } else {
                append("intitle:\"").append(metadata.title).append('\"')
                metadata.author?.takeIf(String::isMeaningfulBookAuthor)?.let { author ->
                    append(" inauthor:\"").append(author).append('\"')
                }
            }
        }
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query.encodeQuery()}&maxResults=10&printType=books"
        return try {
            val response = transport.get(
                rawUrl = url,
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Elovaire/1.0",
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
            if (response.statusCode !in 200..299) {
                AudiobookDescriptionProviderResult.TemporaryFailure
            } else {
                val candidate = AudiobookDescriptionMatcher.select(
                    metadata,
                    parseGoogleBooksCandidates(String(response.body, StandardCharsets.UTF_8)),
                )
                candidate?.description?.let(AudiobookDescriptionProviderResult::Found)
                    ?: AudiobookDescriptionProviderResult.NoMatch
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: JSONException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: SecurityException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: IllegalArgumentException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: IllegalStateException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        }
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 512 * 1024
    }
}

internal class OpenLibraryDescriptionProvider(
    private val transport: BoundedHttpTransport,
) : AudiobookDescriptionProvider {
    override suspend fun lookup(metadata: AudiobookLookupMetadata): AudiobookDescriptionProviderResult {
        val queryParameters = buildList {
            val isbn = metadata.identifiers.firstNotNullOfOrNull(::normalizeIsbn)
            if (isbn != null) {
                add("isbn=${isbn.encodeQuery()}")
            } else {
                add("title=${metadata.title.encodeQuery()}")
                metadata.author?.let { add("author=${it.encodeQuery()}") }
            }
            add("limit=8")
            add("fields=key,title,subtitle,author_name,first_publish_year")
        }.joinToString("&")
        return try {
            val search = transport.get(
                rawUrl = "https://openlibrary.org/search.json?$queryParameters",
                headers = mapOf("Accept" to "application/json", "User-Agent" to "Elovaire/1.0"),
                maxBytes = SEARCH_RESPONSE_BYTES,
            )
            if (search.statusCode !in 200..299) return AudiobookDescriptionProviderResult.TemporaryFailure
            val candidate = AudiobookDescriptionMatcher.selectWithoutDescription(
                metadata,
                parseOpenLibraryCandidates(String(search.body, StandardCharsets.UTF_8)),
            ) ?: return AudiobookDescriptionProviderResult.NoMatch
            val workKey = candidate.providerKey?.takeIf { it.matches(WORK_KEY_REGEX) }
                ?: return AudiobookDescriptionProviderResult.NoMatch
            val work = transport.get(
                rawUrl = "https://openlibrary.org$workKey.json",
                headers = mapOf("Accept" to "application/json", "User-Agent" to "Elovaire/1.0"),
                maxBytes = WORK_RESPONSE_BYTES,
            )
            if (work.statusCode !in 200..299) return AudiobookDescriptionProviderResult.TemporaryFailure
            parseOpenLibraryDescription(String(work.body, StandardCharsets.UTF_8))
                ?.let(AudiobookDescriptionProviderResult::Found)
                ?: AudiobookDescriptionProviderResult.NoMatch
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: JSONException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: SecurityException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: IllegalArgumentException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        } catch (_: IllegalStateException) {
            AudiobookDescriptionProviderResult.TemporaryFailure
        }
    }

    private companion object {
        const val SEARCH_RESPONSE_BYTES = 256 * 1024
        const val WORK_RESPONSE_BYTES = 256 * 1024
        val WORK_KEY_REGEX = Regex("^/works/[A-Za-z0-9_-]+$")
    }
}

internal object AudiobookDescriptionMatcher {
    fun select(
        metadata: AudiobookLookupMetadata,
        candidates: List<AudiobookDescriptionCandidate>,
    ): AudiobookDescriptionCandidate? = candidates
        .asSequence()
        .filter { !it.description.isNullOrBlank() }
        .mapNotNull { candidate -> score(metadata, candidate)?.let { it to candidate } }
        .maxWithOrNull(compareBy<Pair<Int, AudiobookDescriptionCandidate>> { it.first }.thenBy { it.second.title })
        ?.second

    fun selectWithoutDescription(
        metadata: AudiobookLookupMetadata,
        candidates: List<AudiobookDescriptionCandidate>,
    ): AudiobookDescriptionCandidate? = candidates
        .asSequence()
        .mapNotNull { candidate -> score(metadata, candidate)?.let { it to candidate } }
        .maxWithOrNull(compareBy<Pair<Int, AudiobookDescriptionCandidate>> { it.first }.thenBy { it.second.title })
        ?.second

    fun score(
        metadata: AudiobookLookupMetadata,
        candidate: AudiobookDescriptionCandidate,
    ): Int? {
        val requestedIdentifiers = metadata.identifiers.mapNotNull(::normalizeIsbn).toSet()
        val candidateIdentifier = candidate.identifier?.let(::normalizeIsbn)
        if (requestedIdentifiers.isNotEmpty() && candidateIdentifier != null) {
            if (candidateIdentifier in requestedIdentifiers) return 1_000
            return null
        }
        val requestedTitle = normalizeAudiobookMetadataText(metadata.title)
        val candidateTitle = normalizeAudiobookMetadataText(candidate.title)
        if (requestedTitle.isBlank() || candidateTitle.isBlank()) return null
        if (hasConflictingEditionWords(metadata.title, candidate.title, candidate.subtitle)) return null

        val titleScore = titleScore(requestedTitle, candidate.title, candidate.subtitle) ?: return null
        val requestedAuthor = metadata.author?.takeIf(String::isMeaningfulBookAuthor)
        val candidateAuthors = candidate.authors.filter(String::isMeaningfulBookAuthor)
        val authorScore = when {
            requestedAuthor == null && candidateAuthors.isEmpty() -> 90
            requestedAuthor == null -> 0
            candidateAuthors.isEmpty() -> 0
            else -> candidateAuthors.maxOfOrNull { authorScore(requestedAuthor, it) } ?: 0
        }
        if (requestedAuthor != null && authorScore < MIN_AUTHOR_SCORE) return null
        if (metadata.releaseYear != null && candidate.releaseYear != null &&
            kotlin.math.abs(metadata.releaseYear - candidate.releaseYear) > MAX_YEAR_DISTANCE
        ) return null
        if (requestedAuthor == null && candidateAuthors.isNotEmpty() && metadata.releaseYear == null) return null
        return titleScore + authorScore
    }

    private fun titleScore(requested: String, candidateTitle: String, subtitle: String?): Int? {
        val candidate = normalizeAudiobookMetadataText(candidateTitle)
        if (requested == candidate) return 100
        val candidateBase = candidateTitle
            .substringBefore(':')
            .substringBefore(" - ")
            .let(::normalizeAudiobookMetadataText)
        if (candidateBase == requested) return if (subtitle.isNullOrBlank()) 95 else 98
        if (requested.split(' ').toSet() == candidate.split(' ').toSet()) return 96
        if (requested.split(' ').size >= 2 && candidate.startsWith("$requested ")) return 88
        return null
    }

    private fun authorScore(requested: String, candidate: String): Int {
        val first = normalizeAudiobookMetadataText(requested)
        val second = normalizeAudiobookMetadataText(candidate)
        if (first == second) return 100
        if (first.split(' ').toSet() == second.split(' ').toSet()) return 94
        val requestedInitials = first.split(' ').mapNotNull { it.firstOrNull() }.joinToString("")
        val candidateInitials = second.split(' ').mapNotNull { it.firstOrNull() }.joinToString("")
        return if (requestedInitials == candidateInitials) 88 else 0
    }

    private fun hasConflictingEditionWords(requested: String, title: String, subtitle: String?): Boolean {
        val requestedText = normalizeAudiobookMetadataText(requested)
        val candidateText = normalizeAudiobookMetadataText("$title ${subtitle.orEmpty()}")
        return EDITION_WORDS.any { it in candidateText && it !in requestedText }
    }

    private const val MIN_AUTHOR_SCORE = 88
    private const val MAX_YEAR_DISTANCE = 2
    private val EDITION_WORDS = setOf(
        "study guide",
        "summary",
        "workbook",
        "companion",
        "analysis",
        "cliff notes",
        "cliffsnotes",
        "sparknotes",
        "boxed set",
        "box set",
    )
}

internal fun parseGoogleBooksDescription(body: String, book: Audiobook): String? {
    val metadata = AudiobookLookupMetadata(
        title = book.title.trim(),
        author = book.author.trim().takeIf(String::isMeaningfulBookAuthor),
        releaseYear = book.parts.mapNotNull { it.song.releaseYear }.distinct().singleOrNull(),
    )
    return AudiobookDescriptionMatcher
        .select(metadata, parseGoogleBooksCandidates(body))
        ?.description
}

internal fun parseGoogleBooksCandidates(body: String): List<AudiobookDescriptionCandidate> {
    val items = JSONObject(body).optJSONArray("items") ?: return emptyList()
    return (0 until items.length()).mapNotNull { index ->
        val volumeInfo = items.optJSONObject(index)?.optJSONObject("volumeInfo") ?: return@mapNotNull null
        AudiobookDescriptionCandidate(
            title = volumeInfo.optString("title"),
            subtitle = volumeInfo.optString("subtitle").takeIf(String::isNotBlank),
            authors = volumeInfo.optStringArray("authors"),
            releaseYear = volumeInfo.optString("publishedDate").firstYear(),
            description = normalizeBookDescription(volumeInfo.optString("description")),
            identifier = volumeInfo.optJSONArray("industryIdentifiers")
                ?.optJSONObject(0)
                ?.optString("identifier")
                ?.takeIf(String::isNotBlank),
        )
    }
}

private fun parseOpenLibraryCandidates(body: String): List<AudiobookDescriptionCandidate> {
    val docs = JSONObject(body).optJSONArray("docs") ?: return emptyList()
    return (0 until docs.length()).mapNotNull { index ->
        val doc = docs.optJSONObject(index) ?: return@mapNotNull null
        AudiobookDescriptionCandidate(
            title = doc.optString("title"),
            subtitle = doc.optString("subtitle").takeIf(String::isNotBlank),
            authors = doc.optStringArray("author_name"),
            releaseYear = doc.optInt("first_publish_year").takeIf { it > 0 },
            providerKey = doc.optString("key").takeIf(String::isNotBlank),
        )
    }
}

private fun parseOpenLibraryDescription(body: String): String? {
    val value = JSONObject(body).opt("description")
    val description = when (value) {
        is String -> value
        is JSONObject -> value.optString("value")
        null -> ""
        else -> ""
    }
    return normalizeBookDescription(description)
}

private fun org.json.JSONObject.optStringArray(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
}

private fun String.firstYear(): Int? = Regex("\\b\\d{4}\\b").find(this)?.value?.toIntOrNull()

internal fun normalizeAudiobookMetadataText(value: String): String {
    return Normalizer.normalize(value, Normalizer.Form.NFKD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
        .replace('&', ' ')
        .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun normalizeBookDescription(value: String): String? {
    val normalized = value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\\n")
        .replace(Regex("</(p|div|li)>", RegexOption.IGNORE_CASE), "\\n")
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ", ignoreCase = true)
        .replace("&amp;", "&", ignoreCase = true)
        .replace("&lt;", "<", ignoreCase = true)
        .replace("&gt;", ">", ignoreCase = true)
        .replace("&quot;", "\"", ignoreCase = true)
        .replace("&apos;", "'", ignoreCase = true)
        .replace("&#39;", "'", ignoreCase = true)
        .replace(Regex("&#(x[0-9a-fA-F]+|[0-9]+);")) { match ->
            match.groupValues[1]
                .removePrefix("x")
                .toIntOrNull(if (match.groupValues[1].startsWith("x")) 16 else 10)
                ?.let { codePoint -> String(Character.toChars(codePoint)) }
                ?: match.value
        }
        .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
        .replace(Regex("\\n{3,}"), "\\n\\n")
        .trim()
        .take(EmbeddedDescriptionMetadata.MAX_CHARACTERS)
        .trim()
    return normalized.takeIf(String::isNotBlank)
}

private fun String.encodeQuery(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

private fun String.isMeaningfulBookAuthor(): Boolean = isNotBlank() &&
    !equals("Unknown Author", ignoreCase = true) &&
    !equals("Unknown Artist", ignoreCase = true)

private fun normalizeIsbn(value: String): String? {
    val normalized = value
        .replace("-", "")
        .replace(" ", "")
        .uppercase(Locale.ROOT)
    if (normalized.length == 10 && normalized.withIndex().all { (index, character) ->
            character.isDigit() || (character == 'X' && index == 9)
        }
    ) {
        val valid = normalized.mapIndexed { index, character ->
            (if (character == 'X') 10 else character.digitToInt()) * (10 - index)
        }.sum() % 11 == 0
        return normalized.takeIf { valid }
    }
    if (normalized.length == 13 && normalized.all(Char::isDigit)) {
        val checksum = normalized.mapIndexed { index, character ->
            character.digitToInt() * if (index % 2 == 0) 1 else 3
        }.sum()
        return normalized.takeIf { checksum % 10 == 0 }
    }
    return null
}
