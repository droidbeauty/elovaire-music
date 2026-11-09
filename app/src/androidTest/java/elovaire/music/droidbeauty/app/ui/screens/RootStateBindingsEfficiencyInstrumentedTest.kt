package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RootStateBindingsEfficiencyInstrumentedTest {
    @Test
    fun rootSongIndexAvoidsBuildingAnUnusedAlbumGrouping() {
        val songs = (1L..50_000L).map { id ->
            Song(
                id = id,
                title = "Song $id",
                isExplicit = false,
                artist = "Artist ${id / 100L}",
                album = "Album ${(id - 1L) / 20L}",
                releaseYear = null,
                genre = "Genre",
                audioFormat = "MP3",
                audioQuality = null,
                fileName = "$id.mp3",
                albumId = (id - 1L) / 20L,
                durationMs = 180_000L,
                trackNumber = (id % 20L).toInt() + 1,
                discNumber = 1,
                dateAddedSeconds = id,
                uri = Uri.parse("content://elovaire/root-index/$id"),
                artUri = null,
            )
        }
        val targetAlbumId = 1_000L
        val expectedAlbumSongIds = songs.filter { it.albumId == targetAlbumId }.map(Song::id)
        val candidate = buildRootSongIndexes(songs)
        assertEquals(expectedAlbumSongIds, candidate.songsForAlbumId(targetAlbumId).map(Song::id))

        val baseline = {
            val songsById = linkedMapOf<Long, Song>()
            val songsByAlbumId = linkedMapOf<Long, MutableList<Song>>()
            songs.forEach { song ->
                songsById[song.id] = song
                songsByAlbumId.getOrPut(song.albumId, ::mutableListOf).add(song)
            }
            songsById.size
        }
        val optimized = {
            val indexes = buildRootSongIndexes(songs)
            indexes.songsById.size
        }
        assertEquals(baseline(), optimized())

        val timingsMs = Array(2) { mutableListOf<Double>() }
        val allocations = Array(2) { mutableListOf<Long>() }
        repeat(SAMPLES) { sample ->
            for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                timingsMs[variant] += measureNanoTime {
                    if (variant == 0) baseline() else optimized()
                } / 1_000_000.0
                allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
            }
        }
        Log.i(
            TAG,
            "root song indexes: baselineMs=${timingsMs[0]} candidateMs=${timingsMs[1]} " +
                "baselineBytes=${allocations[0]} candidateBytes=${allocations[1]} songs=${songs.size}",
        )
        assertTrue(
            "Avoiding unused album groups should reduce allocation: $allocations",
            allocations[1].sum() < allocations[0].sum(),
        )
        assertTrue("Avoiding unused album groups should reduce index time: $timingsMs", median(timingsMs[1]) < median(timingsMs[0]))
    }

    private fun median(values: List<Double>): Double = values.sorted()[values.size / 2]

    private companion object {
        const val SAMPLES = 7
        const val TAG = "RootIndexEfficiency"
    }
}
