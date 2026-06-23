package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
internal fun RootNavigationHost(
    navState: RootNavigationState,
    modifier: Modifier = Modifier,
    content: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navState.navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
        enterTransition = {
            resolveForwardEnterTransition(
                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                    initialRoute = initialState.destination.route,
                    targetRoute = targetState.destination.route,
                    initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                    targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                    detailRouteTransitionMode = navState.detailRouteTransitionMode,
                ),
                expandOrigin = navState.detailExpandOrigin,
            )
        },
        exitTransition = {
            resolveForwardExitTransition(
                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                    initialRoute = initialState.destination.route,
                    targetRoute = targetState.destination.route,
                    initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                    targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                    detailRouteTransitionMode = navState.detailRouteTransitionMode,
                ),
            )
        },
        popEnterTransition = {
            resolvePopEnterTransition(
                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                    initialRoute = initialState.destination.route,
                    targetRoute = targetState.destination.route,
                    initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                    targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                    detailRouteTransitionMode = navState.detailRouteTransitionMode,
                ),
            )
        },
        popExitTransition = {
            resolvePopExitTransition(
                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                    initialRoute = initialState.destination.route,
                    targetRoute = targetState.destination.route,
                    initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                    targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                    detailRouteTransitionMode = navState.detailRouteTransitionMode,
                ),
                expandOrigin = navState.detailExpandOrigin,
            )
        },
        builder = content,
    )
}
