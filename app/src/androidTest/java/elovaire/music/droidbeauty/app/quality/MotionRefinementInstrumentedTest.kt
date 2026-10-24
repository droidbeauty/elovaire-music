package elovaire.music.droidbeauty.app.quality

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import elovaire.music.droidbeauty.app.ui.interaction.elovaireCombinedClickable
import elovaire.music.droidbeauty.app.ui.motion.MotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.MotionRuntime
import elovaire.music.droidbeauty.app.ui.motion.MotionRuntimeProvider
import elovaire.music.droidbeauty.app.ui.motion.MotionSpecs
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.elovaireContentChangeMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MotionRefinementInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Before
    fun keepRenderingActivityVisible() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).wakeUp()
        composeRule.runOnUiThread {
            composeRule.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun pressFeedbackDrawsWithoutRemeasuringOrRecomposingItsContent() {
        val source = MutableInteractionSource()
        val compositions = AtomicInteger()
        val measurements = AtomicInteger()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MotionRuntimeProvider(MotionRuntime(1f)) {
                Box(Modifier.size(120.dp).background(Color.Black).testTag("press_capture"), Alignment.Center) {
                    Box(Modifier.size(100.dp).elovaireClickable(interactionSource = source) {}.background(Color.White)
                        .layout { measurable, constraints ->
                            measurements.incrementAndGet()
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                        }) {
                        SideEffect { compositions.incrementAndGet() }
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        val initial = leftWhitePixel()
        val measured = measurements.get()
        val press = PressInteraction.Press(Offset.Zero)
        composeRule.runOnIdle { assertTrue(source.tryEmit(press)) }
        composeRule.mainClock.advanceTimeBy(32L)
        composeRule.waitForIdle()
        val composed = compositions.get()
        composeRule.mainClock.advanceTimeBy(96L)
        assertTrue("Press should visually contract the card", leftWhitePixel() > initial)
        assertEquals(measured, measurements.get())
        assertEquals(composed, compositions.get())
        composeRule.runOnIdle { assertTrue(source.tryEmit(PressInteraction.Cancel(press))) }
        composeRule.mainClock.advanceTimeBy(1_000L)
        assertEquals(initial, leftWhitePixel())
    }

    @Test
    fun rapidLayoutChangesReturnToFullOpacityWithoutChangingTheirSize() {
        val key = mutableStateOf(0)
        val measurements = AtomicInteger()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MotionRuntimeProvider(MotionRuntime(1f)) {
                Box(Modifier.size(60.dp).background(Color.Black).testTag("layout_capture")) {
                    Box(Modifier.size(60.dp).elovaireContentChangeMotion(key.value).background(Color.White)
                        .layout { measurable, constraints ->
                            measurements.incrementAndGet()
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                        })
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        val initial = measurements.get()
        for (value in listOf(1, 2, 0)) {
            composeRule.runOnIdle { key.value = value }
            composeRule.mainClock.advanceTimeBy(32L)
        }
        composeRule.mainClock.advanceTimeBy(1_000L)
        val pixels = composeRule.onNodeWithTag("layout_capture").captureToImage().toPixelMap()
        assertTrue(pixels[pixels.width / 2, pixels.height / 2].red > 0.99f)
        assertEquals(initial, measurements.get())
    }

    @Test
    fun popupMotionDoesNotRemeasureItsContentsEveryFrame() {
        val visible = mutableStateOf(false)
        val measurements = AtomicInteger()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MotionRuntimeProvider(MotionRuntime(1f)) {
                PopupCardMotionHost(visible.value, "test.refined_popup") {
                    Box(Modifier.size(200.dp).layout { measurable, constraints ->
                        measurements.incrementAndGet()
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                    })
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { visible.value = true }
        composeRule.mainClock.advanceTimeBy(64L)
        composeRule.waitForIdle()
        val initial = measurements.get()
        assertTrue("Popup content must be measured before sampling", initial > 0)
        repeat(12) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertTrue("Popup should retain measured content", measurements.get() <= initial + 2)
    }

    @Test
    fun reducedMotionRevealsRowsImmediatelyAndReusesSpecsWithinTheProvider() {
        val runtime = mutableStateOf(MotionRuntime(0f))
        lateinit var first: MotionSpecs
        lateinit var second: MotionSpecs
        composeRule.setContent {
            MotionRuntimeProvider(runtime.value) {
                first = rememberMotionSpecs()
                Box(Modifier.size(60.dp).testTag("reveal_capture").background(Color.Black)) {
                    second = rememberMotionSpecs()
                    val registry = remember { MotionRevealRegistry() }
                    Box(Modifier.size(60.dp).elovaireListReveal("row", 0, registry).background(Color.White))
                }
            }
        }
        composeRule.waitForIdle()
        assertSame(first, second)
        val image = composeRule.onNodeWithTag("reveal_capture").captureToImage().toPixelMap()
        assertTrue(image[image.width / 2, image.height / 2].red > 0.95f)
        val previous = first
        composeRule.runOnIdle { runtime.value = MotionRuntime(1f) }
        composeRule.waitForIdle()
        assertNotSame(previous, first)
        assertSame(first, second)
        assertEquals(0, previous.tween<Float>().durationMillis)
        assertTrue(first.tween<Float>().durationMillis > 0)
    }

    @Test
    fun sharedInteractionsPreserveClickLongClickAndDisabledSemantics() {
        var clicks = 0
        var longClicks = 0
        composeRule.setContent {
            Box {
                Box(Modifier.size(100.dp).testTag("combined")
                    .elovaireCombinedClickable(onLongClick = { longClicks++ }) { clicks++ })
                Box(Modifier.size(40.dp).testTag("disabled").elovaireClickable(enabled = false) { clicks++ })
            }
        }
        composeRule.onNodeWithTag("disabled").assertIsNotEnabled()
        composeRule.onNodeWithTag("combined").performClick()
        composeRule.onNodeWithTag("combined").performTouchInput { longClick() }
        composeRule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(1, longClicks)
        }
    }

    @Test
    fun exitingPopupRejectsTouchesAndReentryRestoresInteraction() {
        val visible = mutableStateOf(true)
        var clicks = 0
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MotionRuntimeProvider(MotionRuntime(1f)) {
                PopupCardMotionHost(visible.value, "test.popup_input") {
                    Box(Modifier.size(100.dp).testTag("popup_action").elovaireClickable { clicks++ })
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.runOnIdle { visible.value = false }
        composeRule.mainClock.advanceTimeBy(32L)
        composeRule.onNodeWithTag("popup_action").performTouchInput { click() }
        composeRule.runOnIdle { assertEquals(0, clicks); visible.value = true }
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.onNodeWithTag("popup_action").performTouchInput { click() }
        composeRule.runOnIdle { assertEquals(1, clicks) }
    }

    private fun leftWhitePixel(): Int {
        val pixels = composeRule.onNodeWithTag("press_capture").captureToImage().toPixelMap()
        return (0 until pixels.width).first { pixels[it, pixels.height / 2].red > 0.95f }
    }
}
