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
    uiDevice.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 10_000)
}

internal fun MacrobenchmarkScope.clickDescription(description: String) {
    uiDevice.findObject(By.desc(description))?.click()
}

internal fun MacrobenchmarkScope.clickText(text: String) {
    uiDevice.findObject(By.text(text))?.click()
}

internal fun MacrobenchmarkScope.clickTextContains(text: String) {
    uiDevice.findObject(By.textContains(text))?.click()
}

internal fun MacrobenchmarkScope.homeJourney() {
    waitForAppVisible()
    uiDevice.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.7f)
    uiDevice.findObject(By.scrollable(true))?.scroll(Direction.UP, 0.7f)
}

internal fun MacrobenchmarkScope.topLevelNavigationJourney() {
    listOf("Albums", "Playlists", "Search", "Home").forEach { destination ->
        clickDescription(destination)
        waitForAppVisible()
    }
}

internal fun MacrobenchmarkScope.searchJourney() {
    clickDescription("Search")
    waitForAppVisible()
    uiDevice.findObject(By.focusable(true))?.click()
    uiDevice.waitForIdle()
    uiDevice.pressKeyCode(android.view.KeyEvent.KEYCODE_A)
    uiDevice.waitForIdle()
    uiDevice.pressBack()
    clickDescription("Home")
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
    clickDescription("Albums")
    waitForAppVisible()
    uiDevice.findObject(By.clickable(true))?.click()
    waitForAppVisible()
    uiDevice.pressBack()
    clickDescription("Playlists")
    waitForAppVisible()
    uiDevice.pressBack()
    clickDescription("Home")
    clickDescription("Menu")
    clickText("Settings")
    waitForAppVisible()
    uiDevice.pressBack()
    clickDescription("Menu")
    clickText("Equalizer")
    waitForAppVisible()
    uiDevice.pressBack()
}
