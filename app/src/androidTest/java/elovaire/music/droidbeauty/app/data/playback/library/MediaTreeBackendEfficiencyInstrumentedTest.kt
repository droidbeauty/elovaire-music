package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaTreeBackendEfficiencyInstrumentedTest {
    @Test
    fun repeatedArtistBrowseReusesSortedContextData() {
        val songs = (1L..5_000L).map { id ->
            Song(
                id = id,
                title = "Song $id",
                isExplicit = false,
                artist = "Artist",
                album = "Album ${(id % 25L) + 1L}",
                releaseYear = null,
                genre = "Genre",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "$id.mp3",
                albumId = id % 25L,
                durationMs = 180_000L,
                trackNumber = (id % 20L).toInt() + 1,
                discNumber = 1,
                dateAddedSeconds = id,
                uri = Uri.parse("content://elovaire/efficiency/$id"),
                artUri = null,
            )
        }
        val snapshot = MediaTreeSnapshotCache().snapshot(
            permissionGranted = true,
            songs = songs,
            albums = emptyList(),
            playlists = emptyList(),
            favoriteSongIds = emptyList(),
            recentSongIds = emptyList(),
            lastPlayedCollectionKind = null,
            lastPlayedCollectionId = null,
            libraryRevision = "efficiency-fixture",
        )
        val comparator = compareBy<Song>(
            { it.album.lowercase(Locale.ROOT) },
            { it.discNumber },
            { it.trackNumber },
            { it.title.lowercase(Locale.ROOT) },
            { it.id },
        )

        repeat(3) { snapshot.songsForArtistInContext("Artist") }
        val repeatedSortNanos = measureNanoTime {
            repeat(REPETITIONS) {
                snapshot.songsForArtist("Artist").sortedWith(comparator)
            }
        }
        val cachedBrowseNanos = measureNanoTime {
            repeat(REPETITIONS) {
                snapshot.songsForArtistInContext("Artist")
            }
        }

        Log.i(
            TAG,
            "artist browse benchmark: repeatedSortMs=${repeatedSortNanos / 1_000_000.0}, " +
                "cachedBrowseMs=${cachedBrowseNanos / 1_000_000.0}, songs=${songs.size}",
        )
        assertTrue(
            "Cached artist browse should avoid repeated sorting: $repeatedSortNanos vs $cachedBrowseNanos",
            cachedBrowseNanos < repeatedSortNanos,
        )
    }

    private companion object {
        const val REPETITIONS = 24
        const val TAG = "MediaTreeEfficiency"
    }
}
