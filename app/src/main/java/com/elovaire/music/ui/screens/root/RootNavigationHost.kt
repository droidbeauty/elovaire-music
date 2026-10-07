package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun RootNavigationHost(
    navState: RootNavigationState,
    motionTransitions: MotionTransitions,
    modifier: Modifier = Modifier,
    content: NavGraphBuilder.() -> Unit,
) {
    val navigationMotionResolver = remember { NavigationMotionResolver() }
    val sharedTransitionController = remember { AlbumSharedTransitionController() }
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(
            LocalAlbumSharedTransitionScope provides this,
            LocalAlbumSharedTransitionController provides sharedTransitionController,
        ) {
            NavHost(
                navController = navState.navController,
                startDestination = HOME_ROUTE,
                modifier = Modifier,
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
            DeferBackUntilNavigationSettles(navState)
        }
    }
}

@Composable
internal fun DeferBackUntilNavigationSettles(navState: RootNavigationState) {
    val visibleEntries by navState.navController.visibleEntries.collectAsState()
    val transitionInProgress = visibleEntries.size > 1
    var backRequested by remember { mutableStateOf(false) }

    LaunchedEffect(transitionInProgress, backRequested) {
        if (!transitionInProgress && backRequested) {
            backRequested = false
            navState.navigateUp()
        }
    }

    BackHandler(enabled = transitionInProgress) {
        if (!backRequested) backRequested = true
    }
}
