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
    data class Found(val albums: List<ArtistDiscographyAlbum>) : ArtistDiscographyResult
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

    private val cacheDirectory = context?.cacheDir?.resolve(CACHE_DIRECTORY)
    private val cacheLock = Any()
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val requestMutex = Mutex()
    private val client = MusicBrainzArtistDiscographyClient(transport)
    private var lastRequestStartedAtMs = 0L

    override suspend fun discography(artistName: String): ArtistDiscographyResult {
        val safeArtistName = artistName.filterNot(Char::isISOControl).trim()
        if (safeArtistName.isBlank()) return ArtistDiscographyResult.NotFound
        if (safeArtistName.length > MAX_ARTIST_NAME_LENGTH) return ArtistDiscographyResult.Unavailable
        val normalizedArtist = normalizeArtistName(safeArtistName)
        cachedResult(normalizedArtist)?.let { return it }

        return requestMutex.withLock {
            cachedResult(normalizedArtist)?.let { return@withLock it }
            val minimumDelayMs = MIN_REQUEST_INTERVAL_MS -
                (clock.elapsedTimeMs() - lastRequestStartedAtMs).coerceAtLeast(0L)
            if (lastRequestStartedAtMs > 0L && minimumDelayMs > 0L) delay(minimumDelayMs)

            lastRequestStartedAtMs = clock.elapsedTimeMs()
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
                    if (albums.isEmpty()) return null
                    ArtistDiscographyResult.Found(albums) to POSITIVE_CACHE_TTL_MS
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
        const val CACHE_DIRECTORY = "artist-discographies"
        const val MEMORY_CACHE_CAPACITY = 64
        const val MIN_REQUEST_INTERVAL_MS = 1_100L
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 24L * 60L * 60L * 1_000L
    }
}

internal class MusicBrainzArtistDiscographyClient(
    private val transport: BoundedHttpTransport,
) {
    suspend fun lookup(artistName: String): ArtistDiscographyResult {
        val safeArtistName = artistName.filterNot(Char::isISOControl).trim()
        if (safeArtistName.isBlank()) return ArtistDiscographyResult.NotFound
        if (safeArtistName.length > MAX_ARTIST_NAME_LENGTH) return ArtistDiscographyResult.Unavailable
        val query = "artistname:\"${safeArtistName.escapeLucenePhrase()}\" AND primarytype:album"
        val url = "https://musicbrainz.org/ws/2/release-group/?query=${query.encodeQuery()}&fmt=json&limit=100"
        return try {
            val response = transport.get(
                rawUrl = url,
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)",
                ),
                maxBytes = MAX_RESPONSE_BYTES,
            )
            if (response.statusCode !in 200..299) {
                ArtistDiscographyResult.Unavailable
            } else {
                val albums = parseMusicBrainzArtistDiscography(
                    response.body.toString(StandardCharsets.UTF_8),
                    safeArtistName,
                )
                if (albums.isEmpty()) ArtistDiscographyResult.NotFound else ArtistDiscographyResult.Found(albums)
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

    private companion object {
        const val MAX_RESPONSE_BYTES = 1024 * 1024
    }
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

private const val MAX_ARTIST_NAME_LENGTH = 200
