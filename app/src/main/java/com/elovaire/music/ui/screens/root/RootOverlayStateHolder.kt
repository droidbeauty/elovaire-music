package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

internal class RootOverlayStateHolder(
    currentRoute: String? = null,
    showTopBarMenu: Boolean,
    showChangelogSheet: Boolean,
    showPlaylistCreateDialog: Boolean,
    artistInfoArtist: String?,
    private val setTopBarMenu: (Boolean) -> Unit,
    private val setChangelogSheet: (Boolean) -> Unit,
    private val setPlaylistCreateDialog: (Boolean) -> Unit,
    private val setArtistInfoArtist: (String?) -> Unit,
) {
    val showTopBarMenu: Boolean = showTopBarMenu && currentRoute in TOP_BAR_MENU_ROUTES
    val showChangelogSheet: Boolean = showChangelogSheet && currentRoute in TOP_BAR_MENU_ROUTES
    val showPlaylistCreateDialog: Boolean = showPlaylistCreateDialog && currentRoute == PLAYLISTS_ROUTE
    val artistInfoArtist: String? = artistInfoArtist.takeIf { currentRoute.isArtistDetailRoute() }

    fun openTopBarMenu() = setTopBarMenu(true)
    fun dismissTopBarMenu() = setTopBarMenu(false)
    fun openChangelogSheet() {
        setTopBarMenu(false)
        setChangelogSheet(true)
    }
    fun dismissChangelogSheet() = setChangelogSheet(false)
    fun requestCreatePlaylist() {
        setTopBarMenu(false)
        setPlaylistCreateDialog(true)
    }
    fun dismissPlaylistCreateDialog() = setPlaylistCreateDialog(false)
    fun openArtistInfo(artistName: String) = setArtistInfoArtist(artistName)
    fun dismissArtistInfo() = setArtistInfoArtist(null)

    fun onRouteChanged(route: String?) {
        setTopBarMenu(false)
        setChangelogSheet(false)
        if (route != PLAYLISTS_ROUTE) {
            setPlaylistCreateDialog(false)
        }
        if (route?.isArtistDetailRoute() != true) setArtistInfoArtist(null)
    }
}

@Composable
internal fun rememberRootOverlayStateHolder(currentRoute: String?): RootOverlayStateHolder {
    var showTopBarMenu by rememberSaveable { mutableStateOf(false) }
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    var showPlaylistCreateDialog by rememberSaveable { mutableStateOf(false) }
    var artistInfoArtist by rememberSaveable { mutableStateOf<String?>(null) }
    val currentHolder = RootOverlayStateHolder(
        currentRoute = currentRoute,
        showTopBarMenu = showTopBarMenu,
        showChangelogSheet = showChangelogSheet,
        showPlaylistCreateDialog = showPlaylistCreateDialog,
        artistInfoArtist = artistInfoArtist,
        setTopBarMenu = { showTopBarMenu = it },
        setChangelogSheet = { showChangelogSheet = it },
        setPlaylistCreateDialog = { showPlaylistCreateDialog = it },
        setArtistInfoArtist = { artistInfoArtist = it },
    )
    LaunchedEffect(currentRoute) {
        currentHolder.onRouteChanged(currentRoute)
    }
    return currentHolder
}

private fun String?.isArtistDetailRoute(): Boolean = this?.startsWith("$ARTIST_ROUTE/") == true

private val TOP_BAR_MENU_ROUTES = setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE)
