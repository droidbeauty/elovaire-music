package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchSortMode
import elovaire.music.droidbeauty.app.domain.search.SearchIndex
import elovaire.music.droidbeauty.app.domain.search.RankedResult
import elovaire.music.droidbeauty.app.domain.search.SearchResults
import elovaire.music.droidbeauty.app.domain.search.SearchableSong
import elovaire.music.droidbeauty.app.domain.search.scoreMatch
import elovaire.music.droidbeauty.app.domain.search.toSearchableSong
import elovaire.music.droidbeauty.app.domain.search.buildSearchIndex
import elovaire.music.droidbeauty.app.domain.search.buildSearchResults
import kotlin.system.measureNanoTime
import java.util.PriorityQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScaleInstrumentedTest {
    @Test
    fun boundedSearchPreviewMatchesFullRankingAtScale() {
        for (size in listOf(1_000, 10_000, 50_000)) {
            val index = SearchIndex(songs = List(size) { song(it.toLong() + 1).toSearchableSong() })
            for (sort in SearchSortMode.entries) {
                for (rawQuery in listOf("track", "trak", "track artist", "unmatchedzzzz")) {
                    val query = NormalizedSearchQuery.from(rawQuery)
                    val expected = if (size <= 10_000) {
                        buildSearchResults(query, sort, index)
                    } else {
                        // A full 50k-song result retains another complete ranked list. The
                        // bounded candidate is qualified against the established preview
                        // algorithm at that size; smaller cases prove that preview agrees with
                        // the authoritative full ranking.
                        allocatingPreview(index.songs, query, sort)
                    }
                    val baseline = { allocatingPreview(index.songs, query, sort) }
                    val candidate = { buildSearchResults(query, sort, index, false) }
                    val check: (SearchResults) -> Unit = { actual ->
                        val expectedPreview = if (size <= 10_000) {
                            expected.allMatchingSongs.take(20)
                        } else {
                            expected.matchingSongs
                        }
                        assertEquals(expectedPreview, actual.matchingSongs)
                        assertEquals(expected.totalSongMatchCount, actual.totalSongMatchCount)
                        assertEquals(emptyList<Song>(), actual.allMatchingSongs)
                    }
                    repeat(3) { check(baseline()); check(candidate()) }
                    val times = Array(2) { mutableListOf<Double>() }
                    val allocations = Array(2) { mutableListOf<Long>() }
                    repeat(5) { sample ->
                        val order = if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)
                        for (variant in order) {
                            val block = if (variant == 0) baseline else candidate
                            val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                            lateinit var actual: SearchResults
                            times[variant] += measureNanoTime { actual = block() } / 1_000_000.0
                            allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
                            check(actual)
                        }
                    }
                    Log.i("LibraryScale", "paired_search songs=$size sort=$sort query=$rawQuery " +
                        "baselineMs=${times[0]} candidateMs=${times[1]} " +
                        "baselineBytes=${allocations[0]} candidateBytes=${allocations[1]}")
                }
            }
        }
    }

    // Previous preview algorithm, retained as an allocation and ordering reference.
    private fun allocatingPreview(
        songs: List<SearchableSong>,
        query: NormalizedSearchQuery,
        sort: SearchSortMode,
    ): SearchResults {
        val scoreOrder = compareByDescending<RankedResult<SearchableSong>> { it.score }
        val bestFirst = when (sort) {
            SearchSortMode.Title -> scoreOrder.thenBy { it.value.normalizedTitle }
                .thenBy { it.value.normalizedArtist }.thenBy { it.value.normalizedAlbum }.thenBy { it.value.song.id }
            SearchSortMode.Artist -> scoreOrder.thenBy { it.value.normalizedArtist }
                .thenBy { it.value.normalizedTitle }.thenBy { it.value.normalizedAlbum }.thenBy { it.value.song.id }
        }
        val heap = PriorityQueue(20, bestFirst.reversed())
        var count = 0
        for (song in songs) {
            val score = scoreMatch(query, song.normalizedTitle, song.normalizedArtist,
                song.normalizedAlbum, song.normalizedComposite) ?: continue
            count++
            val ranked = RankedResult(song, score)
            if (heap.size < 20) heap += ranked
            else if (bestFirst.compare(ranked, heap.peek()) < 0) {
                heap.poll()
                heap += ranked
            }
        }
        return SearchResults(matchingSongs = heap.sortedWith(bestFirst).map { it.value.song },
            totalSongMatchCount = count)
    }

    @Test
    fun assemblySearchAndBatchPatchPreserveLargeLibrary() {
        listOf(1_000, 10_000, 50_000).forEach(::qualifySize)
    }

    @Test
    fun patchAlbumGroupingFastPathMatchesCanonicalizationForLargeMetadataEdits() {
        val size = 50_000
        val songs = List(size) { song(it.toLong() + 1) }
        val updated = songs.map { it.copy(title = "Edited ${it.id}") }
        val positions = updated.indices.take(500).toList()
        repeat(2) {
            LibrarySnapshotAssembler.canonicalizeAlbumIds(updated)
            LibrarySnapshotAssembler.canonicalizeAlbumIdsAfterPatch(songs, updated, positions)
        }
        val timings = Array(2) { mutableListOf<Double>() }
        val allocations = Array(2) { mutableListOf<Long>() }
        repeat(5) { sample ->
            for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                lateinit var result: List<Song>
                timings[variant] += measureNanoTime {
                    result = if (variant == 0) {
                        LibrarySnapshotAssembler.canonicalizeAlbumIds(updated)
                    } else {
                        LibrarySnapshotAssembler.canonicalizeAlbumIdsAfterPatch(songs, updated, positions)
                    }
                } / 1_000_000.0
                allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
                assertSame(updated, result)
            }
        }
        Log.i("LibraryScale", "paired_patch_canonicalization songs=$size " +
            "baselineMs=${timings[0]} candidateMs=${timings[1]} " +
            "baselineBytes=${allocations[0]} candidateBytes=${allocations[1]}")
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
