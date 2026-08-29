package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import elovaire.music.droidbeauty.app.core.AppShortcutCommand
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
internal fun RootEffectsHost(
    composition: RootComposition,
    uiRuntime: RootUiRuntime,
    derivedState: RootLibraryDerivedState,
) {
    val playerLayerController = uiRuntime.playerLayerController
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    val currentComposition by rememberUpdatedState(composition)
    val currentDerivedState by rememberUpdatedState(derivedState)
    val currentUiRuntime by rememberUpdatedState(uiRuntime)
    LaunchedEffect(composition.container) {
        composition.container.openNowPlayingCommands.collect {
            currentPlayerLayerController.requestOpen(null)
        }
    }
    LaunchedEffect(composition.container) {
        composition.container.appShortcutCommands.collect { command ->
            when (command) {
                AppShortcutCommand.LastPlayed -> {
                    launch {
                        snapshotFlow { currentDerivedState }.first { state ->
                            val playlist = state.lastPlayedPlaylist
                            val hasPlayablePlaylist = playlist?.songIds
                                ?.any(state.songsById::containsKey) == true
                            hasPlayablePlaylist ||
                                state.lastPlayedAlbum?.songs?.isNotEmpty() == true
                        }
                        val current = currentComposition
                        val playlist = currentDerivedState.lastPlayedPlaylist
                        val lastPlayedAlbum = currentDerivedState.lastPlayedAlbum
                        val playlistSongs = playlist?.songIds
                            ?.mapNotNull(currentDerivedState.songsById::get)
                            .orEmpty()
                        when {
                            playlist != null && playlistSongs.isNotEmpty() -> {
                                current.container.playbackManager.playSong(
                                    song = playlistSongs.first(),
                                    collection = playlistSongs,
                                    sourceLabel = playlist.name,
                                    sourcePlaylistId = playlist.id,
                                )
                            }
                            lastPlayedAlbum != null -> {
                                current.container.playbackManager.playAlbum(
                                    album = lastPlayedAlbum,
                                    startSongId = null,
                                    sourceLabel = lastPlayedAlbum.title,
                                    shuffleEnabled = false,
                                    sourcePlaylistId = null,
                                )
                            }
                        }
                    }
                }
                AppShortcutCommand.Albums -> currentUiRuntime.navigateShortcut(ALBUMS_ROUTE)
                AppShortcutCommand.Playlists -> currentUiRuntime.navigateShortcut(PLAYLISTS_ROUTE)
                AppShortcutCommand.Search -> currentUiRuntime.navigateShortcut(SEARCH_ROUTE)
            }
        }
    }
    RootSystemBarEffect(
        darkTheme = uiRuntime.shellInputs.darkTheme,
        showPlayerOverlay = uiRuntime.chromeVisibility.showPlayerOverlay,
        playerContentColor = uiRuntime.shellInputs.playerAdaptivePalette.contentColor,
    )
}

private fun RootUiRuntime.navigateShortcut(route: String) {
    navigationState.navigateBottomTab(
        route = route,
        activeBottomRoute = routeObservation.activeBottomRoute,
        currentRoute = routeObservation.route,
    )
}
