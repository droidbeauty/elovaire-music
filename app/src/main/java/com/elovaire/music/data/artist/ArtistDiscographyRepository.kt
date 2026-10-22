package elovaire.music.droidbeauty.app.data.artist

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal data class ArtistDiscographyAlbum(
    val title: String,
    val firstReleaseDate: String?,
)

internal data class MusicBrainzReleaseGroup(
    val title: String,
    val primaryType: String?,
    val firstReleaseDate: String?,
    val artistNames: List<String>,
)

internal sealed interface ArtistDiscographyResult {
    data class Found(
        val albums: List<ArtistDiscographyAlbum>,
        val description: String? = null,
        val genres: List<String> = emptyList(),
        val descriptionSourceUrl: String? = null,
    ) : ArtistDiscographyResult
    data object NotFound : ArtistDiscographyResult
    data object Unavailable : ArtistDiscographyResult
}

internal interface ArtistDiscographyReader {
    suspend fun discography(artistName: String): ArtistDiscographyResult
}

internal class CachedArtistDiscographyReader(
    context: Context?,
    private val transport: BoundedHttpTransport,
    private val clock: AppClock = AndroidAppClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistDiscographyReader {
    private data class CacheEntry(
        val result: ArtistDiscographyResult,
        val expiresAtMs: Long,
    )

    private val cacheDirectory by lazy { context?.cacheDir?.resolve(CACHE_DIRECTORY) }
    private val cacheLock = Any()
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val requestMutex = Mutex()
    private val client = MusicBrainzArtistDiscographyClient(transport, ::waitForMusicBrainzSlot)
    private var lastRequestStartedAtMs: Long? = null

    override suspend fun discography(artistName: String): ArtistDiscographyResult {
        val safeArtistName = artistName.filterNot(Char::isISOControl).trim()
        if (safeArtistName.isBlank()) return ArtistDiscographyResult.NotFound
        if (safeArtistName.length > MAX_ARTIST_NAME_LENGTH) return ArtistDiscographyResult.Unavailable
        val normalizedArtist = normalizeArtistName(safeArtistName)
        cachedResult(normalizedArtist)?.let { return it }

        return requestMutex.withLock {
            cachedResult(normalizedArtist)?.let { return@withLock it }
            val result = client.lookup(safeArtistName)
            when (result) {
                is ArtistDiscographyResult.Found,
                ArtistDiscographyResult.NotFound,
                -> cacheResult(normalizedArtist, result)

                ArtistDiscographyResult.Unavailable -> Unit
            }
            result
        }
    }

    private suspend fun waitForMusicBrainzSlot() {
        lastRequestStartedAtMs?.let { lastRequestAt ->
            val minimumDelayMs = MIN_REQUEST_INTERVAL_MS -
                (clock.elapsedTimeMs() - lastRequestAt).coerceAtLeast(0L)
            if (minimumDelayMs > 0L) delay(minimumDelayMs)
        }
        lastRequestStartedAtMs = clock.elapsedTimeMs()
    }

    private suspend fun cachedResult(artistName: String): ArtistDiscographyResult? {
        val now = clock.wallTimeMs()
        synchronized(cacheLock) {
            val cached = memoryCache[artistName]
            if (cached != null && cached.expiresAtMs > now) return cached.result
            memoryCache.remove(artistName)
        }
        val cachedFromDisk = withContext(ioDispatcher) { readCacheFile(artistName, now) } ?: return null
        synchronized(cacheLock) {
            memoryCache[artistName] = cachedFromDisk
            while (memoryCache.size > MEMORY_CACHE_CAPACITY) {
                memoryCache.keys.iterator().apply {
                    if (hasNext()) {
                        next()
                        remove()
                    }
                }
            }
        }
        return cachedFromDisk.result
    }

    private suspend fun cacheResult(artistName: String, result: ArtistDiscographyResult) {
        val ttlMs = if (result is ArtistDiscographyResult.Found) POSITIVE_CACHE_TTL_MS else NEGATIVE_CACHE_TTL_MS
        val entry = CacheEntry(result, clock.wallTimeMs() + ttlMs)
        synchronized(cacheLock) {
            memoryCache[artistName] = entry
            while (memoryCache.size > MEMORY_CACHE_CAPACITY) {
                memoryCache.keys.iterator().apply {
                    if (hasNext()) {
                        next()
                        remove()
                    }
                }
            }
        }
        withContext(ioDispatcher) { writeCacheFile(artistName, result) }
    }

    private fun readCacheFile(artistName: String, nowMs: Long): CacheEntry? {
        val file = cacheFile(artistName) ?: return null
        if (!file.isFile) return null
        val ageMs = nowMs - file.lastModified()
        if (ageMs < 0L || ageMs > POSITIVE_CACHE_TTL_MS) return null
        return try {
            val payload = JSONObject(file.readText(StandardCharsets.UTF_8))
            if (payload.optString("artist") != artistName) return null
            val (result, ttlMs) = when (payload.optString("status")) {
                "not_found" -> ArtistDiscographyResult.NotFound to NEGATIVE_CACHE_TTL_MS
                "found" -> {
                    val albums = payload.optJSONArray("albums")?.toArtistAlbums().orEmpty()
                    val genres = payload.optJSONArray("genres")?.toStringList().orEmpty()
                    val description = payload.optString("description")
                        .takeIf { it.isNotBlank() && it != "null" }
                    if (albums.isEmpty() && genres.isEmpty() && description == null) return null
                    ArtistDiscographyResult.Found(
                        albums = albums,
                        description = description,
                        genres = genres,
                        descriptionSourceUrl = payload.optString("description_source_url")
                            .takeIf { it.isNotBlank() && it != "null" },
                    ) to POSITIVE_CACHE_TTL_MS
                }
                else -> return null
            }
            if (ageMs > ttlMs) return null
            CacheEntry(result, nowMs + (ttlMs - ageMs))
        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            file.delete()
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun writeCacheFile(artistName: String, result: ArtistDiscographyResult) {
        val directory = cacheDirectory ?: return
        if (!directory.exists() && !directory.mkdirs()) return
        val file = cacheFile(artistName) ?: return
        val temporaryFile = File(file.parentFile, "${file.nameWithoutExtension}.tmp")
        val payload = JSONObject()
            .put("artist", artistName)
            .put(
                "status",
                if (result is ArtistDiscographyResult.Found) "found" else "not_found",
            )
        if (result is ArtistDiscographyResult.Found) {
            payload.put("description", result.description)
            payload.put("description_source_url", result.descriptionSourceUrl)
            payload.put("genres", JSONArray(result.genres))
            payload.put(
                "albums",
                JSONArray().apply {
                    result.albums.forEach { album ->
                        put(
                            JSONObject()
                                .put("title", album.title)
                                .put("date", album.firstReleaseDate),
                        )
                    }
                },
            )
        }
        try {
            temporaryFile.writeText(payload.toString(), StandardCharsets.UTF_8)
            try {
                Files.move(
                    temporaryFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temporaryFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
        } catch (_: IOException) {
            temporaryFile.delete()
        } catch (_: SecurityException) {
            temporaryFile.delete()
        }
    }

    private fun cacheFile(artistName: String): File? =
        cacheDirectory?.resolve("${artistName.cacheFileKey()}.json")

    private companion object {
        const val CACHE_DIRECTORY = "artist-info-v2"
        const val MEMORY_CACHE_CAPACITY = 64
        const val MIN_REQUEST_INTERVAL_MS = 1_100L
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 24L * 60L * 60L * 1_000L
    }
}

internal class MusicBrainzArtistDiscographyClient(
    private val transport: BoundedHttpTransport,
    private val beforeMusicBrainzRequest: suspend () -> Unit,
) {
    suspend fun lookup(artistName: String): ArtistDiscographyResult {
        val safeArtistName = artistName.filterNot(Char::isISOControl).trim()
        if (safeArtistName.isBlank()) return ArtistDiscographyResult.NotFound
        if (safeArtistName.length > MAX_ARTIST_NAME_LENGTH) return ArtistDiscographyResult.Unavailable
        return try {
            val artistId = lookupArtistId(safeArtistName)
            val artistMetadata = if (artistId != null) lookupArtistMetadata(artistId) else null
            val wikipediaSummary = if (artistMetadata?.wikipediaUrl != null) {
                lookupWikipediaSummary(artistMetadata.wikipediaUrl)
            } else {
                null
            }
            val albums = lookupAlbums(safeArtistName)
            val result = ArtistDiscographyResult.Found(
                albums = albums.orEmpty(),
                description = wikipediaSummary?.description,
                genres = artistMetadata?.genres.orEmpty(),
                descriptionSourceUrl = wikipediaSummary?.sourceUrl,
            )
            if (result.albums.isNotEmpty() || result.description != null || result.genres.isNotEmpty()) {
                result
            } else if (albums != null || artistMetadata != null) {
                ArtistDiscographyResult.NotFound
            } else {
                ArtistDiscographyResult.Unavailable
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            ArtistDiscographyResult.Unavailable
        } catch (_: JSONException) {
            ArtistDiscographyResult.Unavailable
        } catch (_: IllegalArgumentException) {
            ArtistDiscographyResult.Unavailable
        } catch (_: SecurityException) {
            ArtistDiscographyResult.Unavailable
        } catch (_: IllegalStateException) {
            ArtistDiscographyResult.Unavailable
        }
    }

    private suspend fun lookupArtistId(artistName: String): String? {
        val query = "artist:\"${artistName.escapeLucenePhrase()}\""
        val payload = musicBrainzJson(
            "https://musicbrainz.org/ws/2/artist/?query=${query.encodeQuery()}&fmt=json&limit=10",
        ) ?: return null
        val artists = payload.optJSONArray("artists") ?: return null
        for (index in 0 until artists.length()) {
            val artist = artists.optJSONObject(index) ?: continue
            if (normalizeArtistName(artist.optString("name")) == normalizeArtistName(artistName)) {
                return artist.optString("id").takeIf(String::isNotBlank)
            }
        }
        return null
    }

    private suspend fun lookupArtistMetadata(artistId: String): ArtistMetadata? {
        if (!MUSICBRAINZ_ID_REGEX.matches(artistId)) return null
        val payload = musicBrainzJson(
            "https://musicbrainz.org/ws/2/artist/$artistId?inc=genres+url-rels&fmt=json",
        ) ?: return null
        val genres = payload.optJSONArray("genres")?.toStringList().orEmpty()
            .distinctBy { it.lowercase(Locale.ROOT) }
        val relations = payload.optJSONArray("relations")
        val wikipediaUrl = relations?.let { items ->
            (0 until items.length()).asSequence()
                .mapNotNull { items.optJSONObject(it) }
                .filter { it.optString("type").equals("wikipedia", ignoreCase = true) }
                .mapNotNull { it.optJSONObject("url")?.optString("resource") }
                .firstOrNull(::isWikipediaArticleUrl)
        }
        return ArtistMetadata(genres, wikipediaUrl)
    }

    private suspend fun lookupWikipediaSummary(articleUrl: String): WikipediaSummary? {
        val articleUri = try {
            java.net.URI(articleUrl)
        } catch (_: java.net.URISyntaxException) {
            return null
        }
        val host = articleUri.host?.lowercase(Locale.ROOT) ?: return null
        if (!articleUri.scheme.equals("https", ignoreCase = true) ||
            !(host == "wikipedia.org" || host.endsWith(".wikipedia.org"))
        ) return null
        val title = articleUri.rawPath
            ?.substringAfter("/wiki/", "")
            ?.takeIf(String::isNotBlank)
            ?.replace("+", "%20")
            ?: return null
        val payload = json(
            "https://$host/api/rest_v1/page/summary/$title",
            headers = mapOf(
                "Accept" to "application/json",
                "User-Agent" to "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)",
            ),
        ) ?: return null
        val description = payload.optString("extract")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.trim()
            ?.take(MAX_DESCRIPTION_CHARS)
            ?: return null
        val sourceUrl = payload.optJSONObject("content_urls")
            ?.optJSONObject("desktop")
            ?.optString("page")
            ?.takeIf(::isWikipediaArticleUrl)
            ?: articleUrl
        return WikipediaSummary(description, sourceUrl)
    }

    private suspend fun lookupAlbums(artistName: String): List<ArtistDiscographyAlbum>? {
        val query = "artistname:\"${artistName.escapeLucenePhrase()}\" AND primarytype:album"
        val payload = musicBrainzJson(
            "https://musicbrainz.org/ws/2/release-group/?query=${query.encodeQuery()}&fmt=json&limit=100",
        ) ?: return null
        return parseMusicBrainzArtistDiscography(payload.toString(), artistName)
    }

    private suspend fun musicBrainzJson(url: String): JSONObject? {
        beforeMusicBrainzRequest()
        return json(
            url,
            headers = mapOf(
                "Accept" to "application/json",
                "User-Agent" to "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)",
            ),
        )
    }

    private suspend fun json(url: String, headers: Map<String, String>): JSONObject? {
        return try {
            val response = transport.get(url, headers, MAX_RESPONSE_BYTES)
            if (response.statusCode !in 200..299) return null
            JSONObject(response.body.toString(StandardCharsets.UTF_8))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
    }

    private data class ArtistMetadata(val genres: List<String>, val wikipediaUrl: String?)
    private data class WikipediaSummary(val description: String, val sourceUrl: String)

    private companion object {
        const val MAX_RESPONSE_BYTES = 1024 * 1024
        const val MAX_DESCRIPTION_CHARS = 2_000
    }
}

private fun JSONArray.toStringList(): List<String> = buildList {
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        item.optString("name").trim().takeIf(String::isNotBlank)?.let(::add)
    }
}

private fun isWikipediaArticleUrl(value: String): Boolean = try {
    val uri = java.net.URI(value)
    val host = uri.host?.lowercase(Locale.ROOT)
    uri.scheme.equals("https", ignoreCase = true) &&
        host != null && (host == "wikipedia.org" || host.endsWith(".wikipedia.org")) &&
        uri.rawPath?.contains("/wiki/") == true
} catch (_: java.net.URISyntaxException) {
    false
}

internal fun parseMusicBrainzArtistDiscography(
    responseBody: String,
    artistName: String,
): List<ArtistDiscographyAlbum> {
    val groups = JSONObject(responseBody).optJSONArray("release-groups") ?: return emptyList()
    val candidates = buildList {
        for (index in 0 until groups.length()) {
            val group = groups.optJSONObject(index) ?: continue
            val credits = group.optJSONArray("artist-credit")
            val artistNames = buildList {
                if (credits != null) {
                    for (creditIndex in 0 until credits.length()) {
                        val credit = credits.optJSONObject(creditIndex) ?: continue
                        credit.optString("name").takeIf(String::isNotBlank)?.let(::add)
                        credit.optJSONObject("artist")?.optString("name")
                            ?.takeIf(String::isNotBlank)
                            ?.let(::add)
                    }
                }
            }
            add(
                MusicBrainzReleaseGroup(
                    title = group.optString("title"),
                    primaryType = group.optString("primary-type"),
                    firstReleaseDate = group.optString("first-release-date"),
                    artistNames = artistNames,
                ),
            )
        }
    }
    return selectMusicBrainzArtistDiscography(artistName, candidates)
}

internal fun selectMusicBrainzArtistDiscography(
    artistName: String,
    groups: List<MusicBrainzReleaseGroup>,
): List<ArtistDiscographyAlbum> {
    val expectedArtist = normalizeArtistName(artistName)
    val albums = LinkedHashMap<String, ArtistDiscographyAlbum>()
    groups.forEach { group ->
        if (!group.primaryType.equals("Album", ignoreCase = true)) return@forEach
        if (group.artistNames.none { normalizeArtistName(it) == expectedArtist }) return@forEach
        val title = group.title.trim().takeIf(String::isNotEmpty) ?: return@forEach
        val date = group.firstReleaseDate
            ?.takeIf { it.isNotBlank() && it != "null" && RELEASE_DATE_REGEX.matches(it) }
        albums.putIfAbsent(
            normalizeArtistName(title) + "|" + date.orEmpty(),
            ArtistDiscographyAlbum(title, date),
        )
    }
    return albums.values.sortedWith(
        compareByDescending<ArtistDiscographyAlbum> { it.firstReleaseDate != null }
            .thenByDescending { it.firstReleaseDate }
            .thenBy { it.title.lowercase(Locale.ROOT) },
    )
}

private fun JSONArray.toArtistAlbums(): List<ArtistDiscographyAlbum> = buildList {
    for (index in 0 until length()) {
        val album = optJSONObject(index) ?: continue
        val title = album.optString("title").trim().takeIf(String::isNotEmpty) ?: continue
        val date = album.optString("date")
            .takeIf { it.isNotBlank() && it != "null" && RELEASE_DATE_REGEX.matches(it) }
        add(ArtistDiscographyAlbum(title, date))
    }
}

private fun normalizeArtistName(value: String): String =
    value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

private fun String.escapeLucenePhrase(): String = replace("\\", "\\\\").replace("\"", "\\\"")

private fun String.encodeQuery(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

private fun String.cacheFileKey(): String = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray(StandardCharsets.UTF_8))
    .joinToString(separator = "") { byte -> "%02x".format(Locale.ROOT, byte.toInt() and 0xff) }

private val RELEASE_DATE_REGEX = Regex("\\d{4}(?:-\\d{2}(?:-\\d{2})?)?")
private val MUSICBRAINZ_ID_REGEX = Regex("[0-9a-fA-F-]{36}")

private const val MAX_ARTIST_NAME_LENGTH = 200
