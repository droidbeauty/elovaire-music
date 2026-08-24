package elovaire.music.droidbeauty.app.data.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackSleepTimerTest {
    @Test
    fun timerUsesVirtualTimeAndFiresOnce() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var fired = 0
        var pauseAtEndOfSong = false
        val controller = PlaybackSleepTimerController(
            scope = CoroutineScope(dispatcher),
            elapsedRealtimeMs = { 0L },
            onTimerFired = { fired += 1 },
            setPauseAtEndOfMediaItems = { pauseAtEndOfSong = it },
        )

        controller.setTimer(SleepTimerOption.TenMinutes, currentSongId = 1L)
        assertEquals(SleepTimerOption.TenMinutes, controller.state.value.option)
        testScheduler.advanceTimeBy(SleepTimerOption.TenMinutes.durationMs ?: 0L)
        testScheduler.runCurrent()

        assertEquals(1, fired)
        assertEquals(SleepTimerOption.Off, controller.state.value.option)
        assertTrue(!pauseAtEndOfSong)
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun endOfSongOnlyFiresForTheTargetSong() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var fired = 0
        val controller = PlaybackSleepTimerController(
            scope = CoroutineScope(dispatcher),
            elapsedRealtimeMs = { 0L },
            onTimerFired = { fired += 1 },
            setPauseAtEndOfMediaItems = {},
        )

        controller.setTimer(SleepTimerOption.EndOfSong, currentSongId = 7L)
        controller.onEndOfSongReached(currentSongId = 8L)
        assertEquals(0, fired)
        controller.onEndOfSongReached(currentSongId = 7L)
        assertEquals(1, fired)
    }

    @Test
    fun replacingTimerInvalidatesLateCompletion() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var fired = 0
        val controller = PlaybackSleepTimerController(
            scope = CoroutineScope(dispatcher),
            elapsedRealtimeMs = { 0L },
            onTimerFired = { fired += 1 },
            setPauseAtEndOfMediaItems = {},
        )

        controller.setTimer(SleepTimerOption.TenMinutes, currentSongId = 1L)
        controller.setTimer(SleepTimerOption.TwentyMinutes, currentSongId = 1L)
        testScheduler.advanceTimeBy(SleepTimerOption.TenMinutes.durationMs ?: 0L)
        testScheduler.runCurrent()
        assertEquals(0, fired)
    }
}
