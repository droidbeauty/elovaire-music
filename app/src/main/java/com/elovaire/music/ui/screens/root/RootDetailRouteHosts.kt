package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import elovaire.music.droidbeauty.app.data.artist.ArtistBackdropState
import elovaire.music.droidbeauty.app.data.artist.ArtistImageReader
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import kotlinx.coroutines.CancellationException

@Composable
internal fun LibraryHubRouteHost(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val audiobookProgressRevision by routeActions.playback.audiobookProgressRevision.collectAsStateWithLifecycle()
    val audiobookProgressByKey = remember(routeState.libraryState.audiobooks, audiobookProgressRevision) {
        routeState.libraryState.audiobooks.mapNotNull { book ->
            routeActions.playback.audiobookProgress(book.stableKey)?.let { progress ->
                book.stableKey to progress
            }
        }.toMap()
    }
    LibraryHubScreen(
        libraryState = routeState.libraryState,
        audiobookProgressByKey = audiobookProgressByKey,
        currentSongId = routeState.playbackState.currentSong?.id,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.libraryScrollRequestVersion,
        onOpenCollection = routeActions::openLibraryCollection,
        onOpenRecentlyAdded = routeActions::openRecentlyAdded,
        onOpenAudiobooks = routeActions::openAudiobooks,
        onAudiobookSelected = { book -> routeActions.openAudiobook(book.stableKey) },
        onAlbumSelected = { album, origin ->
            routeActions.openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
        },
    )
}

@Composable
internal fun AudiobooksRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    AudiobooksScreen(
        books = routeState.libraryState.audiobooks,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onBookSelected = { book -> routeActions.openAudiobook(book.stableKey) },
    )
}

@Composable
internal fun AudiobookDetailRouteHost(
    stableKey: String,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
    chapterReader: AudiobookChapterReader,
) {
    val book = routeState.libraryState.audiobooks.firstOrNull { it.stableKey == stableKey }
    if (book == null) {
        AudiobookUnavailableScreen(bottomPadding = padding.detailBottom, onBack = routeActions::navigateUp)
        return
    }
    val progress by routeActions.playback.progressState.collectAsStateWithLifecycle()
    val sleepTimerState by routeActions.playback.sleepTimerState.collectAsStateWithLifecycle()
    val audiobookSettings by routeActions.settings.appearanceSettings.audiobookSettings.collectAsStateWithLifecycle()
    val audiobookProgressRevision by routeActions.playback.audiobookProgressRevision.collectAsStateWithLifecycle()
    val savedProgress: AudiobookProgress? = remember(book.stableKey, audiobookProgressRevision) {
        routeActions.playback.audiobookProgress(book.stableKey)
    }
    val chapterLoadState by androidx.compose.runtime.produceState<AudiobookChapterLoadState>(
        initialValue = AudiobookChapterLoadState.Loading,
        key1 = book.stableKey,
        key2 = book.parts,
    ) {
        try {
            val expandedParts = buildList {
                book.parts.forEach { part ->
                    val chapters = chapterReader.chapters(part.song)
                    if (chapters.isEmpty()) {
                        add(part)
                    } else {
                        chapters.forEach { chapter ->
                            add(
                                AudiobookPart(
                                    song = part.song,
                                    number = size + 1,
                                    titleOverride = chapter.title,
                                    startMs = chapter.startMs,
                                    endMs = chapter.endMs,
                                ),
                            )
                        }
                    }
                }
            }
            value = if (expandedParts.isNotEmpty() && expandedParts != book.parts) {
                AudiobookChapterLoadState.Loaded(expandedParts)
            } else {
                AudiobookChapterLoadState.UnavailableFallback
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: RuntimeException) {
            value = AudiobookChapterLoadState.UnavailableFallback
        }
    }
    val displayedBook = remember(book, chapterLoadState) {
        (chapterLoadState as? AudiobookChapterLoadState.Loaded)
            ?.parts
            ?.let { book.copy(parts = it) }
            ?: book
    }
    AudiobookDetailScreen(
        book = displayedBook,
        rewindSeconds = audiobookSettings.rewindSeconds,
        forwardSeconds = audiobookSettings.forwardSeconds,
        currentSongId = routeState.playbackState.currentSong?.id,
        progressMs = progress.positionMs,
        savedProgress = savedProgress,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onPlay = { part, resume ->
            routeActions.playback.playAudiobook(
                displayedBook,
                part,
                resume = resume && audiobookSettings.resumePlayback,
            )
        },
        onStartOver = {
            displayedBook.parts.firstOrNull()?.let { routeActions.playback.playAudiobook(displayedBook, it, resume = false) }
        },
        onSeekBack = { routeActions.playback.seekCurrentBy(-audiobookSettings.rewindSeconds * 1_000L) },
        onSeekForward = { routeActions.playback.seekCurrentBy(audiobookSettings.forwardSeconds * 1_000L) },
        onSetSpeed = routeActions.playback::setPlaybackSpeed,
        sleepTimerOption = sleepTimerState.option,
        onSleepTimerSelected = routeActions.playback::setSleepTimer,
    )
}

private sealed interface AudiobookChapterLoadState {
    data object Loading : AudiobookChapterLoadState
    data class Loaded(val parts: List<AudiobookPart>) : AudiobookChapterLoadState
    data object UnavailableFallback : AudiobookChapterLoadState
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
    artistImageRepository: ArtistImageReader,
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
        artistImageRepository = artistImageRepository,
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
internal fun RecentlyAddedRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    RecentlyAddedAlbumsScreen(
        albums = recentlyAddedAlbumsFor(routeState.libraryState),
        playlists = routeState.appState.playlists,
        playlistSongsById = routeState.songsById,
        favoriteSongIds = routeState.appState.favoriteSongIds,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onAlbumSelected = { album, origin -> routeActions.openAlbum(album, origin, AlbumOpenSource.LibraryAlbums) },
        onAddAlbumToQueue = routeActions::enqueueAlbum,
        onAddAlbumToPlaylist = routeActions.playlists::addAlbumToPlaylist,
        onCreatePlaylist = routeActions.playlists::createPlaylist,
        onSetAlbumFavorite = routeActions.playlists::setSongsFavorite,
        onDeleteAlbumFromDevice = routeActions.delete::deleteAlbumFromDevice,
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
    artistImageRepository: ArtistImageReader,
) {
    val appState = routeState.appState
    val normalizedArtist = artistName.ifBlank { "Unknown Artist" }
    val artistSongs = remember(normalizedArtist, routeState.libraryState.songs) {
        routeState.libraryState.songs.filter { song ->
            song.mediaKind == AudioMediaKind.Music &&
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
    val settings = routeActions.settings.appearanceSettings
    val themeMode by settings.themeMode.collectAsStateWithLifecycle()
    val textSizePreset by settings.textSizePreset.collectAsStateWithLifecycle()
    val appLanguage by settings.appLanguage.collectAsStateWithLifecycle()
    val eqSettings by settings.eqSettings.collectAsStateWithLifecycle()
    val volumeNormalizationEnabled by settings.volumeNormalizationEnabled.collectAsStateWithLifecycle()
    val onlineLyricsEnabled by settings.onlineLyricsEnabled.collectAsStateWithLifecycle()
    val crossfadeDurationMs by settings.crossfadeDurationMs.collectAsStateWithLifecycle()
    val crossfadeSilenceThresholdDb by settings.crossfadeSilenceThresholdDb.collectAsStateWithLifecycle()
    SettingsScreen(
        themeMode = themeMode,
        textSizePreset = textSizePreset,
        appLanguage = appLanguage,
        eqSettings = eqSettings,
        volumeNormalizationEnabled = volumeNormalizationEnabled,
        onlineLyricsEnabled = onlineLyricsEnabled,
        crossfadeDurationMs = crossfadeDurationMs,
        crossfadeSilenceThresholdDb = crossfadeSilenceThresholdDb,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onThemeModeSelected = routeActions.settings::setThemeMode,
        onTextSizePresetSelected = routeActions.settings::setTextSizePreset,
        onAppLanguageSelected = routeActions.settings::setAppLanguage,
        onVolumeNormalizationChanged = routeActions.settings::setVolumeNormalizationEnabled,
        onOnlineLyricsChanged = routeActions.settings::setOnlineLyricsEnabled,
        onOpenEqualizer = routeActions::openEqualizer,
        onOpenCrossfade = routeActions::openCrossfade,
        onOpenAudiobookSettings = routeActions::openAudiobookSettings,
        onOpenLibraryFolders = routeActions::openLibraryFolders,
        onOpenManagePlaylists = routeActions::openManagePlaylists,
        onOpenSmartPlaylistSettings = routeActions::openSmartPlaylistSettings,
        onOpenNowPlayingBarStyle = routeActions::openNowPlayingBarStyle,
        onOpenPrivacyPolicy = routeActions::openPrivacyPolicy,
        onOpenChangelog = routeActions::openChangelog,
        onScanLibrary = routeActions::refreshLibrary,
        updateController = routeActions.updateController,
    )
}

@Composable
internal fun NowPlayingBarStyleRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val settings = routeActions.settings.appearanceSettings
    val nowPlayingBarStyle by settings.nowPlayingBarStyle.collectAsStateWithLifecycle()
    NowPlayingBarStyleScreen(
        selectedStyle = nowPlayingBarStyle,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onStyleSelected = routeActions.settings::setNowPlayingBarStyle,
    )
}

@Composable
internal fun CrossfadeRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val settings = routeActions.settings.appearanceSettings
    val durationMs by settings.crossfadeDurationMs.collectAsStateWithLifecycle()
    val silenceThresholdDb by settings.crossfadeSilenceThresholdDb.collectAsStateWithLifecycle()
    CrossfadeScreen(
        durationMs = durationMs,
        silenceThresholdDb = silenceThresholdDb,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onDurationChanged = routeActions.settings::setCrossfadeDurationMs,
        onSilenceThresholdChanged = routeActions.settings::setCrossfadeSilenceThresholdDb,
    )
}

@Composable
internal fun AudiobookSettingsRouteHost(
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val settings = routeActions.settings.appearanceSettings
    val audiobookSettings by settings.audiobookSettings.collectAsStateWithLifecycle()
    AudiobookSettingsScreen(
        settings = audiobookSettings,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onRewindChanged = routeActions.settings::setAudiobookRewindSeconds,
        onForwardChanged = routeActions.settings::setAudiobookForwardSeconds,
        onResumePlaybackChanged = routeActions.settings::setAudiobookResumePlayback,
    )
}

@Composable
internal fun SmartPlaylistSettingsRouteHost(
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val settings = routeActions.settings.appearanceSettings
    val enabledTypes by settings.smartPlaylistEnabledTypes.collectAsStateWithLifecycle()
    val maxSongs by settings.smartPlaylistMaxSongs.collectAsStateWithLifecycle()
    SmartPlaylistSettingsScreen(
        enabledTypes = enabledTypes,
        maxSongs = maxSongs,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onTypeEnabledChanged = routeActions.settings::setSmartPlaylistEnabled,
        onMaxSongsChanged = routeActions.settings::setSmartPlaylistMaxSongs,
    )
}

@Composable
internal fun LibraryFoldersRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    val libraryFolders by routeActions.libraryFolders.collectAsStateWithLifecycle()
    val networkSources by routeActions.networkSources.collectAsStateWithLifecycle()
    val networkProbeResults by routeActions.networkProbeResults.collectAsStateWithLifecycle()
    LibraryFoldersScreen(
        appLanguage = routeState.appState.appLanguage,
        folders = libraryFolders,
        networkSources = networkSources,
        networkProbeResults = networkProbeResults,
        songs = routeState.libraryState.songs,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onAddFolder = routeActions::addLibraryFolder,
        onAddNetworkSource = routeActions::addNetworkSource,
        onRemoveFolder = routeActions::removeLibraryFolder,
        onRemoveNetworkSource = routeActions::removeNetworkSource,
        onRefresh = routeActions::refreshLibrary,
    )
}

@Composable
internal fun ManagePlaylistsRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    ManagePlaylistsScreen(
        appLanguage = routeState.appState.appLanguage,
        playlists = routeState.playlists.playlists,
        songsById = routeState.songsById,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
        onImportPlaylists = routeActions::importPlaylists,
    )
}

@Composable
internal fun ChangelogRouteHost(
    routeActions: RootRouteActions,
) {
    ChangelogScreen(
        releases = rememberChangelogReleases(),
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

@Composable
internal fun PrivacyPolicyRouteHost(
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
) {
    PrivacyPolicyScreen(
        appLanguage = routeState.appState.appLanguage,
        bottomPadding = padding.detailBottom,
        onBack = routeActions::navigateUp,
    )
}
