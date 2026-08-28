package elovaire.music.droidbeauty.app.quality

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.motion.MotionVisibilityHost
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidMotionInterleavingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun visibilityReversalBeforeExitSettlesDoesNotReportAStaleExit() {
        val visible = mutableStateOf(true)
        val exitCallbacks = AtomicInteger()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MotionVisibilityHost(
                visible = visible.value,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)),
                onExitFinished = { exitCallbacks.incrementAndGet() },
            ) {
                Box(modifier = Modifier.size(20.dp).testTag("motion_surface"))
            }
        }

        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(500L)
        composeRule.onNodeWithTag("motion_surface").assertIsDisplayed()
        composeRule.runOnIdle { visible.value = false }
        composeRule.mainClock.advanceTimeBy(25L)
        composeRule.runOnIdle { visible.value = true }
        composeRule.mainClock.advanceTimeBy(500L)
        assertEquals(0, exitCallbacks.get())
        composeRule.onNodeWithTag("motion_surface").assertIsDisplayed()
    }
}
