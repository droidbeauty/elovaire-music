package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataRecoveryPolicyTest {
    @Test
    fun semanticallyIdenticalStateDoesNotRewriteRecoverySnapshot() {
        val persisted = UserDataSnapshot(favoriteSongIds = listOf(1L, 2L))

        assertFalse(shouldPersistRecoverySnapshot(persisted, persisted.copy()))
        assertTrue(shouldPersistRecoverySnapshot(null, persisted))
        assertTrue(shouldPersistRecoverySnapshot(persisted, persisted.copy(favoriteSongIds = listOf(1L, 2L, 3L))))
    }

    @Test
    fun highChurnHistoryIsClassifiedSeparatelyFromStructuralUserData() {
        val persisted = UserDataSnapshot(favoriteSongIds = listOf(1L))

        assertEquals(
            RecoverySnapshotChange.HighChurn,
            recoverySnapshotChange(persisted, persisted.copy(recentSongIds = listOf(2L))),
        )
        assertEquals(
            RecoverySnapshotChange.Structural,
            recoverySnapshotChange(persisted, persisted.copy(favoriteSongIds = listOf(2L))),
        )
    }
}
