package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal fun RootEffectsHost(
    composition: RootComposition,
    uiRuntime: RootUiRuntime,
    isPlaybackActuallyPlaying: Boolean,
) {
    val playerLayerController = uiRuntime.playerLayerController
    RootNotificationPermissionEffect(
        isPlaybackActuallyPlaying,
        composition.permissionController.state,
        composition.permissionController::requestNotificationPermission,
    )
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    LaunchedEffect(composition.container) {
        composition.container.openNowPlayingCommands.collect {
            currentPlayerLayerController.requestOpen(null)
        }
    }
    RootSystemBarEffect(
        darkTheme = uiRuntime.shellInputs.darkTheme,
        showPlayerOverlay = uiRuntime.chromeVisibility.showPlayerOverlay,
        playerContentColor = uiRuntime.shellInputs.playerAdaptivePalette.contentColor,
    )
}
