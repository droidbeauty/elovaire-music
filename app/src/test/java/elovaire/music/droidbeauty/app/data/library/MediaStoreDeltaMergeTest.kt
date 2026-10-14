package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.testing.testSong
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaStoreDeltaMergeTest {
    @Test
    fun albumSortTreatsMissingDiscNumberAsDiscOne() {
        val unknownDisc = testSong(id = 1L).copy(discNumber = 0, trackNumber = 2)
        val secondDisc = testSong(id = 2L).copy(discNumber = 2, trackNumber = 1)
        val firstDisc = testSong(id = 3L).copy(discNumber = 1, trackNumber = 3)

        assertEquals(
            listOf(unknownDisc, firstDisc, secondDisc),
            sortAlbumSongs(listOf(secondDisc, firstDisc, unknownDisc)),
        )
    }

    @Test
    fun albumSortKeepsStableInputOrderForCaseInsensitiveFileNameTies() {
        val lowerCase = testSong(id = 1L).copy(fileName = "song.mp3", trackNumber = 1)
        val upperCase = testSong(id = 2L).copy(fileName = "SONG.mp3", trackNumber = 1)

        assertEquals(listOf(upperCase, lowerCase), sortAlbumSongs(listOf(upperCase, lowerCase)))
    }

    @Test
    fun deltaReplacesRetainsAndAppendsByStableIdentity() {
        val original = testSong(id = 1L, title = "Original")
        val retained = testSong(id = 2L, title = "Retained")
        val replacement = original.copy(title = "Replacement")
        val added = testSong(id = 3L, title = "Added")
        val currentIdentityKeys = setOf(
            MediaIdentityResolver.stableKey(replacement),
            MediaIdentityResolver.stableKey(retained),
            MediaIdentityResolver.stableKey(added),
        )

        val result = mergeMediaStoreDelta(
            baseSongs = listOf(original, retained),
            changedSongs = listOf(replacement, added),
            changedRowIdentityKeys = setOf(MediaIdentityResolver.stableKey(replacement)),
            currentIdentityKeys = currentIdentityKeys,
        )

        assertEquals(listOf(replacement, retained, added), result.songs)
        assertEquals(currentIdentityKeys, result.retainedIdentityKeys)
    }

    @Test
    fun deltaDropsBaseRowsMissingFromCurrentIdentitySet() {
        val retained = testSong(id = 1L)
        val deleted = testSong(id = 2L)
        val retainedKey = MediaIdentityResolver.stableKey(retained)

        val result = mergeMediaStoreDelta(
            baseSongs = listOf(retained, deleted),
            changedSongs = emptyList(),
            changedRowIdentityKeys = emptySet(),
            currentIdentityKeys = setOf(retainedKey),
        )

        assertEquals(listOf(retained), result.songs)
        assertEquals(setOf(retainedKey), result.retainedIdentityKeys)
    }

    @Test
    fun deltaRetainsCurrentBaseRowWhenChangedRowCouldNotBeProcessed() {
        val existing = testSong(id = 1L)
        val key = MediaIdentityResolver.stableKey(existing)

        val result = mergeMediaStoreDelta(
            baseSongs = listOf(existing),
            changedSongs = emptyList(),
            changedRowIdentityKeys = emptySet(),
            currentIdentityKeys = setOf(key),
        )

        assertEquals(listOf(existing), result.songs)
        assertEquals(setOf(key), result.retainedIdentityKeys)
    }

    @Test
    fun deltaDropsChangedRowsThatAreNoLongerAcceptedByLibraryFilters() {
        val movedOutsideLibrary = testSong(id = 1L)
        val unchanged = testSong(id = 2L)
        val movedKey = MediaIdentityResolver.stableKey(movedOutsideLibrary)
        val unchangedKey = MediaIdentityResolver.stableKey(unchanged)

        val result = mergeMediaStoreDelta(
            baseSongs = listOf(movedOutsideLibrary, unchanged),
            changedSongs = emptyList(),
            changedRowIdentityKeys = setOf(movedKey),
            currentIdentityKeys = setOf(movedKey, unchangedKey),
        )

        assertEquals(listOf(unchanged), result.songs)
        assertEquals(setOf(unchangedKey), result.retainedIdentityKeys)
    }

    @Test
    fun deltaDeduplicatesChangedRowsWithTheSameStableIdentity() {
        val first = testSong(id = 1L, title = "Old title")
        val second = first.copy(title = "Updated title")
        val key = MediaIdentityResolver.stableKey(first)

        val result = mergeMediaStoreDelta(
            baseSongs = emptyList(),
            changedSongs = listOf(first, second),
            changedRowIdentityKeys = setOf(key),
            currentIdentityKeys = setOf(key),
        )

        assertEquals(listOf(second), result.songs)
        assertEquals(setOf(key), result.retainedIdentityKeys)
    }
}
