package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
internal fun HomeRouteHost(
    navState: RootNavigationState,
    state: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    HomeScreen(
        lastPlayedAlbum = state.libraryDerivedState.lastPlayedAlbum,
        lastPlayedPlaylist = state.libraryDerivedState.lastPlayedPlaylist,
        songsById = state.libraryDerivedState.songsById,
        recentlyAddedAlbums = state.libraryDerivedState.recentlyAddedAlbums,
        recentSongs = state.recentSongs,
        favoriteAlbums = state.libraryDerivedState.favoriteAlbums,
        playbackState = state.appState.playback,
        isLibraryLoading = state.appState.library.isLoading,
        isLibraryAuthoritative = state.appState.library.isAuthoritative,
        libraryScanProgress = state.appState.library.scanProgress,
        favoriteSongIds = state.appState.favoriteSongIds,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.homeScrollRequestVersion,
        resetScrollOnColdStart = state.resetHomeScrollOnColdStart,
        playInitialReveal = state.playFirstLaunchHomeReveal,
        onInitialRevealFinished = routeActions.onInitialRevealFinished,
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.HomeSection)
        },
        onPlaylistSelected = { playlist -> routeActions.openPlaylist(playlist.id) },
        onPlayAlbum = { album -> routeActions.playback.playAlbum(album) },
        onPlayPlaylist = routeActions.playback::playPlaylist,
        onShufflePlaylist = { playlist, songs ->
            routeActions.playback.playPlaylist(playlist, songs, shuffle = true)
        },
        onOpenRecentlyAdded = routeActions::openRecentlyAdded,
        onSongSelected = routeActions.playback::playSongFromAlbumOrSingle,
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}
