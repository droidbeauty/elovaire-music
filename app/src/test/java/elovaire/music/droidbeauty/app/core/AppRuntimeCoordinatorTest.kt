package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AppRuntimeCoordinatorTest {
    @Test
    fun playback_then_full_start_runs_each_phase_once() {
        val events = mutableListOf<String>()
        val coordinator = coordinator(events)

        coordinator.startPlayback()
        coordinator.startPlayback()
        coordinator.start()
        coordinator.start()

        assertEquals(listOf("playback", "start"), events)
        assertEquals(AppRuntimePhase.Started, coordinator.currentPhase())
    }

    @Test
    fun release_is_idempotent_and_blocks_later_work() {
        val events = mutableListOf<String>()
        val coordinator = coordinator(events)

        coordinator.release()
        coordinator.release()
        coordinator.startPlayback()
        coordinator.start()
        coordinator.onMemoryPressure(MemoryPressure.Critical)

        assertEquals(listOf("release"), events)
        assertEquals(AppRuntimePhase.Released, coordinator.currentPhase())
    }

    @Test
    fun failed_start_releases_the_runtime() {
        val events = mutableListOf<String>()
        val coordinator = AppRuntimeCoordinator(
            startPlaybackAction = { events += "playback" },
            startAction = {
                events += "start"
                error("startup failed")
            },
            memoryPressureAction = { events += "memory" },
            releaseAction = { events += "release" },
        )

        var failed = false
        try {
            coordinator.start()
        } catch (_: IllegalStateException) {
            // Expected: a failed startup cannot be reused.
            failed = true
        }

        assertTrue(failed)
        assertEquals(listOf("start", "release"), events)
        assertEquals(AppRuntimePhase.Released, coordinator.currentPhase())
    }

    @Test
    fun reentrant_release_is_deferred_until_the_transition_action_finishes() {
        val events = mutableListOf<String>()
        lateinit var coordinator: AppRuntimeCoordinator
        coordinator = AppRuntimeCoordinator(
            startPlaybackAction = {},
            startAction = {
                events += "start"
                coordinator.release()
                events += "after-start"
            },
            memoryPressureAction = {},
            releaseAction = { events += "release" },
        )

        coordinator.start()

        assertEquals(listOf("start", "after-start", "release"), events)
        assertEquals(AppRuntimePhase.Released, coordinator.currentPhase())
    }

    @Test
    fun concurrent_full_start_waits_for_playback_start_to_finish() {
        val playbackEntered = CountDownLatch(1)
        val releasePlayback = CountDownLatch(1)
        val fullStartEntered = CountDownLatch(1)
        val events = mutableListOf<String>()
        val coordinator = AppRuntimeCoordinator(
            startPlaybackAction = {
                events += "playback-started"
                playbackEntered.countDown()
                check(releasePlayback.await(2, TimeUnit.SECONDS))
                events += "playback-finished"
            },
            startAction = {
                fullStartEntered.countDown()
                events += "full-start"
            },
            memoryPressureAction = {},
            releaseAction = {},
        )
        val playbackThread = Thread(coordinator::startPlayback)
        val fullStartThread = Thread(coordinator::start)

        playbackThread.start()
        check(playbackEntered.await(2, TimeUnit.SECONDS))
        fullStartThread.start()
        assertFalse(fullStartEntered.await(50, TimeUnit.MILLISECONDS))
        assertEquals(listOf("playback-started"), events)

        releasePlayback.countDown()
        playbackThread.join(2_000)
        fullStartThread.join(2_000)

        assertEquals(listOf("playback-started", "playback-finished", "full-start"), events)
        assertEquals(AppRuntimePhase.Started, coordinator.currentPhase())
    }

    @Test
    @Suppress("TooGenericExceptionCaught")
    fun release_attempts_all_children_and_preserves_the_first_failure() {
        val events = mutableListOf<String>()
        val first = IllegalStateException("first")
        val second = IllegalArgumentException("second")
        val coordinator = AppRuntimeCoordinator(
            startPlaybackAction = {},
            startAction = {},
            memoryPressureAction = {},
            releaseAction = {
                releaseBestEffort(
                    { events += "one"; throw first },
                    { events += "two"; throw second },
                    { events += "three" },
                )
            },
        )

        val thrown = try {
            coordinator.release()
            null
        } catch (failure: Throwable) {
            failure
        }

        assertSame(first, thrown)
        assertEquals(listOf("one", "two", "three"), events)
        assertEquals(1, thrown?.suppressed?.size)
        coordinator.release()
        assertEquals(listOf("one", "two", "three"), events)
    }

    private fun coordinator(events: MutableList<String>): AppRuntimeCoordinator {
        return AppRuntimeCoordinator(
            startPlaybackAction = { events += "playback" },
            startAction = { events += "start" },
            memoryPressureAction = { events += "memory" },
            releaseAction = { events += "release" },
        )
    }
}
