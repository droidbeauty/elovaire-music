package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import elovaire.music.droidbeauty.app.data.artist.ArtistImageReader
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.library.AudiobookDescriptionReader
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@Composable
@Suppress("LongMethod")
internal fun RootRouteGraph(
    navState: RootNavigationState,
    motionTransitions: MotionTransitions,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
    searchViewModel: SearchViewModel,
    viewModelFactory: ElovaireViewModelFactory,
    artistImageRepository: ArtistImageReader,
    audiobookChapterReader: AudiobookChapterReader,
    audiobookDescriptionReader: AudiobookDescriptionReader,
    modifier: Modifier = Modifier,
) {
    RootNavigationHost(
        navState = navState,
        motionTransitions = motionTransitions,
        modifier = modifier,
    ) {
        composable(RootRouteRegistry.HOME) {
            AlbumTransitionContent(this) {
                HomeRouteHost(navState, routeState.home, routeActions, padding)
            }
        }
        composable(RootRouteRegistry.ALBUMS) {
            AlbumTransitionContent(this) {
                LibraryHubRouteHost(navState, routeState, routeActions, padding)
            }
        }
        composable(RootRouteRegistry.RECENTLY_ADDED) {
            AlbumTransitionContent(this) {
                RecentlyAddedRouteHost(
                    routeState = routeState,
                    routeActions = routeActions,
                    padding = padding,
                )
            }
        }
        composable(RootRouteRegistry.AUDIOBOOKS) {
            AlbumTransitionContent(this) {
                AudiobooksRouteHost(routeState, routeActions, padding)
            }
        }
        composable(
            route = RootRouteRegistry.AUDIOBOOK,
            arguments = listOf(navArgument("bookKey") { type = NavType.StringType }),
        ) { backStackEntry ->
            AlbumTransitionContent(this) {
                AudiobookDetailRouteHost(
                    stableKey = backStackEntry.audiobookRouteKey(),
                    routeState = routeState,
                    routeActions = routeActions,
                    padding = padding,
                    chapterReader = audiobookChapterReader,
                    descriptionReader = audiobookDescriptionReader,
                )
            }
        }
        composable(
            route = RootRouteRegistry.AUDIOBOOK_TAG_EDITOR,
            arguments = listOf(navArgument("bookKey") { type = NavType.StringType }),
        ) { backStackEntry ->
            AudiobookTagEditorRouteHost(
                bookKey = backStackEntry.audiobookRouteKey(),
                backStackEntry = backStackEntry,
                viewModelFactory = viewModelFactory,
                appLanguage = routeState.appState.appLanguage,
                onBack = routeActions::navigateUp,
                onSaveSucceeded = routeActions::completeAudiobookTagEdit,
            )
        }
        composable(RootRouteRegistry.PLAYLISTS) {
            PlaylistsRouteHost(navState, routeState.playlists, routeActions, padding)
        }
        composable(RootRouteRegistry.SEARCH) {
            AlbumTransitionContent(this) {
                SearchRouteHost(navState, routeState, routeActions, padding, searchViewModel)
            }
        }
        composable(
            route = RootRouteRegistry.PLAYLIST,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            PlaylistDetailRouteHost(backStackEntry.playlistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(
            route = RootRouteRegistry.SMART_PLAYLIST,
            arguments = listOf(navArgument("smartPlaylistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            SmartPlaylistDetailRouteHost(backStackEntry.smartPlaylistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(RootRouteRegistry.SMART_PLAYLIST_EDITOR) {
            SmartPlaylistEditorRouteHost(null, routeState.playlists, routeActions, padding)
        }
        composable(
            route = RootRouteRegistry.SMART_PLAYLIST_EDITOR_EXISTING,
            arguments = listOf(navArgument("smartPlaylistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            SmartPlaylistEditorRouteHost(backStackEntry.smartPlaylistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(
            route = RootRouteRegistry.ALBUM,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            AlbumTransitionContent(this) {
                AlbumRouteHost(backStackEntry.albumRouteId(), navState, routeState, routeActions, padding)
            }
        }
        composable(
            route = RootRouteRegistry.ALBUM_TAG_EDITOR,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            AlbumTagEditorRouteHost(
                albumId = backStackEntry.albumRouteId(),
                backStackEntry = backStackEntry,
                viewModelFactory = viewModelFactory,
                appLanguage = routeState.appState.appLanguage,
                onBack = routeActions::navigateUp,
            )
        }
        composable(
            route = RootRouteRegistry.LIBRARY_COLLECTION,
            arguments = listOf(navArgument("kind") { type = NavType.StringType }),
        ) { backStackEntry ->
            AlbumTransitionContent(this) {
                LibraryCollectionRouteHost(
                    kind = backStackEntry.libraryCollectionKindArg(),
                    routeState = routeState,
                    routeActions = routeActions,
                    padding = padding,
                    artistImageRepository = artistImageRepository,
                )
            }
        }
        composable(
            route = RootRouteRegistry.GENRE,
            arguments = listOf(navArgument("genre") { type = NavType.StringType }),
        ) { backStackEntry ->
            AlbumTransitionContent(this) {
                GenreRouteHost(backStackEntry.genreRouteArg(), routeState, routeActions, padding)
            }
        }
        composable(
            route = RootRouteRegistry.ARTIST,
            arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
        ) { backStackEntry ->
            AlbumTransitionContent(this) {
                ArtistRouteHost(backStackEntry.artistRouteArg(), routeState, routeActions, padding, artistImageRepository)
            }
        }
        composable(RootRouteRegistry.EQUALIZER) {
            EqualizerRouteHost(viewModelFactory, routeActions)
        }
        composable(RootRouteRegistry.CROSSFADE) {
            CrossfadeRouteHost(routeState, routeActions, padding)
        }
        composable(RootRouteRegistry.AUDIOBOOK_SETTINGS) {
            AudiobookSettingsRouteHost(routeActions, padding)
        }
        composable(RootRouteRegistry.SETTINGS) {
            SettingsRouteHost(routeState, routeActions, padding)
        }
        composable(RootRouteRegistry.MANAGE_PLAYLISTS) {
            ManagePlaylistsRouteHost(routeState, routeActions, padding)
        }
        composable(RootRouteRegistry.LIBRARY_FOLDERS) {
            LibraryFoldersRouteHost(routeState, routeActions, padding)
        }
        composable(RootRouteRegistry.NOW_PLAYING_BAR_STYLE) {
            NowPlayingBarStyleRouteHost(routeState, routeActions, padding)
        }
        composable(RootRouteRegistry.SMART_PLAYLIST_SETTINGS) {
            SmartPlaylistSettingsRouteHost(routeActions, padding)
        }
        composable(RootRouteRegistry.CHANGELOG) {
            ChangelogRouteHost(routeActions)
        }
        composable(RootRouteRegistry.ABOUT) {
            AboutRouteHost(routeActions, padding)
        }
        composable(RootRouteRegistry.PRIVACY_POLICY) {
            PrivacyPolicyRouteHost(routeState, routeActions, padding)
        }
    }
}

@Composable
private fun SearchRouteHost(
    navState: RootNavigationState,
    routeState: RootRouteState,
    routeActions: RootRouteActions,
    padding: RootRoutePadding,
    searchViewModel: SearchViewModel,
) {
    SearchRoute(
        viewModel = searchViewModel,
        libraryState = routeState.libraryState,
        playlists = routeState.playlists.playlists,
        favoriteSongIds = routeState.appState.favoriteSongIds,
        topPadding = padding.topContent,
        bottomPadding = padding.bottomContent,
        scrollToTopRequestVersion = navState.searchScrollRequestVersion,
        onSearchActiveChanged = routeActions.onSearchActiveChanged,
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
        onAudiobookSelected = { book -> routeActions.openAudiobook(book.stableKey) },
        onPlaylistSelected = { playlist -> routeActions.openPlaylist(playlist.id) },
        onToggleFavorite = routeActions.playlists::toggleFavorite,
    )
}
