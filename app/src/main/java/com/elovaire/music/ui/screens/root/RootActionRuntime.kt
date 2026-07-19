package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.core.AppContainer

internal data class RootActionRuntime(
    val songMenuActions: SongMenuActions,
    val playlistActions: RootPlaylistActions,
    val routeState: RootRouteState,
    val routeActions: RootRouteActions,
)

@Composable
internal fun rememberRootActionRuntime(
    context: Context,
    container: AppContainer,
    navController: NavHostController,
    appState: RootAppState,
    derivedState: RootLibraryDerivedState,
    albumCollectionLayoutMode: AlbumLayoutMode,
    resetHomeScrollOnColdStart: Boolean,
    permissionController: RootPermissionController,
    deleteController: RootDeleteController,
    uiRuntime: RootUiRuntime,
): RootActionRuntime {
    val songMenuActions = rememberRootSongMenuActions(
        playlists = appState.playlists,
        songsById = derivedState.songsById,
        albumsById = derivedState.albumsById,
        playbackManager = container.playbackManager,
        preferenceStore = container.preferenceStore,
        onDeleteSongsFromDevice = deleteController::deleteSongsFromDevice,
        openAlbum = uiRuntime.openAlbum,
        navigateToAlbumId = { albumId -> navController.navigate(Routes.album(albumId)) },
    )
    val playbackActions = rememberRootPlaybackActions(
        dependencies = container.playbackActionDependencies,
        playbackManager = container.playbackManager,
        appLanguage = appState.appLanguage,
        songsByAlbumId = derivedState.songsByAlbumId,
        albumsById = derivedState.albumsById,
        openNowPlaying = uiRuntime.playerLayerController::requestOpen,
    )
    val playlistActions = rememberRootPlaylistActions(container.playlistActionDependencies)
    val routeState = rootRouteStateOf(
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = permissionController.state.playFirstLaunchHomeReveal,
        searchFieldFocused = uiRuntime.searchChromeState.isFieldFocused,
    )
    val routeActions = rememberRootRouteActions(
        context = context,
        libraryDependencies = container.libraryActionDependencies,
        settingsDependencies = container.settingsActionDependencies,
        playlistDependencies = container.playlistActionDependencies,
        navController = navController,
        navigationState = uiRuntime.navigationState,
        playbackActions = playbackActions,
        playlistActions = playlistActions,
        deleteController = deleteController,
        onRequestCreatePlaylist = uiRuntime.overlayState::requestCreatePlaylist,
        onInitialRevealFinished = permissionController::onInitialRevealFinished,
        onSearchFieldFocusedChange = uiRuntime.searchChromeState::onFieldFocusedChanged,
        onSearchQueryActiveChanged = uiRuntime.searchChromeState::onQueryActiveChanged,
        openAlbum = uiRuntime.openAlbum,
    )
    return RootActionRuntime(
        songMenuActions = songMenuActions,
        playlistActions = playlistActions,
        routeState = routeState,
        routeActions = routeActions,
    )
}
