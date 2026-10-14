package elovaire.music.droidbeauty.app.quality

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.core.performance.MotionDiagnosticRecorder
import elovaire.music.droidbeauty.app.ui.motion.MotionRuntime
import elovaire.music.droidbeauty.app.ui.motion.MotionRuntimeProvider
import elovaire.music.droidbeauty.app.ui.motion.MotionVisibilityHost
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RapidMotionInterleavingTest {
    @Test
    fun visibilityReversalBeforeExitSettlesDoesNotReportAStaleExit() {
        val visible = mutableStateOf(true)
        val reentering = mutableStateOf(false)
        val initialVisible = CountDownLatch(1)
        val exitStarted = CountDownLatch(1)
        val reenteredVisible = CountDownLatch(1)
        val exitCallbacks = AtomicInteger()
        val motionMarker = MotionDiagnosticRecorder.sequenceMarker()
        val scenario = ActivityScenario.launch(PlaylistTestActivity::class.java)
        MotionDiagnosticRecorder.recordInteractionState("navigation")

        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    val transitions = rememberMotionTransitions()
                    MotionVisibilityHost(
                        visible = visible.value,
                        surfaceId = "test.motion_reversal",
                        enter = transitions.standardEnter(),
                        exit = transitions.standardExit(),
                        onExitFinished = { exitCallbacks.incrementAndGet() },
                    ) {
                        val visibilityTransition = transition
                        LaunchedEffect(visible.value, reentering.value, visibilityTransition.isRunning) {
                            if (!visibilityTransition.isRunning) {
                                if (reentering.value && visible.value) {
                                    reenteredVisible.countDown()
                                } else if (visible.value) {
                                    initialVisible.countDown()
                                }
                            } else if (!visible.value) {
                                exitStarted.countDown()
                            }
                        }
                        Box(modifier = Modifier.size(20.dp))
                    }
                }
            }

            assertTrue("initial enter did not settle", initialVisible.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            scenario.onActivity { visible.value = false }
            assertTrue("exit did not start", exitStarted.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            scenario.onActivity {
                reentering.value = true
                visible.value = true
            }
            assertTrue("reversed enter did not settle", reenteredVisible.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))

            assertEquals(0, exitCallbacks.get())
            val motionEvents = MotionDiagnosticRecorder.eventsSince(motionMarker).second
            assertTrue(motionEvents.any { it.surfaceId == "test.motion_reversal" && it.phase == "motion_reversed" })
            assertTrue(
                motionEvents.any {
                    it.surfaceId == "test.motion_reversal" && it.phase == "motion_settled" && it.visible == true
                },
            )
        } finally {
            MotionDiagnosticRecorder.recordInteractionState(null)
            scenario.close()
        }
    }

    @Test
    fun zeroMotionScaleRemovesContentAndReportsOneCompletedExit() {
        val visible = mutableStateOf(true)
        val contentComposed = CountDownLatch(1)
        val contentDisposed = CountDownLatch(1)
        val exitFinished = CountDownLatch(1)
        val exitCallbacks = AtomicInteger()
        val scenario = ActivityScenario.launch(PlaylistTestActivity::class.java)

        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    MotionRuntimeProvider(runtime = MotionRuntime(durationScale = 0f)) {
                        val transitions = rememberMotionTransitions()
                        MotionVisibilityHost(
                            visible = visible.value,
                            surfaceId = "test.zero_motion",
                            enter = transitions.standardEnter(),
                            exit = transitions.standardExit(),
                            onExitFinished = {
                                exitCallbacks.incrementAndGet()
                                exitFinished.countDown()
                            },
                        ) {
                            SideEffect { contentComposed.countDown() }
                            DisposableEffect(Unit) {
                                onDispose { contentDisposed.countDown() }
                            }
                            Box(modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            assertTrue("visible content was not composed", contentComposed.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            scenario.onActivity { visible.value = false }
            assertTrue("zero-scale exit did not dispose content", contentDisposed.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertTrue("zero-scale exit callback did not complete", exitFinished.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(1, exitCallbacks.get())
        } finally {
            scenario.close()
        }
    }

    private companion object {
        const val TEST_TIMEOUT_SECONDS = 3L
    }
}
