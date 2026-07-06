package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
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
    uiDevice.findObject(By.desc(description))?.click()
}

internal fun MacrobenchmarkScope.clickText(text: String) {
    uiDevice.findObject(By.text(text))?.click()
}

internal fun MacrobenchmarkScope.requireClickDescription(description: String) {
    val node = uiDevice.wait(Until.findObject(By.desc(description)), 5_000)
        ?: error("Missing required UI element with contentDescription=$description")
    node.click()
    uiDevice.waitForIdle()
}

internal fun MacrobenchmarkScope.requireClickText(text: String) {
    val node = uiDevice.wait(Until.findObject(By.text(text)), 5_000)
        ?: error("Missing required UI element with text=$text")
    node.click()
    uiDevice.waitForIdle()
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
    uiDevice.pressBack()
    requireClickDescription("Playlists")
    waitForAppVisible()
    uiDevice.pressBack()
    requireClickDescription("Home")
    requireClickDescription("Menu")
    requireClickText("Settings")
    waitForAppVisible()
    uiDevice.pressBack()
    requireClickDescription("Menu")
    requireClickText("Equalizer")
    waitForAppVisible()
    uiDevice.pressBack()
}
