package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState

@Composable
internal fun RootPermissionGate(
    permissionState: RootPermissionState,
    onRequestAudioPermission: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!permissionState.hasAudioPermission) {
        PerformanceState("screen", "permission")
        FirstLaunchPermissionLoadingScreen(
            showLoading = false,
            onRequestPermission = onRequestAudioPermission,
        )
        return
    }
    content()
}
