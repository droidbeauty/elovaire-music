package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtistProjectionTest {
    @Test
    fun topArtistSongs_matchesStableFullSortForLargeArtist() {
        val songs = listOf(
            song(1L, "zeta"),
            song(2L, "Alpha"),
            song(3L, "alpha"),
            song(4L, "Beta"),
            song(5L, "gamma"),
            song(6L, "delta"),
            song(7L, "epsilon"),
            song(8L, "eta"),
        )
        val playCounts = mapOf(
            1L to 4,
            2L to 7,
            3L to 7,
            4L to 7,
            5L to 6,
            6L to 5,
            7L to 3,
            8L to 2,
        )
        val reference = songs
            .sortedWith(
                compareByDescending<Song> { playCounts[it.id] ?: 0 }
                    .thenBy { it.title.lowercase() },
            )
            .take(5)

        assertEquals(reference, topArtistSongs(songs, playCounts))
    }

    private fun song(id: Long, title: String) = Song(
        id = id,
        title = title,
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = TestUri("content://artist/$id"),
        artUri = null,
    )
}
