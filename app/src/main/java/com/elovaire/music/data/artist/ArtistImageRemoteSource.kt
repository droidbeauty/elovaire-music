package elovaire.music.droidbeauty.app.data.artist

import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal data class ArtistImageRemoteMatch(
    val providerArtistId: String,
    val providerName: String,
    val musicBrainzArtistId: String?,
    val imageUrl: String,
)

internal sealed interface ArtistImageRemoteResult {
    data class Match(val value: ArtistImageRemoteMatch) : ArtistImageRemoteResult
    data object Disabled : ArtistImageRemoteResult
    data object NoReliableMatch : ArtistImageRemoteResult
    data object Unauthorized : ArtistImageRemoteResult
    data class RateLimited(val retryAfterMs: Long?) : ArtistImageRemoteResult
    data object TransientFailure : ArtistImageRemoteResult
    data object MalformedResponse : ArtistImageRemoteResult
}

internal sealed interface ArtistImageDownloadResult {
    data class Success(
        val bytes: ByteArray,
        val etag: String?,
        val lastModified: String?,
    ) : ArtistImageDownloadResult

    data object NotModified : ArtistImageDownloadResult
    data object Unauthorized : ArtistImageDownloadResult
    data class RateLimited(val retryAfterMs: Long?) : ArtistImageDownloadResult
    data object TransientFailure : ArtistImageDownloadResult
    data object InvalidResponse : ArtistImageDownloadResult
}

internal interface ArtistImageRemoteSource {
    suspend fun resolveArtistImage(
        artistName: String,
        providerArtistId: String?,
    ): ArtistImageRemoteResult

    suspend fun downloadArtistImage(
        imageUrl: String,
        etag: String?,
        lastModified: String?,
    ): ArtistImageDownloadResult
}

internal class TheAudioDbArtistImageRemoteSource(
    private val apiKey: String,
    private val rateLimiter: ArtistProviderRateLimiter = ArtistProviderRateLimiter(),
) : ArtistImageRemoteSource {
    override suspend fun resolveArtistImage(
        artistName: String,
        providerArtistId: String?,
    ): ArtistImageRemoteResult {
        if (apiKey.isBlank()) return ArtistImageRemoteResult.Disabled
        if (isPseudoArtistName(artistName)) return ArtistImageRemoteResult.NoReliableMatch
        val encodedValue = Uri.encode(providerArtistId?.takeIf(String::isNotBlank) ?: artistName.trim())
        val path = if (providerArtistId.isNullOrBlank()) {
            "search/artist/$encodedValue"
        } else {
            "lookup/artist/$encodedValue"
        }
        return when (val response = getJson(path)) {
            is JsonResponse.Success -> {
                try {
                    val candidates = parseArtistCandidates(response.body)
                    val match = selectReliableArtistMatch(
                        requestedName = artistName,
                        candidates = candidates,
                        stableProviderId = providerArtistId,
                    )
                    if (match == null) {
                        ArtistImageRemoteResult.NoReliableMatch
                    } else {
                        ArtistImageRemoteResult.Match(match)
                    }
                } catch (_: JSONException) {
                    ArtistImageRemoteResult.MalformedResponse
                }
            }

            JsonResponse.NoMatch -> ArtistImageRemoteResult.NoReliableMatch
            JsonResponse.Unauthorized -> ArtistImageRemoteResult.Unauthorized
            is JsonResponse.RateLimited -> ArtistImageRemoteResult.RateLimited(response.retryAfterMs)
            JsonResponse.TransientFailure -> ArtistImageRemoteResult.TransientFailure
            JsonResponse.Malformed -> ArtistImageRemoteResult.MalformedResponse
        }
    }

    override suspend fun downloadArtistImage(
        imageUrl: String,
        etag: String?,
        lastModified: String?,
    ): ArtistImageDownloadResult {
        if (apiKey.isBlank()) return ArtistImageDownloadResult.TransientFailure
        val initialUrl = validatedArtworkUrl(imageUrl) ?: return ArtistImageDownloadResult.InvalidResponse
        return fetchImage(initialUrl, etag, lastModified)
    }

    private suspend fun getJson(path: String): JsonResponse {
        val url = URL("$API_BASE/$path")
        var attempt = 0
        while (attempt < MAX_RATE_LIMIT_RETRIES) {
            rateLimiter.awaitSlot()
            val response = try {
                openConnection(url).useConnection { connection ->
                    when (val code = connection.responseCode) {
                        in 200..299 -> {
                            val bytes = connection.inputStream.readBounded(MAX_JSON_BYTES)
                                ?: return@useConnection JsonResponse.Malformed
                            JsonResponse.Success(bytes.toString(Charsets.UTF_8))
                        }

                        HttpURLConnection.HTTP_NOT_FOUND -> JsonResponse.NoMatch
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        -> JsonResponse.Unauthorized

                        HTTP_TOO_MANY_REQUESTS -> JsonResponse.RateLimited(connection.retryAfterMs())
                        in 500..599 -> JsonResponse.TransientFailure
                        else -> JsonResponse.TransientFailure
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                JsonResponse.TransientFailure
            }
            if (response !is JsonResponse.RateLimited) return response
            val retryAfterMs = response.retryAfterMs ?: DEFAULT_RETRY_AFTER_MS
            rateLimiter.blockFor(retryAfterMs)
            attempt++
            if (attempt < MAX_RATE_LIMIT_RETRIES) delay(retryAfterMs)
        }
        return JsonResponse.RateLimited(DEFAULT_RETRY_AFTER_MS)
    }

    private suspend fun fetchImage(
        initialUrl: URL,
        etag: String?,
        lastModified: String?,
        rateLimitAttempts: Int = 0,
    ): ArtistImageDownloadResult {
        var currentUrl = initialUrl
        repeat(MAX_REDIRECTS + 1) { redirectIndex ->
            rateLimiter.awaitSlot()
            val response = try {
                openConnection(currentUrl, accept = "image/*", includeApiKey = false).apply {
                    if (!etag.isNullOrBlank()) setRequestProperty("If-None-Match", etag)
                    if (!lastModified.isNullOrBlank()) setRequestProperty("If-Modified-Since", lastModified)
                }.useConnection { connection ->
                    when (val code = connection.responseCode) {
                        in 200..299 -> {
                            val contentType = connection.contentType.orEmpty().lowercase(Locale.ROOT)
                            if (contentType.isNotBlank() && !contentType.startsWith("image/")) {
                                ArtistImageDownloadResult.InvalidResponse
                            } else {
                                connection.inputStream.readBounded(MAX_IMAGE_BYTES)?.let { bytes ->
                                    ArtistImageDownloadResult.Success(
                                        bytes = bytes,
                                        etag = connection.getHeaderField("ETag"),
                                        lastModified = connection.getHeaderField("Last-Modified"),
                                    )
                                } ?: ArtistImageDownloadResult.InvalidResponse
                            }
                        }

                        HttpURLConnection.HTTP_NOT_MODIFIED -> ArtistImageDownloadResult.NotModified
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        -> ArtistImageDownloadResult.Unauthorized

                        HTTP_TOO_MANY_REQUESTS -> ArtistImageDownloadResult.RateLimited(connection.retryAfterMs())
                        in 300..399 -> {
                            val location = connection.getHeaderField("Location")
                                ?: return@useConnection ArtistImageDownloadResult.InvalidResponse
                            val redirectUrl = URL(currentUrl, location)
                            currentUrl = validatedArtworkUrl(redirectUrl.toString())
                                ?: return@useConnection ArtistImageDownloadResult.InvalidResponse
                            if (redirectIndex >= MAX_REDIRECTS) {
                                ArtistImageDownloadResult.InvalidResponse
                            } else {
                                null
                            }
                        }

                        else -> ArtistImageDownloadResult.TransientFailure
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                ArtistImageDownloadResult.TransientFailure
            }
            if (response == null) return@repeat
            if (response is ArtistImageDownloadResult.RateLimited) {
                if (rateLimitAttempts >= MAX_RATE_LIMIT_RETRIES - 1) return response
                val retryAfterMs = response.retryAfterMs ?: DEFAULT_RETRY_AFTER_MS
                rateLimiter.blockFor(retryAfterMs)
                delay(retryAfterMs)
                return fetchImage(currentUrl, etag, lastModified, rateLimitAttempts + 1)
            }
            return response
        }
        return ArtistImageDownloadResult.InvalidResponse
    }

    private fun openConnection(
        url: URL,
        accept: String = "application/json",
        includeApiKey: Boolean = true,
    ): HttpURLConnection {
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = false
            setRequestProperty("Accept", accept)
            setRequestProperty("User-Agent", USER_AGENT)
            if (includeApiKey) setRequestProperty("X-API-KEY", apiKey)
        }
    }

    private sealed interface JsonResponse {
        data object NoMatch : JsonResponse
        data object Unauthorized : JsonResponse
        data object TransientFailure : JsonResponse
        data object Malformed : JsonResponse
        data class Success(val body: String) : JsonResponse
        data class RateLimited(val retryAfterMs: Long?) : JsonResponse
    }

    private companion object {
        const val API_BASE = "https://www.theaudiodb.com/api/v2/json"
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 10_000
        const val MAX_JSON_BYTES = 512 * 1024
        const val MAX_IMAGE_BYTES = 4 * 1024 * 1024
        const val MAX_REDIRECTS = 3
        const val MAX_RATE_LIMIT_RETRIES = 2
        const val DEFAULT_RETRY_AFTER_MS = 60_000L
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val USER_AGENT = "Elovaire/1.0 (TheAudioDB artist artwork)"
    }
}

internal class ArtistProviderRateLimiter(
    private val maxRequests: Int = 25,
    private val windowMs: Long = 60_000L,
) {
    private val mutex = Mutex()
    private val requestTimes = ArrayDeque<Long>()
    private var blockedUntilMs = 0L

    suspend fun awaitSlot() {
        while (true) {
            val waitMs = mutex.withLock {
                val now = System.currentTimeMillis()
                while (requestTimes.firstOrNull()?.let { now - it >= windowMs } == true) {
                    requestTimes.removeFirst()
                }
                when {
                    blockedUntilMs > now -> blockedUntilMs - now
                    requestTimes.size < maxRequests -> {
                        requestTimes.addLast(now)
                        0L
                    }
                    else -> (requestTimes.first() + windowMs - now).coerceAtLeast(1L)
                }
            }
            if (waitMs == 0L) return
            delay(waitMs)
        }
    }

    suspend fun blockFor(delayMs: Long) {
        mutex.withLock {
            blockedUntilMs = maxOf(blockedUntilMs, System.currentTimeMillis() + delayMs.coerceIn(1L, 60_000L))
        }
    }
}

internal fun normalizeArtistIdentity(value: String): String {
    return Normalizer.normalize(value, Normalizer.Form.NFKC)
        .trim()
        .replace(WHITESPACE, " ")
        .lowercase(Locale.ROOT)
}

internal fun isPseudoArtistName(value: String): Boolean {
    return normalizeArtistIdentity(value) in PSEUDO_ARTIST_NAMES
}

internal fun selectReliableArtistMatch(
    requestedName: String,
    candidates: List<ArtistImageCandidate>,
    stableProviderId: String?,
): ArtistImageRemoteMatch? {
    val normalizedRequestedName = normalizeArtistIdentity(requestedName)
    if (normalizedRequestedName.isBlank() || isPseudoArtistName(requestedName)) return null
    val parsedCandidates = candidates.mapNotNull { candidate ->
        val imageUrl = smallArtworkUrl(candidate.imageUrl)
            .let(::validatedArtworkUrl)
            ?: return@mapNotNull null
        ArtistCandidate(
            providerId = candidate.providerId,
            providerName = candidate.providerName,
            normalizedName = normalizeArtistIdentity(candidate.providerName),
            alternateNames = candidate.alternateNames.map(::normalizeArtistIdentity).toSet(),
            musicBrainzArtistId = candidate.musicBrainzArtistId,
            imageUrl = imageUrl.toString(),
        )
    }
    val stableMatch = stableProviderId?.let { id -> parsedCandidates.filter { it.providerId == id } }
    val selected = when {
        stableMatch?.size == 1 -> stableMatch.single()
        stableMatch?.isNotEmpty() == true -> null
        else -> {
            val exactPrimary = parsedCandidates.filter { it.normalizedName == normalizedRequestedName }
            when {
                exactPrimary.size == 1 -> exactPrimary.single()
                exactPrimary.size > 1 -> null
                else -> {
                    val exactAlternate = parsedCandidates.filter { normalizedRequestedName in it.alternateNames }
                    exactAlternate.singleOrNull()
                }
            }
        }
    } ?: return null
    return ArtistImageRemoteMatch(
        providerArtistId = selected.providerId,
        providerName = selected.providerName,
        musicBrainzArtistId = selected.musicBrainzArtistId,
        imageUrl = selected.imageUrl,
    )
}

internal data class ArtistImageCandidate(
    val providerId: String,
    val providerName: String,
    val alternateNames: Set<String> = emptySet(),
    val musicBrainzArtistId: String? = null,
    val imageUrl: String,
)

private data class ArtistCandidate(
    val providerId: String,
    val providerName: String,
    val normalizedName: String,
    val alternateNames: Set<String>,
    val musicBrainzArtistId: String?,
    val imageUrl: String,
)

private fun parseArtistCandidates(serialized: String): List<ArtistImageCandidate> {
    return parseArtists(serialized).mapNotNull { candidate ->
        val providerId = candidate.firstNonBlank("idArtist", "id", "artistId") ?: return@mapNotNull null
        val providerName = candidate.firstNonBlank("strArtist", "artist", "name") ?: return@mapNotNull null
        val imageUrl = candidate.firstNonBlank("strArtistThumb", "artistThumb", "thumb", "image")
            ?: return@mapNotNull null
        ArtistImageCandidate(
            providerId = providerId,
            providerName = providerName,
            alternateNames = candidate.alternateArtistNames(),
            musicBrainzArtistId = candidate.firstNonBlank("strMusicBrainzID", "musicBrainzArtistId", "musicbrainzId"),
            imageUrl = imageUrl,
        )
    }
}

private fun parseArtists(serialized: String): List<JSONObject> {
    val root = JSONObject(serialized)
    val array = root.optJSONArray("artists")
        ?: root.optJSONArray("data")
        ?: root.optJSONArray("results")
    if (array != null) return array.toObjectList()
    return root.optJSONObject("artist")?.let(::listOf).orEmpty()
}

private fun JSONArray.toObjectList(): List<JSONObject> {
    return (0 until length()).mapNotNull(::optJSONObject)
}

private fun JSONObject.alternateArtistNames(): Set<String> {
    val result = linkedSetOf<String>()
    listOf("strArtistAlternate", "artistAlternate", "alternateName", "alias").forEach { key ->
        optString(key).takeIf(String::isNotBlank)?.split(',', ';', '|')?.forEach { value ->
            normalizeArtistIdentity(value).takeIf(String::isNotBlank)?.let(result::add)
        }
    }
    listOf("aliases", "alternateNames").forEach { key ->
        optJSONArray(key)?.let { aliases ->
            (0 until aliases.length()).forEach { index ->
                normalizeArtistIdentity(aliases.optString(index))
                    .takeIf(String::isNotBlank)
                    ?.let(result::add)
            }
        }
    }
    return result
}

private fun JSONObject.firstNonBlank(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key -> optString(key).trim().takeIf(String::isNotBlank) }
}

private fun smallArtworkUrl(value: String): String {
    val clean = value.trim()
    return if (clean.substringBefore('?').endsWith("/small", ignoreCase = true)) clean else "$clean/small"
}

internal fun validatedArtworkUrl(value: String): URL? {
    return runCatching {
        URL(value).takeIf { url ->
            url.protocol.equals("https", ignoreCase = true) &&
                url.userInfo == null &&
                (url.port == -1 || url.port == 443) &&
                url.host.lowercase(Locale.ROOT) in ARTWORK_HOSTS
        }
    }.getOrNull()
}

private fun HttpURLConnection.retryAfterMs(): Long? {
    return getHeaderField("Retry-After")?.trim()?.toLongOrNull()?.times(1_000L)?.coerceIn(1_000L, 60_000L)
}

private fun java.io.InputStream.readBounded(limit: Int): ByteArray? {
    val output = ByteArrayOutputStream(minOf(limit, 16 * 1024))
    val buffer = ByteArray(8 * 1024)
    while (true) {
        val count = read(buffer)
        if (count < 0) return output.toByteArray()
        if (output.size() > limit - count) return null
        output.write(buffer, 0, count)
    }
}

private inline fun <T> HttpURLConnection.useConnection(block: (HttpURLConnection) -> T): T {
    return try {
        block(this)
    } finally {
        disconnect()
    }
}

private val WHITESPACE = Regex("[\\s\\p{Zs}]+")
private val PSEUDO_ARTIST_NAMES = setOf("unknown artist", "various artists")
private val ARTWORK_HOSTS = setOf("theaudiodb.com", "www.theaudiodb.com", "r2.theaudiodb.com")
