package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.artist.ArtistBackdropState
import elovaire.music.droidbeauty.app.data.artist.ArtistImageRepository
import elovaire.music.droidbeauty.app.domain.model.Song

@Composable
internal fun LibraryHubRouteHost(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    LibraryHubScreen(
        libraryState = routeState.libraryState,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.libraryScrollRequestVersion,
        onOpenCollection = routeActions::openLibraryCollection,
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
        },
    )
}

@Composable
internal fun AlbumRouteHost(
    albumId: Long?,
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    var routedAlbumSongIds by remember(albumId) { mutableStateOf<Set<Long>>(emptySet()) }
    val album = libraryState.albums.firstOrNull { it.id == albumId }
        ?: libraryState.albums.firstOrNull { candidate ->
            routedAlbumSongIds.isNotEmpty() && candidate.songs.any { it.id in routedAlbumSongIds }
        }
    LaunchedEffect(album?.id) {
        album?.songs?.mapTo(linkedSetOf(), Song::id)?.let { routedAlbumSongIds = it }
    }
    val previousRoute = navState.navController.previousBackStackEntry?.destination?.route
    AlbumScreen(
        album = album,
        removingSongIds = libraryState.removingSongIds,
        favoriteSongIds = appState.favoriteSongIds,
        currentSongId = routeState.playbackState.currentSong?.id,
        isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        collapsedTopBarTitle = detailFallbackTitle(previousRoute, appState.appLanguage),
        onBack = routeActions::navigateUp,
        onOpenTagEditor = { selectedAlbum -> routeActions.openTagEditor(selectedAlbum.id) },
        onPlayAlbum = { selectedAlbum -> routeActions.playback.playAlbum(selectedAlbum) },
        onShuffleAlbum = { selectedAlbum -> routeActions.playback.playAlbum(selectedAlbum, shuffle = true) },
        onSongSelected = { selectedSong, songs ->
            routeActions.playback.playSongQueue(
                song = selectedSong,
                queue = songs,
                sourceLabel = album?.title ?: selectedSong.album,
            )
        },
        onArtistSelected = routeActions::openArtist,
        playlists = appState.playlists,
        onAddSongsToPlaylist = routeActions.playlists::addSongsToPlaylist,
        onCreatePlaylist = routeActions.playlists::createPlaylist,
        playlistSongsById = routeState.songsById,
        onDeleteSongsFromDevice = routeActions.delete::deleteSongsFromDevice,
        onToggleFavorite = routeActions.playlists::toggleFavorite,
        onSetAlbumFavorite = routeActions.playlists::setSongsFavorite,
    )
}

@Composable
internal fun LibraryCollectionRouteHost(
    kind: LibraryCollectionKind,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    LibraryCollectionScreen(
        kind = kind,
        libraryState = routeState.libraryState,
        playlists = appState.playlists,
        songPlayCounts = appState.songPlayCounts,
        favoriteSongIds = appState.favoriteSongIds,
        albumCollectionLayoutMode = routeState.albumCollectionLayoutMode,
        songCollectionLayoutMode = if (appState.songCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
        albumSortMode = appState.albumCollectionSortModeName.toAlbumSortMode(),
        songSortMode = appState.songCollectionSortModeName.toSongSortMode(),
        currentSongId = routeState.playbackState.currentSong?.id,
        isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
        },
        onAddAlbumToQueue = routeActions::enqueueAlbum,
        onSongSelected = { song, queue ->
            if (kind == LibraryCollectionKind.Songs) {
                routeActions.playback.playAllSongs(song, queue)
            } else {
                routeActions.playback.playSongQueue(song, queue)
            }
        },
        onToggleFavorite = routeActions.playlists::toggleFavorite,
        onAddAlbumToPlaylist = routeActions.playlists::addAlbumToPlaylist,
        onCreatePlaylist = routeActions.playlists::createPlaylist,
        playlistSongsById = routeState.songsById,
        onSetAlbumFavorite = routeActions.playlists::setSongsFavorite,
        onDeleteAlbumFromDevice = routeActions.delete::deleteAlbumFromDevice,
        onAlbumCollectionLayoutModeChanged = routeActions::setAlbumCollectionLayoutMode,
        onSongCollectionLayoutModeChanged = routeActions::setSongCollectionLayoutMode,
        onAlbumSortModeChanged = routeActions::setAlbumSortMode,
        onSongSortModeChanged = routeActions::setSongSortMode,
        onGenreSelected = routeActions::openGenre,
        onArtistSelected = routeActions::openArtist,
    )
}

@Composable
internal fun GenreRouteHost(
    genre: String,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    GenreAlbumsScreen(
        genre = genre,
        libraryState = routeState.libraryState,
        playlists = appState.playlists,
        layoutMode = routeState.albumCollectionLayoutMode,
        sortMode = appState.albumCollectionSortModeName.toAlbumSortMode(),
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onLayoutModeChanged = routeActions::setAlbumCollectionLayoutMode,
        onSortModeChanged = routeActions::setAlbumSortMode,
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.GenreDetail)
        },
        onAddAlbumToQueue = routeActions::enqueueAlbum,
        onAddAlbumToPlaylist = routeActions.playlists::addAlbumToPlaylist,
        onCreatePlaylist = routeActions.playlists::createPlaylist,
        playlistSongsById = routeState.songsById,
        favoriteSongIds = appState.favoriteSongIds,
        onSetAlbumFavorite = routeActions.playlists::setSongsFavorite,
        onDeleteAlbumFromDevice = routeActions.delete::deleteAlbumFromDevice,
    )
}

@Composable
internal fun ArtistRouteHost(
    artistName: String,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
    artistImageRepository: ArtistImageRepository,
) {
    val appState = routeState.appState
    val normalizedArtist = artistName.ifBlank { "Unknown Artist" }
    val artistSongs = remember(normalizedArtist, routeState.libraryState.songs) {
        routeState.libraryState.songs.filter { song ->
            song.libraryArtistName().equals(normalizedArtist, ignoreCase = true)
        }
    }
    val artistAlbums = remember(normalizedArtist, routeState.libraryState.albums) {
        routeState.libraryState.albums
            .filter { album -> album.artist.equals(normalizedArtist, ignoreCase = true) }
            .sortedBy { it.title.lowercase() }
    }
    val artistBackdropState by remember(normalizedArtist, artistSongs, artistAlbums, artistImageRepository) {
        artistImageRepository.backdropState(normalizedArtist, artistSongs, artistAlbums)
    }.collectAsStateWithLifecycle(
        initialValue = ArtistBackdropState.Fallback(
            localArtworkUri = artistAlbums.firstOrNull { it.artUri != null }?.artUri
                ?: artistSongs.firstOrNull { it.artUri != null }?.artUri,
            artistKey = normalizedArtist,
        ),
    )
    ArtistDetailScreen(
        artistName = artistName,
        libraryState = routeState.libraryState,
        artistBackdropState = artistBackdropState,
        songPlayCounts = appState.songPlayCounts,
        favoriteSongIds = appState.favoriteSongIds,
        currentSongId = routeState.playbackState.currentSong?.id,
        isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onSongSelected = { song, queue ->
            routeActions.playback.playSongQueue(song, queue, sourceLabel = artistName)
        },
        onPlayArtist = { songs ->
            songs.firstOrNull()?.let { song ->
                routeActions.playback.playSongQueue(song, songs, sourceLabel = artistName)
            }
        },
        onShuffleArtist = { songs ->
            val shuffledSongs = songs.shuffled()
            shuffledSongs.firstOrNull()?.let { song ->
                routeActions.playback.playSongQueue(song, shuffledSongs, sourceLabel = artistName)
            }
        },
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.ArtistDetail)
        },
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}

@Composable
internal fun EqualizerRouteHost(
    viewModelFactory: ElovaireViewModelFactory,
    routeActions: RootRouteActions,
) {
    val equalizerViewModel: EqualizerViewModel = viewModel(factory = viewModelFactory)
    val equalizerUiState by equalizerViewModel.uiState.collectAsStateWithLifecycle()
    EqualizerScreen(
        settings = equalizerUiState.toEqSettings(),
        selectedPresetName = equalizerUiState.presetName,
        equalizerEnabled = equalizerUiState.enabled,
        onBack = routeActions::navigateUp,
        onBandChanged = equalizerViewModel::updateBand,
        onBassChanged = equalizerViewModel::updateBass,
        onMidrangeChanged = equalizerViewModel::updateMidrange,
        onTrebleChanged = equalizerViewModel::updateTreble,
        onSpaciousnessChanged = equalizerViewModel::updateSpaciousness,
        onSpaciousnessModeChanged = equalizerViewModel::updateSpaciousnessMode,
        onReverbDurationChanged = equalizerViewModel::updateReverbDuration,
        onReverbProfileChanged = equalizerViewModel::updateReverbProfile,
        onResetReverb = equalizerViewModel::resetReverb,
        onApplyPreset = equalizerViewModel::applyPreset,
        onReset = equalizerViewModel::resetEffects,
    )
}

@Composable
internal fun SettingsRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val appState = routeState.appState
    SettingsScreen(
        themeMode = appState.themeMode,
        textSizePreset = appState.textSizePreset,
        appLanguage = appState.appLanguage,
        eqSettings = appState.eqSettings,
        onlineLyricsLookupEnabled = appState.onlineLyricsLookupEnabled,
        volumeNormalizationEnabled = appState.volumeNormalizationEnabled,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onThemeModeSelected = routeActions.settings::setThemeMode,
        onTextSizePresetSelected = routeActions.settings::setTextSizePreset,
        onAppLanguageSelected = routeActions.settings::setAppLanguage,
        onVolumeNormalizationChanged = routeActions.settings::setVolumeNormalizationEnabled,
        onMonoPlaybackChanged = routeActions.settings::updateMonoPlaybackEnabled,
        onOnlineLyricsLookupChanged = routeActions.settings::setOnlineLyricsLookupEnabled,
        onOpenEqualizer = routeActions::openEqualizer,
        onOpenLibraryFolders = routeActions::openLibraryFolders,
        onOpenPrivacySafety = routeActions::openPrivacySafety,
        onOpenChangelog = routeActions::openChangelog,
        onScanLibrary = routeActions::refreshLibrary,
    )
}

@Composable
internal fun LibraryFoldersRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val libraryFolders by routeActions.libraryFolders.collectAsStateWithLifecycle()
    LibraryFoldersScreen(
        appLanguage = routeState.appState.appLanguage,
        folders = libraryFolders,
        songs = routeState.libraryState.songs,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onAddFolder = routeActions::addLibraryFolder,
        onRemoveFolder = routeActions::removeLibraryFolder,
        onRefresh = routeActions::refreshLibrary,
    )
}

@Composable
internal fun PrivacySafetyRouteHost(
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    PrivacySafetyScreen(
        onBack = routeActions::navigateUp,
        bottomPadding = padding.detailBottom,
    )
}

@Composable
internal fun ChangelogRouteHost(
    releases: List<ChangelogRelease>,
    routeActions: RootRouteActions,
) {
    ChangelogScreen(
        releases = releases,
        onBack = routeActions::navigateUp,
    )
}

@Composable
internal fun AboutRouteHost(
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    AboutScreen(
        onBack = routeActions::navigateUp,
        bottomPadding = padding.detailBottom,
    )
}
