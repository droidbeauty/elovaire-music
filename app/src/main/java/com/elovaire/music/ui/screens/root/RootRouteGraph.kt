package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
    RootNavigationHost(
        navState = navState,
        modifier = modifier,
    ) {
        composable(HOME_ROUTE) {
            HomeRouteHost(navState, routeState.home, routeActions, padding)
        }
        composable(ALBUMS_ROUTE) {
            LibraryHubRouteHost(navState, routeState, routeActions, padding)
        }
        composable(PLAYLISTS_ROUTE) {
            PlaylistsRouteHost(navState, routeState.playlists, routeActions, padding)
        }
        composable(SEARCH_ROUTE) {
            SearchRouteHost(navState, routeState, routeActions, padding, searchViewModel)
        }
        composable(
            route = "$PLAYLIST_ROUTE/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            PlaylistDetailRouteHost(backStackEntry.playlistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(
            route = "$SMART_PLAYLIST_ROUTE/{smartPlaylistId}",
            arguments = listOf(navArgument("smartPlaylistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            SmartPlaylistDetailRouteHost(backStackEntry.smartPlaylistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(SMART_PLAYLIST_EDITOR_ROUTE) {
            SmartPlaylistEditorRouteHost(null, routeState.playlists, routeActions, padding)
        }
        composable(
            route = "$SMART_PLAYLIST_EDITOR_ROUTE/{smartPlaylistId}",
            arguments = listOf(navArgument("smartPlaylistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            SmartPlaylistEditorRouteHost(backStackEntry.smartPlaylistRouteId(), routeState.playlists, routeActions, padding)
        }
        composable(
            route = "$ALBUM_ROUTE/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            AlbumRouteHost(backStackEntry.albumRouteId(), navState, routeState, routeActions, padding)
        }
        composable(
            route = "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
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
            route = "$LIBRARY_COLLECTION_ROUTE/{kind}",
            arguments = listOf(navArgument("kind") { type = NavType.StringType }),
        ) { backStackEntry ->
            LibraryCollectionRouteHost(backStackEntry.libraryCollectionKindArg(), routeState, routeActions, padding)
        }
        composable(
            route = "$GENRE_ROUTE/{genre}",
            arguments = listOf(navArgument("genre") { type = NavType.StringType }),
        ) { backStackEntry ->
            GenreRouteHost(backStackEntry.genreRouteArg(), routeState, routeActions, padding)
        }
        composable(
            route = "$ARTIST_ROUTE/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
        ) { backStackEntry ->
            ArtistRouteHost(backStackEntry.artistRouteArg(), routeState, routeActions, padding)
        }
        composable(EQUALIZER_ROUTE) {
            EqualizerRouteHost(viewModelFactory, routeActions)
        }
        composable(SETTINGS_ROUTE) {
            SettingsRouteHost(routeState, routeActions, padding)
        }
        composable(LIBRARY_FOLDERS_ROUTE) {
            LibraryFoldersRouteHost(routeState, routeActions, padding)
        }
        composable(PRIVACY_SAFETY_ROUTE) {
            PrivacySafetyRouteHost(routeActions, padding)
        }
        composable(CHANGELOG_ROUTE) {
            ChangelogRouteHost(changelogReleases, routeActions)
        }
        composable(ABOUT_ROUTE) {
            AboutRouteHost(routeActions, padding)
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
        favoriteSongIds = routeState.appState.favoriteSongIds,
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
