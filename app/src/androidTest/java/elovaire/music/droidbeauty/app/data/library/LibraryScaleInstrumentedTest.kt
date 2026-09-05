package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchSortMode
import elovaire.music.droidbeauty.app.domain.search.buildSearchIndex
import elovaire.music.droidbeauty.app.domain.search.buildSearchResults
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScaleInstrumentedTest {
    @Test
    fun assemblySearchAndBatchPatchPreserveLargeLibrary() {
        listOf(1_000, 10_000, 50_000).forEach(::qualifySize)
    }

    private fun qualifySize(size: Int) {
        // Keep the largest case representative without retaining several additional full
        // snapshots solely for repeated timing samples on a constrained test device.
        val snapshot = LibrarySnapshotAssembler.assemble((1..size).map { song(it.toLong()) })
        val current = LibraryContentState(
            songs = snapshot.songs,
            albums = snapshot.albums,
            contentRevision = snapshot.contentRevision,
        )
        val edits = snapshot.songs.filterIndexed { index, _ -> index % 20 == 0 }
            .take(500).map { it.copy(title = "Edited ${it.id}") }
        val publisher = LibrarySnapshotPublisher({}, { current })
        val index = buildSearchIndex(snapshot.songs, snapshot.albums)
        val query = NormalizedSearchQuery.from("track")
        val samples = if (size >= 50_000) 1 else 5
        measure("assemble", size, samples) {
            assertEquals(size, LibrarySnapshotAssembler.assemble(snapshot.songs).songs.size)
        }
        measure("search_index", size, samples) {
            assertEquals(size, buildSearchIndex(snapshot.songs, snapshot.albums).songs.size)
        }
        measure("search_query", size, samples) {
            assertEquals(size, buildSearchResults(query, SearchSortMode.Title, index, false).totalSongMatchCount)
        }
        measure("batch_patch", size, samples) {
            val patched = publisher.patchSongs(edits, emptySet(), emptySet())
            assertEquals(size, patched.songs.size)
            assertEquals(size, patched.albums.sumOf { it.songCount })
            assertEquals(edits.size, patched.songs.count { it.title.startsWith("Edited ") })
        }
    }

    private fun measure(phase: String, size: Int, samples: Int, block: () -> Unit) {
        repeat(if (size >= 50_000) 0 else 2) { block() }
        val allocatedBefore = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
        val timings = List(samples) { measureNanoTime(block) / 1_000_000.0 }
        val allocatedBytes = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - allocatedBefore
        Log.i("LibraryScale", "phase=$phase songs=$size samplesMs=$timings allocatedBytes=$allocatedBytes")
    }

    private fun song(id: Long) = Song(
        id = id,
        title = "Track $id",
        isExplicit = false,
        artist = "Artist ${id / 200}",
        album = "Album ${(id - 1) / 20}",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = (id - 1) / 20 + 1,
        durationMs = 180_000L,
        trackNumber = ((id - 1) % 20).toInt() + 1,
        discNumber = 1,
        dateAddedSeconds = id,
        uri = Uri.parse("content://media/external_primary/audio/media/$id"),
        artUri = null,
    )
}
