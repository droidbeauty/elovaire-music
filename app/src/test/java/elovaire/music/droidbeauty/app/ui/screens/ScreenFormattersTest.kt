package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenFormattersTest {
    @Test
    fun recentlyAddedAlbumsFor_sortsRecentAlbumsByLatestTimestamp() {
        val nowSeconds = System.currentTimeMillis() / 1_000L
        val albums = (1L..5L).map { id -> album(id, addedAtSeconds = nowSeconds - id) }

        val result = recentlyAddedAlbumsFor(LibraryUiState(albums = albums))

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), result.map(Album::id))
    }

    @Test
    fun recentlyAddedAlbumsFor_limitPreservesStableOrdering() {
        val nowSeconds = System.currentTimeMillis() / 1_000L
        val albums = listOf(
            album(1L, nowSeconds - 1L),
            album(2L, nowSeconds - 3L),
            album(3L, nowSeconds - 1L),
        )

        assertEquals(
            recentlyAddedAlbumsFor(LibraryUiState(albums = albums)).take(2).map(Album::id),
            recentlyAddedAlbumsFor(LibraryUiState(albums = albums), limit = 2).map(Album::id),
        )
        assertEquals(emptyList<Album>(), recentlyAddedAlbumsFor(LibraryUiState(albums = albums), limit = 0))
    }

    @Test
    fun buildPlaylistPreviewBoundsArtworkSongsButKeepsFullDuration() {
        val songs = (1L..6L).map { id ->
            song(
                id = id,
                albumId = if (id == 2L) 1L else id,
                durationMs = id * 1_000L,
            )
        }

        val result = buildPlaylistPreview(
            songIds = listOf(1L, 2L, 3L, 4L, 5L, 6L, 999L),
            songsById = songs.associateBy(Song::id),
        )

        assertEquals(listOf(1L, 3L, 4L, 5L), result.songs.map(Song::id))
        assertEquals(21_000L, result.durationMs)
    }

    @Test
    fun playlistArtworkPreviewStopsAfterFourDistinctAlbums() {
        val songs = (1L..8L).map { id -> song(id, albumId = id, durationMs = 1_000L) }
        val songsById = songs.associateBy(Song::id)

        val result = playlistArtworkPreviewSongs(
            songIds = listOf(999L, 1L, 2L, 2L, 3L, 4L, 5L, 6L),
            songsById = songsById,
        )

        assertEquals(listOf(1L, 2L, 3L, 4L), result.map(Song::id))
    }

    @Test
    fun recentSongsForSkipsMissingAndNonMusicIdsBeforeApplyingLimit() {
        val songs = (1L..7L).map { id ->
            song(id, albumId = id, durationMs = 1_000L).let {
                if (id == 2L || id == 4L) it.copy(mediaKind = AudioMediaKind.Audiobook) else it
            }
        }

        val result = recentSongsFor(
            recentSongIds = listOf(1L, 2L, 999L, 3L, 4L, 5L, 6L, 7L),
            songsById = songs.associateBy(Song::id),
        )

        assertEquals(listOf(1L, 3L, 5L, 6L, 7L), result.map(Song::id))
    }

    @Test
    fun songAndAlbumCollectionSortsMatchTheirSortModes() {
        val songs = listOf(
            song(1L, albumId = 1L, durationMs = 1_000L).copy(title = "b", artist = "A", album = "z"),
            song(2L, albumId = 2L, durationMs = 1_000L).copy(title = "A", artist = "b", album = "a"),
            song(3L, albumId = 3L, durationMs = 1_000L).copy(title = "A", artist = "A", album = "a"),
        )
        val albums = listOf(
            album(1L, 1L).copy(title = "b", artist = "A"),
            album(2L, 1L).copy(title = "A", artist = "b"),
            album(3L, 1L).copy(title = "A", artist = "A"),
        )

        val sortedSongs = songSortModeResults(songs)
        SongSortMode.entries.forEach { mode ->
            assertEquals(sortedSongs.getValue(mode), sortSongCollection(songCollectionSortEntries(songs), mode).map(Song::id))
        }
        val sortedAlbums = albumSortModeResults(albums)
        AlbumSortMode.entries.forEach { mode ->
            assertEquals(sortedAlbums.getValue(mode), sortAlbumCollection(albumCollectionSortEntries(albums), mode).map(Album::id))
        }
    }

    @Test
    fun artistAndGenreEntriesCountUniqueAlbumsAndKeepUnknownGenre() {
        val songs = listOf(
            song(1L, albumId = 1L, durationMs = 1_000L).copy(artist = "Artist", albumArtist = "Library Artist", genre = "Jazz"),
            song(2L, albumId = 1L, durationMs = 1_000L).copy(artist = "Artist", albumArtist = "Library Artist", genre = "Jazz"),
            song(3L, albumId = 2L, durationMs = 1_000L).copy(artist = "Other", genre = ""),
        )

        assertEquals(listOf("Library Artist" to 2, "Other" to 1), artistEntriesFor(songs).map { it.name to it.songCount })
        assertEquals(2, distinctMusicArtistCount(songs))
        assertEquals(listOf("Jazz" to 1, "Unknown Genre" to 1), genreEntriesFor(songs).map { it.name to it.albumCount })
    }

    @Test
    fun songCollectionArtistSortUsesAlbumArtistWhileSongsKeepTheirTrackArtist() {
        val songs = listOf(
            song(1L, albumId = 1L, durationMs = 1_000L).copy(
                title = "Track",
                artist = "Track Artist Z",
                albumArtist = "Album Artist A",
            ),
            song(2L, albumId = 2L, durationMs = 1_000L).copy(
                title = "Track",
                artist = "Track Artist A",
                albumArtist = "Album Artist Z",
            ),
        )

        val sorted = sortSongCollection(songCollectionSortEntries(songs), SongSortMode.Artist)

        assertEquals(listOf(1L, 2L), sorted.map(Song::id))
        assertEquals(listOf("Track Artist Z", "Track Artist A"), sorted.map(Song::artist))
    }

    @Test
    fun libraryHubCountsUseTheSameUnknownLabels() {
        val songs = listOf(
            song(1L, albumId = 1L, durationMs = 1_000L).copy(artist = "", genre = ""),
            song(2L, albumId = 2L, durationMs = 1_000L).copy(artist = "Unknown Artist", genre = "Unknown Genre"),
            song(3L, albumId = 3L, durationMs = 1_000L).copy(artist = "Second", genre = "Jazz"),
        )

        assertEquals(songs.map { it.artist.ifBlank { "Unknown Artist" } }.distinct().size, distinctMusicArtistCount(songs))
        assertEquals(songs.map { it.genre.ifBlank { "Unknown Genre" } }.distinct().size, distinctMusicGenreCount(songs))
    }

    @Test
    fun favoriteAlbumsRanksOnceAndFillsRemainingSlotsFromRecentAlbums() {
        val albums = (1L..7L).map { id -> album(id, addedAtSeconds = id).copy(artist = "Artist $id") }
        val playCounts = mapOf(1L to 10, 2L to 10, 3L to 5, 4L to 1)

        val result = favoriteAlbumsFor(
            libraryState = LibraryUiState(albums = albums),
            songPlayCounts = playCounts,
            recentAlbums = listOf(albums[3], albums[4], albums[5]),
            recentlyAddedAlbums = listOf(albums[6]),
        )

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), result.map(Album::id))
    }

    private fun album(id: Long, addedAtSeconds: Long): Album {
        val song = song(id, id, 1_000L).copy(dateAddedSeconds = addedAtSeconds)
        return Album(
            id = id,
            title = "Album $id",
            artist = "Artist",
            artUri = null,
            songCount = 1,
            durationMs = 1_000L,
            songs = listOf(song),
        )
    }

    private fun song(id: Long, albumId: Long, durationMs: Long): Song {
        return Song(
            id = id,
            title = "Song $id",
            isExplicit = false,
            artist = "Artist",
            album = "Album $id",
            releaseYear = null,
            genre = "",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$id.mp3",
            albumId = albumId,
            durationMs = durationMs,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 0L,
            uri = TestUri("content://media/$id"),
            artUri = null,
        )
    }

    private fun songSortModeResults(songs: List<Song>): Map<SongSortMode, List<Long>> = mapOf(
        SongSortMode.Title to songs.sortedWith(compareBy<Song> { it.title.lowercase() }.thenBy { it.artist.lowercase() }.thenBy { it.album.lowercase() }).map(Song::id),
        SongSortMode.Artist to songs.sortedWith(compareBy<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() }.thenBy { it.album.lowercase() }).map(Song::id),
        SongSortMode.Album to songs.sortedWith(compareBy<Song> { it.album.lowercase() }.thenBy { it.title.lowercase() }.thenBy { it.artist.lowercase() }).map(Song::id),
    )

    private fun albumSortModeResults(albums: List<Album>): Map<AlbumSortMode, List<Long>> = mapOf(
        AlbumSortMode.Artist to albums.sortedWith(compareBy<Album> { it.artist.lowercase() }.thenBy { it.title.lowercase() }).map(Album::id),
        AlbumSortMode.Album to albums.sortedWith(compareBy<Album> { it.title.lowercase() }.thenBy { it.artist.lowercase() }).map(Album::id),
    )
}
