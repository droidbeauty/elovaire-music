package elovaire.music.droidbeauty.app.macrobenchmark

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "elovaire.music.droidbeauty.app"

internal val MacrobenchmarkScope.uiDevice: UiDevice
    get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

internal fun MacrobenchmarkScope.waitForAppVisible() {
    check(uiDevice.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 10_000)) {
        "App package did not become visible: $TARGET_PACKAGE"
    }
    acceptFirstLaunchStoragePermissionIfVisible()
}

internal fun MacrobenchmarkScope.grantMediaPermission() {
    runCatching {
        uiDevice.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.READ_MEDIA_AUDIO")
    }
    runCatching {
        uiDevice.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.READ_EXTERNAL_STORAGE")
    }
}

internal fun MacrobenchmarkScope.clickDescription(description: String) {
    clickIfAvailable(By.desc(description))
}

internal fun MacrobenchmarkScope.clickText(text: String) {
    clickIfAvailable(By.text(text))
}

internal fun MacrobenchmarkScope.requireClickText(text: String) {
    requireClick(By.text(text), "text=$text")
}

internal fun MacrobenchmarkScope.requireClickDescription(description: String) {
    requireClick(By.desc(description), "contentDescription=$description")
}

internal fun MacrobenchmarkScope.requireClickTestTag(tag: String) {
    repeat(4) {
        if (uiDevice.findObject(By.res(tag)) != null) return requireClick(By.res(tag), "testTag=$tag")
        uiDevice.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.65f)
        uiDevice.waitForIdle()
    }
    requireClick(By.res(tag), "testTag=$tag")
}

private fun MacrobenchmarkScope.clickIfAvailable(selector: BySelector) {
    val deadlineMs = SystemClock.uptimeMillis() + CLICK_TIMEOUT_MS
    while (SystemClock.uptimeMillis() < deadlineMs) {
        val remainingMs = (deadlineMs - SystemClock.uptimeMillis()).coerceAtLeast(1L)
        val node = uiDevice.wait(Until.findObject(selector), remainingMs.coerceAtMost(FIND_TIMEOUT_MS))
            ?: return
        try {
            node.clickActionable()
            return
        } catch (_: StaleObjectException) {
            // Compose replaced the semantics node between lookup and click; retry within the deadline.
        }
    }
}

private fun MacrobenchmarkScope.requireClick(selector: BySelector, label: String) {
    val deadlineMs = SystemClock.uptimeMillis() + CLICK_TIMEOUT_MS
    var lastStale: StaleObjectException? = null
    while (SystemClock.uptimeMillis() < deadlineMs) {
        val remainingMs = (deadlineMs - SystemClock.uptimeMillis()).coerceAtLeast(1L)
        val node = uiDevice.wait(Until.findObject(selector), remainingMs.coerceAtMost(FIND_TIMEOUT_MS))
            ?: continue
        try {
            node.clickActionable()
            uiDevice.waitForIdle()
            return
        } catch (stale: StaleObjectException) {
            lastStale = stale
        }
    }
    throw lastStale ?: error("Missing required UI element with $label")
}

private fun UiObject2.clickActionable() {
    var target: UiObject2? = this
    while (target != null && !target.isClickable) {
        target = target.parent
    }
    checkNotNull(target) { "UI element has no clickable ancestor" }.click()
}

internal fun MacrobenchmarkScope.clickTextContains(text: String) {
    uiDevice.findObject(By.textContains(text))?.click()
}

private fun MacrobenchmarkScope.acceptFirstLaunchStoragePermissionIfVisible() {
    uiDevice.findObject(By.text("Allow storage access"))?.let { button ->
        button.click()
        uiDevice.waitForIdle()
        uiDevice.wait(Until.findObject(By.text("Allow")), 5_000)?.click()
        uiDevice.waitForIdle()
    }
}

internal fun MacrobenchmarkScope.homeJourney() {
    waitForAppVisible()
    uiDevice.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.7f)
    uiDevice.findObject(By.scrollable(true))?.scroll(Direction.UP, 0.7f)
}

internal fun MacrobenchmarkScope.topLevelNavigationJourney() {
    listOf("Albums", "Playlists", "Search", "Home").forEach { destination ->
        requireClickDescription(destination)
        waitForAppVisible()
    }
}

internal fun MacrobenchmarkScope.searchJourney() {
    requireClickDescription("Search")
    waitForAppVisible()
    uiDevice.click(uiDevice.displayWidth / 2, (uiDevice.displayHeight * 0.16f).toInt())
    uiDevice.waitForIdle()
    uiDevice.pressKeyCode(android.view.KeyEvent.KEYCODE_A)
    uiDevice.waitForIdle()
    uiDevice.pressBack()
    requireClickDescription("Home")
}

internal fun MacrobenchmarkScope.playerJourneyIfAvailable() {
    clickDescription("Home")
    waitForAppVisible()
    clickDescription("Play album")
    waitForAppVisible()
    uiDevice.findObject(By.desc("Pause")) ?: return
    uiDevice.click(uiDevice.displayWidth / 3, (uiDevice.displayHeight * 0.85f).toInt())
    waitForAppVisible()
    uiDevice.findObject(By.desc("Minimize"))?.click() ?: uiDevice.pressBack()
}

internal fun MacrobenchmarkScope.routeOpenBackJourney() {
    requireClickDescription("Albums")
    waitForAppVisible()
    uiDevice.click(uiDevice.displayWidth / 2, (uiDevice.displayHeight * 0.35f).toInt())
    waitForAppVisible()
    if (uiDevice.findObject(By.desc("Playlists")) == null) {
        uiDevice.pressBack()
        waitForAppVisible()
    }
    requireClickDescription("Playlists")
    waitForAppVisible()
    requireClickDescription("Home")
    requireClickDescription("Menu")
    requireClickTestTag("top_menu_settings")
    waitForAppVisible()
    requireClickTestTag("settings_privacy_policy")
    waitForAppVisible()
    uiDevice.pressBack()
    waitForAppVisible()
    uiDevice.pressBack()
    requireClickDescription("Menu")
    requireClickTestTag("top_menu_equalizer")
    waitForAppVisible()
    uiDevice.pressBack()
}

private const val CLICK_TIMEOUT_MS = 5_000L
private const val FIND_TIMEOUT_MS = 1_000L
