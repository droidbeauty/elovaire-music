package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaTreeBackendEfficiencyInstrumentedTest {
    @Test
    fun playbackContextUpdatesRetainExpensiveLibraryIndexes() {
        val songs = (1L..10_000L).map { id ->
            Song(
                id = id,
                title = "Song $id",
                isExplicit = false,
                artist = "Artist ${id % 100L}",
                album = "Album ${id % 500L}",
                releaseYear = null,
                genre = "Genre ${id % 12L}",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "$id.mp3",
                albumId = id % 500L,
                durationMs = 180_000L,
                trackNumber = (id % 20L).toInt() + 1,
                discNumber = 1,
                dateAddedSeconds = id,
                uri = Uri.parse("content://elovaire/context/$id"),
                artUri = null,
            )
        }
        val albums = emptyList<Album>()
        val playlists = emptyList<Playlist>()

        fun exercise(reuseCache: Boolean): List<Int> {
            val cache = MediaTreeSnapshotCache()
            val musicSongs = if (reuseCache) emptyList() else songs.filter {
                it.mediaKind == AudioMediaKind.Music
            }
            return (0 until CONTEXT_UPDATES).map { update ->
                val favoriteSongIds = listOf(update.toLong())
                val recentSongIds = listOf(update.toLong())
                val snapshot = if (reuseCache) cache.snapshot(
                    permissionGranted = true,
                    songs = songs,
                    albums = albums,
                    playlists = playlists,
                    favoriteSongIds = favoriteSongIds,
                    recentSongIds = recentSongIds,
                    lastPlayedCollectionKind = null,
                    lastPlayedCollectionId = null,
                    libraryRevision = "context-efficiency",
                ) else {
                    ElovaireMediaTree.MediaTreeSnapshot(
                        permissionGranted = true,
                        songs = musicSongs,
                        albums = albums,
                        playlists = playlists,
                        favoriteSongIdSource = favoriteSongIds,
                        favoriteSongIds = favoriteSongIds.toSet(),
                        recentSongIds = recentSongIds,
                        lastPlayedCollectionKind = null,
                        lastPlayedCollectionId = null,
                        libraryRevision = "context-efficiency",
                    )
                }
                listOf(
                    snapshot.songsByTitle().size,
                    snapshot.searchableSongs().size,
                    snapshot.songsForArtistInContext("artist ${update % 100}").size,
                    snapshot.favoriteSongsByTitle().size,
                )
            }.flatten()
        }

        repeat(2) { assertEquals(exercise(reuseCache = false), exercise(reuseCache = true)) }
        val timingsMs = Array(2) { mutableListOf<Double>() }
        val allocations = Array(2) { mutableListOf<Long>() }
        repeat(SAMPLES) { sample ->
            val order = if (sample % 2 == 0) listOf(false, true) else listOf(true, false)
            order.forEach { reuseCache ->
                val variant = if (reuseCache) 1 else 0
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                timingsMs[variant] += measureNanoTime {
                    exercise(reuseCache)
                } / 1_000_000.0
                allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
            }
        }
        Log.i(
            TAG,
            "playback-context index reuse: baselineMs=${timingsMs[0]} candidateMs=${timingsMs[1]} " +
                "baselineBytes=${allocations[0]} candidateBytes=${allocations[1]} songs=${songs.size}",
        )
        assertTrue(
            "Stable library indexes should reduce allocation: $allocations",
            allocations[1].sum() < allocations[0].sum(),
        )
        assertTrue("Stable library indexes should reduce repeated work: $timingsMs", median(timingsMs[1]) < median(timingsMs[0]))
    }

    @Test
    fun bucketIdsRoundTripForEveryBrowseCategory() {
        listOf("songs", "albums", "artists").forEach { parent ->
            listOf("A", "#").forEach { key ->
                val id = ElovaireMediaId.Bucket(parent, key)
                assertEquals(id, ElovaireMediaIds.parse(id.value))
            }
        }
    }

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

    @Test
    fun repeatedSnapshotReuseAvoidsMusicProjectionScan() {
        val songs = (1L..5_000L).map { id ->
            Song(
                id = id,
                title = "Song $id",
                isExplicit = false,
                artist = "Artist",
                album = "Album",
                releaseYear = null,
                genre = "Genre",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "$id.mp3",
                albumId = id % 25L,
                durationMs = 180_000L,
                trackNumber = 1,
                discNumber = 1,
                dateAddedSeconds = id,
                uri = Uri.parse("content://elovaire/projection/$id"),
                artUri = null,
            )
        }
        val cache = MediaTreeSnapshotCache()
        cache.snapshot(
            permissionGranted = true,
            songs = songs,
            albums = emptyList(),
            playlists = emptyList(),
            favoriteSongIds = emptyList(),
            recentSongIds = emptyList(),
            lastPlayedCollectionKind = null,
            lastPlayedCollectionId = null,
            libraryRevision = "projection-fixture",
        )

        val repeatedProjectionNanos = measureNanoTime {
            repeat(REPETITIONS) {
                songs.filter { it.mediaKind == elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Music }
            }
        }
        val cachedSnapshotNanos = measureNanoTime {
            repeat(REPETITIONS) {
                cache.snapshot(
                    true,
                    songs,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    null,
                    null,
                    "projection-fixture",
                )
            }
        }

        Log.i(
            TAG,
            "snapshot projection benchmark: repeatedProjectionMs=${repeatedProjectionNanos / 1_000_000.0}, " +
                "cachedSnapshotMs=${cachedSnapshotNanos / 1_000_000.0}, songs=${songs.size}",
        )
        assertTrue(
            "Cached snapshot should avoid repeated music projection: $cachedSnapshotNanos vs $repeatedProjectionNanos",
            cachedSnapshotNanos < repeatedProjectionNanos,
        )
    }

    private companion object {
        const val CONTEXT_UPDATES = 5
        const val REPETITIONS = 24
        const val SAMPLES = 5
        const val TAG = "MediaTreeEfficiency"
    }

    private fun median(values: List<Double>): Double = values.sorted()[values.size / 2]
}
