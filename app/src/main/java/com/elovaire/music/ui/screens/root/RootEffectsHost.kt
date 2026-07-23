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
) {
    val playerLayerController = uiRuntime.playerLayerController
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    val currentComposition by rememberUpdatedState(composition)
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
                        val current = snapshotFlow { currentComposition }.first { state ->
                            val playlist = state.derivedState.lastPlayedPlaylist
                            val hasPlayablePlaylist = playlist?.songIds
                                ?.any(state.derivedState.songsById::containsKey) == true
                            hasPlayablePlaylist ||
                                state.derivedState.lastPlayedAlbum?.songs?.isNotEmpty() == true
                        }
                        val playlist = current.derivedState.lastPlayedPlaylist
                        val playlistSongs = playlist?.songIds
                            ?.mapNotNull(current.derivedState.songsById::get)
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
                            current.derivedState.lastPlayedAlbum != null -> {
                                current.container.playbackManager.playAlbum(
                                    current.derivedState.lastPlayedAlbum,
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
