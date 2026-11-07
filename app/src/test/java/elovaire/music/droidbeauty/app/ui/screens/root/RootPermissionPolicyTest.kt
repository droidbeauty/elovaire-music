package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootPermissionPolicyTest {
    @Test
    fun overlay_stays_visible_without_audio_permission() {
        assertTrue(
            shouldShowFirstLaunchPermissionOverlay(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = false,
                isLibraryLoading = false,
                songsCount = 4,
                albumsCount = 1,
                errorMessage = null,
                playFirstLaunchHomeReveal = true,
            ),
        )
    }

    @Test
    fun overlay_stays_visible_while_library_is_loading() {
        assertTrue(
            shouldShowFirstLaunchPermissionOverlay(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = true,
                isLibraryLoading = true,
                songsCount = 0,
                albumsCount = 0,
                errorMessage = null,
                playFirstLaunchHomeReveal = false,
            ),
        )
    }

    @Test
    fun empty_successful_library_waits_for_reveal() {
        assertTrue(
            shouldShowFirstLaunchPermissionOverlay(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = true,
                isLibraryLoading = false,
                songsCount = 0,
                albumsCount = 0,
                errorMessage = null,
                playFirstLaunchHomeReveal = false,
            ),
        )
        assertFalse(
            shouldShowFirstLaunchPermissionOverlay(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = true,
                isLibraryLoading = false,
                songsCount = 0,
                albumsCount = 0,
                errorMessage = null,
                playFirstLaunchHomeReveal = true,
            ),
        )
    }

    @Test
    fun reveal_waits_for_loaded_content_or_error() {
        assertFalse(
            shouldRevealFirstLaunchHome(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = true,
                isLibraryLoading = true,
                songsCount = 1,
                albumsCount = 1,
                errorMessage = null,
                scanProgress = 0.5f,
                initialWaitTimedOut = false,
            ),
        )
        assertTrue(
            shouldRevealFirstLaunchHome(
                firstLaunchPermissionExperienceActive = true,
                hasAudioPermission = true,
                isLibraryLoading = false,
                songsCount = 0,
                albumsCount = 0,
                errorMessage = "scan failed",
                scanProgress = 0f,
                initialWaitTimedOut = false,
            ),
        )
    }

    @Test
    fun completedEmptyLibraryRevealsHome() {
        assertFalse(shouldRevealFirstLaunchHome(true, true, false, 0, 0, null, 0f, false))
        assertTrue(shouldRevealFirstLaunchHome(true, true, false, 0, 0, null, 1f, false))
    }

    @Test
    fun prolongedScanDoesNotKeepPermissionOverlayBlockingHome() {
        assertFalse(shouldRevealFirstLaunchHome(true, true, true, 0, 0, null, 0f, false))
        assertTrue(shouldRevealFirstLaunchHome(true, true, true, 0, 0, null, 0f, true))
        assertFalse(shouldShowFirstLaunchPermissionOverlay(true, true, true, 0, 0, null, true))
        assertFalse(shouldRevealFirstLaunchHome(true, false, true, 0, 0, null, 0f, true))
    }
}
