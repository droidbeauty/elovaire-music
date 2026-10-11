package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

@Composable
internal fun PlaylistsRouteHost(
    navState: RootNavigationState,
    state: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val smartSummaries = remember(
        state.appState.smartPlaylists,
        state.appState.library.songs,
        state.appState.favoriteSongIds,
        state.appState.songPlayCounts,
    ) {
        SmartPlaylistEngine.resolveAll(
            definitions = state.appState.smartPlaylists,
            songs = state.appState.library.songs,
            favoriteSongIds = state.appState.favoriteSongIds,
            playCounts = state.appState.songPlayCounts,
        )
    }
    PlaylistsScreen(
        playlists = state.appState.playlists,
        smartPlaylists = smartSummaries,
        songsById = state.libraryDerivedState.songsById,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.playlistsScrollRequestVersion,
        onRenamePlaylist = routeActions::renamePlaylist,
        onDeletePlaylists = routeActions::deletePlaylists,
        onOpenPlaylist = { playlist, origin -> routeActions.openPlaylist(playlist.id, origin) },
        onOpenSmartPlaylist = { result, origin -> routeActions.openSmartPlaylist(result.playlist.id, origin) },
    )
}

@Composable
internal fun SmartPlaylistDetailRouteHost(
    playlistId: Long?,
    state: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val playlist = state.appState.smartPlaylists.firstOrNull { it.id == playlistId }
    val scope = rememberCoroutineScope()
    SmartPlaylistDetailScreen(
        playlist = playlist,
        songs = state.appState.library.songs,
        favoriteSongIds = state.appState.favoriteSongIds,
        songPlayCounts = state.appState.songPlayCounts,
        currentSongId = state.appState.playback.currentSong?.id,
        isCurrentSongPlaying = state.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onEdit = { routeActions.openSmartPlaylistEditor(it.id) },
        onDelete = { id ->
            scope.launch {
                if (routeActions.deleteSmartPlaylist(id).await() is PlaylistMutationResult.Success) {
                    routeActions.navigateUp()
                }
            }
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
    state: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val playlist = playlistId?.let { id -> state.appState.smartPlaylists.firstOrNull { it.id == id } }
    val scope = rememberCoroutineScope()
    SmartPlaylistEditorScreen(
        playlist = playlist,
        songs = state.appState.library.songs,
        favoriteSongIds = state.appState.favoriteSongIds,
        songPlayCounts = state.appState.songPlayCounts,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onSave = { smart ->
            scope.launch {
                if (playlistId == null) {
                    when (val created = routeActions.createSmartPlaylist(smart).await()) {
                        is PlaylistMutationResult.Success -> {
                            val id = created.playlistId ?: return@launch
                            routeActions.navigateUp()
                            routeActions.openSmartPlaylist(id)
                        }
                        else -> Unit
                    }
                } else if (routeActions.updateSmartPlaylist(smart).await() is PlaylistMutationResult.Success) {
                    routeActions.navigateUp()
                }
            }
        },
    )
}

@Composable
internal fun PlaylistDetailRouteHost(
    playlistId: Long?,
    state: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val playlist = playlistId?.let { id -> state.appState.playlists.firstOrNull { it.id == id } }
    PlaylistDetailScreen(
        playlist = playlist,
        librarySongs = state.appState.library.songs,
        songsById = state.libraryDerivedState.songsById,
        favoriteSongIds = state.appState.favoriteSongIds,
        currentSongId = state.appState.playback.currentSong?.id,
        isCurrentSongPlaying = state.isPlaybackActuallyPlaying,
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
                    language = state.appState.appLanguage,
                ),
                sourcePlaylistId = playlist?.id,
            )
        },
        onUpdateSongOrder = { songIds ->
            playlistId?.let { routeActions.updatePlaylistSongOrder(it, songIds) }
                ?: CompletableDeferred(PlaylistMutationResult.InvalidInput)
        },
        onRenamePlaylist = routeActions::renamePlaylist,
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}
