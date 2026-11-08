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
    @Test
    fun suggestionsMatchStableFullSortWithDuplicateIdsAndMixedCounts() {
        val random = kotlin.random.Random(219)
        repeat(100) {
            val albums = List(120) { index ->
                Album(
                    id = random.nextLong(1, 70),
                    title = "Title ${random.nextInt(8)}-$index",
                    artist = "Artist ${random.nextInt(6)}",
                    artUri = null,
                    songCount = 0,
                    durationMs = 0,
                    songs = emptyList(),
                ).toSearchableAlbum()
            }
            val counts = (1L..70L).associateWith { random.nextInt(-1, 5) }
            val recent = List(20) { random.nextLong(1, 70) }
            val recentSet = recent.toSet()
            val expected = listOf(true, false).flatMap { played ->
                albums.withIndex().filter { (_, item) ->
                    val count = counts[item.album.id] ?: 0
                    if (played) count > 0 else count == 0
                }.sortedWith(
                    compareBy<IndexedValue<SearchableAlbum>> { if (played) counts[it.value.album.id] ?: 0 else 0 }
                        .thenBy { it.value.album.id in recentSet }
                        .thenBy { it.value.normalizedArtist }
                        .thenBy { it.value.normalizedTitle }
                        .thenBy { it.index },
                ).map { it.value.album }
            }.distinctBy(Album::id).take(6)
            assertEquals(expected, buildSuggestedAlbums(albums, counts, recent))
        }
    }

}
