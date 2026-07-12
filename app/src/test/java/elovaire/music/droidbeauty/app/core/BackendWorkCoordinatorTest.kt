package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRefreshRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendWorkCoordinatorTest {
    @Test
    fun mergesTargetedRefreshes() {
        val coordinator = BackendWorkCoordinator()
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(LibraryRefreshRequest(targetedPaths = listOf("a"))))
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(LibraryRefreshRequest(targetedPaths = listOf("b"))))

        assertEquals(listOf("a", "b"), coordinator.takeLibraryRefresh()?.targetedPaths)
    }

    @Test
    fun fullRefreshSupersedesTargetedRefresh() {
        val coordinator = BackendWorkCoordinator()
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(LibraryRefreshRequest(targetedPaths = listOf("a"))))
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(LibraryRefreshRequest(forceMediaIndex = true)))

        val request = coordinator.takeLibraryRefresh()
        assertTrue(request?.forceMediaIndex == true)
        assertTrue(request?.targetedPaths?.isEmpty() == true)
    }

    @Test
    fun newestLyricsPrefetchSupersedesObsoleteQueue() {
        val coordinator = BackendWorkCoordinator()
        coordinator.enqueue(BackendWorkRequest.LyricsPrefetch(listOf(1L, 2L), userInitiated = false))
        coordinator.enqueue(BackendWorkRequest.LyricsPrefetch(listOf(3L), userInitiated = true))

        assertEquals(listOf(3L), coordinator.takeLyricsPrefetch()?.songIds)
    }

    @Test
    fun releaseRejectsFurtherWork() {
        val coordinator = BackendWorkCoordinator()
        coordinator.release()
        coordinator.enqueue(BackendWorkRequest.LibraryRefresh(LibraryRefreshRequest(forceMediaIndex = true)))

        assertNull(coordinator.takeLibraryRefresh())
    }

    @Test
    fun automaticUpdateWaitsForStartupToSettle() {
        val coordinator = BackendWorkCoordinator()
        coordinator.enqueue(BackendWorkRequest.AutomaticUpdateCheck)

        assertEquals(false, coordinator.takeAutomaticUpdateCheck(startupSettled = false))
        assertEquals(true, coordinator.takeAutomaticUpdateCheck(startupSettled = true))
        assertEquals(false, coordinator.takeAutomaticUpdateCheck(startupSettled = true))
    }
}
