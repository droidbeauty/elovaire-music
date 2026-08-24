package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRuntimeBoundariesTest {
    @Test
    fun wallTimeDeadlineRejectsExpiredAndImplausiblyFutureValues() {
        val day = 24L * 60L * 60L * 1_000L

        assertTrue(isWallTimeDeadlineFresh(10L * day, 11L * day, 7L * day))
        assertFalse(isWallTimeDeadlineFresh(10L * day, 9L * day, 7L * day))
        assertFalse(isWallTimeDeadlineFresh(10L * day, 18L * day, 7L * day))
        assertFalse(isWallTimeDeadlineFresh(365L * day, day, 7L * day))
        assertFalse(isWallTimeDeadlineFresh(day, 365L * day, 7L * day))
    }
}
