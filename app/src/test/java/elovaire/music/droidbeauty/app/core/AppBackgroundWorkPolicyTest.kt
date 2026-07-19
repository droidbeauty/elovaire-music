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
                environment = WorkEnvironment(
                    foreground = true,
                    batterySaver = true,
                    criticalUserOperationActive = true,
                ),
            ),
        )
    }

    @Test
    fun automaticMaintenanceDefersDuringCriticalWorkOrBatterySaver() {
        assertEquals(
            WorkDecision.Defer,
            decideWorkAdmission(
                AppWorkKind.ForegroundOnlyMaintenance,
                userInitiated = false,
                environment = WorkEnvironment(foreground = true, criticalUserOperationActive = true),
            ),
        )
        assertEquals(
            WorkDecision.Defer,
            decideWorkAdmission(
                AppWorkKind.ForegroundOnlyMaintenance,
                userInitiated = false,
                environment = WorkEnvironment(foreground = true, batterySaver = true),
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
}
