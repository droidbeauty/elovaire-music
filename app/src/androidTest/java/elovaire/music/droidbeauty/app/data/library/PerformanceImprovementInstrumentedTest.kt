package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.SearchableAlbum
import elovaire.music.droidbeauty.app.domain.search.buildSuggestedAlbums
import elovaire.music.droidbeauty.app.domain.search.toSearchableAlbum
import java.util.Locale
import java.util.PriorityQueue
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerformanceImprovementInstrumentedTest {
    @Test
    fun singletonSmartPlaylistAvoidsTheBatchIntermediate() {
        val songs = List(30_000) { index -> song(index.toLong() + 1) }
        val definition = SmartPlaylist(id = 7, name = "All", limit = 25, createdAtMs = 0, updatedAtMs = 0)
        val definitions = listOf(definition)
        val baseline = { SmartPlaylistEngine.resolveAllBatched(definitions, songs, emptySet(), emptyMap(), 1000) }
        val candidate = { SmartPlaylistEngine.resolveAll(definitions, songs, emptySet(), emptyMap(), 1000) }
        assertEquals(baseline(), candidate())
        measurePair("singleton_smart_playlist", baseline, candidate)
    }

    @Test
    fun suggestedAlbumsAllocateOnlyForHeapCandidates() {
        val albums = List(30_000) { index ->
            Album(index.toLong(), "Album ${30_000 - index}", "Artist ${index % 97}", null, 0, 0, emptyList())
                .toSearchableAlbum()
        }
        val counts = albums.associate { it.album.id to (it.album.id % 50L + 1L).toInt() }
        val recent = (1L..80L).toList()
        val baseline = { oldSuggestedAlbums(albums, counts, recent) }
        val candidate = { buildSuggestedAlbums(albums, counts, recent) }
        assertEquals(baseline(), candidate())
        measurePair("suggested_albums", baseline, candidate)
    }

    @Test
    fun audiobookSortingNormalizesEachTitleAndAuthorOnce() {
        val books = List(6_000) { index ->
            Audiobook("key-$index", "BOOK ${6_000 - index}", "AUTHOR ${index % 71}", null, 0, emptyList())
        }.shuffled(kotlin.random.Random(123))
        val comparator = compareBy<Audiobook>(
            { it.title.lowercase(Locale.ROOT) },
            { it.author.lowercase(Locale.ROOT) },
            { it.stableKey },
        )
        val baseline = { books.sortedWith(comparator) }
        val candidate = { AudiobookCatalog.sortAudiobooks(books) }
        assertEquals(baseline(), candidate())
        listOf(0, 1, 2, 15, 16).forEach { size ->
            val sample = books.take(size)
            assertEquals(sample.sortedWith(comparator), AudiobookCatalog.sortAudiobooks(sample))
        }
        measurePair("audiobook_sort", baseline, candidate)
    }

    private fun <T> measurePair(label: String, baseline: () -> T, candidate: () -> T) {
        repeat(3) { baseline(); candidate() }
        val times = Array(2) { mutableListOf<Double>() }
        val bytes = Array(2) { mutableListOf<Long>() }
        repeat(7) { sample ->
            for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                times[variant] += measureNanoTime { if (variant == 0) baseline() else candidate() } / 1_000_000.0
                bytes[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
            }
        }
        Log.i("PerformanceImprovement", "$label baselineMs=${times[0]} candidateMs=${times[1]} baselineBytes=${bytes[0]} candidateBytes=${bytes[1]}")
        val baselineMedian = bytes[0].sorted()[3]
        val candidateMedian = bytes[1].sorted()[3]
        assertTrue("$label allocation did not improve: $baselineMedian vs $candidateMedian", candidateMedian < baselineMedian)
    }

    private data class Candidate(val album: SearchableAlbum, val count: Int, val recent: Boolean, val index: Int)

    private fun oldSuggestedAlbums(
        albums: List<SearchableAlbum>,
        counts: Map<Long, Int>,
        recent: List<Long>,
    ): List<Album> {
        val recentSet = recent.toHashSet()
        val seen = HashSet<Long>(6)
        val output = ArrayList<Album>(6)
        for (played in listOf(true, false)) {
            val remaining = 6 - output.size
            if (remaining <= 0) break
            val comparator = if (played) {
                compareBy<Candidate> { it.count }.thenBy { if (it.recent) 1 else 0 }
                    .thenBy { it.album.normalizedArtist }.thenBy { it.album.normalizedTitle }.thenBy { it.index }
            } else {
                compareBy<Candidate> { if (it.recent) 1 else 0 }
                    .thenBy { it.album.normalizedArtist }.thenBy { it.album.normalizedTitle }.thenBy { it.index }
            }
            val heap = PriorityQueue(remaining, comparator.reversed())
            val byId = HashMap<Long, Candidate>(remaining)
            albums.forEachIndexed { index, searchable ->
                val id = searchable.album.id
                val count = counts[id] ?: 0
                if (if (played) count <= 0 else count != 0) return@forEachIndexed
                val entry = Candidate(searchable, count, id in recentSet, index)
                val existing = byId[id]
                if (existing != null) {
                    if (comparator.compare(entry, existing) < 0) {
                        heap.remove(existing)
                        byId[id] = entry
                        heap += entry
                    }
                } else if (heap.size < remaining) {
                    byId[id] = entry
                    heap += entry
                } else if (comparator.compare(entry, heap.peek()) < 0) {
                    byId.remove(heap.poll().album.album.id)
                    byId[id] = entry
                    heap += entry
                }
            }
            heap.sortedWith(comparator).forEach { if (seen.add(it.album.album.id)) output += it.album.album }
        }
        return output
    }

    private fun song(id: Long) = Song(
        id = id, title = "TRACK $id", isExplicit = false, artist = "ARTIST ${id % 97}",
        album = "ALBUM ${id % 500}", releaseYear = null, genre = "Genre", audioFormat = "MP3",
        audioQuality = null, fileName = "$id.mp3", albumId = id % 500, durationMs = 180_000,
        trackNumber = 1, discNumber = 1, dateAddedSeconds = id,
        uri = Uri.parse("content://media/external/audio/media/$id"), artUri = null,
    )
}
