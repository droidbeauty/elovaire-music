package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Song

@Composable
internal fun RootRouteGraph(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
    searchViewModel: SearchViewModel,
    viewModelFactory: ElovaireViewModelFactory,
    changelogReleases: List<elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease>,
    modifier: Modifier = Modifier,
) {
    val appState = routeState.appState
    val libraryState = routeState.libraryState
    val playbackState = routeState.playbackState
    RootNavigationHost(
        navState = navState,
        modifier = modifier,
    ) {
        composable(HOME_ROUTE) {
            val recentSongs = remember(routeState.songsById, playbackState.recentSongIds) {
                playbackState.recentSongIds.mapNotNull(routeState.songsById::get).take(5)
            }
            HomeScreen(
                lastPlayedAlbum = routeState.lastPlayedAlbum,
                lastPlayedPlaylist = routeState.lastPlayedPlaylist,
                songsById = routeState.songsById,
                recentlyAddedAlbums = routeState.recentlyAddedAlbums,
                recentSongs = recentSongs,
                favoriteAlbums = routeState.favoriteAlbums,
                playbackState = playbackState,
                isLibraryLoading = libraryState.isLoading,
                libraryScanProgress = libraryState.scanProgress,
                favoriteSongIds = appState.favoriteSongIds,
                topPadding = padding.topContent,
                bottomPadding = padding.bottomContent,
                scrollToTopRequestVersion = navState.homeScrollRequestVersion,
                resetScrollOnColdStart = routeState.resetHomeScrollOnColdStart,
                playInitialReveal = routeState.playFirstLaunchHomeReveal,
                onInitialRevealFinished = routeActions.onInitialRevealFinished,
                onAlbumSelected = { album, origin ->
                    routeActions.openAlbum(album, origin, AlbumOpenSource.HomeSection)
                },
                onPlaylistSelected = { playlist -> routeActions.openPlaylist(playlist.id) },
                onPlayAlbum = { album -> routeActions.playback.playAlbum(album) },
                onPlayPlaylist = routeActions.playback::playPlaylist,
                onShufflePlaylist = { playlist, songs ->
                    routeActions.playback.playPlaylist(playlist, songs, shuffle = true)
                },
                onSongSelected = routeActions.playback::playSongFromAlbumOrSingle,
                onToggleFavorite = routeActions.playlists::toggleFavorite,
            )
        }

        composable(ALBUMS_ROUTE) {
            LibraryHubScreen(
                libraryState = libraryState,
                topPadding = padding.topContent,
                bottomPadding = padding.bottomContent,
                scrollToTopRequestVersion = navState.libraryScrollRequestVersion,
                onOpenCollection = routeActions::openLibraryCollection,
                onAlbumSelected = { album, origin ->
                    routeActions.openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
                },
            )
        }

        composable(PLAYLISTS_ROUTE) {
            PlaylistsScreen(
                playlists = appState.playlists,
                libraryState = libraryState,
                topPadding = padding.topContent,
                bottomPadding = padding.bottomContent,
                scrollToTopRequestVersion = navState.playlistsScrollRequestVersion,
                onRequestCreatePlaylist = routeActions.onRequestCreatePlaylist,
                onRenamePlaylist = routeActions::renamePlaylist,
                onDeletePlaylists = routeActions::deletePlaylists,
                onOpenPlaylist = { playlist, origin -> routeActions.openPlaylist(playlist.id, origin) },
            )
        }

        composable(SEARCH_ROUTE) {
            SearchRoute(
                viewModel = searchViewModel,
                libraryState = libraryState,
                favoriteSongIds = appState.favoriteSongIds,
                topPadding = padding.topContent,
                bottomPadding = padding.bottomContent,
                scrollToTopRequestVersion = navState.searchScrollRequestVersion,
                isSearchFieldFocused = routeState.searchFieldFocused,
                onSearchFieldFocusedChange = routeActions.onSearchFieldFocusedChange,
                onSearchQueryActiveChanged = routeActions.onSearchQueryActiveChanged,
                onPlaySong = { song, queue ->
                    routeActions.playback.playSongQueue(
                        song = song,
                        queue = queue,
                        sourceLabel = searchViewModel.playbackSourceLabelFor(queue, song.album),
                    )
                },
                onAlbumSelected = { album, origin ->
                    routeActions.openAlbum(album, origin, AlbumOpenSource.SearchResults)
                },
                onArtistSelected = routeActions::openArtist,
                onToggleFavorite = routeActions.playlists::toggleFavorite,
            )
        }

        composable(
            route = "$PLAYLIST_ROUTE/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val playlist = appState.playlists.firstOrNull { it.id == playlistId }
            PlaylistDetailScreen(
                playlist = playlist,
                librarySongs = libraryState.songs,
                favoriteSongIds = appState.favoriteSongIds,
                currentSongId = playbackState.currentSong?.id,
                isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
                bottomPadding = padding.detailBottom,
                onBack = routeActions::navigateUp,
                onPlayPlaylist = { songs, sourceLabel ->
                    songs.firstOrNull()?.let { firstSong ->
                        routeActions.playback.playSongQueue(
                            song = firstSong,
                            queue = songs,
                            sourceLabel = sourceLabel,
                            sourcePlaylistId = playlist?.id,
                        )
                    }
                },
                onShufflePlaylist = { songs, sourceLabel ->
                    val shuffledSongs = songs.shuffled()
                    shuffledSongs.firstOrNull()?.let { firstSong ->
                        routeActions.playback.playSongQueue(
                            song = firstSong,
                            queue = shuffledSongs,
                            sourceLabel = sourceLabel,
                            sourcePlaylistId = playlist?.id,
                        )
                    }
                },
                onSongSelected = { song, queue ->
                    routeActions.playback.playSongQueue(
                        song = song,
                        queue = queue,
                        sourceLabel = playlist?.name ?: queue.playbackSourceLabel(
                            fallbackAlbum = song.album,
                            language = appState.appLanguage,
                        ),
                        sourcePlaylistId = playlist?.id,
                    )
                },
                onUpdateSongOrder = { songIds -> routeActions.updatePlaylistSongOrder(playlistId, songIds) },
                onRenamePlaylist = routeActions::renamePlaylist,
                onToggleFavorite = routeActions.playlists::toggleFavorite,
            )
        }

        composable(
            route = "$ALBUM_ROUTE/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
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
                currentSongId = playbackState.currentSong?.id,
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
                playlists = appState.playlists,
                onAddSongsToPlaylist = routeActions.playlists::addSongsToPlaylist,
                onCreatePlaylist = routeActions.playlists::createPlaylist,
                playlistSongsById = routeState.songsById,
                onDeleteSongsFromDevice = routeActions.delete::deleteSongsFromDevice,
                onToggleFavorite = routeActions.playlists::toggleFavorite,
                onSetAlbumFavorite = routeActions.playlists::setSongsFavorite,
            )
        }

        composable(
            route = "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            AlbumTagEditorRouteHost(
                albumId = albumId,
                backStackEntry = backStackEntry,
                viewModelFactory = viewModelFactory,
                appLanguage = appState.appLanguage,
                onBack = routeActions::navigateUp,
            )
        }

        composable(
            route = "$LIBRARY_COLLECTION_ROUTE/{kind}",
            arguments = listOf(navArgument("kind") { type = NavType.StringType }),
        ) { backStackEntry ->
            val kindArg = backStackEntry.arguments?.getString("kind")
            val kind = kindArg?.let { runCatching { LibraryCollectionKind.valueOf(it) }.getOrNull() }
                ?: LibraryCollectionKind.Albums
            LibraryCollectionScreen(
                kind = kind,
                libraryState = libraryState,
                playlists = appState.playlists,
                songPlayCounts = appState.songPlayCounts,
                favoriteSongIds = appState.favoriteSongIds,
                albumCollectionLayoutMode = routeState.albumCollectionLayoutMode,
                songCollectionLayoutMode = if (appState.songCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
                albumSortMode = appState.albumCollectionSortModeName.toAlbumSortMode(),
                songSortMode = appState.songCollectionSortModeName.toSongSortMode(),
                currentSongId = playbackState.currentSong?.id,
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

        composable(
            route = "$GENRE_ROUTE/{genre}",
            arguments = listOf(navArgument("genre") { type = NavType.StringType }),
        ) { backStackEntry ->
            val genre = backStackEntry.arguments?.getString("genre")?.let(Uri::decode).orEmpty()
            GenreAlbumsScreen(
                genre = genre,
                libraryState = libraryState,
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

        composable(
            route = "$ARTIST_ROUTE/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")?.let(Uri::decode).orEmpty()
            ArtistDetailScreen(
                artistName = artistName,
                libraryState = libraryState,
                songPlayCounts = appState.songPlayCounts,
                favoriteSongIds = appState.favoriteSongIds,
                currentSongId = playbackState.currentSong?.id,
                isCurrentSongPlaying = routeState.isPlaybackActuallyPlaying,
                bottomPadding = padding.detailBottom,
                onBack = routeActions::navigateUp,
                onSongSelected = { song, queue ->
                    routeActions.playback.playSongQueue(song, queue, sourceLabel = artistName)
                },
                onAlbumSelected = { album, origin ->
                    routeActions.openAlbum(album, origin, AlbumOpenSource.ArtistDetail)
                },
                onToggleFavorite = routeActions.playlists::toggleFavorite,
            )
        }

        composable(EQUALIZER_ROUTE) {
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

        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                themeMode = appState.themeMode,
                textSizePreset = appState.textSizePreset,
                appLanguage = appState.appLanguage,
                eqSettings = appState.eqSettings,
                onlineLyricsLookupEnabled = appState.onlineLyricsLookupEnabled,
                bottomPadding = padding.detailBottom,
                onBack = routeActions::navigateUp,
                onThemeModeSelected = routeActions.settings::setThemeMode,
                onTextSizePresetSelected = routeActions.settings::setTextSizePreset,
                onAppLanguageSelected = routeActions.settings::setAppLanguage,
                onBassChanged = routeActions.settings::updateBass,
                onMidrangeChanged = routeActions.settings::updateMidrange,
                onTrebleChanged = routeActions.settings::updateTreble,
                onMonoPlaybackChanged = routeActions.settings::updateMonoPlaybackEnabled,
                onOnlineLyricsLookupChanged = routeActions.settings::setOnlineLyricsLookupEnabled,
                onOpenEqualizer = routeActions::openEqualizer,
                onOpenLibraryFolders = routeActions::openLibraryFolders,
                onOpenPrivacySafety = routeActions::openPrivacySafety,
                onOpenChangelog = routeActions::openChangelog,
                onScanLibrary = routeActions::refreshLibrary,
                showUpdateChecks = BuildConfig.ENABLE_GITHUB_UPDATE_FLOW,
                onCheckForUpdates = routeActions::checkForUpdates,
            )
        }

        composable(LIBRARY_FOLDERS_ROUTE) {
            val libraryFolders by routeActions.libraryFolders.collectAsStateWithLifecycle()
            LibraryFoldersScreen(
                appLanguage = appState.appLanguage,
                folders = libraryFolders,
                songs = libraryState.songs,
                bottomPadding = padding.detailBottom,
                onBack = routeActions::navigateUp,
                onAddFolder = routeActions::addLibraryFolder,
                onRemoveFolder = routeActions::removeLibraryFolder,
                onRefresh = routeActions::refreshLibrary,
            )
        }

        composable(PRIVACY_SAFETY_ROUTE) {
            PrivacySafetyScreen(
                onBack = routeActions::navigateUp,
                bottomPadding = padding.detailBottom,
            )
        }

        composable(CHANGELOG_ROUTE) {
            ChangelogScreen(
                releases = changelogReleases,
                onBack = routeActions::navigateUp,
            )
        }

        composable(ABOUT_ROUTE) {
            AboutScreen(
                onBack = routeActions::navigateUp,
                bottomPadding = padding.detailBottom,
            )
        }
    }
}
