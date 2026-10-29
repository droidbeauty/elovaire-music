package elovaire.music.droidbeauty.app.data.library

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDeletePendingStateTest {
    @Test
    fun pendingOperationIsNotOverwritten() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val preferences = context.getSharedPreferences("device-delete-pending-test", 0)
        preferences.edit().clear().commit()
        try {
            assertTrue(persistPendingDeleteState(preferences, "first", "first-plan"))
            assertFalse(persistPendingDeleteState(preferences, "second", "second-plan"))
            assertEquals("first", preferences.getString("pending_device_delete_operation_id", null))
            assertEquals("first-plan", preferences.getString("pending_device_delete_plan", null))
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
