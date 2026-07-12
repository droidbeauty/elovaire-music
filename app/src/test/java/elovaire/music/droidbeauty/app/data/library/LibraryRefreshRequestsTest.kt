package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryRefreshRequestsTest {
    @Test
    fun takeForImmediateScan_mergesPendingRequest() {
        val requests = LibraryRefreshRequests()
        requests.enqueue(
            LibraryRefreshRequest(
                enrichMetadata = true,
                targetedPaths = listOf(" /music/a.mp3 ", "", "/music/a.mp3"),
            ),
        )

        val result = requests.takeForImmediateScan(
            LibraryRefreshRequest(targetedPaths = listOf("/music/b.mp3")),
        )

        assertEquals(
            LibraryRefreshRequest(
                forceMediaIndex = false,
                enrichMetadata = true,
                targetedPaths = listOf("/music/b.mp3", "/music/a.mp3"),
            ),
            result,
        )
        assertNull(requests.takePendingAfterScan())
    }

    @Test
    fun forceMediaIndex_dropsTargetedPaths() {
        val requests = LibraryRefreshRequests()
        requests.enqueue(targetedPaths = listOf("/music/a.mp3"))
        requests.enqueue(forceMediaIndex = true, targetedPaths = listOf("/music/b.mp3"))

        assertEquals(
            LibraryRefreshRequest(forceMediaIndex = true),
            requests.takePendingAfterScan(),
        )
    }

    @Test
    fun tooManyTargetedPaths_fallsBackToFullFastScan() {
        val requests = LibraryRefreshRequests()

        requests.enqueue(
            targetedPaths = (1..65).map { index -> "/music/$index.mp3" },
        )

        assertEquals(
            LibraryRefreshRequest(forceMediaIndex = true),
            requests.takePendingAfterScan(),
        )
    }

    @Test
    fun clearIndexRefresh_keepsPendingMetadataEnrichmentOnly() {
        val requests = LibraryRefreshRequests()
        requests.enqueue(
            LibraryRefreshRequest(
                forceMediaIndex = true,
                enrichMetadata = true,
                targetedPaths = listOf("/music/a.mp3"),
            ),
        )

        requests.clearIndexRefresh()

        assertEquals(
            LibraryRefreshRequest(enrichMetadata = true),
            requests.takePendingAfterScan(),
        )
    }
}
