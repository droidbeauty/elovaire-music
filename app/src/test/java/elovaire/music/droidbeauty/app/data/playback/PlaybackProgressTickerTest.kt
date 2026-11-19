package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackProgressTickerTest {
    @Test
    fun slowTickWaitsBeforePollingAgain() = runTest {
        val ticks = mutableListOf<Long>()
        var simulatedWorkMs = 0L
        val ticker = PlaybackProgressTicker(
            scope = this,
            intervalMs = { 100L },
            elapsedTimeMs = { testScheduler.currentTime + simulatedWorkMs },
            onTick = {
                ticks += testScheduler.currentTime
                if (ticks.size == 1) simulatedWorkMs = 150L
                ticks.size < 2
            },
        )

        ticker.start()
        runCurrent()
        assertEquals(listOf(0L), ticks)
        advanceTimeBy(99L)
        runCurrent()
        assertEquals(listOf(0L), ticks)
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(listOf(0L, 100L), ticks)
    }
}
