package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.data.settings.hasMissingPortableSongReferences
import elovaire.music.droidbeauty.app.domain.model.Playlist

class AppStartupCoordinatorTest {
    @Test
    fun portableBackupMustNotDiscardReferencesMissingFromTheCurrentLibrary() {
        val snapshot = UserDataSnapshot(
            playlists = listOf(Playlist(10L, "Saved", listOf(1L, 2L))),
            favoriteSongIds = listOf(1L),
            songPlayCounts = mapOf(1L to 3),
            recentSongIds = listOf(1L),
        )
        assertTrue(snapshot.hasMissingPortableSongReferences(setOf(1L)))
        assertFalse(snapshot.hasMissingPortableSongReferences(setOf(1L, 2L)))
        assertFalse(UserDataSnapshot(playlists = listOf(Playlist(1L, "System", listOf(3L), isSystem = true)))
            .hasMissingPortableSongReferences(emptySet()))
    }

    @Test
    fun portableBackupRestoresOnlyOnAnUninitializedLocalStore() {
        assertTrue(shouldRestorePortableUserData(0L, false, 0L))
        assertFalse(shouldRestorePortableUserData(1L, false, 0L))
        assertFalse(shouldRestorePortableUserData(0L, true, 0L))
        assertFalse(shouldRestorePortableUserData(0L, false, null))
    }

    @Test
    fun startupBecomesReadyOnlyWhenAllCriticalRecoveryIsComplete() {
        assertEquals(
            DurableStartupPhase.Ready,
            startupStateAfterRecovery(
                mediaMutationRecoverySucceeded = true,
                networkRecoverySucceeded = true,
                blockedSourceIds = emptySet(),
            ).phase,
        )
    }

    @Test
    fun startupRetainsRetryableComponentsWhenRecoveryIsDeferred() {
        val state = startupStateAfterRecovery(
            mediaMutationRecoverySucceeded = false,
            networkRecoverySucceeded = false,
            blockedSourceIds = setOf("source-a"),
        )

        assertEquals(DurableStartupPhase.Degraded, state.phase)
        assertEquals(
            setOf(
                DurableStartupComponent.MediaMutationRecovery,
                DurableStartupComponent.NetworkSourceMutationRecovery,
            ),
            state.retryableComponents,
        )
    }

    @Test
    fun onlyReadyAndDegradedStartupStatesAllowMediaButtons() {
        assertTrue(DurableStartupState(DurableStartupPhase.Ready).allowsMediaButton())
        assertTrue(DurableStartupState(DurableStartupPhase.Degraded).allowsMediaButton())
        assertFalse(DurableStartupState(DurableStartupPhase.Recovering).allowsMediaButton())
        assertFalse(DurableStartupState(DurableStartupPhase.Released).allowsMediaButton())
    }
}
