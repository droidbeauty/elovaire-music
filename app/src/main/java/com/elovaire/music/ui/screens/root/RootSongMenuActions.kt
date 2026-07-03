package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase

internal data class SongMenuActions(
    val playlists: List<Playlist> = emptyList(),
    val songsById: Map<Long, Song> = emptyMap(),
    val onAddToPlaylist: (playlistId: Long, song: Song) -> Unit = { _, _ -> },
    val onCreatePlaylist: (String) -> Long = { -1L },
    val onAddToQueue: (Song) -> Unit = {},
    val onGoToAlbum: (Song) -> Unit = {},
    val onDeleteFromLibrary: (Song) -> Unit = {},
    val deletePhrase: UiPhrase = UiPhrase.DeleteFromLibrary,
)

internal val LocalSongMenuActions = compositionLocalOf { SongMenuActions() }

@Composable
internal fun rememberRootSongMenuActions(
    playlists: List<Playlist>,
    songsById: Map<Long, Song>,
    albumsById: Map<Long, Album>,
    playbackManager: PlaybackManager,
    preferenceStore: PreferenceStore,
    onDeleteSongsFromDevice: (List<Song>) -> Unit,
    openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
    navigateToAlbumId: (Long) -> Unit,
): SongMenuActions {
    return remember(playlists, songsById, albumsById, playbackManager, preferenceStore, onDeleteSongsFromDevice) {
        SongMenuActions(
            playlists = playlists.filterNot { it.isSystem },
            songsById = songsById,
            onAddToPlaylist = { playlistId, song ->
                preferenceStore.addSongsToPlaylist(playlistId, listOf(song.id))
            },
            onCreatePlaylist = preferenceStore::createPlaylist,
            onAddToQueue = playbackManager::enqueueSong,
            onGoToAlbum = { song ->
                val album = albumsById[song.albumId]
                if (album != null) {
                    openAlbum(album, ExpandOrigin(), AlbumOpenSource.LibraryAlbums)
                } else if (song.albumId != 0L) {
                    navigateToAlbumId(song.albumId)
                }
            },
            onDeleteFromLibrary = { song ->
                onDeleteSongsFromDevice(listOf(song))
            },
        )
    }
}
