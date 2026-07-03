package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootOverlayStateHolderTest {
    @Test
    fun openChangelogSheet_closesTopBarMenu() {
        var showTopBarMenu = true
        var showChangelogSheet = false
        val holder = RootOverlayStateHolder(
            showTopBarMenu = showTopBarMenu,
            showChangelogSheet = showChangelogSheet,
            showPlaylistCreateDialog = false,
            setTopBarMenu = { showTopBarMenu = it },
            setChangelogSheet = { showChangelogSheet = it },
            setPlaylistCreateDialog = {},
        )

        holder.openChangelogSheet()

        assertFalse(showTopBarMenu)
        assertTrue(showChangelogSheet)
    }

    @Test
    fun onRouteChanged_closesPlaylistDialogOutsidePlaylistsRoute() {
        var showTopBarMenu = true
        var showPlaylistCreateDialog = true
        val holder = RootOverlayStateHolder(
            showTopBarMenu = showTopBarMenu,
            showChangelogSheet = false,
            showPlaylistCreateDialog = showPlaylistCreateDialog,
            setTopBarMenu = { showTopBarMenu = it },
            setChangelogSheet = {},
            setPlaylistCreateDialog = { showPlaylistCreateDialog = it },
        )

        holder.onRouteChanged(HOME_ROUTE)

        assertFalse(showTopBarMenu)
        assertFalse(showPlaylistCreateDialog)
    }

    @Test
    fun onRouteChanged_keepsPlaylistDialogOnPlaylistsRoute() {
        var showPlaylistCreateDialog = true
        val holder = RootOverlayStateHolder(
            showTopBarMenu = false,
            showChangelogSheet = false,
            showPlaylistCreateDialog = showPlaylistCreateDialog,
            setTopBarMenu = {},
            setChangelogSheet = {},
            setPlaylistCreateDialog = { showPlaylistCreateDialog = it },
        )

        holder.onRouteChanged(PLAYLISTS_ROUTE)

        assertTrue(showPlaylistCreateDialog)
    }
}
