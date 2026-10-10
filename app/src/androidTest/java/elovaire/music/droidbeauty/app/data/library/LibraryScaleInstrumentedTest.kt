package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Album
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
import elovaire.music.droidbeauty.app.ui.screens.distinctMusicArtistCount
import elovaire.music.droidbeauty.app.ui.screens.distinctMusicGenreCount
import elovaire.music.droidbeauty.app.ui.screens.favoriteAlbumsFor
import elovaire.music.droidbeauty.app.ui.screens.recentlyAddedAlbumsFor
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

    @Test
    fun libraryHubDerivedMetricsAvoidFullSortAndMappedListsAtScale() {
        val size = 50_000
        val nowSeconds = System.currentTimeMillis() / 1_000L
        val songs = List(size) { index ->
            song(index.toLong() + 1).copy(dateAddedSeconds = nowSeconds - index % 1_000)
        }
        val albums = songs.chunked(20).map { albumSongs ->
            val firstSong = albumSongs.first()
            Album(
                id = firstSong.albumId,
                title = firstSong.album,
                artist = firstSong.artist,
                artUri = null,
                songCount = albumSongs.size,
                durationMs = albumSongs.sumOf(Song::durationMs),
                songs = albumSongs,
            )
        }
        val library = LibraryUiState(songs = songs, albums = albums)

        val baselineRecent = {
            library.albums.mapNotNull { album ->
                val timestamp = album.songs.maxOfOrNull { it.dateAddedSeconds } ?: return@mapNotNull null
                if (timestamp > 0L && timestamp >= nowSeconds - 14L * 24L * 60L * 60L) album to timestamp else null
            }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(4)
        }
        val candidateRecent = { recentlyAddedAlbumsFor(library, limit = 4) }
        assertEquals(baselineRecent().map(Album::id), candidateRecent().map(Album::id))
        measurePair("recent_albums_top4", size, baselineRecent, candidateRecent)

        val baselineArtists = { songs.map { it.artist.ifBlank { "Unknown Artist" } }.distinct().size }
        val candidateArtists = { distinctMusicArtistCount(songs) }
        assertEquals(baselineArtists(), candidateArtists())
        measurePair("hub_artist_count", size, baselineArtists, candidateArtists)

        val baselineGenres = { songs.map { it.genre.ifBlank { "Unknown Genre" } }.distinct().size }
        val candidateGenres = { distinctMusicGenreCount(songs) }
        assertEquals(baselineGenres(), candidateGenres())
        measurePair("hub_genre_count", size, baselineGenres, candidateGenres)

        val playCounts = albums.mapIndexed { index, album ->
            album.songs.first().id to (albums.size - index)
        }.toMap()
        val baselineFavorites = {
            allocatingFavoriteAlbums(albums, playCounts, albums.take(6), albums.takeLast(6))
        }
        val candidateFavorites = {
            favoriteAlbumsFor(
                libraryState = library,
                songPlayCounts = playCounts,
                recentAlbums = albums.take(6),
                recentlyAddedAlbums = albums.takeLast(6),
            )
        }
        assertEquals(baselineFavorites().map(Album::id), candidateFavorites().map(Album::id))
        measurePair("favorite_album_candidates", size, baselineFavorites, candidateFavorites)
    }

    private data class RankedFavoriteReference(
        val album: Album,
        val playCount: Int,
        val artistKey: String,
        val titleKey: String,
        val index: Int,
    )

    private fun allocatingFavoriteAlbums(
        albums: List<Album>,
        songPlayCounts: Map<Long, Int>,
        recentAlbums: List<Album>,
        recentlyAddedAlbums: List<Album>,
    ): List<Album> {
        val comparator = compareByDescending<RankedFavoriteReference> { it.playCount }
            .thenBy { it.artistKey }
            .thenBy { it.titleKey }
            .thenBy { it.index }
        val bestAlbums = PriorityQueue(6, comparator.reversed())
        val bestAlbumsById = HashMap<Long, RankedFavoriteReference>(6)
        albums.forEachIndexed { index, album ->
            val playCount = album.songs.sumOf { songPlayCounts[it.id] ?: 0 }
            if (playCount <= 0) return@forEachIndexed
            val candidate = RankedFavoriteReference(
                album = album,
                playCount = playCount,
                artistKey = album.artist.lowercase(),
                titleKey = album.title.lowercase(),
                index = index,
            )
            val existing = bestAlbumsById[album.id]
            if (existing != null) {
                if (comparator.compare(candidate, existing) >= 0) return@forEachIndexed
                bestAlbums.remove(existing)
            } else if (bestAlbums.size == 6) {
                val worst = requireNotNull(bestAlbums.peek())
                if (comparator.compare(candidate, worst) >= 0) return@forEachIndexed
                bestAlbums.poll()
                bestAlbumsById.remove(worst.album.id)
            }
            bestAlbumsById[album.id] = candidate
            bestAlbums += candidate
        }

        val selected = ArrayList<Album>(6)
        val selectedIds = HashSet<Long>(6)
        fun addIfMissing(album: Album) {
            if (selected.size < 6 && selectedIds.add(album.id)) selected += album
        }
        bestAlbums.toList().sortedWith(comparator).forEach { addIfMissing(it.album) }
        recentAlbums.forEach(::addIfMissing)
        recentlyAddedAlbums.forEach(::addIfMissing)
        return selected
    }

    @Test
    fun snapshotDeduplicationAvoidsPerSongStrongKeySetsAtScale() {
        val songs = List(50_000) { song(it.toLong() + 1) }
        val baseline = { allocatingSnapshotDedupe(songs) }
        val candidate = { LibrarySongDuplicateResolver.dedupeLoadedSnapshotSongs(songs) }

        assertEquals(baseline(), candidate())
        measurePair("snapshot_dedupe", songs.size, baseline, candidate)
    }

    private fun allocatingSnapshotDedupe(songs: List<Song>): List<Song> {
        val mediaStoreSongs = ArrayList<Song>(songs.size)
        songs.forEach { song ->
            val source = MediaIdentityResolver.resolve(song)
            if (source is MediaSourceIdentity.MediaStoreItem || (source == null && song.id > 0L)) {
                mediaStoreSongs += song
            }
        }
        val accepted = ArrayList<Song>(mediaStoreSongs.size)
        val keys = linkedSetOf<String>()
        mediaStoreSongs.forEach { song ->
            val songKeys = LibrarySongDuplicateResolver.strongKeys(song)
            if (songKeys.isEmpty() || songKeys.none { it in keys }) {
                accepted += song
                keys += songKeys
            }
        }
        return accepted
    }

    private fun <T> measurePair(
        phase: String,
        size: Int,
        baseline: () -> T,
        candidate: () -> T,
    ) {
        val times = Array(2) { mutableListOf<Double>() }
        val allocations = Array(2) { mutableListOf<Long>() }
        repeat(5) { sample ->
            for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val block = if (variant == 0) baseline else candidate
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                times[variant] += measureNanoTime { block() } / 1_000_000.0
                allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
            }
        }
        Log.i("LibraryScale", "paired_screen_format songs=$size phase=$phase " +
            "baselineMs=${times[0]} candidateMs=${times[1]} " +
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
            portableMediaIdentityRevision = MediaIdentityResolver.portableIdentityRevision(snapshot.songs),
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
            val patched = publisher.patchSongs(edits, emptySet(), emptySet()).state
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
