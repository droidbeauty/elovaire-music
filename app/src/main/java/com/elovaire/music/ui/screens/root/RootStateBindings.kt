package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode

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
    val smartPlaylists: List<SmartPlaylist>,
    val favoriteSongIds: Set<Long>,
    val albumPlayCounts: Map<Long, Int>,
    val songPlayCounts: Map<Long, Int>,
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
    val albumCollectionSortModeName: String,
    val songCollectionSortModeName: String,
    val volumeNormalizationEnabled: Boolean,
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
