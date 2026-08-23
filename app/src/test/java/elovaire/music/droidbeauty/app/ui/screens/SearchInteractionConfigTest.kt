package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchInteractionConfigTest {
    @Test
    fun blankQueryCollapsesAllSongsAndSortOptions() {
        val normalized = SearchInteractionConfig(
            query = "   ",
            showAllSongs = true,
            showSortOptions = true,
        ).normalized()

        assertFalse(normalized.showAllSongs)
        assertFalse(normalized.showSortOptions)
    }

    @Test
    fun sortOptionsRequireAllSongs() {
        val normalized = SearchInteractionConfig(
            query = "radiohead",
            showAllSongs = false,
            showSortOptions = true,
        ).normalized()

        assertFalse(normalized.showSortOptions)
    }

    @Test
    fun allSongsRetainsSortOptionsForNonBlankQuery() {
        val normalized = SearchInteractionConfig(
            query = "radiohead",
            showAllSongs = true,
            showSortOptions = true,
        ).normalized()

        assertTrue(normalized.showAllSongs)
        assertTrue(normalized.showSortOptions)
    }

    @Test
    fun resultKeyRejectsOlderQueryGeneration() {
        val config = SearchInteractionConfig(
            query = "radiohead",
            queryGeneration = 4L,
        )
        val staleKey = SearchResultKey(
            queryGeneration = 3L,
            sortMode = SearchSongSortMode.Title,
            includeAllSongs = false,
            indexRevision = "library-1",
        )

        assertFalse(staleKey.matches(config, indexRevision = "library-1"))
    }

    @Test
    fun resultKeyRejectsSortAndIndexChanges() {
        val config = SearchInteractionConfig(
            query = "radiohead",
            showAllSongs = true,
            sortMode = SearchSongSortMode.Artist,
            queryGeneration = 4L,
        )
        val key = SearchResultKey(
            queryGeneration = 4L,
            sortMode = SearchSongSortMode.Title,
            includeAllSongs = true,
            indexRevision = "library-1",
        )

        assertFalse(key.matches(config, indexRevision = "library-1"))
        assertFalse(
            key.copy(sortMode = SearchSongSortMode.Artist)
                .matches(config, indexRevision = "library-2"),
        )
        assertTrue(
            key.copy(sortMode = SearchSongSortMode.Artist)
                .matches(config, indexRevision = "library-1"),
        )
    }
}
