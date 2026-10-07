package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.testing.testSong
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaStoreDeltaMergeTest {
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
}
