package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class RootRouteState(
    val appState: RootAppState,
    val libraryDerivedState: RootLibraryDerivedState,
    val recentSongs: List<Song>,
    val albumCollectionLayoutMode: AlbumLayoutMode,
    val resetHomeScrollOnColdStart: Boolean,
    val playFirstLaunchHomeReveal: Boolean,
) {
    val isPlaybackActuallyPlaying: Boolean
        get() = appState.playback.isPlaying && appState.playback.currentSong != null
}

internal fun resolveRoutedAlbum(
    albumId: Long?,
    albums: List<Album>,
    albumsById: Map<Long, Album>,
    routedSongIds: Set<Long>,
): Album? {
    albumId?.let(albumsById::get)?.let { return it }
    if (routedSongIds.isEmpty()) return null
    return albums.firstOrNull { candidate -> candidate.songs.any { it.id in routedSongIds } }
}

internal fun rootRouteStateOf(
    appState: RootAppState,
    derivedState: RootLibraryDerivedState,
    albumCollectionLayoutMode: AlbumLayoutMode,
    resetHomeScrollOnColdStart: Boolean,
    playFirstLaunchHomeReveal: Boolean,
): RootRouteState {
    return RootRouteState(
        appState = appState,
        libraryDerivedState = derivedState,
        recentSongs = recentSongsFor(appState.playback.recentSongIds, derivedState.songsById),
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = playFirstLaunchHomeReveal,
    )
}
