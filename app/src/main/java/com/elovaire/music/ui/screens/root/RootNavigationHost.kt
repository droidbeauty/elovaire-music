package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions

@Composable
internal fun RootNavigationHost(
    navState: RootNavigationState,
    modifier: Modifier = Modifier,
    content: NavGraphBuilder.() -> Unit,
) {
    val motionTransitions = rememberMotionTransitions()
    val navigationMotionResolver = remember { NavigationMotionResolver() }
    NavHost(
        navController = navState.navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
        enterTransition = {
            resolveForwardEnterTransition(
                transition = navigationMotionResolver.resolve(
                    NavigationMotionKey(
                        initialRoute = initialState.destination.route,
                        targetRoute = targetState.destination.route,
                        initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                        targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                        detailMode = navState.detailRouteTransitionMode,
                    ),
                ),
                expandOrigin = navState.detailExpandOrigin,
                motionTransitions = motionTransitions,
            )
        },
        exitTransition = {
            resolveForwardExitTransition(
                transition = navigationMotionResolver.resolve(
                    NavigationMotionKey(
                        initialRoute = initialState.destination.route,
                        targetRoute = targetState.destination.route,
                        initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                        targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                        detailMode = navState.detailRouteTransitionMode,
                    ),
                ),
                motionTransitions = motionTransitions,
            )
        },
        popEnterTransition = {
            resolvePopEnterTransition(
                transition = navigationMotionResolver.resolve(
                    NavigationMotionKey(
                        initialRoute = initialState.destination.route,
                        targetRoute = targetState.destination.route,
                        initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                        targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                        detailMode = navState.detailRouteTransitionMode,
                    ),
                ),
                motionTransitions = motionTransitions,
            )
        },
        popExitTransition = {
            resolvePopExitTransition(
                transition = navigationMotionResolver.resolve(
                    NavigationMotionKey(
                        initialRoute = initialState.destination.route,
                        targetRoute = targetState.destination.route,
                        initialFallbackTopLevelRoute = navState.browsingOriginRoute,
                        targetFallbackTopLevelRoute = navState.selectedBottomRoute,
                        detailMode = navState.detailRouteTransitionMode,
                    ),
                ),
                expandOrigin = navState.detailExpandOrigin,
                motionTransitions = motionTransitions,
            )
        },
        builder = content,
    )
}
