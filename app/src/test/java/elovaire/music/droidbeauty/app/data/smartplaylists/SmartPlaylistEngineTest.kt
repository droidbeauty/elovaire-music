package elovaire.music.droidbeauty.app.data.smartplaylists

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartPlaylistEngineTest {
    private val engine = SmartPlaylistEngine

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

    @Test
    fun malformedMatchModeIsRejectedInsteadOfBecomingMatchAll() {
        val playlist = SmartPlaylist(
            id = 12L,
            name = "Corrupt",
            matchMode = SmartPlaylistMatchMode.Any,
            createdAtMs = 10L,
            updatedAtMs = 20L,
        )
        val serialized = serializeSmartPlaylists(listOf(playlist)).replace("Any", "UnknownMode")

        assertEquals(emptyList<SmartPlaylist>(), deserializeSmartPlaylists(serialized))
    }

    @Test
    fun unknownVersionAndVersionedUnknownSortAreRejected() {
        val playlist = SmartPlaylist(
            id = 12L,
            name = "Corrupt",
            createdAtMs = 10L,
            updatedAtMs = 20L,
        )
        val serialized = serializeSmartPlaylists(listOf(playlist))

        assertEquals(emptyList<SmartPlaylist>(), deserializeSmartPlaylists("v99:${serialized.removePrefix("v1:")}"))
        assertEquals(
            emptyList<SmartPlaylist>(),
            deserializeSmartPlaylists(serialized.replace("Title", "FutureSort")),
        )
    }

    @Test
    fun extraFieldsDoNotChangeCurrentRuleSemantics() {
        val playlist = SmartPlaylist(
            id = 12L,
            name = "Strict",
            rules = listOf(SmartPlaylistRule.FavoriteIs(true)),
            createdAtMs = 10L,
            updatedAtMs = 20L,
        )
        val serialized = serializeSmartPlaylists(listOf(playlist))
            .replace(
                Base64.getUrlEncoder().withoutPadding().encodeToString("favorite:true".toByteArray()),
                Base64.getUrlEncoder().withoutPadding().encodeToString("favorite:true:future".toByteArray()),
            )

        assertEquals(emptyList<SmartPlaylist>(), deserializeSmartPlaylists(serialized))
    }

    @Test
    fun recentlyAddedRejectsOverflowingOrWildlyFutureTimestamps() {
        val playlist = SmartPlaylistDefaults.builtIns()
            .first { it.builtInType == BuiltInSmartPlaylistType.RecentlyAdded }
        val nowMs = 1_000_000L
        val result = engine.resolve(
            definition = playlist,
            songs = listOf(
                song(1L, "Valid").copy(dateAddedSeconds = (nowMs / 1_000L) - 1L),
                song(2L, "Future").copy(dateAddedSeconds = Long.MAX_VALUE),
            ),
            favoriteSongIds = emptySet(),
            playCounts = emptyMap(),
            nowMs = nowMs,
        )

        assertEquals(listOf(1L), result.songs.map(Song::id))
    }

    @Test
    fun folderAndFormatRulesUseTheSameNormalizedValuesAsTheirDirectFields() {
        val playlist = SmartPlaylist(
            id = 5L,
            name = "Scoped",
            rules = listOf(
                SmartPlaylistRule.FolderContains("music/live"),
                SmartPlaylistRule.FileFormatIs(".MP3"),
            ),
            createdAtMs = 1L,
            updatedAtMs = 1L,
        )

        val result = engine.resolve(
            definition = playlist,
            songs = listOf(
                song(1L, "Match").copy(
                    audioFormat = "MP3",
                    fileName = "recording.flac",
                    libraryPath = "/storage/emulated/0/Music/Live/recording.flac",
                ),
                song(2L, "Wrong format").copy(
                    audioFormat = "OGG",
                    fileName = "recording.ogg",
                    libraryPath = "/storage/emulated/0/Music/Live/recording.ogg",
                ),
            ),
            favoriteSongIds = emptySet(),
            playCounts = emptyMap(),
        )

        assertEquals(listOf(1L), result.songs.map(Song::id))
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
