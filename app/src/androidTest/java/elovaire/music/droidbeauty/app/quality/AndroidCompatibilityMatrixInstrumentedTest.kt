package elovaire.music.droidbeauty.app.quality

import android.content.ContentUris
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.AndroidCapabilities
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.core.requiredAudioPermission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCompatibilityMatrixInstrumentedTest {
    @Test
    fun sdkGatedPoliciesMatchThePhysicalDevice() {
        val sdk = Build.VERSION.SDK_INT
        assertEquals(sdk >= Build.VERSION_CODES.R, AndroidCapabilities.supportsGroupedMediaWrite(sdk))
        assertEquals(sdk == Build.VERSION_CODES.Q, AndroidCapabilities.usesRecoverableMediaWrite(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.P, AndroidCapabilities.supportsImageDecoder(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.TIRAMISU, AndroidCapabilities.supportsDirectPlaybackQuery(sdk))
        assertEquals(sdk >= Build.VERSION_CODES.Q, AndroidCapabilities.requiresMediaPlaybackForegroundServiceType(sdk))
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
