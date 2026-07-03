package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal fun HomeRouteHost(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playbackState = routeState.playbackState
    val recentSongs = remember(routeState.songsById, playbackState.recentSongIds) {
        playbackState.recentSongIds.mapNotNull(routeState.songsById::get).take(5)
    }
    HomeScreen(
        lastPlayedAlbum = routeState.lastPlayedAlbum,
        lastPlayedPlaylist = routeState.lastPlayedPlaylist,
        songsById = routeState.songsById,
        recentlyAddedAlbums = routeState.recentlyAddedAlbums,
        recentSongs = recentSongs,
        favoriteAlbums = routeState.favoriteAlbums,
        playbackState = playbackState,
        isLibraryLoading = libraryState.isLoading,
        libraryScanProgress = libraryState.scanProgress,
        favoriteSongIds = appState.favoriteSongIds,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.homeScrollRequestVersion,
        resetScrollOnColdStart = routeState.resetHomeScrollOnColdStart,
        playInitialReveal = routeState.playFirstLaunchHomeReveal,
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

@Composable
internal fun PlaylistDetailRouteHost(
    playlistId: Long?,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playbackState = routeState.playbackState
    val playlist = playlistId?.let { id -> appState.playlists.firstOrNull { it.id == id } }
    PlaylistDetailScreen(
        playlist = playlist,
        librarySongs = libraryState.songs,
        favoriteSongIds = appState.favoriteSongIds,
        currentSongId = playbackState.currentSong?.id,
        isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onPlayPlaylist = { songs, sourceLabel ->
            songs.firstOrNull()?.let { firstSong ->
                routeActions.playback.playSongQueue(
                    song = firstSong,
                    queue = songs,
                    sourceLabel = sourceLabel,
                    sourcePlaylistId = playlist?.id,
                )
            }
        },
        onShufflePlaylist = { songs, sourceLabel ->
            val shuffledSongs = songs.shuffled()
            shuffledSongs.firstOrNull()?.let { firstSong ->
                routeActions.playback.playSongQueue(
                    song = firstSong,
                    queue = shuffledSongs,
                    sourceLabel = sourceLabel,
                    sourcePlaylistId = playlist?.id,
                )
            }
        },
        onSongSelected = { song, queue ->
            routeActions.playback.playSongQueue(
                song = song,
                queue = queue,
                sourceLabel = playlist?.name ?: queue.playbackSourceLabel(
                    fallbackAlbum = song.album,
                    language = appState.appLanguage,
                ),
                sourcePlaylistId = playlist?.id,
            )
        },
        onUpdateSongOrder = { songIds ->
            playlistId?.let { routeActions.updatePlaylistSongOrder(it, songIds) }
        },
        onRenamePlaylist = routeActions::renamePlaylist,
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}
