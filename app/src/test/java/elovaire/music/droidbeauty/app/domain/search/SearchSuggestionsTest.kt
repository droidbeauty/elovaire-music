package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSuggestionsTest {
    @Test
    fun buildSuggestedAlbums_prefersLowestPlayCountsThenFillsWithUnplayedAlbums() {
        val albums = (1L..8L).map { id ->
            Album(
                id = id,
                title = "Album $id",
                artist = "Artist $id",
                artUri = null,
                songCount = 0,
                durationMs = 0L,
                songs = emptyList(),
            )
        }
        val searchableAlbums = albums.map(Album::toSearchableAlbum)

        assertEquals(
            listOf(2L, 1L, 3L, 4L, 5L, 6L),
            buildSuggestedAlbums(
                albums = searchableAlbums,
                albumPlayCounts = mapOf(1L to 2, 2L to 1, 3L to 3),
                recentAlbumIds = emptyList(),
            ).map(Album::id),
        )
    }
}
