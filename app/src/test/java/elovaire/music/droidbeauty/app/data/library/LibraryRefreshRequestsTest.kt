package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun backgroundRefreshesMergeInsteadOfReplacingEarlierWork() {
        assertEquals(
            LibraryRefreshRequest(
                enrichMetadata = true,
                targetedPaths = listOf("/music/a.mp3", "/music/b.mp3"),
            ),
            mergeBackgroundRefreshRequest(
                existing = LibraryRefreshRequest(targetedPaths = listOf("/music/a.mp3")),
                incoming = LibraryRefreshRequest(
                    enrichMetadata = true,
                    targetedPaths = listOf("/music/b.mp3"),
                ),
            ),
        )
    }

    @Test
    fun tooManyTargetedPaths_fallsBackToFullProviderReconciliationWithoutIndexWalk() {
        val requests = LibraryRefreshRequests()

        requests.enqueue(
            targetedPaths = (1..65).map { index -> "/music/$index.mp3" },
        )

        assertEquals(
            LibraryRefreshRequest(forceMediaIndex = false),
            requests.takePendingAfterScan(),
        )
    }

    @Test
    fun tooManyPaths_doNotBroadenAnUnrelatedNetworkRefresh() {
        val merged = LibraryRefreshRequest(
            targetedNetworkSourceIds = setOf("nas-a"),
        ).mergedWith(
            LibraryRefreshRequest(
                targetedPaths = (1..65).map { "/music/$it.mp3" },
                targetedNetworkSourceIds = emptySet(),
            ),
        )

        assertEquals(setOf("nas-a"), merged.targetedNetworkSourceIds)
        assertTrue(merged.targetedPaths.isEmpty())
        assertTrue(merged.targetedSafTreeIds == null)
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

    @Test
    fun sourceTargetedRefreshes_mergeWithoutEscalatingToAllSources() {
        val merged = LibraryRefreshRequest(targetedNetworkSourceIds = setOf("nas-a"))
            .mergedWith(LibraryRefreshRequest(targetedNetworkSourceIds = setOf("nas-b")))

        assertEquals(setOf("nas-a", "nas-b"), merged.targetedNetworkSourceIds)
    }

    @Test
    fun localReuseIsDroppedWhenAnotherRefreshIsCoalesced() {
        val merged = LibraryRefreshRequest(
            targetedPaths = listOf("/storage/emulated/0/Audiobooks"),
            targetedNetworkSourceIds = emptySet(),
            reuseLocalState = true,
        ).mergedWith(LibraryRefreshRequest(enrichMetadata = true))

        assertFalse(merged.reuseLocalState)
    }

    @Test
    fun localReuseSurvivesNormalizationForTargetedSafDiscovery() {
        val normalized = LibraryRefreshRequest(
            targetedSafTreeIds = setOf(" provider | tree/a "),
            targetedNetworkSourceIds = emptySet(),
            reuseLocalState = true,
        ).normalized()

        assertTrue(normalized.reuseLocalState)
        assertEquals(setOf("provider | tree/a"), normalized.targetedSafTreeIds)
        assertTrue(normalized.targetedPaths.isEmpty())
    }

    @Test
    fun targetedSafTreeRefreshes_mergeWithoutBroadeningToAllTrees() {
        val merged = LibraryRefreshRequest(
            targetedSafTreeIds = setOf("provider|tree/a"),
            targetedNetworkSourceIds = emptySet(),
            reuseLocalState = true,
        ).mergedWith(
            LibraryRefreshRequest(
                targetedSafTreeIds = setOf("provider|tree/b"),
                targetedNetworkSourceIds = emptySet(),
                reuseLocalState = true,
            ),
        )

        assertEquals(setOf("provider|tree/a", "provider|tree/b"), merged.targetedSafTreeIds)
        assertEquals(emptySet<String>(), merged.targetedNetworkSourceIds)
        assertTrue(merged.reuseLocalState)
    }

    @Test
    fun targetedSafRefresh_reusesPublishedMediaStoreState() {
        assertTrue(
            shouldReusePublishedMediaStoreState(
                targetedSafTreeIds = setOf("provider|tree/a"),
                hasBaseSnapshot = true,
                refreshMediaIndex = false,
                enrichMetadata = false,
            ),
        )
        assertFalse(
            shouldReusePublishedMediaStoreState(
                targetedSafTreeIds = setOf("provider|tree/a"),
                hasBaseSnapshot = true,
                refreshMediaIndex = false,
                enrichMetadata = true,
            ),
        )
    }

    @Test
    fun fullSafRefresh_supersedesTargetedSafTreeRefresh() {
        val merged = LibraryRefreshRequest(targetedSafTreeIds = setOf("provider|tree/a"))
            .mergedWith(LibraryRefreshRequest())

        assertNull(merged.targetedSafTreeIds)
    }

    @Test
    fun fullRefreshSupersedesSourceTargetedRefresh() {
        val merged = LibraryRefreshRequest(targetedNetworkSourceIds = setOf("nas-a"))
            .mergedWith(LibraryRefreshRequest())

        assertNull(merged.targetedNetworkSourceIds)
    }

    @Test
    fun resolveTargetedRefreshPaths_usesKnownSongPathWithoutFullScan() {
        val paths = resolveTargetedRefreshPaths(
            requestedPaths = emptyList(),
            songIds = listOf(7L),
            currentSongs = listOf(song(7L, "/music/track.mp3"), song(8L, "/music/other.mp3")),
        )

        assertEquals(listOf("/music/track.mp3"), paths)
    }

    @Test
    fun resolveTargetedRefreshPaths_deduplicatesExplicitAndResolvedPaths() {
        val paths = resolveTargetedRefreshPaths(
            requestedPaths = listOf(" /music/track.mp3 "),
            songIds = listOf(7L),
            currentSongs = listOf(song(7L, "/music/track.mp3")),
        )

        assertEquals(listOf("/music/track.mp3"), paths)
    }

    @Test
    fun targetedMediaPathOnlyScansTheContainingSafTree() {
        val tree = LibraryFolderSelection(
            uri = TestUri("content://provider/tree/a"),
            path = "/storage/emulated/0/Music/A",
            displayName = "A",
        )

        assertTrue(shouldScanSafTreeForPaths(tree, listOf("/storage/emulated/0/Music/A/track.flac")))
        assertFalse(shouldScanSafTreeForPaths(tree, listOf("/storage/emulated/0/Music/B/track.flac")))
    }

    @Test
    fun uriOnlySafTreeIsNotScannedForAnUnattributedFilePath() {
        val tree = LibraryFolderSelection(
            uri = TestUri("content://provider/tree/a"),
            path = "content://provider/tree/a",
            displayName = "A",
        )

        assertFalse(shouldScanSafTreeForPaths(tree, listOf("/storage/emulated/0/Music/track.flac")))
    }

    private fun song(id: Long, path: String) = Song(
        id = id,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "track.mp3",
        albumId = 2L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        dateModifiedSeconds = 1L,
        libraryPath = path,
        uri = TestUri("content://media/external/audio/media/$id"),
        artUri = null,
    )
}
