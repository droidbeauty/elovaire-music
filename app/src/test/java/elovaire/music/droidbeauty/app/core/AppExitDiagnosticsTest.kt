package elovaire.music.droidbeauty.app.core

import android.app.ApplicationExitInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppExitDiagnosticsTest {
    @Test
    fun classifiesExpectedAndAbnormalExits() {
        assertEquals(AppExitCategory.Expected, classifyAppExitReason(ApplicationExitInfo.REASON_USER_REQUESTED))
        assertEquals(AppExitCategory.Crash, classifyAppExitReason(ApplicationExitInfo.REASON_CRASH))
        assertEquals(AppExitCategory.Crash, classifyAppExitReason(ApplicationExitInfo.REASON_INITIALIZATION_FAILURE))
        assertEquals(AppExitCategory.Anr, classifyAppExitReason(ApplicationExitInfo.REASON_ANR))
        assertEquals(AppExitCategory.ResourcePressure, classifyAppExitReason(ApplicationExitInfo.REASON_LOW_MEMORY))
        assertEquals(
            AppExitCategory.ResourcePressure,
            classifyAppExitReason(ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE),
        )
        assertEquals(AppExitCategory.Expected, classifyAppExitReason(ApplicationExitInfo.REASON_PACKAGE_UPDATED))
        assertEquals(AppExitCategory.Unknown, classifyAppExitReason(Int.MIN_VALUE))
    }

    @Test
    fun classifiesAndroid17MemoryLimiterDescriptionAsResourcePressure() {
        assertEquals(
            AppExitCategory.ResourcePressure,
            classifyAppExitReason(ApplicationExitInfo.REASON_OTHER, "MemoryLimiter:AnonSwap"),
        )
    }

    @Test
    fun suppressesOptionalStartupOnlyForRecentCrashLoop() {
        val now = 1_000_000L
        val recentCrashes = listOf(
            record(AppExitCategory.Crash, now - 1_000L),
            record(AppExitCategory.Anr, now - 2_000L),
            record(AppExitCategory.Crash, now - 3_000L),
        )

        assertTrue(shouldSuppressOptionalStartup(recentCrashes, now))
        assertFalse(shouldSuppressOptionalStartup(recentCrashes + record(AppExitCategory.Expected, now), now + 700_000L))
    }

    @Test
    fun futureDatedCrashRecordsDoNotCreatePermanentStartupSuppression() {
        val now = 1_000_000L
        val futureCrashes = listOf(
            record(AppExitCategory.Crash, now + 365L * 24L * 60L * 60L * 1_000L),
            record(AppExitCategory.Crash, now + 365L * 24L * 60L * 60L * 1_000L + 1L),
            record(AppExitCategory.Anr, now + 365L * 24L * 60L * 60L * 1_000L + 2L),
        )

        assertFalse(shouldSuppressOptionalStartup(futureCrashes, now))
    }

    @Test
    fun recentResourcePressureEnablesTemporaryConservativeStartup() {
        val now = 1_000_000L

        assertTrue(
            shouldSuppressOptionalStartup(
                listOf(record(AppExitCategory.ResourcePressure, now - 1_000L)),
                now,
            ),
        )
        assertFalse(
            shouldSuppressOptionalStartup(
                listOf(record(AppExitCategory.ResourcePressure, now - 10L * 60L * 1_000L - 1L)),
                now,
            ),
        )
    }

    private fun record(category: AppExitCategory, timestampMs: Long) = AppExitRecord(
        reason = 0,
        status = 0,
        importance = 0,
        timestampMs = timestampMs,
        category = category,
    )
}
