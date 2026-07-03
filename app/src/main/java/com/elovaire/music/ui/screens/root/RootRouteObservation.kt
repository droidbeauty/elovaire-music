package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.library.LibraryUiState

internal data class RootRouteObservation(
    val backStackEntry: NavBackStackEntry?,
    val route: String?,
    val concreteRoute: String?,
    val activeBottomRoute: String,
    val currentAlbumRouteId: Long?,
)

@Composable
internal fun rememberRootRouteObservation(
    navController: NavHostController,
    navigationState: RootNavigationState,
    libraryState: LibraryUiState,
    isPlaybackActuallyPlaying: Boolean,
): RootRouteObservation {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val concreteRoute = backStackEntry?.concreteNavigationRoute() ?: route
    RootPerformanceStates(
        route = route,
        libraryState = libraryState,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    var previousMotionRoute by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(concreteRoute) {
        ElovaireTrace.section("route_change") {
            navigationState.logRouteTransition(previousMotionRoute, concreteRoute)
        }
        previousMotionRoute = concreteRoute
    }
    LaunchedEffect(route) {
        navigationState.syncTopLevelSelection(route)
    }
    LaunchedEffect(backStackEntry, route, concreteRoute, navigationState.browsingOriginRoute) {
        navigationState.syncRouteOwnership(backStackEntry, route)
    }
    return RootRouteObservation(
        backStackEntry = backStackEntry,
        route = route,
        concreteRoute = concreteRoute,
        activeBottomRoute = navigationState.activeBottomRoute(concreteRoute, route),
        currentAlbumRouteId = backStackEntry?.currentAlbumRouteId(),
    )
}

internal fun NavBackStackEntry.currentAlbumRouteId(): Long? {
    val arguments = arguments ?: return null
    return when {
        arguments.containsKey("albumId") -> arguments.getString("albumId")?.toLongOrNull()
            ?: arguments.getLong("albumId").takeIf { it != 0L }
        else -> null
    }
}
