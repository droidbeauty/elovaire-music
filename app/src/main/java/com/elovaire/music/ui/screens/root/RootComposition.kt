package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions

internal class RootComposition(
    val container: AppContainer,
    val context: Context,
    val navController: NavHostController,
    val motionTransitions: MotionTransitions,
    val viewModelFactory: ElovaireViewModelFactory,
    val rootViewModel: RootViewModel,
    val permissionController: RootPermissionController,
    val deleteController: RootDeleteController,
    val searchViewModel: SearchViewModel,
    val nowPlayingViewModel: NowPlayingViewModel,
)

@Composable
internal fun rememberRootComposition(container: AppContainer): RootComposition {
    val context = LocalContext.current
    val navController = rememberNavController()
    val motionTransitions = rememberMotionTransitions()
    val viewModelFactory = remember(container) { ElovaireViewModelFactory(container.viewModelDependencies) }
    val rootViewModel: RootViewModel = viewModel(factory = viewModelFactory)
    val libraryState by rootViewModel.libraryState.collectAsStateWithLifecycle()
    val permissionController = rememberRootPermissionController(
        container = container,
        libraryState = libraryState,
    )
    val deleteController = rememberRootDeleteController(container)
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)
    return RootComposition(
        container = container,
        context = context,
        navController = navController,
        motionTransitions = motionTransitions,
        viewModelFactory = viewModelFactory,
        rootViewModel = rootViewModel,
        permissionController = permissionController,
        deleteController = deleteController,
        searchViewModel = searchViewModel,
        nowPlayingViewModel = nowPlayingViewModel,
    )
}
