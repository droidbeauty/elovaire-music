package elovaire.music.droidbeauty.app.ui.screens

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

internal class RootNavigationState(
    val navController: NavHostController,
    browsingOriginRouteState: MutableState<String>,
    selectedBottomRouteState: MutableState<String>,
    lastHomeTabRouteState: MutableState<String>,
    lastLibraryTabRouteState: MutableState<String>,
    lastPlaylistsTabRouteState: MutableState<String>,
    lastSearchTabRouteState: MutableState<String>,
    homeScrollRequestVersionState: MutableLongState,
    libraryScrollRequestVersionState: MutableLongState,
    playlistsScrollRequestVersionState: MutableLongState,
    searchScrollRequestVersionState: MutableLongState,
) {
    var browsingOriginRoute by browsingOriginRouteState
    var selectedBottomRoute by selectedBottomRouteState
    var lastHomeTabRoute by lastHomeTabRouteState
    var lastLibraryTabRoute by lastLibraryTabRouteState
    var lastPlaylistsTabRoute by lastPlaylistsTabRouteState
    var lastSearchTabRoute by lastSearchTabRouteState
    val routeOwnerOverrides = mutableStateMapOf<String, String>()

    var detailExpandOrigin by mutableStateOf(ExpandOrigin())
    var detailRouteTransitionMode by mutableStateOf(DetailRouteTransitionMode.TileExpand)
    var homeScrollRequestVersion by homeScrollRequestVersionState
    var libraryScrollRequestVersion by libraryScrollRequestVersionState
    var playlistsScrollRequestVersion by playlistsScrollRequestVersionState
    var searchScrollRequestVersion by searchScrollRequestVersionState

    fun logRouteTransition(previousRoute: String?, currentRoute: String?) {
        if (BuildConfig.DEBUG && previousRoute != currentRoute) {
            Log.d(
                "ElovaireMotion",
                "Route ${previousRoute.normalizedNavigationRoute().orEmpty()} -> ${currentRoute.normalizedNavigationRoute().orEmpty()}",
            )
        }
    }

    fun syncTopLevelSelection(currentRoute: String?) {
        if (currentRoute in TopLevelRoutes) {
            browsingOriginRoute = currentRoute.orEmpty()
            selectedBottomRoute = currentRoute.orEmpty()
        }
    }

    fun syncRouteOwnership(
        currentBackStackEntry: androidx.navigation.NavBackStackEntry?,
        currentRoute: String?,
    ) {
        val concreteRoute = currentBackStackEntry?.elovaireConcreteRoute() ?: return
        val normalizedConcreteRoute = concreteRoute.normalizedNavigationRoute()
        val ownerRoute = when (normalizedConcreteRoute) {
            HOME_ROUTE -> HOME_ROUTE
            SEARCH_ROUTE -> SEARCH_ROUTE
            ALBUMS_ROUTE -> ALBUMS_ROUTE
            PLAYLISTS_ROUTE -> PLAYLISTS_ROUTE
            "$LIBRARY_COLLECTION_ROUTE/{kind}",
            "$GENRE_ROUTE/{genre}",
            "$ARTIST_ROUTE/{artistName}",
            -> ALBUMS_ROUTE

            "$PLAYLIST_ROUTE/{playlistId}" -> PLAYLISTS_ROUTE
            "$ALBUM_ROUTE/{albumId}" -> {
                routeOwnerOverrides[concreteRoute]
                    ?: navController.previousBackStackEntry?.concreteNavigationRoute()?.let(routeOwnerOverrides::get)
                    ?: topLevelOwnerRoute(
                        navController.previousBackStackEntry?.destination?.route,
                        browsingOriginRoute,
                    )
                    ?: browsingOriginRoute.takeIf { it in TopLevelRoutes }
                    ?: selectedBottomRoute
            }

            else -> topLevelOwnerRoute(currentRoute, browsingOriginRoute) ?: selectedBottomRoute
        }
        if (ownerRoute in TopLevelRoutes) {
            routeOwnerOverrides[concreteRoute] = ownerRoute
        }
        if (concreteRoute in setOf(PLAYER_ROUTE, SETTINGS_ROUTE, EQUALIZER_ROUTE, LIBRARY_FOLDERS_ROUTE, PRIVACY_SAFETY_ROUTE, CHANGELOG_ROUTE, ABOUT_ROUTE)) {
            return
        }
        if (normalizedConcreteRoute == "$ALBUM_TAG_EDITOR_ROUTE/{albumId}") {
            return
        }
        if (normalizedConcreteRoute in setOf("$ALBUM_ROUTE/{albumId}", "$PLAYLIST_ROUTE/{playlistId}")) {
            return
        }
        when (ownerRoute) {
            HOME_ROUTE -> lastHomeTabRoute = concreteRoute
            ALBUMS_ROUTE -> lastLibraryTabRoute = concreteRoute
            PLAYLISTS_ROUTE -> lastPlaylistsTabRoute = concreteRoute
            SEARCH_ROUTE -> lastSearchTabRoute = concreteRoute
        }
    }

    fun activeBottomRoute(currentConcreteRoute: String?, currentRoute: String?): String {
        return routeOwnerOverrides[currentConcreteRoute]
            ?: topLevelOwnerRoute(currentRoute, browsingOriginRoute)
            ?: selectedBottomRoute
    }

    fun resetTopLevelTabState(route: String) {
        clearTopLevelScrollPositionMemory(route)
        when (route) {
            HOME_ROUTE -> {
                lastHomeTabRoute = HOME_ROUTE
                homeScrollRequestVersion += 1L
            }
            ALBUMS_ROUTE -> {
                lastLibraryTabRoute = ALBUMS_ROUTE
                libraryScrollRequestVersion += 1L
            }
            PLAYLISTS_ROUTE -> {
                lastPlaylistsTabRoute = PLAYLISTS_ROUTE
                playlistsScrollRequestVersion += 1L
            }
            SEARCH_ROUTE -> {
                lastSearchTabRoute = SEARCH_ROUTE
                searchScrollRequestVersion += 1L
            }
        }
    }

    fun navigateBottomTab(
        route: String,
        activeBottomRoute: String,
        currentRoute: String?,
    ) {
        browsingOriginRoute = route
        selectedBottomRoute = route
        routeOwnerOverrides[route] = route
        if (route == activeBottomRoute) {
            if (currentRoute != route) {
                val poppedToTabRoot = navController.popBackStack(route, inclusive = false)
                if (!poppedToTabRoot) {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
                resetTopLevelTabState(route)
            } else {
                resetTopLevelTabState(route)
            }
        } else {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    }

    fun openAlbum(album: Album, origin: ExpandOrigin, source: AlbumOpenSource) {
        if (BuildConfig.DEBUG) {
            Log.d("ElovaireMotion", "Album transition ${source.name} -> AlbumDetail(${album.id})")
        }
        detailExpandOrigin = origin
        detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
        navController.navigate(Routes.album(album.id))
    }
}

@Composable
internal fun rememberRootNavigationState(
    navController: NavHostController,
): RootNavigationState {
    val browsingOriginRoute = rememberSaveable { mutableStateOf(HOME_ROUTE) }
    val selectedBottomRoute = rememberSaveable { mutableStateOf(HOME_ROUTE) }
    val lastHomeTabRoute = rememberSaveable { mutableStateOf(HOME_ROUTE) }
    val lastLibraryTabRoute = rememberSaveable { mutableStateOf(ALBUMS_ROUTE) }
    val lastPlaylistsTabRoute = rememberSaveable { mutableStateOf(PLAYLISTS_ROUTE) }
    val lastSearchTabRoute = rememberSaveable { mutableStateOf(SEARCH_ROUTE) }
    val homeScrollRequestVersion = remember { mutableLongStateOf(0L) }
    val libraryScrollRequestVersion = remember { mutableLongStateOf(0L) }
    val playlistsScrollRequestVersion = remember { mutableLongStateOf(0L) }
    val searchScrollRequestVersion = remember { mutableLongStateOf(0L) }
    return remember(
        navController,
        browsingOriginRoute,
        selectedBottomRoute,
        lastHomeTabRoute,
        lastLibraryTabRoute,
        lastPlaylistsTabRoute,
        lastSearchTabRoute,
        homeScrollRequestVersion,
        libraryScrollRequestVersion,
        playlistsScrollRequestVersion,
        searchScrollRequestVersion,
    ) {
        RootNavigationState(
            navController = navController,
            browsingOriginRouteState = browsingOriginRoute,
            selectedBottomRouteState = selectedBottomRoute,
            lastHomeTabRouteState = lastHomeTabRoute,
            lastLibraryTabRouteState = lastLibraryTabRoute,
            lastPlaylistsTabRouteState = lastPlaylistsTabRoute,
            lastSearchTabRouteState = lastSearchTabRoute,
            homeScrollRequestVersionState = homeScrollRequestVersion,
            libraryScrollRequestVersionState = libraryScrollRequestVersion,
            playlistsScrollRequestVersionState = playlistsScrollRequestVersion,
            searchScrollRequestVersionState = searchScrollRequestVersion,
        )
    }
}

internal fun clearTopLevelScrollPositionMemory(route: String) {
    val prefixes = topLevelScrollCachePrefixes[route].orEmpty()
    if (prefixes.isEmpty()) return
    lazyListPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
    lazyGridPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
    scrollPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
}

internal fun String?.isAlbumDetailRoute(): Boolean = this == "$ALBUM_ROUTE/{albumId}"

internal fun resolveForwardEnterTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
    motionTransitions: MotionTransitions,
): EnterTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.targetUsesTileExpand -> motionTransitions.albumDetailForwardEnter(expandOrigin.toTransformOrigin())
    transition.topLevelTransition.isTopLevelTransition -> motionTransitions.topLevelEnter()
    transition.targetRoute.isAlbumDetailRoute() -> motionTransitions.albumDetailForwardEnter(expandOrigin.toTransformOrigin())
    transition.targetUsesDetailTransition -> motionTransitions.detailForwardEnter()
    else -> motionTransitions.fullScreenForwardEnter()
}

internal fun resolveForwardExitTransition(
    transition: NavHostTransitionResolution,
    motionTransitions: MotionTransitions,
): ExitTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.targetUsesTileExpand -> motionTransitions.albumDetailForwardExit()
    transition.topLevelTransition.isTopLevelTransition -> motionTransitions.topLevelExit()
    transition.targetRoute.isAlbumDetailRoute() -> motionTransitions.albumDetailForwardExit()
    transition.targetUsesDetailTransition -> motionTransitions.detailForwardExit()
    else -> motionTransitions.fullScreenForwardExit()
}

internal fun resolvePopEnterTransition(
    transition: NavHostTransitionResolution,
    motionTransitions: MotionTransitions,
): EnterTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.initialUsesTileExpand -> motionTransitions.albumDetailBackEnter()
    transition.topLevelTransition.isTopLevelTransition -> motionTransitions.topLevelEnter()
    transition.targetRoute.isAlbumDetailRoute() -> motionTransitions.albumDetailBackEnter()
    transition.targetUsesDetailTransition -> motionTransitions.detailBackEnter()
    else -> motionTransitions.fullScreenBackEnter()
}

internal fun resolvePopExitTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
    motionTransitions: MotionTransitions,
): ExitTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.initialUsesTileExpand -> motionTransitions.albumDetailBackExit(expandOrigin.toTransformOrigin())
    transition.topLevelTransition.isTopLevelTransition -> motionTransitions.topLevelExit()
    transition.initialRoute.isAlbumDetailRoute() -> motionTransitions.albumDetailBackExit(expandOrigin.toTransformOrigin())
    transition.initialUsesDetailTransition -> motionTransitions.detailBackExit()
    else -> motionTransitions.fullScreenBackExit()
}
