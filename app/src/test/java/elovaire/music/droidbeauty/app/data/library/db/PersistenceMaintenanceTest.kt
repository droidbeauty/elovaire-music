package elovaire.music.droidbeauty.app.data.library.db

import org.junit.Assert.assertEquals
import org.junit.Test

class PersistenceMaintenanceTest {
    @Test
    fun retentionCutoffNeverPredatesEpoch() {
        assertEquals(0L, terminalMutationCutoff(1L))
    }

    @Test
    fun retentionCutoffKeepsThirtyDays() {
        val thirtyOneDays = 31L * 24L * 60L * 60L * 1_000L
        val oneDay = 24L * 60L * 60L * 1_000L
        assertEquals(oneDay, terminalMutationCutoff(thirtyOneDays))
    }
}
