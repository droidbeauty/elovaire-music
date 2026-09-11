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
            currentIdentityKeys = setOf(retainedKey),
        )

        assertEquals(listOf(retained), result.songs)
        assertEquals(setOf(retainedKey), result.retainedIdentityKeys)
    }
}
