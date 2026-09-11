package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStartupCoordinatorTest {
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
}
