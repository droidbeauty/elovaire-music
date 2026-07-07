package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.domain.model.Song

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
internal fun PlaylistsRouteHost(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playbackState = routeState.playbackState
    val smartSummaries = remember(
        appState.smartPlaylists,
        libraryState.songs,
        appState.favoriteSongIds,
        appState.songPlayCounts,
        playbackState.recentSongIds,
    ) {
        buildSmartPlaylistSummaries(
            playlists = appState.smartPlaylists,
            songs = libraryState.songs,
            favoriteSongIds = appState.favoriteSongIds,
            songPlayCounts = appState.songPlayCounts,
            recentSongIds = playbackState.recentSongIds,
        )
    }
    PlaylistsScreen(
        playlists = appState.playlists,
        smartPlaylists = smartSummaries,
        libraryState = libraryState,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.playlistsScrollRequestVersion,
        onRenamePlaylist = routeActions::renamePlaylist,
        onDeletePlaylists = routeActions::deletePlaylists,
        onOpenPlaylist = { playlist, origin -> routeActions.openPlaylist(playlist.id, origin) },
        onOpenSmartPlaylist = { summary, origin -> routeActions.openSmartPlaylist(summary.playlist.id, origin) },
    )
}

@Composable
internal fun SmartPlaylistDetailRouteHost(
    playlistId: Long?,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playbackState = routeState.playbackState
    val playlist = appState.smartPlaylists.firstOrNull { it.id == playlistId }
    SmartPlaylistDetailScreen(
        playlist = playlist,
        songs = libraryState.songs,
        favoriteSongIds = appState.favoriteSongIds,
        songPlayCounts = appState.songPlayCounts,
        recentSongIds = playbackState.recentSongIds,
        currentSongId = playbackState.currentSong?.id,
        isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onEdit = { routeActions.openSmartPlaylistEditor(it.id) },
        onDelete = routeActions::deleteSmartPlaylist,
        onConvertToNormalPlaylist = { smart, songs ->
            routeActions.playlists.createPlaylistAndAddSongs(smart.name, songs.map(Song::id))
        },
        onPlay = { smart, songs, shuffle ->
            val queue = if (shuffle) songs.shuffled() else songs
            queue.firstOrNull()?.let { first ->
                routeActions.playback.playSongQueue(
                    song = first,
                    queue = queue,
                    sourceLabel = smart.name,
                )
            }
        },
        onSongSelected = { song, queue, smart ->
            routeActions.playback.playSongQueue(song, queue, sourceLabel = smart.name)
        },
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}

@Composable
internal fun SmartPlaylistEditorRouteHost(
    playlistId: Long?,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playlist = playlistId?.let { id -> appState.smartPlaylists.firstOrNull { it.id == id } }
    val now = remember { System.currentTimeMillis() }
    SmartPlaylistEditorScreen(
        playlist = playlist,
        songs = libraryState.songs,
        favoriteSongIds = appState.favoriteSongIds,
        songPlayCounts = appState.songPlayCounts,
        recentSongIds = routeState.playbackState.recentSongIds,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onSave = { smart ->
            if (playlistId == null) {
                val id = routeActions.createSmartPlaylist(smart.name)
                if (id > 0L) {
                    routeActions.updateSmartPlaylist(
                        smart.copy(id = id, createdAtMs = now, updatedAtMs = System.currentTimeMillis()),
                    )
                    routeActions.navigateUp()
                    routeActions.openSmartPlaylist(id)
                }
            } else {
                routeActions.updateSmartPlaylist(smart)
                routeActions.navigateUp()
            }
        },
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
