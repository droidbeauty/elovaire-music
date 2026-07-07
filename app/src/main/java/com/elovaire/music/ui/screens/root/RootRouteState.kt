package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class HomeRouteState(
    val lastPlayedAlbum: Album?,
    val lastPlayedPlaylist: Playlist?,
    val songsById: Map<Long, Song>,
    val recentlyAddedAlbums: List<Album>,
    val recentSongs: List<Song>,
    val favoriteAlbums: List<Album>,
    val playbackState: PlaybackUiState,
    val isLibraryLoading: Boolean,
    val libraryScanProgress: Float,
    val favoriteSongIds: Set<Long>,
    val resetScrollOnColdStart: Boolean,
    val playInitialReveal: Boolean,
)

internal data class PlaylistRouteState(
    val playlists: List<Playlist>,
    val smartPlaylists: List<elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist>,
    val libraryState: LibraryUiState,
    val favoriteSongIds: Set<Long>,
    val songPlayCounts: Map<Long, Int>,
    val recentSongIds: List<Long>,
    val currentSongId: Long?,
    val isCurrentSongPlaying: Boolean,
    val songsById: Map<Long, Song>,
    val appLanguage: elovaire.music.droidbeauty.app.domain.model.AppLanguage,
)

internal data class RootRouteState(
    val appState: RootAppState,
    val libraryState: LibraryUiState,
    val playbackState: PlaybackUiState,
    val home: HomeRouteState,
    val playlists: PlaylistRouteState,
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
    val recentSongs = playbackState.recentSongIds.mapNotNull(derivedState.songsById::get).take(5)
    val home = HomeRouteState(
        lastPlayedAlbum = derivedState.lastPlayedAlbum,
        lastPlayedPlaylist = derivedState.lastPlayedPlaylist,
        songsById = derivedState.songsById,
        recentlyAddedAlbums = derivedState.recentlyAddedAlbums,
        recentSongs = recentSongs,
        favoriteAlbums = derivedState.favoriteAlbums,
        playbackState = playbackState,
        isLibraryLoading = appState.library.isLoading,
        libraryScanProgress = appState.library.scanProgress,
        favoriteSongIds = appState.favoriteSongIds,
        resetScrollOnColdStart = resetHomeScrollOnColdStart,
        playInitialReveal = playFirstLaunchHomeReveal,
    )
    val playlists = PlaylistRouteState(
        playlists = appState.playlists,
        smartPlaylists = appState.smartPlaylists,
        libraryState = appState.library,
        favoriteSongIds = appState.favoriteSongIds,
        songPlayCounts = appState.songPlayCounts,
        recentSongIds = playbackState.recentSongIds,
        currentSongId = playbackState.currentSong?.id,
        isCurrentSongPlaying = playbackState.isPlaying && playbackState.currentSong != null,
        songsById = derivedState.songsById,
        appLanguage = appState.appLanguage,
    )
    return RootRouteState(
        appState = appState,
        libraryState = appState.library,
        playbackState = playbackState,
        home = home,
        playlists = playlists,
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
