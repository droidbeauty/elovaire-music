package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.domain.model.Album

internal class RootRouteActions(
    private val context: Context,
    private val container: AppContainer,
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
    val libraryFolders = container.preferenceStore.libraryFolders

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
        container.libraryRepository.refresh(
            forceMediaIndex = true,
            enrichMetadata = true,
            showLoadingIndicator = true,
        )
    }

    fun addLibraryFolder(uri: Uri) {
        val selection = LibraryFolderSelectionResolver.fromTreeUri(context, uri)
        val currentFolders = container.preferenceStore.libraryFolders.value
        val normalizedFolders = LibraryFolderSelectionResolver.normalize(currentFolders + selection)
        if (normalizedFolders == LibraryFolderSelectionResolver.normalize(currentFolders)) return
        container.preferenceStore.setLibraryFolders(normalizedFolders)
        container.libraryRepository.setLibraryFolders(
            selections = normalizedFolders,
            enrichMetadata = true,
            showLoadingIndicator = true,
        )
    }

    fun renamePlaylist(
        playlistId: Long,
        name: String,
    ) {
        container.preferenceStore.renamePlaylist(playlistId, name)
    }

    fun deletePlaylists(playlistIds: Set<Long>) {
        container.preferenceStore.deletePlaylists(playlistIds)
    }

    fun updatePlaylistSongOrder(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        container.preferenceStore.updatePlaylistSongIds(playlistId, songIds)
    }

    fun removeLibraryFolder(selection: LibraryFolderSelection) {
        container.preferenceStore.removeLibraryFolder(selection)
    }

    fun enqueueAlbum(album: Album) {
        album.songs.forEach(container.playbackManager::enqueueSong)
    }

    fun setAlbumCollectionLayoutMode(mode: AlbumLayoutMode) {
        container.preferenceStore.setAlbumCollectionLayoutMode(mode.name)
    }

    fun setSongCollectionLayoutMode(mode: AlbumLayoutMode) {
        container.preferenceStore.setSongCollectionGridEnabled(mode == AlbumLayoutMode.Grid)
    }

    fun setAlbumSortMode(mode: AlbumSortMode) {
        container.preferenceStore.setAlbumCollectionSortMode(mode.name)
    }

    fun setSongSortMode(mode: SongSortMode) {
        container.preferenceStore.setSongCollectionSortMode(mode.name)
    }

    fun checkForUpdates() {
        container.appUpdateManager.checkForUpdates(force = true)
    }

    val settings = SettingsRouteActions(container)
}

internal class SettingsRouteActions(
    private val container: AppContainer,
) {
    fun setThemeMode(mode: elovaire.music.droidbeauty.app.domain.model.ThemeMode) {
        container.preferenceStore.setThemeMode(mode)
    }

    fun setTextSizePreset(preset: elovaire.music.droidbeauty.app.domain.model.TextSizePreset) {
        container.preferenceStore.setTextSizePreset(preset)
    }

    fun setAppLanguage(language: elovaire.music.droidbeauty.app.domain.model.AppLanguage) {
        container.preferenceStore.setAppLanguage(language)
    }

    fun updateBass(value: Float) {
        container.preferenceStore.updateBass(value)
    }

    fun updateMidrange(value: Float) {
        container.preferenceStore.updateMidrange(value)
    }

    fun updateTreble(value: Float) {
        container.preferenceStore.updateTreble(value)
    }

    fun updateMonoPlaybackEnabled(enabled: Boolean) {
        container.preferenceStore.updateMonoPlaybackEnabled(enabled)
    }

    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        container.preferenceStore.setVolumeNormalizationEnabled(enabled)
    }

    fun setOnlineLyricsLookupEnabled(enabled: Boolean) {
        container.preferenceStore.setOnlineLyricsLookupEnabled(enabled)
    }
}

@Composable
internal fun rememberRootRouteActions(
    context: Context,
    container: AppContainer,
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
        container,
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
            container = container,
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
