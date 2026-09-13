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

    @Test
    fun previousProcessBreadcrumbIsBoundedAndRejectsMalformedData() {
        val encoded = encodePreviousProcessBreadcrumb(
            PreviousProcessBreadcrumb(
                processId = "process",
                versionCode = elovaire.music.droidbeauty.app.BuildConfig.VERSION_CODE,
                phase = "recovery",
                subsystem = "Persistence",
                operationPhase = "started",
                outcome = null,
                resourceCounts = (0 until 32).associate { "resource-$it" to it + 1 },
                timestampMs = 42L,
            ),
        )

        val decoded = decodePreviousProcessBreadcrumb(encoded)
        assertEquals("process", decoded?.processId)
        assertTrue(decoded!!.resourceCounts.size <= 16)
        assertEquals(null, decodePreviousProcessBreadcrumb(encoded.dropLast(1)))
    }

    @Test
    fun previousProcessBreadcrumbDoesNotStoreFreeFormSensitiveFields() {
        val breadcrumb = PreviousProcessBreadcrumb(
            processId = "id",
            versionCode = elovaire.music.droidbeauty.app.BuildConfig.VERSION_CODE,
            phase = "ready",
            subsystem = "Library",
            operationPhase = "completed",
            outcome = "ok",
            resourceCounts = emptyMap(),
            timestampMs = 1L,
        )

        val encoded = encodePreviousProcessBreadcrumb(breadcrumb)

        assertFalse(encoded.contains("/storage/"))
        assertFalse(encoded.contains("content://"))
        assertFalse(encoded.contains("title"))
    }

    @Test
    fun previousProcessBreadcrumbRequiresCurrentBuildAndFreshTimestamp() {
        val breadcrumb = PreviousProcessBreadcrumb(
            processId = "id",
            versionCode = 7,
            phase = "ready",
            subsystem = null,
            operationPhase = null,
            outcome = null,
            resourceCounts = emptyMap(),
            timestampMs = 1_000L,
        )

        assertTrue(isUsablePreviousProcessBreadcrumb(breadcrumb, 2_000L, 7))
        assertFalse(isUsablePreviousProcessBreadcrumb(breadcrumb, 2_000L, 8))
        assertFalse(isUsablePreviousProcessBreadcrumb(breadcrumb, 901_001L, 7))
        assertFalse(isUsablePreviousProcessBreadcrumb(breadcrumb.copy(timestampMs = 2_001L), 2_000L, 7))
    }

    private fun record(category: AppExitCategory, timestampMs: Long) = AppExitRecord(
        reason = 0,
        status = 0,
        importance = 0,
        timestampMs = timestampMs,
        category = category,
    )
}
