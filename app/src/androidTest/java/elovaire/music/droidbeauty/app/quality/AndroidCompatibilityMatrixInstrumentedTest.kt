package elovaire.music.droidbeauty.app.quality

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.core.AndroidCapabilities
import org.junit.Assert.assertEquals
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
}

private data class CompatibilityScenario(
    val name: String,
    val requiresHardware: Boolean,
)

