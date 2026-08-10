package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.update.AppUpdateUiState
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import kotlinx.coroutines.launch
import elovaire.music.droidbeauty.app.ui.screens.UpdateAvailableDialog
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions

@Composable
internal fun BoxScope.RootOverlayHost(
    showTopBarMenu: Boolean,
    onDismissTopBarMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenAbout: () -> Unit,
    showChangelogSheet: Boolean,
    changelogReleases: List<ChangelogRelease>,
    onDismissChangelogSheet: () -> Unit,
    showPlaylistCreateDialog: Boolean,
    onDismissPlaylistCreateDialog: () -> Unit,
    onCreatePlaylist: PlaylistCreateAction,
    permissionState: RootPermissionState,
    onRequestAudioPermission: () -> Unit,
    updateController: UpdateController,
    updateState: AppUpdateUiState,
    motionTransitions: MotionTransitions,
) {
    val scope = rememberCoroutineScope()
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    TopBarContextMenuOverlay(
        expanded = showTopBarMenu,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(RootLayerZ.TopBarMenu),
        onDismiss = onDismissTopBarMenu,
        onOpenSettings = onOpenSettings,
        onOpenEqualizer = onOpenEqualizer,
        onOpenChangelog = onOpenChangelog,
        onOpenAbout = onOpenAbout,
    )
    ElovaireAnimatedVisibility(
        visible = showChangelogSheet,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(RootLayerZ.ChangelogSheet),
        enter = motionTransitions.bottomSheetEnter(),
        exit = motionTransitions.bottomSheetExit(),
        label = "ChangelogSheetOverlay",
    ) {
        ChangelogBottomSheetOverlay(
            releases = changelogReleases,
            onDismiss = onDismissChangelogSheet,
        )
    }
    if (showPlaylistCreateDialog) {
        PlaylistNameDialog(
            onDismiss = onDismissPlaylistCreateDialog,
            onConfirm = { name ->
                if (!isCreatingPlaylist) {
                    isCreatingPlaylist = true
                    scope.launch {
                        when (onCreatePlaylist(name).await()) {
                            is PlaylistMutationResult.Success -> onDismissPlaylistCreateDialog()
                            else -> isCreatingPlaylist = false
                        }
                        if (isCreatingPlaylist) isCreatingPlaylist = false
                    }
                }
            },
        )
    }
    if (updateController.isSupported) {
        updateState.availableRelease?.let { release ->
            UpdateAvailableDialog(updateController, updateState, release)
        }
    }
    ElovaireAnimatedVisibility(
        visible = permissionState.showFirstLaunchPermissionOverlay,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(RootLayerZ.PermissionOverlay),
        enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.82f),
        exit = motionTransitions.overlayFadeExit(targetAlpha = 0.96f),
        label = "FirstLaunchPermissionOverlayVisibility",
    ) {
        FirstLaunchPermissionLoadingScreen(
            showLoading = permissionState.hasAudioPermission,
            onRequestPermission = onRequestAudioPermission,
        )
    }
}
