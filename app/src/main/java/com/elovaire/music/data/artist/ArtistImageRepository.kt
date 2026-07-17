package elovaire.music.droidbeauty.app.data.artist

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.network.HttpRequest
import elovaire.music.droidbeauty.app.data.network.HttpTransport
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

data class ArtistIdentity(
    val displayName: String,
    val normalizedName: String,
    val musicBrainzId: String? = null,
    val youtubeChannelId: String? = null,
) {
    val stableKey: String = musicBrainzId?.let { "mbid:$it" } ?: "name:$normalizedName"
}

data class ArtistBackdrop(
    val artistKey: String,
    val imageUri: Uri,
    val source: ArtistImageSource,
    val revision: Long,
)

enum class ArtistImageSource {
    FanartTv,
    TheAudioDb,
    YouTubeDataApi,
    LocalArtwork,
    Generated,
}

sealed interface ArtistBackdropState {
    data object Loading : ArtistBackdropState
    data class Available(val backdrop: ArtistBackdrop) : ArtistBackdropState
    data class Fallback(val localArtworkUri: Uri?, val artistKey: String) : ArtistBackdropState
}

internal class ArtistImageRepository(
    context: Context,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val httpTransport: HttpTransport = HttpTransport,
    private val clock: AppClock = AndroidAppClock,
) {
    private val appContext = context.applicationContext
    private val store = ArtistImageStore(appContext)
    private val cacheDirectory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        File(appContext.cacheDir, "artist_backdrops")
    }
    private val inFlight = ConcurrentHashMap<String, Deferred<ArtistBackdrop?>>()
    private val musicBrainzLimiter = ArtistRequestRateLimiter(MUSICBRAINZ_INTERVAL_MS, clock)

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (pressure != MemoryPressure.Critical) return
        inFlight.values.forEach(Deferred<ArtistBackdrop?>::cancel)
        inFlight.clear()
    }

    fun backdropState(
        artistName: String,
        songs: List<Song>,
        albums: List<Album>,
    ): Flow<ArtistBackdropState> = flow {
        val baseIdentity = ArtistIdentity(
            displayName = artistName.ifBlank { UNKNOWN_ARTIST },
            normalizedName = normalizeArtistIdentity(artistName),
        )
        val localFallback = bestLocalArtistArtwork(albums, songs)
        val generatedFallback = ArtistBackdropState.Fallback(localFallback, baseIdentity.stableKey)
        emit(generatedFallback)

        if (baseIdentity.shouldSkipRemoteLookup()) return@flow
        val cachedEntry = store.cached(baseIdentity.stableKey)
        cachedBackdrop(baseIdentity, cachedEntry, freshOnly = false)?.let {
            emit(ArtistBackdropState.Available(it))
            if (cachedEntry?.isPositiveFresh(clock.wallTimeMs()) == true) return@flow
        }
        if (!backgroundWorkPolicy.shouldStartLyricsPrefetch()) return@flow

        val resolved = lookupBackdrop(baseIdentity, localFallback)
        if (resolved != null) {
            emit(ArtistBackdropState.Available(resolved))
        }
    }.flowOn(ioDispatcher)

    private suspend fun lookupBackdrop(
        baseIdentity: ArtistIdentity,
        localFallback: Uri?,
    ): ArtistBackdrop? = coroutineScope {
        val requestKey = baseIdentity.stableKey
        val existing = inFlight[requestKey]
        if (existing != null) return@coroutineScope existing.await()
        val request = async(ioDispatcher) {
            val cached = cachedBackdrop(baseIdentity)
            if (cached != null) return@async cached
            val negative = store.cached(requestKey)?.takeIf { it.isNegativeFresh(clock.wallTimeMs()) }
            if (negative != null) return@async null

            val identity = resolveMusicBrainzIdentity(baseIdentity)
            cachedBackdrop(identity)?.let { return@async it }
            val remote = resolveRemoteBackdrop(identity)
            if (remote != null) return@async remote

            val now = clock.wallTimeMs()
            store.put(
                ArtistImageCacheEntry(
                    artistKey = requestKey,
                    source = ArtistImageSource.Generated,
                    imageFilePath = null,
                    expiresAtMs = now + NEGATIVE_TTL_MS,
                    revision = now,
                ),
            )
            localFallback?.let {
                ArtistBackdrop(requestKey, it, ArtistImageSource.LocalArtwork, now)
            }
        }
        val active = inFlight.putIfAbsent(requestKey, request) ?: request
        if (active !== request) request.cancel()
        active.invokeOnCompletion { inFlight.remove(requestKey, active) }
        active.await()
    }

    private fun cachedBackdrop(
        identity: ArtistIdentity,
        entry: ArtistImageCacheEntry? = store.cached(identity.stableKey),
        freshOnly: Boolean = true,
    ): ArtistBackdrop? {
        entry ?: return null
        if (freshOnly && !entry.isPositiveFresh(clock.wallTimeMs())) return null
        val path = entry.imageFilePath ?: return null
        val file = File(path).takeIf { it.isFile && it.length() > 0L } ?: return null
        return ArtistBackdrop(
            artistKey = identity.stableKey,
            imageUri = Uri.fromFile(file),
            source = entry.source,
            revision = entry.revision,
        )
    }

    private suspend fun resolveRemoteBackdrop(identity: ArtistIdentity): ArtistBackdrop? {
        identity.musicBrainzId?.let { mbid ->
            resolveRemoteCandidate(identity, ArtistImageSource.FanartTv, findFanartTvImage(mbid))
                ?.let { return it }
            resolveRemoteCandidate(
                identity,
                ArtistImageSource.TheAudioDb,
                findTheAudioDbImageByMbid(mbid, identity),
            )?.let { return it }
        }
        resolveRemoteCandidate(
            identity,
            ArtistImageSource.TheAudioDb,
            findTheAudioDbImageByName(identity),
        )?.let { return it }
        return resolveRemoteCandidate(
            identity,
            ArtistImageSource.YouTubeDataApi,
            findYouTubeTopicThumbnail(identity),
        )
    }

    private fun resolveRemoteCandidate(
        identity: ArtistIdentity,
        source: ArtistImageSource,
        url: String?,
    ): ArtistBackdrop? {
        val imageFile = url?.let { downloadImage(identity.stableKey, source, it) } ?: return null
        val revision = clock.wallTimeMs()
        store.put(
            ArtistImageCacheEntry(
                artistKey = identity.stableKey,
                source = source,
                imageFilePath = imageFile.absolutePath,
                expiresAtMs = revision + POSITIVE_TTL_MS,
                revision = revision,
            ),
        )
        return ArtistBackdrop(identity.stableKey, Uri.fromFile(imageFile), source, revision)
    }

    private suspend fun resolveMusicBrainzIdentity(identity: ArtistIdentity): ArtistIdentity {
        val cached = store.cached(identity.stableKey)?.musicBrainzId
        if (!cached.isNullOrBlank()) return identity.copy(musicBrainzId = cached)
        val mbid = searchMusicBrainzArtist(identity) ?: return identity
        store.mergeMusicBrainzId(identity.stableKey, mbid, clock.wallTimeMs())
        return identity.copy(musicBrainzId = mbid)
    }

    private suspend fun searchMusicBrainzArtist(identity: ArtistIdentity): String? {
        musicBrainzLimiter.awaitTurn()
        val query = "artist:\"${identity.displayName}\""
        val url = "$MUSICBRAINZ_BASE/artist?query=${query.urlEncode()}&fmt=json&limit=5"
        val artists = getJson(url).optJSONArray("artists") ?: return null
        val strongMatches = buildList {
            for (index in 0 until artists.length()) {
                val artist = artists.optJSONObject(index) ?: continue
                val name = artist.optString("name")
                val sortName = artist.optString("sort-name")
                val score = artist.optInt("score", 0)
                val aliases = artist.optJSONArray("aliases")
                val exact = normalizeArtistIdentity(name) == identity.normalizedName ||
                    normalizeArtistIdentity(sortName) == identity.normalizedName ||
                    aliases.containsNormalizedAlias(identity.normalizedName)
                if (exact && score >= 92) {
                    artist.optString("id").takeIf(String::isNotBlank)?.let(::add)
                }
            }
        }.distinct()
        return strongMatches.singleOrNull()
    }

    private fun findFanartTvImage(mbid: String): String? {
        val apiKey = BuildConfig.FANART_TV_API_KEY.takeIf { it.isNotBlank() } ?: return null
        val json = runCatching {
            getJson("https://webservice.fanart.tv/v3/music/$mbid?api_key=${apiKey.urlEncode()}")
        }.getOrNull() ?: return null
        return json.optJSONArray("artistbackground").bestFanartUrl()
    }

    private fun findTheAudioDbImageByMbid(
        mbid: String,
        identity: ArtistIdentity,
    ): String? {
        val json = runCatching {
            getJson("https://www.theaudiodb.com/api/v1/json/2/artist-mb.php?i=${mbid.urlEncode()}")
        }.getOrNull() ?: return null
        return json.optJSONArray("artists").bestAudioDbImage(identity)
    }

    private fun findTheAudioDbImageByName(identity: ArtistIdentity): String? {
        val json = runCatching {
            getJson("https://www.theaudiodb.com/api/v1/json/2/search.php?s=${identity.displayName.urlEncode()}")
        }.getOrNull() ?: return null
        return json.optJSONArray("artists").bestAudioDbImage(identity)
    }

    private fun findYouTubeTopicThumbnail(identity: ArtistIdentity): String? {
        val apiKey = BuildConfig.YOUTUBE_DATA_API_KEY.takeIf { it.isNotBlank() } ?: return null
        val query = "${identity.displayName} Topic"
        val json = runCatching {
            getJson(
                "https://www.googleapis.com/youtube/v3/search" +
                    "?part=snippet&type=channel&maxResults=5&q=${query.urlEncode()}&key=${apiKey.urlEncode()}",
            )
        }.getOrNull() ?: return null
        val items = json.optJSONArray("items") ?: return null
        for (index in 0 until items.length()) {
            val snippet = items.optJSONObject(index)?.optJSONObject("snippet") ?: continue
            val title = snippet.optString("channelTitle").ifBlank { snippet.optString("title") }
            val normalizedTitle = normalizeArtistIdentity(title.removeSuffix("- Topic").trim())
            val topic = title.endsWith(" - Topic", ignoreCase = true)
            if (topic && normalizedTitle == identity.normalizedName) {
                return snippet.optJSONObject("thumbnails")?.bestYouTubeThumbnail()
            }
        }
        return null
    }

    private fun downloadImage(
        artistKey: String,
        source: ArtistImageSource,
        imageUrl: String,
    ): File? {
        if (!imageUrl.startsWith("https://", ignoreCase = true)) return null
        val bytes = runCatching {
            httpTransport.getBytes(
                HttpRequest(
                    url = imageUrl,
                    accept = "image/*",
                    headers = mapOf("User-Agent" to USER_AGENT),
                    connectTimeoutMs = NETWORK_TIMEOUT_MS,
                    readTimeoutMs = NETWORK_TIMEOUT_MS,
                ),
                MAX_IMAGE_BYTES,
            )
        }.getOrNull()?.takeIf { it.isNotEmpty() } ?: return null
        cacheDirectory.mkdirs()
        val file = File(cacheDirectory, "${artistKey.sha256()}-${source.name.lowercase(Locale.US)}.img")
        file.writeBytes(bytes)
        trimImageCache(file)
        return file
    }

    private fun trimImageCache(currentFile: File) {
        val files = cacheDirectory.listFiles { file -> file.isFile } ?: return
        val overflow = files.size - MAX_CACHED_IMAGES
        if (overflow <= 0) return
        files.asSequence()
            .filterNot { it == currentFile }
            .sortedBy(File::lastModified)
            .take(overflow)
            .forEach { it.delete() }
    }

    private fun getJson(url: String): JSONObject {
        val text = httpTransport.getText(
            HttpRequest(
                url = url,
                accept = "application/json",
                headers = mapOf("User-Agent" to USER_AGENT),
                connectTimeoutMs = NETWORK_TIMEOUT_MS,
                readTimeoutMs = NETWORK_TIMEOUT_MS,
            ),
            MAX_JSON_BYTES,
        )
        return JSONObject(text)
    }

    private companion object {
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val USER_AGENT = "Elovaire/${BuildConfig.VERSION_NAME} (artist-image-lookup)"
        const val MUSICBRAINZ_BASE = "https://musicbrainz.org/ws/2"
        const val MUSICBRAINZ_INTERVAL_MS = 1_050L
        const val POSITIVE_TTL_MS = 21L * 24L * 60L * 60L * 1000L
        const val NEGATIVE_TTL_MS = 2L * 24L * 60L * 60L * 1000L
        const val NETWORK_TIMEOUT_MS = 8_000
        const val MAX_JSON_BYTES = 768 * 1024
        const val MAX_IMAGE_BYTES = 6 * 1024 * 1024
        const val MAX_CACHED_IMAGES = 160
    }
}

internal fun normalizeArtistIdentity(value: String): String {
    return Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
        .replace(ARTIST_MARK_REGEX, "")
        .lowercase(Locale.ROOT)
        .replace(ARTIST_WHITESPACE_REGEX, " ")
        .trim()
}

internal fun bestLocalArtistArtwork(
    albums: List<Album>,
    songs: List<Song>,
): Uri? {
    return albums
        .asSequence()
        .filter { it.artUri != null }
        .sortedWith(
            compareByDescending<Album> { it.songCount }
                .thenBy { it.title.lowercase(Locale.ROOT) },
        )
        .firstOrNull()
        ?.artUri
        ?: songs.firstOrNull { it.artUri != null }?.artUri
}

private fun ArtistIdentity.shouldSkipRemoteLookup(): Boolean {
    return shouldSkipArtistRemoteLookup(normalizedName)
}

internal fun shouldSkipArtistRemoteLookup(normalizedName: String): Boolean {
    return normalizedName.isBlank() ||
        normalizedName in NON_REMOTE_ARTIST_NAMES
}

private fun JSONArray?.bestFanartUrl(): String? {
    if (this == null) return null
    var bestUrl: String? = null
    var bestLikes = Int.MIN_VALUE
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        val url = item.optString("url").takeIf(String::isNotBlank) ?: continue
        val likes = item.optInt("likes", 0)
        if (likes > bestLikes) {
            bestLikes = likes
            bestUrl = url
        }
    }
    return bestUrl
}

private fun JSONArray?.bestAudioDbImage(identity: ArtistIdentity): String? {
    if (this == null) return null
    for (index in 0 until length()) {
        val artist = optJSONObject(index) ?: continue
        val returnedName = artist.optString("strArtist")
        if (normalizeArtistIdentity(returnedName) != identity.normalizedName) continue
        return listOf(
            "strArtistFanart",
            "strArtistFanart2",
            "strArtistFanart3",
            "strArtistFanart4",
            "strArtistWideThumb",
            "strArtistThumb",
        ).firstNotNullOfOrNull { key ->
            artist.optString(key).takeIf { it.startsWith("https://", ignoreCase = true) }
        }
    }
    return null
}

private fun JSONArray?.containsNormalizedAlias(normalizedName: String): Boolean {
    if (this == null) return false
    for (index in 0 until length()) {
        val name = optJSONObject(index)?.optString("name").orEmpty()
        if (normalizeArtistIdentity(name) == normalizedName) return true
    }
    return false
}

private fun JSONObject.bestYouTubeThumbnail(): String? {
    return listOf("maxres", "standard", "high", "medium", "default")
        .firstNotNullOfOrNull { key ->
            optJSONObject(key)?.optString("url")?.takeIf { it.startsWith("https://", ignoreCase = true) }
        }
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

private data class ArtistImageCacheEntry(
    val artistKey: String,
    val source: ArtistImageSource,
    val imageFilePath: String?,
    val expiresAtMs: Long,
    val revision: Long,
    val musicBrainzId: String? = null,
) {
    fun isPositiveFresh(nowMs: Long): Boolean = imageFilePath != null && expiresAtMs > nowMs
    fun isNegativeFresh(nowMs: Long): Boolean = imageFilePath == null && expiresAtMs > nowMs
}

private class ArtistImageStore(context: Context) {
    private val preferences = allowStrictModeDiskReads {
        context.getSharedPreferences("artist_image_cache", Context.MODE_PRIVATE)
    }

    fun cached(artistKey: String): ArtistImageCacheEntry? {
        val raw = preferences.getString(artistKey, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            ArtistImageCacheEntry(
                artistKey = artistKey,
                source = runCatching {
                    ArtistImageSource.valueOf(json.optString("source"))
                }.getOrDefault(ArtistImageSource.Generated),
                imageFilePath = json.optString("imageFilePath").takeIf(String::isNotBlank),
                expiresAtMs = json.optLong("expiresAtMs"),
                revision = json.optLong("revision"),
                musicBrainzId = json.optString("musicBrainzId").takeIf(String::isNotBlank),
            )
        }.getOrNull()
    }

    fun put(entry: ArtistImageCacheEntry) {
        val json = JSONObject()
            .put("source", entry.source.name)
            .put("imageFilePath", entry.imageFilePath.orEmpty())
            .put("expiresAtMs", entry.expiresAtMs)
            .put("revision", entry.revision)
            .put("musicBrainzId", entry.musicBrainzId.orEmpty())
        val editor = preferences.edit().putString(entry.artistKey, json.toString())
        val cachedEntries = preferences.all
        val projectedSize = cachedEntries.size + if (entry.artistKey in cachedEntries) 0 else 1
        if (projectedSize > MAX_CACHE_ENTRIES) {
            val cachedRevisions = cachedEntries.mapValues { (_, raw) ->
                (raw as? String)?.let { encoded ->
                    runCatching { JSONObject(encoded).optLong("revision", 0L) }.getOrDefault(0L)
                } ?: 0L
            }
            artistCacheKeysToTrim(cachedRevisions, entry.artistKey, MAX_CACHE_ENTRIES)
                .forEach(editor::remove)
        }
        editor.apply()
    }

    fun mergeMusicBrainzId(
        artistKey: String,
        musicBrainzId: String,
        nowMs: Long,
    ) {
        val current = cached(artistKey)
        put(
            (current ?: ArtistImageCacheEntry(
                artistKey = artistKey,
                source = ArtistImageSource.Generated,
                imageFilePath = null,
                expiresAtMs = 0L,
                revision = nowMs,
            )).copy(musicBrainzId = musicBrainzId),
        )
    }

    private companion object {
        const val MAX_CACHE_ENTRIES = 320
    }
}

internal fun artistCacheKeysToTrim(
    revisions: Map<String, Long>,
    incomingKey: String,
    maxEntries: Int,
): List<String> {
    val projectedSize = revisions.size + if (incomingKey in revisions) 0 else 1
    val overflow = (projectedSize - maxEntries.coerceAtLeast(0)).coerceAtLeast(0)
    if (overflow == 0) return emptyList()
    return revisions.asSequence()
        .filterNot { (key, _) -> key == incomingKey }
        .map { (key, revision) -> key to revision }
        .sortedWith(compareBy<Pair<String, Long>> { it.second }.thenBy { it.first })
        .take(overflow)
        .map(Pair<String, Long>::first)
        .toList()
}

private class ArtistRequestRateLimiter(
    private val minimumIntervalMs: Long,
    private val clock: AppClock,
) {
    private val mutex = Mutex()
    private var lastRequestAtMs = 0L

    suspend fun awaitTurn() = mutex.withLock {
        val now = clock.elapsedTimeMs()
        val waitMs = if (lastRequestAtMs == 0L) 0L else {
            (lastRequestAtMs + minimumIntervalMs - now).coerceAtLeast(0L)
        }
        if (waitMs > 0L) kotlinx.coroutines.delay(waitMs)
        lastRequestAtMs = clock.elapsedTimeMs()
    }
}

private val ARTIST_MARK_REGEX = Regex("\\p{Mn}+")
private val ARTIST_WHITESPACE_REGEX = Regex("\\s+")
private val NON_REMOTE_ARTIST_NAMES = setOf("unknown artist", "various artists", "various", "unknown")
