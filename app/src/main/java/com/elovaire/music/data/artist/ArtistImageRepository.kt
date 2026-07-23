package elovaire.music.droidbeauty.app.data.artist

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
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
            localArtworkUri = albums.firstOrNull { it.artUri != null }?.artUri
                ?: songs.firstOrNull { it.artUri != null }?.artUri,
            artistKey = artistName.trim().lowercase(),
        ),
    )
}
