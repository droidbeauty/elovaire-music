package elovaire.music.droidbeauty.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class RootTopBarMenuActions(
    val openSettings: () -> Unit,
    val openEqualizer: () -> Unit,
    val openChangelogSheet: () -> Unit,
    val openAbout: () -> Unit,
)

@Composable
internal fun rememberRootTopBarMenuActions(
    navController: NavHostController,
    overlayState: RootOverlayStateHolder,
): RootTopBarMenuActions {
    return remember(navController, overlayState) {
        RootTopBarMenuActions(
            openSettings = {
                overlayState.dismissTopBarMenu()
                navController.navigate(SETTINGS_ROUTE)
            },
            openEqualizer = {
                overlayState.dismissTopBarMenu()
                navController.navigate(EQUALIZER_ROUTE)
            },
            openChangelogSheet = {
                overlayState.openChangelogSheet()
            },
            openAbout = {
                overlayState.dismissTopBarMenu()
                navController.navigate(ABOUT_ROUTE)
            },
        )
    }
}

internal fun rootSharedTopBarSpec(
    currentRoute: String?,
    showTopLevelChrome: Boolean,
    language: AppLanguage,
    onRequestCreatePlaylist: () -> Unit,
    onOpenMenu: () -> Unit,
): SharedTopBarSpec? {
    return if (showTopLevelChrome) {
        SharedTopBarSpec.Unified(
            title = topBarTitle(currentRoute, language),
            showSettings = currentRoute in TopMenuRoutes,
            supplementalActionIconResId = playlistSupplementalActionIcon(currentRoute),
            supplementalActionContentDescription = if (currentRoute == PLAYLISTS_ROUTE) "Create playlist" else null,
            onSupplementalAction = if (currentRoute == PLAYLISTS_ROUTE) onRequestCreatePlaylist else null,
            onOpenMenu = onOpenMenu,
        )
    } else {
        null
    }
}

@DrawableRes
private fun playlistSupplementalActionIcon(currentRoute: String?): Int? {
    return if (currentRoute == PLAYLISTS_ROUTE) R.drawable.ic_lucide_plus else null
}

private val TopMenuRoutes = setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE)
