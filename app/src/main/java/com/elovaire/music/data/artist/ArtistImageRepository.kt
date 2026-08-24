package elovaire.music.droidbeauty.app.data.artist

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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

internal class ArtistImageRepository(
    private val client: YouTubeMusicArtistImageClient = YouTubeMusicArtistImageClient(),
) {
    private sealed interface CachedImage {
        data class Found(val uri: Uri) : CachedImage
    }

    private val remoteArtworkCache = ConcurrentHashMap<String, CachedImage>()

    fun imageState(
        artistName: String,
        localArtworkUri: Uri?,
    ): Flow<ArtistBackdropState> = flow {
        val artistKey = artistName.trim().lowercase(Locale.ROOT)
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
    }.flowOn(Dispatchers.IO)

    fun backdropState(
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

    private suspend fun resolveRemoteArtwork(artistName: String, artistKey: String): Uri? {
        if (artistKey.isBlank() || artistKey == UNKNOWN_ARTIST_KEY) return null
        when (val cached = remoteArtworkCache[artistKey]) {
            is CachedImage.Found -> return cached.uri
            null -> Unit
        }
        val uri = client.findArtistImage(artistName)?.let(Uri::parse)
        if (uri != null) remoteArtworkCache[artistKey] = CachedImage.Found(uri)
        return uri
    }

    private companion object {
        const val UNKNOWN_ARTIST_KEY = "unknown artist"
    }
}
