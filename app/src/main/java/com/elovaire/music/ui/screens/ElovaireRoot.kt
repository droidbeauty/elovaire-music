package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage

@Composable
fun ElovaireRoot(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean = false,
) {
    val composition = rememberRootComposition(container)

    CompositionLocalProvider(LocalAppLanguage provides composition.appState.appLanguage) {
        RootPermissionGate(
            permissionState = composition.permissionController.state,
            onRequestAudioPermission = composition.permissionController::requestAudioPermission,
        ) {
            ElovaireRootReadyHost(
                composition = composition,
                resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
            )
        }
    }
}
