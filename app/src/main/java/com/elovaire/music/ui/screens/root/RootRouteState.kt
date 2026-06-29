package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class RootRouteState(
    val appState: RootAppState,
    val libraryState: LibraryUiState,
    val playbackState: PlaybackUiState,
    val songsById: Map<Long, Song>,
    val songsByAlbumId: Map<Long, List<Song>>,
    val albumsById: Map<Long, Album>,
    val playlistsById: Map<Long, Playlist>,
    val recentlyAddedAlbums: List<Album>,
    val recentAlbums: List<Album>,
    val favoriteAlbums: List<Album>,
    val lastPlayedAlbum: Album?,
    val lastPlayedPlaylist: Playlist?,
    val albumCollectionLayoutMode: AlbumLayoutMode,
    val isPlaybackActuallyPlaying: Boolean,
    val resetHomeScrollOnColdStart: Boolean,
    val playFirstLaunchHomeReveal: Boolean,
    val searchFieldFocused: Boolean,
)

internal fun rootRouteStateOf(
    appState: RootAppState,
    derivedState: RootLibraryDerivedState,
    albumCollectionLayoutMode: AlbumLayoutMode,
    resetHomeScrollOnColdStart: Boolean,
    playFirstLaunchHomeReveal: Boolean,
    searchFieldFocused: Boolean,
): RootRouteState {
    val playbackState = appState.playback
    return RootRouteState(
        appState = appState,
        libraryState = appState.library,
        playbackState = playbackState,
        songsById = derivedState.songsById,
        songsByAlbumId = derivedState.songsByAlbumId,
        albumsById = derivedState.albumsById,
        playlistsById = derivedState.playlistsById,
        recentlyAddedAlbums = derivedState.recentlyAddedAlbums,
        recentAlbums = derivedState.recentAlbums,
        favoriteAlbums = derivedState.favoriteAlbums,
        lastPlayedAlbum = derivedState.lastPlayedAlbum,
        lastPlayedPlaylist = derivedState.lastPlayedPlaylist,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = playFirstLaunchHomeReveal,
        searchFieldFocused = searchFieldFocused,
    )
}
