package elovaire.music.droidbeauty.app.data.smartplaylists

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartPlaylistEngineTest {
    private val engine = SmartPlaylistEngine()

    @Test
    fun resolve_matchesAllRulesAndSortsBeforeLimit() {
        val playlist = SmartPlaylist(
            id = 1L,
            name = "Rock favorites",
            matchMode = SmartPlaylistMatchMode.All,
            rules = listOf(
                SmartPlaylistRule.GenreMatches("rock", TextRuleMode.Contains),
                SmartPlaylistRule.FavoriteIs(true),
            ),
            sort = SmartPlaylistSort(SmartPlaylistSortField.PlayCount, SortDirection.Descending),
            limit = 1,
            createdAtMs = 1L,
            updatedAtMs = 1L,
        )

        val result = engine.resolve(
            definition = playlist,
            songs = listOf(
                song(1L, title = "B", genre = "Rock"),
                song(2L, title = "A", genre = "Rock"),
                song(3L, title = "C", genre = "Jazz"),
            ),
            favoriteSongIds = setOf(1L, 2L),
            playCounts = mapOf(1L to 2, 2L to 7),
            recentSongIds = emptyList(),
        )

        assertEquals(2, result.totalMatchedBeforeLimit)
        assertEquals(listOf(2L), result.songs.map(Song::id))
    }

    @Test
    fun resolve_emptyRulesMeansAllSongs() {
        val playlist = SmartPlaylist(
            id = 1L,
            name = "All",
            sort = SmartPlaylistSort(SmartPlaylistSortField.Title, SortDirection.Ascending),
            createdAtMs = 1L,
            updatedAtMs = 1L,
        )

        val result = engine.resolve(
            definition = playlist,
            songs = listOf(song(2L, "Bravo"), song(1L, "Alpha")),
            favoriteSongIds = emptySet(),
            playCounts = emptyMap(),
            recentSongIds = emptyList(),
        )

        assertEquals(listOf(1L, 2L), result.songs.map(Song::id))
    }

    @Test
    fun serialize_roundTripsUserDefinitionsAndIgnoresBuiltIns() {
        val playlist = SmartPlaylist(
            id = 12L,
            name = "Late Night",
            matchMode = SmartPlaylistMatchMode.Any,
            rules = listOf(
                SmartPlaylistRule.TitleContains("moon"),
                SmartPlaylistRule.PlayCount(NumericOperator.GreaterThan, 3),
            ),
            sort = SmartPlaylistSort(SmartPlaylistSortField.Random, SortDirection.Ascending),
            limit = 25,
            createdAtMs = 10L,
            updatedAtMs = 20L,
        )
        val serialized = serializeSmartPlaylists(
            listOf(
                SmartPlaylistDefaults.builtIns().first(),
                playlist,
            ),
        )

        assertEquals(listOf(playlist), deserializeSmartPlaylists(serialized))
    }

    private fun song(
        id: Long,
        title: String,
        genre: String = "",
    ): Song {
        return Song(
            id = id,
            title = title,
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = genre,
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$id.mp3",
            albumId = 1L,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = id,
            uri = TestUri(),
            artUri = null,
        )
    }
}

