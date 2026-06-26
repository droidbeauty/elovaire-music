package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import elovaire.music.droidbeauty.app.data.update.AppUpdateUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode

internal data class PendingSongDeletion(
    val songs: List<Song>,
    val parentDirectories: Set<String> = emptySet(),
    val filePaths: Set<String> = emptySet(),
)

internal enum class PlayerLayerState {
    Compact,
    Expanded,
    ReturningToCompact,
}

internal fun String?.toPlayerLayerStateOrDefault(): PlayerLayerState {
    return PlayerLayerState.entries.firstOrNull { it.name == this }
        ?: PlayerLayerState.Compact
}

internal data class RootAppState(
    val library: LibraryUiState,
    val playback: PlaybackUiState,
    val eqSettings: EqSettings,
    val themeMode: ThemeMode,
    val textSizePreset: TextSizePreset,
    val appLanguage: AppLanguage,
    val playlists: List<Playlist>,
    val favoriteSongIds: Set<Long>,
    val albumPlayCounts: Map<Long, Int>,
    val songPlayCounts: Map<Long, Int>,
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
    val albumCollectionSortModeName: String,
    val songCollectionSortModeName: String,
    val appUpdateState: AppUpdateUiState,
)

internal data class RootLibraryDerivedState(
    val songsById: Map<Long, Song>,
    val songsByAlbumId: Map<Long, List<Song>>,
    val albumsById: Map<Long, Album>,
    val playlistsById: Map<Long, Playlist>,
    val recentlyAddedAlbums: List<Album>,
    val recentAlbums: List<Album>,
    val favoriteAlbums: List<Album>,
    val lastPlayedAlbum: Album?,
    val lastPlayedPlaylist: Playlist?,
)

internal fun libraryUiStateOf(
    content: LibraryContentState,
    scan: LibraryScanState,
): LibraryUiState {
    return LibraryUiState(
        permissionGranted = scan.permissionGranted,
        isLoading = scan.isLoading,
        scanProgress = scan.scanProgress,
        songs = content.songs,
        albums = content.albums,
        removingSongIds = content.removingSongIds,
        removingAlbumIds = content.removingAlbumIds,
        errorMessage = scan.errorMessage,
    )
}

internal fun playbackUiStateOf(
    nowPlaying: PlaybackNowPlayingState,
    transport: PlaybackTransportState,
    queue: PlaybackQueueState,
    volume: PlaybackVolumeState,
    recent: RecentPlaybackState,
): PlaybackUiState {
    return PlaybackUiState(
        queue = queue.queue,
        currentIndex = queue.currentIndex,
        isPlaying = transport.isPlaying,
        transportShowsPause = transport.transportShowsPause,
        repeatMode = transport.repeatMode,
        shuffleEnabled = transport.shuffleEnabled,
        sourceLabel = nowPlaying.sourceLabel,
        volume = volume.volume,
        audioSessionId = nowPlaying.audioSessionId,
        recentSongIds = recent.recentSongIds,
        recentAlbumIds = recent.recentAlbumIds,
        sourcePlaylistId = queue.sourcePlaylistId,
        lastPlayedCollectionKind = recent.lastPlayedCollectionKind,
        lastPlayedCollectionId = recent.lastPlayedCollectionId,
    )
}

@Composable
internal fun rememberRootAppState(container: AppContainer): RootAppState {
    val libraryContentState by container.libraryRepository.contentState.collectAsStateWithLifecycle()
    val libraryScanState by container.libraryRepository.scanState.collectAsStateWithLifecycle()
    val playbackNowPlayingState by container.playbackManager.nowPlayingState.collectAsStateWithLifecycle()
    val playbackTransportState by container.playbackManager.transportState.collectAsStateWithLifecycle()
    val playbackQueueState by container.playbackManager.queueState.collectAsStateWithLifecycle()
    val playbackVolumeState by container.playbackManager.volumeState.collectAsStateWithLifecycle()
    val recentPlaybackState by container.playbackManager.recentPlaybackState.collectAsStateWithLifecycle()
    val eqSettings by container.preferenceStore.eqSettings.collectAsStateWithLifecycle()
    val themeMode by container.preferenceStore.themeMode.collectAsStateWithLifecycle()
    val textSizePreset by container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
    val appLanguage by container.preferenceStore.appLanguage.collectAsStateWithLifecycle()
    val playlists by container.preferenceStore.playlists.collectAsStateWithLifecycle()
    val favoriteSongIds by container.preferenceStore.favoriteSongIds.collectAsStateWithLifecycle()
    val albumPlayCounts by container.preferenceStore.albumPlayCounts.collectAsStateWithLifecycle()
    val songPlayCounts by container.preferenceStore.songPlayCounts.collectAsStateWithLifecycle()
    val albumCollectionLayoutModeName by container.preferenceStore.albumCollectionLayoutMode.collectAsStateWithLifecycle()
    val songCollectionGridEnabled by container.preferenceStore.songCollectionGridEnabled.collectAsStateWithLifecycle()
    val albumCollectionSortModeName by container.preferenceStore.albumCollectionSortMode.collectAsStateWithLifecycle()
    val songCollectionSortModeName by container.preferenceStore.songCollectionSortMode.collectAsStateWithLifecycle()
    val appUpdateState by container.appUpdateManager.uiState.collectAsStateWithLifecycle()

    val library = remember(libraryContentState, libraryScanState) {
        libraryUiStateOf(libraryContentState, libraryScanState)
    }
    val playback = remember(
        playbackNowPlayingState,
        playbackTransportState,
        playbackQueueState,
        playbackVolumeState,
        recentPlaybackState,
    ) {
        playbackUiStateOf(
            nowPlaying = playbackNowPlayingState,
            transport = playbackTransportState,
            queue = playbackQueueState,
            volume = playbackVolumeState,
            recent = recentPlaybackState,
        )
    }
    val favoriteSongIdSet = remember(favoriteSongIds) { favoriteSongIds.toHashSet() }

    return remember(
        library,
        playback,
        eqSettings,
        themeMode,
        textSizePreset,
        appLanguage,
        playlists,
        favoriteSongIdSet,
        albumPlayCounts,
        songPlayCounts,
        albumCollectionLayoutModeName,
        songCollectionGridEnabled,
        albumCollectionSortModeName,
        songCollectionSortModeName,
        appUpdateState,
    ) {
        RootAppState(
            library = library,
            playback = playback,
            eqSettings = eqSettings,
            themeMode = themeMode,
            textSizePreset = textSizePreset,
            appLanguage = appLanguage,
            playlists = playlists,
            favoriteSongIds = favoriteSongIdSet,
            albumPlayCounts = albumPlayCounts,
            songPlayCounts = songPlayCounts,
            albumCollectionLayoutModeName = albumCollectionLayoutModeName,
            songCollectionGridEnabled = songCollectionGridEnabled,
            albumCollectionSortModeName = albumCollectionSortModeName,
            songCollectionSortModeName = songCollectionSortModeName,
            appUpdateState = appUpdateState,
        )
    }
}

@Composable
internal fun rememberRootLibraryDerivedState(
    library: LibraryUiState,
    playback: PlaybackUiState,
    playlists: List<Playlist>,
    songPlayCounts: Map<Long, Int>,
): RootLibraryDerivedState {
    val songsById = remember(library.songs) { library.songs.associateBy(Song::id) }
    val songsByAlbumId = remember(library.songs) { library.songs.groupBy(Song::albumId) }
    val albumsById = remember(library.albums) { library.albums.associateBy(Album::id) }
    val playlistsById = remember(playlists) { playlists.associateBy(Playlist::id) }
    val recentlyAddedAlbums = remember(library.albums) {
        recentlyAddedAlbumsFor(library)
    }
    val recentAlbums = remember(library.albums, playback.recentAlbumIds) {
        recentAlbumsFor(library, playback)
    }
    val lastPlayedPlaylist = remember(
        playlistsById,
        playback.lastPlayedCollectionKind,
        playback.lastPlayedCollectionId,
    ) {
        if (playback.lastPlayedCollectionKind == PlaybackCollectionKind.Playlist) {
            playback.lastPlayedCollectionId?.let(playlistsById::get)
        } else {
            null
        }
    }
    val lastPlayedAlbum = remember(
        albumsById,
        recentAlbums,
        playback.lastPlayedCollectionKind,
        playback.lastPlayedCollectionId,
    ) {
        when (playback.lastPlayedCollectionKind) {
            PlaybackCollectionKind.Album -> playback.lastPlayedCollectionId?.let(albumsById::get)
            PlaybackCollectionKind.Playlist -> null
            null -> recentAlbums.firstOrNull()
        } ?: recentAlbums.firstOrNull()
    }
    val favoriteAlbums = remember(library.albums, songPlayCounts, recentAlbums, recentlyAddedAlbums) {
        favoriteAlbumsFor(
            libraryState = library,
            songPlayCounts = songPlayCounts,
            recentAlbums = recentAlbums,
            recentlyAddedAlbums = recentlyAddedAlbums,
        )
    }
    return remember(
        songsById,
        songsByAlbumId,
        albumsById,
        playlistsById,
        recentlyAddedAlbums,
        recentAlbums,
        favoriteAlbums,
        lastPlayedAlbum,
        lastPlayedPlaylist,
    ) {
        RootLibraryDerivedState(
            songsById = songsById,
            songsByAlbumId = songsByAlbumId,
            albumsById = albumsById,
            playlistsById = playlistsById,
            recentlyAddedAlbums = recentlyAddedAlbums,
            recentAlbums = recentAlbums,
            favoriteAlbums = favoriteAlbums,
            lastPlayedAlbum = lastPlayedAlbum,
            lastPlayedPlaylist = lastPlayedPlaylist,
        )
    }
}
