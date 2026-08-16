package elovaire.music.droidbeauty.app.data.artist

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.decodeArtworkBytes
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.SupervisorJob

sealed interface ArtistBackdropState {
    data object Loading : ArtistBackdropState

    data class Fallback(
        val localArtworkUri: Uri?,
        val artistKey: String,
    ) : ArtistBackdropState

    data class Remote(
        val localArtworkUri: Uri?,
        val remoteArtworkUri: Uri,
        val artistKey: String,
    ) : ArtistBackdropState
}

internal class ArtistImageRepository(
    context: Context?,
    private val appScope: CoroutineScope,
    private val remoteSource: ArtistImageRemoteSource = TheAudioDbArtistImageRemoteSource(
        BuildConfig.THEAUDIODB_API_KEY,
    ),
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private val cache = context?.let { ArtistImageCache(it.applicationContext, nowMs) }
    private val fetchSemaphore = Semaphore(MAX_CONCURRENT_FETCHES)
    private val inFlightMutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<FetchOutcome>>()

    internal constructor() : this(
        context = null,
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        remoteSource = TheAudioDbArtistImageRemoteSource(""),
    )

    fun backdropState(
        artistName: String,
        songs: List<Song>,
        albums: List<Album>,
    ): Flow<ArtistBackdropState> {
        return imageState(artistName, localArtistArtworkUri(songs, albums))
    }

    fun imageState(
        artistName: String,
        localArtworkUri: Uri?,
    ): Flow<ArtistBackdropState> = flow {
        val artistKey = stableArtistCacheKey(artistName)
            ?: artistName.trim().lowercase(Locale.ROOT)
        val fallback = ArtistBackdropState.Fallback(localArtworkUri, artistKey)
        emit(fallback)
        if (stableArtistCacheKey(artistName) == null) return@flow
        val imageCache = cache ?: return@flow

        val cached = withContext(Dispatchers.IO) { imageCache.get(artistKey) }
        val cachedUri = cached?.let { withContext(Dispatchers.IO) { imageCache.imageUri(it) } }
        if (cachedUri != null) {
            emit(ArtistBackdropState.Remote(localArtworkUri, cachedUri, artistKey))
            if (
                cached.fetchedAtMs > nowMs() - POSITIVE_CACHE_TTL_MS ||
                cached.negativeUntilMs > nowMs()
            ) return@flow
        } else if (cached?.negativeUntilMs?.let { it > nowMs() } == true) {
            return@flow
        }

        when (val outcome = sharedFetch(artistKey, artistName, cached).await()) {
            is FetchOutcome.Image -> {
                val uri = withContext(Dispatchers.IO) { imageCache.imageUri(outcome.entry) }
                if (uri != null) emit(ArtistBackdropState.Remote(localArtworkUri, uri, artistKey))
            }

            FetchOutcome.NoReliableMatch,
            FetchOutcome.Failed,
            -> Unit
        }
    }

    private suspend fun sharedFetch(
        artistKey: String,
        artistName: String,
        cached: ArtistImageCacheEntry?,
    ): Deferred<FetchOutcome> = inFlightMutex.withLock {
        inFlight[artistKey]?.let { return@withLock it }
        val deferred = appScope.async(Dispatchers.IO) {
            fetchSemaphore.withPermit { fetch(artistKey, artistName, cached) }
        }
        inFlight[artistKey] = deferred
        deferred.invokeOnCompletion {
            appScope.async {
                inFlightMutex.withLock {
                    if (inFlight[artistKey] === deferred) inFlight.remove(artistKey)
                }
            }
        }
        deferred
    }

    private suspend fun fetch(
        artistKey: String,
        artistName: String,
        cached: ArtistImageCacheEntry?,
    ): FetchOutcome {
        val imageCache = cache ?: return FetchOutcome.Failed
        return when (val resolved = remoteSource.resolveArtistImage(artistName, cached?.providerArtistId)) {
            is ArtistImageRemoteResult.Match -> {
                when (val downloaded = remoteSource.downloadArtistImage(resolved.value.imageUrl, cached?.etag, cached?.lastModified)) {
                    is ArtistImageDownloadResult.Success -> {
                        val decoded = withContext(Dispatchers.Default) {
                            decodeArtworkBytes(downloaded.bytes, ARTIST_THUMBNAIL_PX, ArtworkPurpose.UiGrid)
                        } ?: return FetchOutcome.Failed
                        decoded.recycle()
                        withContext(Dispatchers.IO) {
                            imageCache.putImage(
                                artistKey = artistKey,
                                match = resolved.value,
                                bytes = downloaded.bytes,
                                etag = downloaded.etag,
                                lastModified = downloaded.lastModified,
                            )
                        }?.let(FetchOutcome::Image) ?: FetchOutcome.Failed
                    }

                    ArtistImageDownloadResult.NotModified -> {
                        cached?.let {
                            withContext(Dispatchers.IO) { imageCache.markValidated(artistKey) }
                                ?.let(FetchOutcome::Image)
                        } ?: FetchOutcome.Failed
                    }
                    ArtistImageDownloadResult.Unauthorized,
                    is ArtistImageDownloadResult.RateLimited,
                    ArtistImageDownloadResult.TransientFailure,
                    ArtistImageDownloadResult.InvalidResponse,
                    -> FetchOutcome.Failed
                }
            }

            ArtistImageRemoteResult.NoReliableMatch -> {
                withContext(Dispatchers.IO) {
                    imageCache.putNegative(artistKey, nowMs() + NEGATIVE_CACHE_TTL_MS)
                }
                cached?.takeIf { it.imageFileName != null }?.let(FetchOutcome::Image)
                    ?: FetchOutcome.NoReliableMatch
            }

            ArtistImageRemoteResult.Disabled,
            ArtistImageRemoteResult.Unauthorized,
            is ArtistImageRemoteResult.RateLimited,
            ArtistImageRemoteResult.TransientFailure,
            ArtistImageRemoteResult.MalformedResponse,
            -> FetchOutcome.Failed
        }
    }

    private fun localArtistArtworkUri(songs: List<Song>, albums: List<Album>): Uri? {
        return albums
            .asSequence()
            .filter { it.artUri != null }
            .sortedWith(compareByDescending<Album> { it.songCount }.thenBy { it.title.lowercase() })
            .mapNotNull(Album::artUri)
            .firstOrNull()
            ?: songs
                .asSequence()
                .filter { it.artUri != null }
                .sortedWith(compareByDescending<Song> { it.durationMs }.thenBy { it.album.lowercase() })
                .mapNotNull(Song::artUri)
                .firstOrNull()
    }

    private sealed interface FetchOutcome {
        data class Image(val entry: ArtistImageCacheEntry) : FetchOutcome
        data object NoReliableMatch : FetchOutcome
        data object Failed : FetchOutcome
    }

    private companion object {
        const val ARTIST_THUMBNAIL_PX = 250
        const val MAX_CONCURRENT_FETCHES = 3
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1_000L
    }
}

private suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
    acquire()
    return try {
        block()
    } finally {
        release()
    }
}
