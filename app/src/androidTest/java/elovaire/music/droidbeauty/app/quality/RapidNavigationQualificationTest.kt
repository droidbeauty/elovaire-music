package elovaire.music.droidbeauty.app.quality

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidNavigationQualificationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private var fixture: RapidUiFixture? = null

    @Before
    fun setUp() {
        device.wakeUp()
        grantAudioPermission()
        fixture = RapidUiFixture(
            context = instrumentation.targetContext,
            assetContext = instrumentation.context,
        )
        fixture?.install()
        shell("logcat -c")
        launchApp()
        assertTrue(device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), STARTUP_TIMEOUT_MS))
        acceptFirstLaunchStoragePermissionIfVisible()
        assertTrue(device.wait(Until.hasObject(By.desc("Home")), STARTUP_TIMEOUT_MS))
    }

    @After
    fun assertProcessAndLogsAreHealthy() {
        assertTrue("Target process exited", shell("pidof $PACKAGE_NAME").trim().isNotEmpty())
        val logcat = shell("logcat -d --pid ${shell("pidof $PACKAGE_NAME").trim()}")
        assertFalse(runtimeFailurePattern.containsMatchIn(logcat))
        fixture?.close()
    }

    @Test
    fun topLevelRouteStormConvergesAtEverySupportedCadence() {
        CADENCES_MS.forEach { cadenceMs ->
            burstDescriptions(
                descriptions = listOf("Albums", "Playlists", "Search", "Home"),
                interInputDelayMs = cadenceMs,
            )
            burstDescriptions(
                descriptions = listOf("Home", "Search", "Playlists", "Albums"),
                interInputDelayMs = cadenceMs,
            )
            burstDescriptions(
                descriptions = listOf("Albums", "Search", "Playlists", "Albums", "Home", "Search"),
                interInputDelayMs = cadenceMs,
            )

            checkpoint()
            assertSelectedTopLevel("Search")
            assertNoDuplicateTopLevelSelection()
            requireDescription("Home")
            checkpoint()
            assertSelectedTopLevel("Home")
        }
    }

    @Test
    fun backBurstDoesNotLeaveAnInvisibleBlockingLayer() {
        requireDescription("Albums")
        requireDescription("Playlists")
        requireDescription("Search")

        repeat(3) {
            pressBackWithoutIdle(64L)
        }

        if (!device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 2_000L)) {
            launchApp()
        }
        checkpoint()
        requireDescription("Home")
        assertSelectedTopLevel("Home")
        assertNoBlockingSystemLayer()
    }

    private fun burstDescriptions(descriptions: List<String>, interInputDelayMs: Long) {
        var lastInputAt = SystemClock.uptimeMillis()
        descriptions.forEach { description ->
            val selector = By.desc(description)
            val injectedAt = SystemClock.uptimeMillis()
            if (clickWithoutIdle(selector)) {
                val elapsed = SystemClock.uptimeMillis() - injectedAt
                val remaining = interInputDelayMs - elapsed
                if (remaining > 0L) SystemClock.sleep(remaining)
                lastInputAt = SystemClock.uptimeMillis()
            } else {
                val cadenceGap = interInputDelayMs - (SystemClock.uptimeMillis() - lastInputAt)
                if (cadenceGap > 0L) SystemClock.sleep(cadenceGap)
            }
        }
    }

    private fun pressBackWithoutIdle(interInputDelayMs: Long) {
        device.pressBack()
        if (interInputDelayMs > 0L) SystemClock.sleep(interInputDelayMs)
    }

    private fun clickWithoutIdle(selector: BySelector): Boolean {
        repeat(STALE_RETRY_COUNT) {
            val node = device.findObject(selector) ?: return false
            try {
                node.clickActionable()
                return true
            } catch (_: StaleObjectException) {
                // The node may be replaced during a legitimate transition; retry without idle sync.
            }
        }
        return false
    }

    private fun requireDescription(description: String) {
        val deadline = SystemClock.uptimeMillis() + ACTION_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val node = device.wait(Until.findObject(By.desc(description)), FIND_TIMEOUT_MS)
            if (node != null) {
                try {
                    node.clickActionable()
                    return
                } catch (_: StaleObjectException) {
                    // Retry only while the transition is replacing this node.
                }
            }
        }
        throw AssertionError("Missing actionable content description: $description")
    }

    private fun checkpoint() {
        assertTrue(device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), ACTION_TIMEOUT_MS))
        device.waitForIdle()
    }

    private fun assertSelectedTopLevel(description: String) {
        val title = TOP_LEVEL_TITLES.getValue(description)
        val titleNode = device.wait(Until.findObject(By.text(title)), ACTION_TIMEOUT_MS)
        assertNotNull("Top-level destination did not converge: $description", titleNode)
    }

    private fun assertNoDuplicateTopLevelSelection() {
        val visibleDestinationCount = TOP_LEVEL_DESCRIPTIONS.count { description ->
            device.findObject(By.desc(description)) != null
        }
        assertTrue("Bottom navigation was lost during route convergence", visibleDestinationCount == 4)
    }

    private fun assertNoBlockingSystemLayer() {
        assertTrue(device.currentPackageName == PACKAGE_NAME)
    }

    private fun launchApp() {
        val intent = instrumentation.targetContext.packageManager
            .getLaunchIntentForPackage(PACKAGE_NAME)
            ?: error("Launch intent not found")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        instrumentation.targetContext.startActivity(intent)
    }

    private fun grantAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        runCatching { instrumentation.uiAutomation.grantRuntimePermission(PACKAGE_NAME, permission) }
            .onFailure { shell("pm grant $PACKAGE_NAME $permission") }
    }

    private fun acceptFirstLaunchStoragePermissionIfVisible() {
        device.findObject(By.text("Allow storage access"))?.let { button ->
            button.clickActionable()
            device.wait(Until.findObject(By.text("Allow")), 5_000L)?.clickActionable()
        }
    }

    private fun shell(command: String): String {
        val descriptor: ParcelFileDescriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input -> input.bufferedReader().readText() }
    }

    private fun UiObject2.clickActionable() {
        var target: UiObject2? = this
        while (target != null && !target.isClickable) target = target.parent
        checkNotNull(target) { "UI element has no clickable ancestor" }.click()
    }

    private companion object {
        const val PACKAGE_NAME = "elovaire.music.droidbeauty.app"
        const val STARTUP_TIMEOUT_MS = 30_000L
        const val ACTION_TIMEOUT_MS = 10_000L
        const val FIND_TIMEOUT_MS = 1_000L
        const val STALE_RETRY_COUNT = 3
        val CADENCES_MS = longArrayOf(160L, 100L, 64L, 32L, 16L)
        val TOP_LEVEL_DESCRIPTIONS = listOf("Home", "Albums", "Playlists", "Search")
        val TOP_LEVEL_TITLES = mapOf(
            "Home" to "Welcome",
            "Albums" to "Library",
            "Playlists" to "Playlists",
            "Search" to "Search",
        )
        val runtimeFailurePattern = Regex("FATAL EXCEPTION|\\bANR\\b|AndroidRuntime:.*fatal", RegexOption.IGNORE_CASE)
    }
}
