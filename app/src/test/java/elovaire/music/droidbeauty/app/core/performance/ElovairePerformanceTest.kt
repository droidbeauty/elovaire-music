package elovaire.music.droidbeauty.app.core.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElovairePerformanceTest {
    @Test
    fun jankWindowsRemainBoundedAndKeepLowCardinalityState() {
        ElovairePerformance.clearJankWindows()
        repeat(20) { index ->
            ElovairePerformance.recordJankWindow(
                JankWindowSnapshot(
                    reason = "window",
                    screen = "home",
                    interaction = "idle",
                    playbackState = "paused",
                    libraryWork = "idle",
                    frameCount = index + 1,
                    jankCount = index % 2,
                    worstFrameMs = 16L,
                ),
            )
        }

        val windows = ElovairePerformance.jankWindowSnapshot()
        assertEquals(16, windows.size)
        assertEquals(5, windows.first().frameCount)
        assertTrue(windows.all { it.screen in setOf("home") })
        ElovairePerformance.clearJankWindows()
    }
}
