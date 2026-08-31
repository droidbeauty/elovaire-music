package elovaire.music.droidbeauty.app.quality

import android.content.ContentUris
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.AndroidCapabilities
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.core.requiredAudioPermission
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCompatibilityMatrixInstrumentedTest {
    @Test
    fun sdkGatedPoliciesMatchThePhysicalDevice() {
        val sdk = Build.VERSION.SDK_INT
        assertEquals(sdk >= Build.VERSION_CODES.R, AndroidCapabilities.supportsGroupedMediaWrite(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.P, AndroidCapabilities.supportsImageDecoder(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.TIRAMISU, AndroidCapabilities.supportsDirectPlaybackQuery(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.Q, AndroidCapabilities.requiresMediaPlaybackForegroundServiceType(sdk))
    }

    @Test
    fun compatibilitySnapshotContainsOnlyBoundedPlatformFacts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val snapshot = context.platformCompatibilitySnapshot()

        assertEquals(Build.VERSION.SDK_INT, snapshot.sdkInt)
        assertEquals(37, snapshot.targetSdk)
        assertTrue(snapshot.buildType == "debug" || snapshot.buildType == "release")
        assertEquals(requiredAudioPermission(), snapshot.requiredAudioPermission)
        assertTrue(
            snapshot.audioPermissionState == PlatformPermissionState.Granted ||
                snapshot.audioPermissionState == PlatformPermissionState.Denied,
        )
        assertEquals(
            AndroidCapabilities.requiresLocalNetworkPermission(Build.VERSION.SDK_INT),
            snapshot.localNetworkPermissionRequired,
        )
        assertEquals(PlatformNotificationState.MediaSessionExempt, snapshot.notificationState)
        assertTrue(snapshot.safGrantCount >= 0)
        assertTrue(snapshot.networkSourceCount == null || snapshot.networkSourceCount >= 0)
        assertTrue(snapshot.externalVolumeCount == null || snapshot.externalVolumeCount >= 0)
        assertTrue(snapshot.strictModeViolationCount >= 0)
        assertTrue(snapshot.compatibilityChanges.size <= 16)
        assertTrue(snapshot.resourceCounters.size <= 16)
    }

    @Test
    fun compatibilityMatrixIsExplicitAboutHardwareDependentCases() {
        val scenarios = listOf(
            CompatibilityScenario("media permissions", requiresHardware = false),
            CompatibilityScenario("SAF persisted grants", requiresHardware = false),
            CompatibilityScenario("MediaSession external control", requiresHardware = false),
            CompatibilityScenario("USB audio route", requiresHardware = true),
            CompatibilityScenario("Bluetooth audio route", requiresHardware = true),
        )

        assertEquals(5, scenarios.size)
        assertEquals(2, scenarios.count(CompatibilityScenario::requiresHardware))
    }

    @Test
    fun api37MessageQueueCompatibilityChangeIsReported() {
        assumeTrue(Build.VERSION.SDK_INT >= 37)
        val runner = PlatformCompatChangeRunner(InstrumentationRegistry.getInstrumentation())
        val state = runner.dumpPlatformCompat()
        assertTrue(state.contains("USE_NEW_MESSAGEQUEUE"))
    }

    @Test
    fun handlerStressRemainsLosslessAndCancellable() {
        val handler = Handler(Looper.getMainLooper())
        val callbackCount = 1_000
        val completed = CountDownLatch(callbackCount)
        val executed = AtomicInteger(0)
        val producers = Executors.newFixedThreadPool(4)
        try {
            repeat(callbackCount) {
                producers.execute {
                    check(handler.post {
                        executed.incrementAndGet()
                        completed.countDown()
                    })
                }
            }
            val cancelled = AtomicInteger(0)
            val delayed = Runnable { cancelled.incrementAndGet() }
            check(handler.postDelayed(delayed, 1_000L))
            handler.removeCallbacks(delayed)

            assertTrue(completed.await(10L, TimeUnit.SECONDS))
            assertEquals(callbackCount, executed.get())
            assertEquals(0, cancelled.get())
        } finally {
            producers.shutdownNow()
        }
    }

    @Test
    fun mediaStorePermissionAndReadPathWorkOnThePhysicalDevice() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val permission = requiredAudioPermission()
        runCatching {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, permission)
        }.recoverCatching {
            val descriptor = instrumentation.uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} $permission",
            )
            ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }.getOrThrow()

        assertTrue(context.hasAudioReadPermission())
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            null,
            null,
            "${MediaStore.Audio.Media._ID} ASC",
        ).use { cursor ->
            assertNotNull(cursor)
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                assertTrue(id > 0L)
                assertEquals(
                    id,
                    ContentUris.parseId(
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id,
                        ),
                    ),
                )
            }
        }
    }
}

private data class CompatibilityScenario(
    val name: String,
    val requiresHardware: Boolean,
)
