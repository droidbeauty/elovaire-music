package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.core.LibraryActionDependencies
import elovaire.music.droidbeauty.app.core.PlaylistActionDependencies
import elovaire.music.droidbeauty.app.core.SettingsActionDependencies
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.Album

internal class RootRouteActions(
    private val context: Context,
    private val libraryDependencies: LibraryActionDependencies,
    private val librarySettings: LibrarySettingsWriter,
    private val playlistDependencies: PlaylistActionDependencies,
    settingsDependencies: SettingsActionDependencies,
    private val navController: NavHostController,
    private val navigationState: RootNavigationState,
    val playback: RootPlaybackActions,
    val playlists: RootPlaylistActions,
    val delete: RootDeleteController,
    val onRequestCreatePlaylist: () -> Unit,
    val onInitialRevealFinished: () -> Unit,
    val onSearchFieldFocusedChange: (Boolean) -> Unit,
    val onSearchQueryActiveChanged: (Boolean) -> Unit,
    private val openAlbumRoute: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
) {
    val libraryFolders = librarySettings.libraryFolders

    fun navigateUp() {
        navController.navigateUp()
    }

    fun openAlbum(
        album: Album,
        origin: ExpandOrigin,
        source: AlbumOpenSource,
    ) {
        openAlbumRoute(album, origin, source)
    }

    fun openPlaylist(
        playlistId: Long,
        origin: ExpandOrigin = ExpandOrigin(),
    ) {
        navigationState.detailExpandOrigin = origin
        navigationState.detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
        navController.navigate(Routes.playlist(playlistId))
    }

    fun openSmartPlaylist(
        playlistId: Long,
        origin: ExpandOrigin = ExpandOrigin(),
    ) {
        navigationState.detailExpandOrigin = origin
        navigationState.detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
        navController.navigate(Routes.smartPlaylist(playlistId))
    }

    fun openSmartPlaylistEditor(playlistId: Long? = null) {
        navController.navigate(Routes.smartPlaylistEditor(playlistId))
    }

    fun openLibraryCollection(kind: LibraryCollectionKind) {
        navController.navigate(Routes.libraryCollection(kind))
    }

    fun openArtist(artistName: String) {
        navController.navigate(Routes.artist(artistName))
    }

    fun openGenre(genre: String) {
        navController.navigate(Routes.genre(genre))
    }

    fun openAlbumId(albumId: Long) {
        navController.navigate(Routes.album(albumId))
    }

    fun openTagEditor(albumId: Long) {
        navController.navigate(Routes.tagEditor(albumId))
    }

    fun openEqualizer() {
        navController.navigate(EQUALIZER_ROUTE)
    }

    fun openLibraryFolders() {
        navController.navigate(LIBRARY_FOLDERS_ROUTE)
    }

    fun openPrivacySafety() {
        navController.navigate(PRIVACY_SAFETY_ROUTE)
    }

    fun openChangelog() {
        navController.navigate(CHANGELOG_ROUTE)
    }

    fun refreshLibrary() {
        libraryDependencies.libraryRepository.refresh(
            forceMediaIndex = true,
            enrichMetadata = true,
            showLoadingIndicator = true,
        )
    }

    fun addLibraryFolder(uri: Uri) {
        val selection = LibraryFolderSelectionResolver.fromTreeUri(context, uri)
        val currentFolders = librarySettings.libraryFolders.value
        val normalizedFolders = LibraryFolderSelectionResolver.normalize(currentFolders + selection)
        if (normalizedFolders == LibraryFolderSelectionResolver.normalize(currentFolders)) return
        librarySettings.setLibraryFolders(normalizedFolders)
        libraryDependencies.libraryRepository.setLibraryFolders(
            selections = normalizedFolders,
            enrichMetadata = true,
            showLoadingIndicator = true,
        )
    }

    fun renamePlaylist(
        playlistId: Long,
        name: String,
    ) {
        playlistDependencies.playlistStore.renamePlaylist(playlistId, name)
    }

    fun deletePlaylists(playlistIds: Set<Long>) {
        playlistDependencies.playlistStore.deletePlaylists(playlistIds)
    }

    fun updatePlaylistSongOrder(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        playlistDependencies.playlistStore.updatePlaylistSongIds(playlistId, songIds)
    }

    fun createSmartPlaylist(name: String): Long {
        return playlistDependencies.playlistStore.createSmartPlaylist(name)
    }

    fun updateSmartPlaylist(playlist: SmartPlaylist) {
        playlistDependencies.playlistStore.updateSmartPlaylist(playlist)
    }

    fun deleteSmartPlaylist(playlistId: Long) {
        playlistDependencies.playlistStore.deleteSmartPlaylists(setOf(playlistId))
    }

    fun removeLibraryFolder(selection: LibraryFolderSelection) {
        librarySettings.removeLibraryFolder(selection)
    }

    fun enqueueAlbum(album: Album) {
        playback.enqueueAlbum(album)
    }

    fun setAlbumCollectionLayoutMode(mode: AlbumLayoutMode) {
        librarySettings.setAlbumCollectionLayoutMode(mode.name)
    }

    fun setSongCollectionLayoutMode(mode: AlbumLayoutMode) {
        librarySettings.setSongCollectionGridEnabled(mode == AlbumLayoutMode.Grid)
    }

    fun setAlbumSortMode(mode: AlbumSortMode) {
        librarySettings.setAlbumCollectionSortMode(mode.name)
    }

    fun setSongSortMode(mode: SongSortMode) {
        librarySettings.setSongCollectionSortMode(mode.name)
    }

    val settings = SettingsRouteActions(settingsDependencies)
}

internal class SettingsRouteActions(
    private val settingsDependencies: SettingsActionDependencies,
) {
    fun setThemeMode(mode: elovaire.music.droidbeauty.app.domain.model.ThemeMode) {
        settingsDependencies.appearanceSettings.setThemeMode(mode)
    }

    fun setTextSizePreset(preset: elovaire.music.droidbeauty.app.domain.model.TextSizePreset) {
        settingsDependencies.appearanceSettings.setTextSizePreset(preset)
    }

    fun setAppLanguage(language: elovaire.music.droidbeauty.app.domain.model.AppLanguage) {
        settingsDependencies.appearanceSettings.setAppLanguage(language)
    }

    fun updateBass(value: Float) {
        settingsDependencies.playbackSettings.updateBass(value)
    }

    fun updateMidrange(value: Float) {
        settingsDependencies.playbackSettings.updateMidrange(value)
    }

    fun updateTreble(value: Float) {
        settingsDependencies.playbackSettings.updateTreble(value)
    }

    fun updateMonoPlaybackEnabled(enabled: Boolean) {
        settingsDependencies.playbackSettings.updateMonoPlaybackEnabled(enabled)
    }

    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        settingsDependencies.playbackSettings.setVolumeNormalizationEnabled(enabled)
    }

}

@Composable
internal fun rememberRootRouteActions(
    context: Context,
    libraryDependencies: LibraryActionDependencies,
    settingsDependencies: SettingsActionDependencies,
    playlistDependencies: PlaylistActionDependencies,
    navController: NavHostController,
    navigationState: RootNavigationState,
    playbackActions: RootPlaybackActions,
    playlistActions: RootPlaylistActions,
    deleteController: RootDeleteController,
    onRequestCreatePlaylist: () -> Unit,
    onInitialRevealFinished: () -> Unit,
    onSearchFieldFocusedChange: (Boolean) -> Unit,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
): RootRouteActions {
    return remember(
        context,
        libraryDependencies,
        settingsDependencies,
        playlistDependencies,
        navController,
        navigationState,
        playbackActions,
        playlistActions,
        deleteController,
        onRequestCreatePlaylist,
        onInitialRevealFinished,
        onSearchFieldFocusedChange,
        onSearchQueryActiveChanged,
        openAlbum,
    ) {
        RootRouteActions(
            context = context,
            libraryDependencies = libraryDependencies,
            librarySettings = settingsDependencies.librarySettings,
            playlistDependencies = playlistDependencies,
            settingsDependencies = settingsDependencies,
            navController = navController,
            navigationState = navigationState,
            playback = playbackActions,
            playlists = playlistActions,
            delete = deleteController,
            onRequestCreatePlaylist = onRequestCreatePlaylist,
            onInitialRevealFinished = onInitialRevealFinished,
            onSearchFieldFocusedChange = onSearchFieldFocusedChange,
            onSearchQueryActiveChanged = onSearchQueryActiveChanged,
            openAlbumRoute = openAlbum,
        )
    }
}
