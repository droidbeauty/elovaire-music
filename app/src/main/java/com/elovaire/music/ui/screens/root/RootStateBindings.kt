package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import elovaire.music.droidbeauty.app.domain.model.Song

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
