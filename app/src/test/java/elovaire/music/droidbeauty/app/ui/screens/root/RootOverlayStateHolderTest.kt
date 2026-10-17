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
            artistInfoArtist = null,
            setTopBarMenu = { showTopBarMenu = it },
            setChangelogSheet = { showChangelogSheet = it },
            setPlaylistCreateDialog = {},
            setArtistInfoArtist = {},
        )

        holder.openChangelogSheet()

        assertFalse(showTopBarMenu)
        assertTrue(showChangelogSheet)
    }

    @Test
    fun onRouteChanged_closesPlaylistDialogOutsidePlaylistsRoute() {
        var showTopBarMenu = true
        var showChangelogSheet = true
        var showPlaylistCreateDialog = true
        val holder = RootOverlayStateHolder(
            showTopBarMenu = showTopBarMenu,
            showChangelogSheet = showChangelogSheet,
            showPlaylistCreateDialog = showPlaylistCreateDialog,
            artistInfoArtist = "Example Artist",
            setTopBarMenu = { showTopBarMenu = it },
            setChangelogSheet = { showChangelogSheet = it },
            setPlaylistCreateDialog = { showPlaylistCreateDialog = it },
            setArtistInfoArtist = {},
        )

        holder.onRouteChanged(HOME_ROUTE)

        assertFalse(showTopBarMenu)
        assertFalse(showChangelogSheet)
        assertFalse(showPlaylistCreateDialog)
    }

    @Test
    fun onRouteChanged_keepsPlaylistDialogOnPlaylistsRoute() {
        var showPlaylistCreateDialog = true
        val holder = RootOverlayStateHolder(
            showTopBarMenu = false,
            showChangelogSheet = false,
            showPlaylistCreateDialog = showPlaylistCreateDialog,
            artistInfoArtist = null,
            setTopBarMenu = {},
            setChangelogSheet = {},
            setPlaylistCreateDialog = { showPlaylistCreateDialog = it },
            setArtistInfoArtist = {},
        )

        holder.onRouteChanged(PLAYLISTS_ROUTE)

        assertTrue(showPlaylistCreateDialog)
    }

    @Test
    fun artistInfoOverlay_isOnlyExposedOnArtistDetailRoute() {
        val visible = RootOverlayStateHolder(
            currentRoute = "$ARTIST_ROUTE/{artistName}",
            showTopBarMenu = false,
            showChangelogSheet = false,
            showPlaylistCreateDialog = false,
            artistInfoArtist = "Example Artist",
            setTopBarMenu = {},
            setChangelogSheet = {},
            setPlaylistCreateDialog = {},
            setArtistInfoArtist = {},
        )
        val hidden = RootOverlayStateHolder(
            currentRoute = HOME_ROUTE,
            showTopBarMenu = false,
            showChangelogSheet = false,
            showPlaylistCreateDialog = false,
            artistInfoArtist = "Example Artist",
            setTopBarMenu = {},
            setChangelogSheet = {},
            setPlaylistCreateDialog = {},
            setArtistInfoArtist = {},
        )

        assertTrue(visible.artistInfoArtist == "Example Artist")
        assertTrue(hidden.artistInfoArtist == null)
    }
}
