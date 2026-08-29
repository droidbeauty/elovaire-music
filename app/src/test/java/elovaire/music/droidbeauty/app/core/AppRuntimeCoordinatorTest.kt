package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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

    private fun coordinator(events: MutableList<String>): AppRuntimeCoordinator {
        return AppRuntimeCoordinator(
            startPlaybackAction = { events += "playback" },
            startAction = { events += "start" },
            memoryPressureAction = { events += "memory" },
            releaseAction = { events += "release" },
        )
    }
}
