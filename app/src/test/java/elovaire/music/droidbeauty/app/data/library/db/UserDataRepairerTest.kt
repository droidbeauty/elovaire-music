package elovaire.music.droidbeauty.app.data.library.db

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataRepairerTest {
    @Test
    fun repairPlanIncludesOnlyDeterministicUserDataFields() {
        val plan = UserDataRepairer.plan(
            invalidPlaylistEntries = true,
            invalidFavorites = false,
            invalidRecentPlayback = true,
            invalidSongPlayCounts = true,
            invalidAlbumPlayCounts = false,
        )

        assertTrue(plan.normalizePlaylistPositions)
        assertFalse(plan.normalizeFavoritePositions)
        assertTrue(plan.normalizeRecentPositions)
        assertTrue(plan.normalizeSongPlayCounts)
        assertFalse(plan.normalizeAlbumPlayCounts)
        assertFalse(plan.isEmpty)
    }

    @Test
    fun emptyRepairPlanIsNoOp() {
        assertTrue(
            UserDataRepairer.plan(
                invalidPlaylistEntries = false,
                invalidFavorites = false,
                invalidRecentPlayback = false,
                invalidSongPlayCounts = false,
                invalidAlbumPlayCounts = false,
            ).isEmpty,
        )
    }
}
