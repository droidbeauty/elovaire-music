package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.core.LibraryActionDependencies
import elovaire.music.droidbeauty.app.core.SettingsActionDependencies
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.settings.FavoritesStore
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.data.update.UpdateController

internal data class RootActionRuntime(
    val songMenuActions: SongMenuActions,
    val playlistActions: RootPlaylistActions,
    val routeState: RootRouteState,
    val routeActions: RootRouteActions,
)

@Composable
internal fun rememberRootActionRuntime(
    context: Context,
    playback: NowPlayingPlayback,
    playlistStore: PlaylistStore,
    favoritesStore: FavoritesStore,
    libraryDependencies: LibraryActionDependencies,
    settingsDependencies: SettingsActionDependencies,
    updateController: UpdateController,
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
        playbackManager = playback,
        playlistStore = playlistStore,
        onDeleteSongsFromDevice = deleteController::deleteSongsFromDevice,
        openAlbum = uiRuntime.openAlbum,
        navigateToAlbumId = { albumId -> uiRuntime.navigationState.navigateTo(Routes.album(albumId)) },
    )
    val playbackActions = rememberRootPlaybackActions(
        playbackManager = playback,
        appLanguage = appState.appLanguage,
        songsByAlbumId = derivedState.songsByAlbumId,
        albumsById = derivedState.albumsById,
        openNowPlaying = uiRuntime.playerLayerController::requestOpen,
    )
    val playlistActions = rememberRootPlaylistActions(playlistStore, favoritesStore)
    val routeState = rootRouteStateOf(
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = permissionController.state.playFirstLaunchHomeReveal,
    )
    val routeActions = rememberRootRouteActions(
        context = context,
        libraryDependencies = libraryDependencies,
        settingsDependencies = settingsDependencies,
        playlistStore = playlistStore,
        navController = navController,
        navigationState = uiRuntime.navigationState,
        playbackActions = playbackActions,
        playlistActions = playlistActions,
        deleteController = deleteController,
        updateController = updateController,
        onRequestCreatePlaylist = uiRuntime.overlayState::requestCreatePlaylist,
        onInitialRevealFinished = permissionController::onInitialRevealFinished,
        onSearchActiveChanged = uiRuntime.searchChromeState::onActiveChanged,
        openAlbum = uiRuntime.openAlbum,
    )
    return RootActionRuntime(
        songMenuActions = songMenuActions,
        playlistActions = playlistActions,
        routeState = routeState,
        routeActions = routeActions,
    )
}
