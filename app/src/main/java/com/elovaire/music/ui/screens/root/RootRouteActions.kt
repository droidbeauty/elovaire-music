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
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsStore
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle

@Suppress("TooManyFunctions")
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
    val updateController: UpdateController,
    val onRequestCreatePlaylist: () -> Unit,
    val onInitialRevealFinished: () -> Unit,
    val onSearchActiveChanged: (Boolean) -> Unit,
    private val openAlbumRoute: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
) {
    val libraryFolders = librarySettings.libraryFolders
    val networkSources = libraryDependencies.networkSources
    val networkProbeResults = libraryDependencies.networkProbeResults

    fun navigateUp() {
        if (navController.navigateUp()) {
            RootInteractionState.begin("back")
        } else {
            RootInteractionState.finish()
        }
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
        navigationState.navigateTo(Routes.playlist(playlistId))
    }

    fun openSmartPlaylist(
        playlistId: Long,
        origin: ExpandOrigin = ExpandOrigin(),
    ) {
        navigationState.detailExpandOrigin = origin
        navigationState.detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
        navigationState.navigateTo(Routes.smartPlaylist(playlistId))
    }

    fun openSmartPlaylistEditor(playlistId: Long? = null) {
        navigationState.navigateTo(Routes.smartPlaylistEditor(playlistId))
    }

    fun openLibraryCollection(kind: LibraryCollectionKind) {
        navigationState.navigateTo(Routes.libraryCollection(kind))
    }

    fun openRecentlyAdded() {
        navigationState.navigateTo(RECENTLY_ADDED_ROUTE)
    }

    fun openAudiobooks() {
        navigationState.navigateTo(AUDIOBOOKS_ROUTE)
    }

    fun openAudiobook(stableKey: String) {
        navigationState.navigateTo(Routes.audiobook(stableKey))
    }

    fun openArtist(artistName: String) {
        navigationState.navigateTo(Routes.artist(artistName))
    }

    fun openGenre(genre: String) {
        navigationState.navigateTo(Routes.genre(genre))
    }

    fun openAlbumId(albumId: Long) {
        navigationState.navigateTo(Routes.album(albumId))
    }

    fun openTagEditor(albumId: Long) {
        navigationState.navigateTo(Routes.tagEditor(albumId))
    }

    fun openEqualizer() {
        navigationState.navigateTo(EQUALIZER_ROUTE)
    }

    fun openCrossfade() {
        navigationState.navigateTo(CROSSFADE_ROUTE)
    }

    fun openAudiobookSettings() {
        navigationState.navigateTo(AUDIOBOOK_SETTINGS_ROUTE)
    }

    fun openLibraryFolders() {
        navigationState.navigateTo(LIBRARY_FOLDERS_ROUTE)
    }

    fun openChangelog() {
        navigationState.navigateTo(CHANGELOG_ROUTE)
    }

    fun openPrivacyPolicy() {
        navigationState.navigateTo(PRIVACY_POLICY_ROUTE)
    }

    fun openManagePlaylists() {
        navigationState.navigateTo(MANAGE_PLAYLISTS_ROUTE)
    }

    fun openNowPlayingBarStyle() {
        navigationState.navigateTo(NOW_PLAYING_BAR_STYLE_ROUTE)
    }

    fun openSmartPlaylistSettings() {
        navigationState.navigateTo(SMART_PLAYLIST_SETTINGS_ROUTE)
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
    ): PlaylistMutationRequest = playlistDependencies.playlistStore.renamePlaylist(playlistId, name)

    fun deletePlaylists(playlistIds: Set<Long>): PlaylistMutationRequest =
        playlistDependencies.playlistStore.deletePlaylists(playlistIds)

    fun updatePlaylistSongOrder(
        playlistId: Long,
        songIds: List<Long>,
    ): PlaylistMutationRequest = playlistDependencies.playlistStore.updatePlaylistSongIds(playlistId, songIds)

    fun importPlaylists(playlists: List<Playlist>): PlaylistMutationRequest =
        playlistDependencies.playlistStore.importPlaylists(playlists)

    fun createSmartPlaylist(name: String): PlaylistMutationRequest = playlistDependencies.playlistStore.createSmartPlaylist(name)

    fun createSmartPlaylist(playlist: SmartPlaylist): PlaylistMutationRequest =
        playlistDependencies.playlistStore.createSmartPlaylist(playlist)

    fun updateSmartPlaylist(playlist: SmartPlaylist): PlaylistMutationRequest =
        playlistDependencies.playlistStore.updateSmartPlaylist(playlist)

    fun deleteSmartPlaylist(playlistId: Long): PlaylistMutationRequest =
        playlistDependencies.playlistStore.deleteSmartPlaylists(setOf(playlistId))

    fun removeLibraryFolder(selection: LibraryFolderSelection) {
        librarySettings.removeLibraryFolder(selection)
    }

    fun addNetworkSource(source: NetworkLibrarySource, credentials: NetworkCredentials) {
        libraryDependencies.saveNetworkSource(source, credentials)
    }

    fun removeNetworkSource(source: NetworkLibrarySource) {
        libraryDependencies.removeNetworkSource(source)
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
    val appearanceSettings: AppearanceSettingsStore
        get() = settingsDependencies.appearanceSettingsReader

    fun setThemeMode(mode: elovaire.music.droidbeauty.app.domain.model.ThemeMode) {
        settingsDependencies.appearanceSettings.setThemeMode(mode)
    }

    fun setTextSizePreset(preset: elovaire.music.droidbeauty.app.domain.model.TextSizePreset) {
        settingsDependencies.appearanceSettings.setTextSizePreset(preset)
    }

    fun setAppLanguage(language: elovaire.music.droidbeauty.app.domain.model.AppLanguage) {
        settingsDependencies.appearanceSettings.setAppLanguage(language)
    }

    fun setNowPlayingBarStyle(style: NowPlayingBarStyle) {
        settingsDependencies.appearanceSettings.setNowPlayingBarStyle(style)
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

    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        settingsDependencies.playbackSettings.setVolumeNormalizationEnabled(enabled)
    }

    fun setOnlineLyricsEnabled(enabled: Boolean) {
        settingsDependencies.playbackSettings.setOnlineLyricsEnabled(enabled)
    }

    fun setCrossfadeDurationMs(value: Long) {
        settingsDependencies.playbackSettings.setCrossfadeDurationMs(value)
    }

    fun setCrossfadeSilenceThresholdDb(value: Float) {
        settingsDependencies.playbackSettings.setCrossfadeSilenceThresholdDb(value)
    }

    fun setAudiobookRewindSeconds(value: Int) {
        settingsDependencies.playbackSettings.setAudiobookRewindSeconds(value)
    }

    fun setAudiobookForwardSeconds(value: Int) {
        settingsDependencies.playbackSettings.setAudiobookForwardSeconds(value)
    }

    fun setAudiobookResumePlayback(enabled: Boolean) {
        settingsDependencies.playbackSettings.setAudiobookResumePlayback(enabled)
    }

    fun setSmartPlaylistEnabled(type: BuiltInSmartPlaylistType, enabled: Boolean) {
        settingsDependencies.playbackSettings.setSmartPlaylistEnabled(type, enabled)
    }

    fun setSmartPlaylistMaxSongs(value: Int) {
        settingsDependencies.playbackSettings.setSmartPlaylistMaxSongs(value)
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
    updateController: UpdateController,
    onRequestCreatePlaylist: () -> Unit,
    onInitialRevealFinished: () -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
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
        updateController,
        onRequestCreatePlaylist,
        onInitialRevealFinished,
        onSearchActiveChanged,
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
            updateController = updateController,
            onRequestCreatePlaylist = onRequestCreatePlaylist,
            onInitialRevealFinished = onInitialRevealFinished,
            onSearchActiveChanged = onSearchActiveChanged,
            openAlbumRoute = openAlbum,
        )
    }
}
