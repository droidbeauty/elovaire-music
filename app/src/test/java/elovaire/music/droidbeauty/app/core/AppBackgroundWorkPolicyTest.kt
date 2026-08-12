package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppBackgroundWorkPolicyTest {
    @Test
    fun userWorkWinsWhileForeground() {
        assertEquals(
            WorkDecision.Admit,
            decideWorkAdmission(
                AppWorkKind.UserInitiatedLongTransfer,
                userInitiated = true,
                environment = WorkEnvironment(foreground = true),
            ),
        )
    }

    @Test
    fun foregroundMaintenanceIsAdmittedWhenVisible() {
        assertEquals(
            WorkDecision.Admit,
            decideWorkAdmission(
                AppWorkKind.ForegroundOnlyMaintenance,
                userInitiated = false,
                environment = WorkEnvironment(foreground = true),
            ),
        )
    }

    @Test
    fun foregroundBoundWorkDefersInBackground() {
        assertEquals(
            WorkDecision.Defer,
            decideWorkAdmission(
                AppWorkKind.ForegroundOnlyUiWork,
                userInitiated = false,
                environment = WorkEnvironment(foreground = false),
            ),
        )
    }

    @Test
    fun crashLoopSuppressesOnlyOptionalAutomaticWork() {
        val policy = AppBackgroundWorkPolicy(MutableStateFlow(true))
        policy.setOptionalStartupSuppressed(true)

        assertFalse(policy.canStart(AppWorkKind.ForegroundOnlyUiWork))
        assertFalse(policy.canStart(AppWorkKind.ForegroundOnlyMaintenance))
        assertTrue(policy.canStart(AppWorkKind.UserInitiatedShortWork, userInitiated = true))
        assertTrue(policy.canStart(AppWorkKind.MediaPlaybackRuntime))
    }

    @Test
    fun mediaStoreObserverIsForegroundBound() {
        val foreground = MutableStateFlow(true)
        val policy = AppBackgroundWorkPolicy(foreground)

        assertTrue(policy.shouldKeepMediaStoreObserver(permissionGranted = true))
        foreground.value = false
        assertFalse(policy.shouldKeepMediaStoreObserver(permissionGranted = true))
        assertFalse(policy.shouldKeepMediaStoreObserver(permissionGranted = false))
    }
}
