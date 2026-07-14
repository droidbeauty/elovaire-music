package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertEquals
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
}
