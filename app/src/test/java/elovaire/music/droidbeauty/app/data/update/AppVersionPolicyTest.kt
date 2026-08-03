package elovaire.music.droidbeauty.app.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionPolicyTest {
    @Test
    fun comparesNumericSegments() {
        assertTrue(AppVersionPolicy.isNewer("1.10.0", "1.9.9"))
        assertFalse(AppVersionPolicy.isNewer("1.2", "1.2.0"))
        assertEquals("2.4.1", AppVersionPolicy.resolve("release-2.4.1", "", "app.apk"))
    }

    @Test
    fun automaticChecksUseSuccessIntervalAndFailureBackoff() {
        assertFalse(shouldRunAutomaticUpdateCheck(100L, 200L, null, 0L, 1_000L, 500L))
        assertFalse(shouldRunAutomaticUpdateCheck(0L, 2_000L, 1_900L, 2_000L, 1_000L, 500L))
        assertTrue(shouldRunAutomaticUpdateCheck(0L, 2_000L, null, 2_000L, 1_000L, 500L))
    }
}
