package elovaire.music.droidbeauty.app.data.artist

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed interface ArtistBackdropState {
    data object Loading : ArtistBackdropState
    data class Fallback(val localArtworkUri: Uri?, val artistKey: String) : ArtistBackdropState
}

internal class ArtistImageRepository {
    fun backdropState(
        artistName: String,
        songs: List<Song>,
        albums: List<Album>,
    ): Flow<ArtistBackdropState> = flowOf(
        ArtistBackdropState.Fallback(
            localArtworkUri = albums
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
                    .firstOrNull(),
            artistKey = artistName.trim().lowercase(Locale.ROOT),
        ),
    )
}
