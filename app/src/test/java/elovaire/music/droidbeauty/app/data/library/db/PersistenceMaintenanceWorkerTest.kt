package elovaire.music.droidbeauty.app.data.library.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenceMaintenanceWorkerTest {
    @Test
    fun durableMaintenanceUsesBoundedResourceConstraints() {
        val request = PersistenceMaintenanceWorker.request()

        assertEquals("persistence-maintenance", PersistenceMaintenanceWorker.UNIQUE_WORK_NAME)
        assertEquals(false, request.workSpec.constraints.requiresBatteryNotLow())
        assertTrue(request.workSpec.constraints.requiresStorageNotLow())
    }

    @Test
    fun maintenanceFailsClosedForEveryIntegrityProblem() {
        assertTrue(DatabaseHealth(true, 0, false).isMaintenanceSuccessful())
        assertFalse(DatabaseHealth(false, 0, false).isMaintenanceSuccessful())
        assertFalse(DatabaseHealth(true, 1, false).isMaintenanceSuccessful())
        assertFalse(DatabaseHealth(true, 0, true).isMaintenanceSuccessful())
    }
}
