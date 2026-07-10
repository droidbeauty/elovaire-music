package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.core.AppContainer

@Composable
fun ElovaireRoot(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean = false,
    adaptiveInfo: ElovaireAdaptiveInfo = elovaireAdaptiveInfo(width = 0.dp),
) {
    val composition = rememberRootComposition(container)

    RootPermissionGate(
        permissionState = composition.permissionController.state,
        onRequestAudioPermission = composition.permissionController::requestAudioPermission,
    ) {
        ElovaireRootReadyHost(
            composition = composition,
            resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
            adaptiveInfo = adaptiveInfo,
        )
    }
}
