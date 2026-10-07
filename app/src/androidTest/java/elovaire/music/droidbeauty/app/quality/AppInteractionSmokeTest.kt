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
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import java.util.regex.Pattern
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInteractionSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val packageName = instrumentation.targetContext.packageName
    private var fixture: RapidUiFixture? = null

    @Before
    fun setUp() {
        device.wakeUp()
        val permission = audioPermission()
        if (instrumentation.targetContext.checkSelfPermission(permission) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            grantRuntimePermission(permission)
        }
        fixture = RapidUiFixture(
            context = instrumentation.targetContext,
            assetContext = instrumentation.context,
        )
        fixture?.install()
        launchApp()
        device.wait(Until.hasObject(By.pkg(packageName)), STARTUP_TIMEOUT_MS)
        assertElovaireInForeground()
        assertTrue(device.wait(Until.hasObject(By.pkg(packageName).desc("Menu")), STARTUP_TIMEOUT_MS))
    }

    @After
    fun assertNoRuntimeFailures() {
        try {
            if (device.currentPackageName == packageName) {
                device.findObject(By.pkg(packageName).desc("Pause"))?.click()
            }
            val pid = shell("pidof $packageName").trim()
            check(pid.isNotBlank()) { "App process is not running" }
            val logcat = shell("logcat -d -v threadtime --pid $pid")
            val runtimeFailure = runtimeFailurePattern.find(logcat)
            assertFalse(runtimeFailure?.value, runtimeFailure != null)
            val strictModeViolation = findAppOwnedStrictModeViolation(logcat)
            assertFalse(
                "App-owned StrictMode violation: ${strictModeViolation?.take(MAX_FAILURE_DETAIL_CHARS)}",
                strictModeViolation != null,
            )
        } finally {
            try {
                fixture?.close()
            } finally {
                fixture = null
            }
        }
    }

    @Test
    fun topLevelNavigationMenuAndPlayerSmoke() {
        clickTopLevel("Albums")
        waitForApp()
        clickTopLevel("Playlists")
        waitForApp()
        clickTopLevel("Search")
        waitForApp()
        pressBackInApp()
        clickTopLevel("Home")
        waitForApp()

        scrollIfAvailable(Direction.DOWN)
        scrollIfAvailable(Direction.UP)

        clickDescription("Menu")
        clickObject(settingsSelector, "localized Settings")
        waitForApp()
        pressBackInApp()

        clickTopLevel("Search")
        val searchInput = device.wait(
            Until.findObject(By.res("search_query_input")),
            APP_READY_TIMEOUT_MS,
        ) ?: error("Search input is unavailable")
        val fixtureTitle = checkNotNull(fixture).firstSongTitle()
        searchInput.click()
        searchInput.text = fixtureTitle
        pressBackInApp()
        waitForApp()
        val fixtureResult = waitForSearchResult(fixtureTitle)
        fixtureResult.clickActionable()
        clickTopLevel("Home")
        assertTrue(device.wait(Until.hasObject(By.pkg(packageName).desc("Pause")), 1_000L))
    }

    private fun waitForSearchResult(title: String): UiObject2 {
        val deadlineMs = SystemClock.uptimeMillis() + APP_READY_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadlineMs) {
            device.findObjects(By.pkg(packageName).textContains(title)).forEach { result ->
                try {
                    if (result.resourceName?.substringAfterLast('/') != "search_query_input") return result
                } catch (_: StaleObjectException) {
                    // Compose can replace result nodes while the query settles.
                }
            }
            SystemClock.sleep(50L)
        }
        error("Disposable fixture song result is unavailable")
    }

    private fun waitForApp() {
        assertElovaireInForeground()
        check(device.wait(Until.hasObject(By.pkg(packageName)), APP_READY_TIMEOUT_MS)) {
            "Elovaire left the foreground during the UI journey"
        }
        device.waitForIdle()
    }

    private fun pressBackInApp() {
        waitForApp()
        device.pressBack()
        waitForApp()
    }

    private fun clickDescription(description: String) {
        clickObject(By.desc(description), description)
        waitForApp()
    }

    private fun clickTopLevel(description: String) {
        clickObject(By.desc(description), description, bottomNavigation = true)
        waitForApp()
    }

    private fun clickObject(selector: BySelector, label: String, bottomNavigation: Boolean = false) {
        val appSelector = selector.pkg(packageName)
        val deadlineMs = SystemClock.uptimeMillis() + CLICK_TIMEOUT_MS
        var lastStale: StaleObjectException? = null
        while (SystemClock.uptimeMillis() < deadlineMs) {
            val remainingMs = (deadlineMs - SystemClock.uptimeMillis()).coerceAtLeast(1L)
            val target = if (bottomNavigation) {
                device.findObjects(appSelector).firstOrNull(::isBottomNavigationNode)
            } else {
                device.wait(Until.findObject(appSelector), remainingMs.coerceAtMost(FIND_TIMEOUT_MS))
            } ?: continue
            try {
                assertElovaireInForeground()
                device.waitForIdle()
                assertElovaireInForeground()
                target.click()
                return
            } catch (stale: StaleObjectException) {
                lastStale = stale
            }
        }
        throw lastStale ?: error("Could not find $label")
    }

    private fun isBottomNavigationNode(node: UiObject2): Boolean {
        return node.visibleBounds.centerY() >= device.displayHeight * 4 / 5
    }

    private fun UiObject2.clickActionable() {
        var target: UiObject2? = this
        while (target != null && !target.isClickable) target = target.parent
        (target ?: this).click()
    }

    private fun scrollIfAvailable(direction: Direction) {
        assertElovaireInForeground()
        repeat(3) {
            assertElovaireInForeground()
            try {
                val target = device.findObject(By.pkg(packageName).scrollable(true))
                if (target != null) {
                    assertElovaireInForeground()
                    target.scroll(direction, 0.5f)
                }
                device.waitForIdle()
                return
            } catch (_: StaleObjectException) {
                device.waitForIdle()
            }
        }
    }

    private fun grantRuntimePermission(permission: String) {
        try {
            instrumentation.uiAutomation.grantRuntimePermission(packageName, permission)
        } catch (_: SecurityException) {
            shell("pm grant $packageName $permission")
        }
    }

    private fun launchApp() {
        val context = instrumentation.targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: error("Elovaire launch intent is unavailable for $packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun assertElovaireInForeground() {
        check(device.currentPackageName == packageName) {
            "Refusing to interact outside Elovaire ($packageName)"
        }
    }

    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
            input.bufferedReader().readText()
        }
    }

    private fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun findAppOwnedStrictModeViolation(logcat: String): String? {
        return logcat
            .split("StrictMode policy violation")
            .drop(1)
            .firstOrNull { violation ->
                !violation.contains("# via Binder call with stack:") &&
                    violation
                        .lineSequence()
                        .takeWhile { line -> !line.contains("StrictMode policy violation") }
                        .any { line ->
                            line.contains("\tat elovaire.music.droidbeauty.app.") &&
                                !line.contains(".quality.")
                        }
            }
            ?.trim()
    }

    private companion object {
        const val STARTUP_TIMEOUT_MS = 30_000L
        const val APP_READY_TIMEOUT_MS = 10_000L
        const val CLICK_TIMEOUT_MS = 10_000L
        const val FIND_TIMEOUT_MS = 1_000L
        const val MAX_FAILURE_DETAIL_CHARS = 2_000
        val runtimeFailurePattern = Regex("FATAL EXCEPTION|\\bANR\\b|AndroidRuntime:.*fatal", RegexOption.IGNORE_CASE)
        val settingsSelector: BySelector = By.text(
            Pattern.compile(
                AppLanguage.entries
                    .joinToString("|") { language -> Pattern.quote(settingsCopy(language).settings) },
            ),
        )
    }
}
