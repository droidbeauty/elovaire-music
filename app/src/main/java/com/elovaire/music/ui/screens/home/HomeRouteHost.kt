package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
internal fun HomeRouteHost(
    navState: RootNavigationState,
    state: HomeRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    HomeScreen(
        lastPlayedAlbum = state.lastPlayedAlbum,
        lastPlayedPlaylist = state.lastPlayedPlaylist,
        songsById = state.songsById,
        recentlyAddedAlbums = state.recentlyAddedAlbums,
        recentSongs = state.recentSongs,
        favoriteAlbums = state.favoriteAlbums,
        playbackState = state.playbackState,
        isLibraryLoading = state.isLibraryLoading,
        libraryScanProgress = state.libraryScanProgress,
        favoriteSongIds = state.favoriteSongIds,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.homeScrollRequestVersion,
        resetScrollOnColdStart = state.resetScrollOnColdStart,
        playInitialReveal = state.playInitialReveal,
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
        onSongSelected = routeActions.playback::playSongFromAlbumOrSingle,
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}
