package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryChangeSetTest {
    @Test
    fun unchangedSnapshotsSkipIndexing() {
        val first = song()
        val second = first.copy(id = 2L, uri = TestUri("content://media/external/audio/media/2"))
        val previous = listOf(first, second)
        val next = listOf(first.copy(), second.copy())

        assertSame(LibraryChangeSet.Empty, LibraryChangeSetCalculator.between(previous, next))
        assertEquals(
            listOf("Changed"),
            LibraryChangeSetCalculator.between(previous, listOf(first, second.copy(title = "Changed")))
                .updated.map { it.after.title },
        )
    }

    @Test
    fun metadataEditIsUpdatedWithoutBecomingAddAndRemove() {
        val before = song(title = "Before")
        val after = before.copy(title = "After")

        val changes = LibraryChangeSetCalculator.between(listOf(before), listOf(after))

        assertEquals(emptyList<Song>(), changes.added)
        assertEquals(emptyList<Song>(), changes.removed)
        assertEquals(listOf(after), changes.updated.map { it.after })
        assertTrue(changes.relocated.isEmpty())
        assertEquals(setOf(before.albumId), changes.affectedAlbumIds)
    }

    @Test
    fun artworkLocatorChangeIsAnUpdateNotAUserDataRelocation() {
        val before = song()
        val after = before.copy(artUri = TestUri("content://media/external/audio/albumart/2"))

        val changes = LibraryChangeSetCalculator.between(listOf(before), listOf(after))

        assertTrue(changes.relocated.isEmpty())
        assertEquals(listOf(after), changes.updated.map { it.after })
        assertEquals(
            setOf("content://art/1", "content://media/external/audio/albumart/2"),
            changes.artworkInvalidatedUris,
        )
    }

    @Test
    fun stableProviderIdentityTurnsMoveIntoRelocation() {
        val before = song()
        val after = before.copy(
            fileName = "renamed.flac",
            libraryPath = "/music/renamed.flac",
        )

        val changes = LibraryChangeSetCalculator.between(listOf(before), listOf(after))

        assertTrue(changes.added.isEmpty())
        assertTrue(changes.removed.isEmpty())
        assertEquals(listOf(after), changes.relocated.map { it.after })
        assertTrue(changes.updated.isEmpty())
    }

    @Test
    fun providerReindexWithSamePathDoesNotBecomeAddAndRemove() {
        val before = song(uri = "content://media/external/audio/media/1")
        val after = before.copy(uri = TestUri("content://media/external/audio/media/2"), id = 2L)

        val changes = LibraryChangeSetCalculator.between(listOf(before), listOf(after))

        assertTrue(changes.added.isEmpty())
        assertTrue(changes.removed.isEmpty())
        assertEquals(listOf(after), changes.relocated.map { it.after })
        assertFalse(changes.isEmpty)
    }

    @Test
    fun providerReindexMatchesNormalizedPathsAmongUnchangedSongs() {
        val before = song().copy(libraryPath = "  \\music\\track.flac  ")
        val after = before.copy(id = 2L, uri = TestUri("content://media/external/audio/media/2"), libraryPath = "/music/track.flac")
        val unchanged = song(uri = "content://media/external/audio/media/3").copy(id = 3L, libraryPath = "/music/other.flac")

        val changes = LibraryChangeSetCalculator.between(listOf(unchanged, before), listOf(unchanged, after))

        assertEquals(listOf(LibrarySongRelocation(before, after)), changes.relocated)
        assertTrue(changes.added.isEmpty())
        assertTrue(changes.removed.isEmpty())
        assertTrue(changes.updated.isEmpty())
    }

    private fun song(
        title: String = "Track",
        uri: String = "content://media/external/audio/media/1",
    ) = Song(
        id = 1L,
        title = title,
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "FLAC",
        audioQuality = null,
        fileName = "track.flac",
        albumId = 2L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        dateModifiedSeconds = 1L,
        libraryPath = "/music/track.flac",
        uri = TestUri(uri),
        artUri = TestUri("content://art/1"),
        metadataResolved = true,
    )
}
