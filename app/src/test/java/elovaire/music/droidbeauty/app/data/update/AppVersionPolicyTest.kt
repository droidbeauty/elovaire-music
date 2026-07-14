package elovaire.music.droidbeauty.app.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionPolicyTest {
    @Test
    fun compare_handlesPrefixesAndMissingParts() {
        assertEquals(0, AppVersionPolicy.compare("v1.2", "1.2.0"))
        assertTrue(AppVersionPolicy.isNewer("1.2.1", "1.2"))
        assertFalse(AppVersionPolicy.isNewer("1.1.9", "1.2"))
    }

    @Test
    fun resolve_usesTagThenNameThenAsset() {
        assertEquals("2.3.4", AppVersionPolicy.resolve("v2.3.4", "Release 9.0", "app-8.0.apk"))
        assertEquals("3.4.5", AppVersionPolicy.resolve("stable", "Release 3.4.5", "app-8.0.apk"))
        assertEquals("4.5.6", AppVersionPolicy.resolve("stable", "release", "app-4.5.6.apk"))
    }

    @Test
    fun resolve_rejectsLabelsWithoutVersion() {
        assertEquals("", AppVersionPolicy.resolve("stable", "release", "app.apk"))
    }

    @Test
    fun automaticCheckUsesWallTimeForPersistedIntervalAndElapsedTimeForBackoff() {
        assertFalse(
            shouldRunAutomaticUpdateCheck(
                lastSuccessfulWallTimeMs = 1_000L,
                nowWallTimeMs = 1_500L,
                lastFailureElapsedTimeMs = null,
                nowElapsedTimeMs = 20L,
                successIntervalMs = 1_000L,
                failureBackoffMs = 100L,
            ),
        )
        assertFalse(
            shouldRunAutomaticUpdateCheck(
                lastSuccessfulWallTimeMs = 0L,
                nowWallTimeMs = 5_000L,
                lastFailureElapsedTimeMs = 50L,
                nowElapsedTimeMs = 100L,
                successIntervalMs = 1_000L,
                failureBackoffMs = 100L,
            ),
        )
        assertTrue(
            shouldRunAutomaticUpdateCheck(
                lastSuccessfulWallTimeMs = 0L,
                nowWallTimeMs = 5_000L,
                lastFailureElapsedTimeMs = null,
                nowElapsedTimeMs = 1L,
                successIntervalMs = 1_000L,
                failureBackoffMs = 100L,
            ),
        )
    }
}
