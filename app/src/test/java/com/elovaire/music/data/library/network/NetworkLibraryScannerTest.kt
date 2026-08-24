package elovaire.music.droidbeauty.app.data.library.network

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkLibraryScannerTest {
    @Test
    fun partialListingPreservesUnseenCachedEntries() {
        val cached = listOf(inventory("old.mp3", 1L), inventory("kept.mp3", 2L))
        val discovered = listOf(inventory("new.mp3", 3L))

        val songs = mergePartialNetworkInventory(cached, discovered)

        assertEquals(listOf(1L, 2L, 3L), songs.map(Song::id))
    }

    @Test
    fun partialListingReplacesCachedEntryOnlyWhenPathWasObserved() {
        val cached = listOf(inventory("track.mp3", 1L))
        val discovered = listOf(inventory("track.mp3", 9L))

        val songs = mergePartialNetworkInventory(cached, discovered)

        assertEquals(listOf(9L), songs.map(Song::id))
    }

    @Test
    fun revisionIndexPreservesUniqueRelocationAndRejectsAmbiguity() {
        val unique = inventory("old.mp3", 1L).copy(
            entry = NetworkFileEntry("old.mp3", false, sizeBytes = 42L, etag = "etag-a"),
        )
        val ambiguous = inventory("other.mp3", 2L).copy(
            entry = NetworkFileEntry("other.mp3", false, sizeBytes = 42L, etag = "etag-a"),
        )

        val uniqueIndex = buildNetworkRevisionIndex(listOf(unique))
        val ambiguousIndex = buildNetworkRevisionIndex(listOf(unique, ambiguous))

        assertEquals(unique, uniqueIndex[NetworkRevisionKey(42L, "etag-a")])
        assertEquals(null, ambiguousIndex[NetworkRevisionKey(42L, "etag-a")])
    }

    @Test
    fun webDavContentRangeParserRejectsMalformedAndPreservesUnknownTotal() {
        assertEquals(
            WebDavContentRange(10L, 19L, 100L),
            parseWebDavContentRange("bytes 10-19/100"),
        )
        assertEquals(null, parseWebDavContentRange("bytes 10-19"))
        assertEquals(null, parseWebDavContentRange("bytes 19-10/100"))
        assertEquals(null, parseWebDavContentRange("bytes 10-19/not-a-size"))
        assertEquals(null, parseWebDavContentRange("bytes 10-19/*")?.totalLength)
    }

    private fun inventory(path: String, id: Long): NetworkInventoryEntry =
        NetworkInventoryEntry(
            entry = NetworkFileEntry(path = path, isDirectory = false),
            song = Song(
                id = id,
                title = path,
                isExplicit = false,
                artist = "Artist",
                album = "Album",
                releaseYear = null,
                genre = "Genre",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = path,
                albumId = 1L,
                durationMs = 1_000L,
                trackNumber = 1,
                discNumber = 1,
                dateAddedSeconds = 1L,
                uri = TestUri("smb://source/$path"),
                artUri = null,
            ),
        )
}
