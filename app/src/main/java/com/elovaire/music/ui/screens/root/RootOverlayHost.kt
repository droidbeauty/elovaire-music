package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.update.AppUpdateTransientStatus
import elovaire.music.droidbeauty.app.data.update.AppUpdateUiState
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
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
    onCreatePlaylist: (String) -> Long,
    showTopLevelChrome: Boolean,
    currentRoute: String?,
    topBarHeight: Dp,
    appUpdateState: AppUpdateUiState,
    onDismissUpdate: () -> Unit,
    onStartUpdate: () -> Unit,
    permissionState: RootPermissionState,
    onRequestAudioPermission: () -> Unit,
    motionTransitions: MotionTransitions,
) {
    TopBarContextMenuOverlay(
        expanded = showTopBarMenu,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
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
            .zIndex(11f),
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
                if (onCreatePlaylist(name) > 0L) {
                    onDismissPlaylistCreateDialog()
                }
            },
        )
    }
    ElovaireAnimatedVisibility(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(7f)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = topBarHeight + 8.dp,
            ),
        visible = BuildConfig.ENABLE_GITHUB_UPDATE_FLOW &&
            (showTopLevelChrome || currentRoute == SETTINGS_ROUTE) &&
            (appUpdateState.availableRelease != null || appUpdateState.transientStatus != null),
        enter = motionTransitions.bannerEnter(),
        exit = motionTransitions.bannerExit(),
        label = "UpdateBannerVisibility",
    ) {
        when {
            appUpdateState.availableRelease != null -> {
                UpdateAvailableBanner(
                    release = requireNotNull(appUpdateState.availableRelease),
                    uiState = appUpdateState,
                    onDismiss = onDismissUpdate,
                    onUpdate = onStartUpdate,
                )
            }
            appUpdateState.transientStatus == AppUpdateTransientStatus.UpToDate -> {
                UpdateStatusBanner(
                    text = rootUiCopy(LocalAppLanguage.current).appUpToDate,
                    iconResId = R.drawable.ic_lucide_check,
                )
            }
        }
    }
    ElovaireAnimatedVisibility(
        visible = permissionState.showFirstLaunchPermissionOverlay,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9f),
        enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.82f),
        exit = motionTransitions.overlayFadeExit(targetAlpha = 0.96f),
        label = "FirstLaunchPermissionOverlayVisibility",
    ) {
        FirstLaunchPermissionLoadingScreen(
            showLoading = true,
            onRequestPermission = onRequestAudioPermission,
        )
    }
}
