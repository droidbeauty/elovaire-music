package elovaire.music.droidbeauty.app.quality

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidNavigationQualificationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val packageName = instrumentation.targetContext.packageName
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
        launchApp()
        assertTrue(device.wait(Until.hasObject(By.pkg(packageName)), STARTUP_TIMEOUT_MS))
        assertCurrentPackageIsElovaire()
        acceptFirstLaunchStoragePermissionIfVisible()
        assertTrue(device.wait(Until.hasObject(By.pkg(packageName).desc("Home")), STARTUP_TIMEOUT_MS))
        assertCurrentPackageIsElovaire()
    }

    @After
    fun assertProcessAndLogsAreHealthy() {
        try {
            val pid = shell("pidof $packageName").trim()
            assertTrue("Target process exited", pid.isNotEmpty())
            val logcat = shell("logcat -d -v threadtime --pid $pid")
            assertFalse(runtimeFailurePattern.containsMatchIn(logcat))
        } finally {
            try {
                fixture?.close()
            } finally {
                fixture = null
            }
        }
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
    fun backNavigationReturnsToHomeWithoutAnInvisibleBlockingLayer() {
        requireDescription("Albums")
        requireDescription("Playlists")
        requireDescription("Search")

        pressBackWithoutIdle(64L)

        checkpoint()
        waitForDescription("Home")
        assertSelectedTopLevel("Home")
        assertNoBlockingSystemLayer()
    }

    private fun burstDescriptions(descriptions: List<String>, interInputDelayMs: Long) {
        var lastInputAt = SystemClock.uptimeMillis()
        descriptions.forEach { description ->
            val injectedAt = SystemClock.uptimeMillis()
            if (clickWithoutIdle(description)) {
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
        assertCurrentPackageIsElovaire()
        device.pressBack()
        if (interInputDelayMs > 0L) SystemClock.sleep(interInputDelayMs)
    }

    private fun clickWithoutIdle(description: String): Boolean {
        assertCurrentPackageIsElovaire()
        repeat(STALE_RETRY_COUNT) {
            val node = findTopLevelNode(description) ?: return false
            try {
                assertCurrentPackageIsElovaire()
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
            assertCurrentPackageIsElovaire()
            val node = findTopLevelNode(description)
            if (node != null) {
                try {
                    assertCurrentPackageIsElovaire()
                    node.clickActionable()
                    return
                } catch (_: StaleObjectException) {
                    // Retry only while the transition is replacing this node.
                }
            }
        }
        throw AssertionError("Missing actionable content description: $description")
    }

    private fun waitForDescription(description: String) {
        val deadline = SystemClock.uptimeMillis() + ACTION_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            assertCurrentPackageIsElovaire()
            if (findTopLevelNode(description) != null) return
            SystemClock.sleep(50L)
        }
        throw AssertionError("Missing content description: $description")
    }

    private fun checkpoint() {
        assertCurrentPackageIsElovaire()
        assertTrue(device.wait(Until.hasObject(By.pkg(packageName)), ACTION_TIMEOUT_MS))
        device.waitForIdle()
    }

    private fun assertCurrentPackageIsElovaire() {
        assertEquals("Refusing to interact outside Elovaire", packageName, device.currentPackageName)
    }

    private fun assertSelectedTopLevel(description: String) {
        val deadline = SystemClock.uptimeMillis() + ACTION_TIMEOUT_MS
        var destinationNode: UiObject2? = null
        while (SystemClock.uptimeMillis() < deadline) {
            destinationNode = findTopLevelNode(description)
            if (destinationNode?.isSelectedOrAncestorSelected() == true) return
            SystemClock.sleep(50L)
        }
        val selectedDestinations = TOP_LEVEL_DESCRIPTIONS.filter { candidate ->
            findTopLevelNode(candidate)?.isSelectedOrAncestorSelected() == true
        }
        val navigationNodes = TOP_LEVEL_DESCRIPTIONS.joinToString { candidate ->
            val nodes = device.findObjects(By.pkg(packageName).desc(candidate))
                .joinToString { node ->
                    "${node.resourceName}@${node.visibleBounds.centerY()} clickable=${node.isClickable} selected=${node.isSelected} checked=${node.isChecked} ancestors=${node.semanticAncestors()}"
                }
            "$candidate=[$nodes]"
        }
        assertTrue(
            "Top-level destination did not converge: $description; selected=$selectedDestinations; nodes=$navigationNodes",
            destinationNode?.isSelectedOrAncestorSelected() == true,
        )
    }

    private fun assertNoDuplicateTopLevelSelection() {
        val visibleDestinationCount = TOP_LEVEL_DESCRIPTIONS.count { description ->
            findTopLevelNode(description) != null
        }
        assertTrue("Bottom navigation was lost during route convergence", visibleDestinationCount == 4)
    }

    private fun assertNoBlockingSystemLayer() {
        assertTrue(device.currentPackageName == packageName)
    }

    private fun findTopLevelNode(description: String): UiObject2? {
        return device.findObjects(By.pkg(packageName).desc(description))
            .firstOrNull { node ->
                node.visibleBounds.centerY() >= device.displayHeight * 4 / 5
            }
    }

    private fun UiObject2.semanticAncestors(): String {
        val ancestors = mutableListOf<String>()
        var node = parent
        repeat(4) {
            if (node != null) {
                ancestors += "clickable=${node.isClickable} selected=${node.isSelected} checked=${node.isChecked} desc=${node.contentDescription}"
                node = node.parent
            }
        }
        return ancestors.joinToString(" -> ")
    }

    private fun UiObject2.isSelectedOrAncestorSelected(): Boolean {
        var node: UiObject2? = this
        repeat(5) {
            if (node?.isSelected == true) return true
            node = node?.parent
        }
        return false
    }

    private fun launchApp() {
        val intent = instrumentation.targetContext.packageManager
            .getLaunchIntentForPackage(packageName)
            ?: error("Launch intent not found")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        instrumentation.targetContext.startActivity(intent)
    }

    private fun grantAudioPermission() {
        // The app hosts this test runner; revocation kills it before Android reports the result.
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (instrumentation.targetContext.checkSelfPermission(permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return
        runCatching { instrumentation.uiAutomation.grantRuntimePermission(packageName, permission) }
            .onFailure { shell("pm grant $packageName $permission") }
    }

    private fun acceptFirstLaunchStoragePermissionIfVisible() {
        device.findObject(By.pkg(packageName).text("Allow storage access"))?.let { button ->
            button.clickActionable()
            val permissionDialog = device.wait(
                Until.findObject(By.pkg(PERMISSION_CONTROLLER_PACKAGES).text("Allow")),
                5_000L,
            )
            permissionDialog?.clickActionable()
        }
    }

    private fun shell(command: String): String {
        val descriptor: ParcelFileDescriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input -> input.bufferedReader().readText() }
    }

    private fun UiObject2.clickActionable() {
        var target: UiObject2? = this
        while (target != null && !target.isClickable) target = target.parent
        (target ?: this).click()
    }

    private companion object {
        const val STARTUP_TIMEOUT_MS = 30_000L
        const val ACTION_TIMEOUT_MS = 10_000L
        const val STALE_RETRY_COUNT = 3
        val CADENCES_MS = longArrayOf(160L, 100L, 64L, 32L, 16L)
        val TOP_LEVEL_DESCRIPTIONS = listOf("Home", "Albums", "Playlists", "Search")
        val PERMISSION_CONTROLLER_PACKAGES = Pattern.compile(
            "com\\.android\\.permissioncontroller|com\\.google\\.android\\.permissioncontroller|" +
                "com\\.android\\.packageinstaller|com\\.samsung\\.android\\.permissioncontroller",
        )
        val runtimeFailurePattern = Regex("FATAL EXCEPTION|\\bANR\\b|AndroidRuntime:.*fatal", RegexOption.IGNORE_CASE)
    }
}
