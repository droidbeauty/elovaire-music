package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
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
