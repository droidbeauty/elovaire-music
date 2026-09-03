package elovaire.music.droidbeauty.app.data.artist

import android.content.Context
import android.net.Uri
import android.util.Log
import elovaire.music.droidbeauty.app.data.artwork.isArtworkFileDecodable
import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.isWallTimeDeadlineFresh
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

sealed interface ArtistBackdropState {
    data object Loading : ArtistBackdropState
    data class Fallback(
        val localArtworkUri: Uri?,
        val artistKey: String,
        val remoteArtworkUri: Uri? = null,
    ) : ArtistBackdropState {
        val artworkUri: Uri?
            get() = remoteArtworkUri ?: localArtworkUri
    }
}

internal interface ArtistImageReader {
    fun imageState(artistName: String, localArtworkUri: Uri?): Flow<ArtistBackdropState>

    fun backdropState(
        artistName: String,
        songs: List<Song>,
        albums: List<Album>,
    ): Flow<ArtistBackdropState>
}

internal class ArtistImageRepository(
    private val client: ArtistImageClient = YouTubeMusicArtistImageClient(),
    private val scope: CoroutineScope,
    private val appContext: Context? = null,
    private val clock: AppClock = AndroidAppClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistImageReader {
    private data class CachedImage(
        val uri: Uri?,
        val expiresAtMs: Long,
    )

    private sealed interface ResolutionClaim {
        data class Cached(val uri: Uri?) : ResolutionClaim
        data class Await(val deferred: CompletableDeferred<Uri?>) : ResolutionClaim
        data class Owner(val deferred: CompletableDeferred<Uri?>) : ResolutionClaim
        data object Saturated : ResolutionClaim
    }

    private val cacheLock = Any()
    private val remoteArtworkCache = LinkedHashMap<String, CachedImage>(CACHE_CAPACITY, 0.75f, true)
    private val inFlight = LinkedHashMap<String, CompletableDeferred<Uri?>>()
    private val artworkTransport = BoundedHttpTransport(
        connectTimeoutMs = REMOTE_ARTWORK_CONNECT_TIMEOUT_MS,
        readTimeoutMs = REMOTE_ARTWORK_READ_TIMEOUT_MS,
    )

    override fun imageState(
        artistName: String,
        localArtworkUri: Uri?,
    ): Flow<ArtistBackdropState> = flow {
        val artistKey = artistName.trim().lowercase(Locale.ROOT).replace(ARTIST_KEY_WHITESPACE, " ")
        emit(
            ArtistBackdropState.Fallback(
                localArtworkUri = localArtworkUri,
                artistKey = artistKey,
            ),
        )
        val remoteArtworkUri = resolveRemoteArtwork(artistName, artistKey)
        if (remoteArtworkUri != null) {
            emit(
                ArtistBackdropState.Fallback(
                    localArtworkUri = localArtworkUri,
                    artistKey = artistKey,
                    remoteArtworkUri = remoteArtworkUri,
                ),
            )
        }
    }.flowOn(ioDispatcher)

    override fun backdropState(
        artistName: String,
        songs: List<Song>,
        albums: List<Album>,
    ): Flow<ArtistBackdropState> {
        val localArtworkUri = albums
                .asSequence()
                .filter { it.artUri != null }
                .sortedWith(compareByDescending<Album> { it.songCount }.thenBy { it.title.lowercase(Locale.ROOT) })
                .mapNotNull(Album::artUri)
                .firstOrNull()
                ?: songs
                    .asSequence()
                    .filter { it.artUri != null }
                    .sortedWith(compareByDescending<Song> { it.durationMs }.thenBy { it.album.lowercase(Locale.ROOT) })
                    .mapNotNull(Song::artUri)
                    .firstOrNull()
        return imageState(artistName, localArtworkUri)
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (pressure == MemoryPressure.Normal) return
        synchronized(cacheLock) {
            remoteArtworkCache.clear()
        }
    }

    fun release() {
        synchronized(cacheLock) {
            inFlight.values.forEach { it.cancel() }
            inFlight.clear()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun resolveRemoteArtwork(artistName: String, artistKey: String): Uri? {
        if (artistKey.isBlank() || artistKey == UNKNOWN_ARTIST_KEY) return null
        val claim = synchronized(cacheLock) {
            val now = clock.wallTimeMs()
            remoteArtworkCache[artistKey]?.let { cached ->
                val maxRemainingMs = if (cached.uri == null) NEGATIVE_CACHE_TTL_MS else POSITIVE_CACHE_TTL_MS
                if (isWallTimeDeadlineFresh(now, cached.expiresAtMs, maxRemainingMs)) {
                    return@synchronized ResolutionClaim.Cached(cached.uri)
                }
                remoteArtworkCache.remove(artistKey)
            }
            inFlight[artistKey]?.let { deferred ->
                return@synchronized ResolutionClaim.Await(deferred)
            }
            if (inFlight.size >= MAX_IN_FLIGHT) {
                return@synchronized ResolutionClaim.Saturated
            }
            CompletableDeferred<Uri?>().let { deferred ->
                inFlight[artistKey] = deferred
                ResolutionClaim.Owner(deferred)
            }
        }
        val deferred = when (claim) {
            is ResolutionClaim.Cached -> return claim.uri
            is ResolutionClaim.Await -> return claim.deferred.await()
            ResolutionClaim.Saturated -> return null
            is ResolutionClaim.Owner -> claim.deferred.also { ownerDeferred ->
                val ownerJob = scope.launch(ioDispatcher) {
                    try {
                        resolveOwnedRemoteArtwork(artistName, artistKey, ownerDeferred)
                    } catch (cancelled: CancellationException) {
                        failRemoteArtwork(artistKey, ownerDeferred, cancelled)
                        throw cancelled
                    } catch (failure: RuntimeException) {
                        failRemoteArtwork(artistKey, ownerDeferred, failure)
                        Log.w(TAG, "Artist artwork request failed", failure)
                    }
                }
                ownerJob.invokeOnCompletion { cause ->
                    if (cause != null && !ownerDeferred.isCompleted) {
                        failRemoteArtwork(artistKey, ownerDeferred, cause)
                    }
                }
            }
        }
        return deferred.await()
    }

    private suspend fun resolveOwnedRemoteArtwork(
        artistName: String,
        artistKey: String,
        deferred: CompletableDeferred<Uri?>,
    ) {
        cachedArtworkFile(artistKey)?.let { file ->
            completeRemoteArtwork(artistKey, deferred, Uri.fromFile(file), cacheResult = true)
            return
        }
        try {
            when (val lookup = client.findArtistImage(artistName)) {
                is ArtistImageLookup.Found -> {
                    val uri = lookup.uri
                    val resolvedUri = persistArtwork(uri, artistKey) ?: uri
                    completeRemoteArtwork(
                        artistKey,
                        deferred,
                        resolvedUri,
                        cacheResult = true,
                    )
                }
                ArtistImageLookup.NotFound -> completeRemoteArtwork(
                    artistKey,
                    deferred,
                    null,
                    cacheResult = true,
                )
                ArtistImageLookup.Failed -> completeRemoteArtwork(
                    artistKey,
                    deferred,
                    null,
                    cacheResult = false,
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            completeRemoteArtwork(
                artistKey,
                deferred,
                null,
                cacheResult = false,
            )
        } catch (failure: IllegalArgumentException) {
            completeRemoteArtwork(
                artistKey,
                deferred,
                null,
                cacheResult = false,
            )
            throw failure
        } catch (failure: IllegalStateException) {
            completeRemoteArtwork(
                artistKey,
                deferred,
                null,
                cacheResult = false,
            )
            throw failure
        }
    }

    private fun failRemoteArtwork(
        artistKey: String,
        deferred: CompletableDeferred<Uri?>,
        failure: Throwable,
    ) {
        synchronized(cacheLock) {
            inFlight.remove(artistKey)
        }
        if (failure is CancellationException) {
            deferred.cancel(failure)
        } else {
            deferred.complete(null)
        }
    }

    private fun completeRemoteArtwork(
        artistKey: String,
        deferred: CompletableDeferred<Uri?>,
        uri: Uri?,
        cacheResult: Boolean,
    ): Uri? {
        synchronized(cacheLock) {
            if (cacheResult) {
                remoteArtworkCache[artistKey] = CachedImage(
                    uri = uri,
                    expiresAtMs = clock.wallTimeMs() +
                        if (uri == null) NEGATIVE_CACHE_TTL_MS else POSITIVE_CACHE_TTL_MS,
                )
                trimMemoryCacheLocked()
            }
            inFlight.remove(artistKey)
        }
        deferred.complete(uri)
        return uri
    }

    private fun cachedArtworkFile(artistKey: String): File? {
        val directory = appContext
            ?.cacheDir
            ?.resolve(ARTIST_IMAGE_CACHE_DIRECTORY)
            ?: return null
        directory.resolve("${cacheKey(artistKey)}.tmp").delete()
        val file = directory.resolve("${cacheKey(artistKey)}.img")
        if (!file.isFile || file.length() <= 0L) return null
        val ageMs = clock.wallTimeMs() - file.lastModified()
        if (ageMs < 0L || ageMs > POSITIVE_CACHE_TTL_MS) {
            file.delete()
            return null
        }
        return file
    }

    private fun persistArtwork(uri: Uri, artistKey: String): Uri? {
        if (uri.scheme != "https") return null
        val context = appContext ?: return null
        val directory = context.cacheDir.resolve(ARTIST_IMAGE_CACHE_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) return null
        val cachedFile = directory.resolve("${cacheKey(artistKey)}.img")
        val temporaryFile = directory.resolve("${cacheKey(artistKey)}.tmp")
        var committed = false
        return try {
            val response = artworkTransport.getBlockingToFile(
                rawUrl = uri.toString(),
                target = temporaryFile,
                headers = mapOf("Accept" to "image/*"),
                maxBytes = MAX_REMOTE_ARTWORK_BYTES,
            )
            if (response.statusCode !in 200..299 || response.bytesWritten <= 0L) return null
            if (!isArtworkFileDecodable(temporaryFile)) return null
            if (!temporaryFile.renameTo(cachedFile)) {
                return null
            }
            committed = true
            trimDiskArtworkCache(directory, cachedFile)
            Uri.fromFile(cachedFile)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        } finally {
            if (!committed) temporaryFile.delete()
        }
    }

    private fun cacheKey(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { byte ->
            "%02x".format(Locale.ROOT, byte.toInt() and 0xff)
        }
    }

    private fun trimMemoryCacheLocked() {
        val now = clock.wallTimeMs()
        remoteArtworkCache.entries.removeIf { (_, value) -> value.expiresAtMs <= now }
        while (remoteArtworkCache.size > CACHE_CAPACITY) {
            remoteArtworkCache.entries.iterator().apply {
                if (hasNext()) {
                    next()
                    remove()
                }
            }
        }
    }

    private fun trimDiskArtworkCache(directory: File, keep: File) {
        val files = directory.listFiles()
            ?.filter { it.isFile && it.extension == "img" && it != keep }
            ?.sortedByDescending(File::lastModified)
            .orEmpty()
        directory.listFiles()
            ?.filter { it.isFile && it.extension == "tmp" }
            ?.forEach(File::delete)
        var totalBytes = keep.length()
        files.forEachIndexed { index, file ->
            if (index < DISK_CACHE_FILE_LIMIT - 1 && totalBytes + file.length() <= MAX_DISK_CACHE_BYTES) {
                totalBytes += file.length()
            } else {
                file.delete()
            }
        }
    }

    private companion object {
        const val TAG = "ArtistImageRepository"
        const val UNKNOWN_ARTIST_KEY = "unknown artist"
        const val ARTIST_IMAGE_CACHE_DIRECTORY = "artist-images"
        const val MAX_REMOTE_ARTWORK_BYTES = 2 * 1024 * 1024
        const val REMOTE_ARTWORK_CONNECT_TIMEOUT_MS = 5_000
        const val REMOTE_ARTWORK_READ_TIMEOUT_MS = 5_000
        const val CACHE_CAPACITY = 64
        const val MAX_IN_FLIGHT = 8
        const val POSITIVE_CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 10L * 60L * 1_000L
        const val DISK_CACHE_FILE_LIMIT = 64
        const val MAX_DISK_CACHE_BYTES = 16L * 1024L * 1024L
        val ARTIST_KEY_WHITESPACE = Regex("\\s+")
    }
}
