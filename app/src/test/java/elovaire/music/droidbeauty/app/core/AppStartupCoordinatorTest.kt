package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertFalse
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
}
