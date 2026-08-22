package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.interaction.LocalInteractionWorkload

@Composable
fun ElovaireRoot(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean = false,
) {
    val composition = rememberRootComposition(container)
    val appearanceState by composition.rootViewModel.appearanceState.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalAppLanguage provides appearanceState.appLanguage,
        LocalInteractionWorkload provides container.interactionWorkPolicy,
    ) {
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
