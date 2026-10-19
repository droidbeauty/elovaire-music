package elovaire.music.droidbeauty.app.ui.screens.equalizer

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.domain.model.EqCustomPreset
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.screens.EqualizerScreen
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EqualizerInteractionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun preampTracksFastGesturesAndPresetsRequireExplicitReplaceAndDelete() {
        val settings = mutableStateOf(EqSettings())
        val presets = mutableStateOf(emptyList<EqCustomPreset>())
        val selected = mutableStateOf<String?>(null)
        val theme = mutableStateOf(ThemeMode.Dark)
        val textSize = mutableStateOf(TextSizePreset.Default)
        val scenario = ActivityScenario.launch(PlaylistTestActivity::class.java)
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    ElovaireTheme(theme.value, textSize.value) {
                        EqualizerScreen(
                            settings = settings.value,
                            selectedPresetName = selected.value,
                            customPresets = presets.value,
                            equalizerEnabled = EqValuePolicy.hasSignalAlteringEffects(settings.value),
                            onBack = {},
                            onBandChanged = { index, value -> settings.value = settings.value.copy(
                                bands = settings.value.bands.mapIndexed { i, old -> if (i == index) value else old },
                            ) },
                            onPreampChanged = { settings.value = settings.value.copy(preampDb = it); selected.value = null },
                            onBassChanged = { settings.value = settings.value.copy(bass = it) },
                            onMidrangeChanged = { settings.value = settings.value.copy(midrange = it) },
                            onTrebleChanged = { settings.value = settings.value.copy(treble = it) },
                            onSpaciousnessChanged = { settings.value = settings.value.copy(spaciousness = it) },
                            onSpaciousnessModeChanged = { settings.value = settings.value.copy(spaciousnessMode = it) },
                            onReverbDurationChanged = { settings.value = settings.value.copy(reverbDurationMs = it) },
                            onReverbProfileChanged = { settings.value = settings.value.copy(reverbProfile = it) },
                            onResetReverb = { settings.value = settings.value.copy(reverbDurationMs = 0) },
                            onApplyPreset = { name, value -> settings.value = value; selected.value = name },
                            onSaveCustomPreset = { name ->
                                presets.value = presets.value.filterNot { it.name.equals(name, true) } + EqCustomPreset(name, settings.value)
                                selected.value = name
                            },
                            onDeleteCustomPreset = { name -> presets.value = presets.value.filterNot { it.name == name } },
                            onReset = { settings.value = EqSettings(); selected.value = null },
                        )
                    }
                }
            }
            val graph = node(By.res("eq_band_graph"))
            device.waitForIdle()
            val graphBounds = graph.visibleBounds
            device.swipe(graphBounds.right - 40, graphBounds.centerY(), graphBounds.left + 40, graphBounds.centerY(), 12)
            instrumentation.waitForIdleSync()
            scenario.onActivity { assertTrue(settings.value.bands.all { it == 0f }) }
            device.waitForIdle()
            val settledGraph = node(By.res("eq_band_graph")).visibleBounds
            device.swipe(settledGraph.centerX(), settledGraph.centerY(), settledGraph.centerX(), settledGraph.top + 40, 12)
            instrumentation.waitForIdleSync()
            capture("eq-bands.png")
            scenario.onActivity { assertTrue("Band drag: ${settings.value.bands}; bounds=$settledGraph", settings.value.bands.any { it > 0.1f }) }
            node(By.text("Reset")).click()
            scrollTo(By.res("eq_preamp"))
            device.waitForIdle()
            val bounds = node(By.res("eq_preamp")).visibleBounds
            repeat(3) {
                device.swipe(bounds.left + 20, bounds.centerY(), bounds.right - 20, bounds.centerY(), 8)
                device.swipe(bounds.right - 20, bounds.centerY(), bounds.left + 20, bounds.centerY(), 8)
            }
            instrumentation.waitForIdleSync()
            capture("eq-gesture.png")
            scenario.onActivity { assertTrue("Preamp after fast swipes: ${settings.value.preampDb}; bounds=$bounds", settings.value.preampDb <= -5f) }
            device.click(bounds.centerX() + (bounds.width() * 0.02f).toInt(), bounds.centerY())
            instrumentation.waitForIdleSync()
            scenario.onActivity { assertEquals(0f, settings.value.preampDb, 0f) }
            device.click(bounds.left + (bounds.width() * 0.68f).toInt(), bounds.centerY())
            instrumentation.waitForIdleSync()
            scenario.onActivity { assertTrue(settings.value.preampDb > 0f) }
            capture("eq-overview.png")

            scrollTo(By.res("eq_save_preset"), Direction.UP).click()
            node(By.clazz("android.widget.EditText")).text = "Night"
            node(By.res("eq_confirm_save")).click()
            assertTrue(device.wait(Until.gone(By.res("eq_preset_name")), 4_000L))
            scenario.onActivity { assertEquals(1, presets.value.size) }
            scrollTo(By.res("eq_save_preset"), Direction.UP).click()
            node(By.text("Replace"))
            node(By.res("eq_confirm_save")).click()
            assertTrue(device.wait(Until.gone(By.res("eq_preset_name")), 4_000L))
            scenario.onActivity { assertEquals(1, presets.value.size) }

            node(By.desc("Delete Night")).click()
            node(By.res("eq_confirm_delete"))
            device.pressBack()
            assertTrue(device.wait(Until.gone(By.res("eq_confirm_delete")), 4_000L))
            scenario.onActivity { assertEquals(1, presets.value.size) }
            node(By.desc("Delete Night")).click()
            node(By.res("eq_confirm_delete")).click()
            assertTrue(device.wait(Until.gone(By.res("eq_confirm_delete")), 4_000L))
            scenario.onActivity {
                assertTrue(presets.value.isEmpty())
                assertTrue(settings.value.preampDb > 0f)
            }

            val frame = CountDownLatch(1)
            scenario.onActivity { activity ->
                theme.value = ThemeMode.Light
                textSize.value = TextSizePreset.ExtraLarge
                activity.window.decorView.postOnAnimation {
                    activity.window.decorView.postOnAnimation { frame.countDown() }
                }
            }
            assertTrue(frame.await(3, TimeUnit.SECONDS))
            capture("eq-light-large.png")
            scrollTo(By.res("eq_reverb"))
            val reverb = node(By.res("eq_reverb")).visibleBounds
            device.click(reverb.centerX(), reverb.centerY())
            instrumentation.waitForIdleSync()
            scenario.onActivity { assertEquals(250, settings.value.reverbDurationMs) }
            capture("eq-effects.png")
        } finally {
            scenario.close()
        }
    }

    private fun node(selector: BySelector): UiObject2 =
        requireNotNull(device.wait(Until.findObject(selector), 5_000L)) { "Missing UI node: $selector" }

    private fun scrollTo(selector: BySelector, direction: Direction = Direction.DOWN): UiObject2 {
        repeat(8) {
            device.findObject(selector)?.let { return it }
            val list = node(By.res("eq_content"))
            list.setGestureMargins(8, 300, list.visibleBounds.width() - 32, 120)
            list.scroll(direction, 0.45f)
        }
        return node(selector)
    }

    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        device.waitForIdle()
        assertTrue(device.takeScreenshot(instrumentation.targetContext.cacheDir.resolve(name)))
    }
}
