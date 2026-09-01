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
    fun comparesPrereleaseIdentifiersWithoutOverflowOrNumericLoss() {
        assertTrue(AppVersionPolicy.isNewer("1.2.3", "1.2.3-rc1"))
        assertTrue(AppVersionPolicy.isNewer("1.2.3-beta", "1.2.3-alpha"))
        assertFalse(AppVersionPolicy.isNewer("1.2.3+build2", "1.2.3+build1"))
        assertTrue(AppVersionPolicy.isSame("v1.2", "1.2.0"))
        assertTrue(AppVersionPolicy.isNewer("999999999999999999999.0.0", "1.999.999"))
        assertFalse(AppVersionPolicy.isNewer("not-a-version", "1.0.0"))
    }

    @Test
    fun resolveUsesTheReleaseTagBeforeNameOrAsset() {
        assertEquals("2.0.0", AppVersionPolicy.resolve("v2.0.0", "Release 1.9.0", "app-3.0.0.apk"))
    }

    @Test
    fun automaticChecksUseSuccessIntervalAndFailureBackoff() {
        assertFalse(shouldRunAutomaticUpdateCheck(100L, 200L, null, 0L, 1_000L, 500L))
        assertFalse(shouldRunAutomaticUpdateCheck(0L, 2_000L, 1_900L, 2_000L, 1_000L, 500L))
        assertTrue(shouldRunAutomaticUpdateCheck(0L, 2_000L, null, 2_000L, 1_000L, 500L))
    }
}
