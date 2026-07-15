package elovaire.music.droidbeauty.app.quality

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInteractionSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun setUp() {
        grantRuntimePermission(audioPermission())
        shell("logcat -c")
        launchApp()
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 10_000)
    }

    @After
    fun assertNoRuntimeFailures() {
        val logcat = shell("logcat -d -t 2000")
        val runtimeFailure = runtimeFailurePattern.find(logcat)
        assertFalse(runtimeFailure?.value, runtimeFailure != null)
        assertFalse(hasAppOwnedStrictModeViolation(logcat))
    }

    @Test
    fun topLevelNavigationMenuAndPlayerSmoke() {
        clickDescription("Albums")
        waitForApp()
        clickDescription("Playlists")
        waitForApp()
        clickDescription("Search")
        waitForApp()
        device.pressBack()
        clickDescription("Home")
        waitForApp()

        device.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.5f)
        device.findObject(By.scrollable(true))?.scroll(Direction.UP, 0.5f)

        clickDescription("Menu")
        device.wait(Until.hasObject(By.text("Settings")), 3_000)
        clickText("Settings")
        waitForApp()
        device.pressBack()

        clickDescription("Home")
        clickDescription("Play album")
        waitForApp()
        if (device.wait(Until.hasObject(By.desc("Pause")), 5_000)) {
            device.click(device.displayWidth / 3, (device.displayHeight * 0.85f).toInt())
            device.wait(Until.hasObject(By.desc("Minimize")), 5_000)
            device.findObject(By.desc("Minimize"))?.click() ?: device.pressBack()
        }
    }

    private fun waitForApp() {
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 5_000)
        device.waitForIdle()
    }

    private fun clickDescription(description: String) {
        device.findObject(By.desc(description))?.click()
        waitForApp()
    }

    private fun clickText(text: String) {
        device.findObject(By.text(text))?.click()
        waitForApp()
    }

    private fun grantRuntimePermission(permission: String) {
        try {
            instrumentation.uiAutomation.grantRuntimePermission(PACKAGE_NAME, permission)
        } catch (_: SecurityException) {
            shell("pm grant $PACKAGE_NAME $permission")
        }
    }

    private fun launchApp() {
        val context = instrumentation.targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
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

    private fun hasAppOwnedStrictModeViolation(logcat: String): Boolean {
        return logcat
            .split("StrictMode policy violation")
            .drop(1)
            .any { violation ->
                violation
                    .lineSequence()
                    .take(40)
                    .any { line ->
                        line.contains("\tat elovaire.music.droidbeauty.app.") &&
                            !line.contains(".quality.")
                    }
            }
    }

    private companion object {
        const val PACKAGE_NAME = "elovaire.music.droidbeauty.app"
        val runtimeFailurePattern = Regex("FATAL EXCEPTION|\\bANR\\b|AndroidRuntime:.*fatal", RegexOption.IGNORE_CASE)
    }
}
