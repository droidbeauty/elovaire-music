package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.settings.PreferenceCollectionCodec
import elovaire.music.droidbeauty.app.data.playback.toPlaybackMediaItem
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSort
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.SortDirection
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.SearchSortMode
import elovaire.music.droidbeauty.app.domain.search.SearchIndex
import elovaire.music.droidbeauty.app.domain.search.SearchLibrarySnapshot
import elovaire.music.droidbeauty.app.domain.search.toSearchIndex
import elovaire.music.droidbeauty.app.domain.search.RankedResult
import elovaire.music.droidbeauty.app.domain.search.SearchResults
import elovaire.music.droidbeauty.app.domain.search.SearchableSong
import elovaire.music.droidbeauty.app.domain.search.scoreMatch
import elovaire.music.droidbeauty.app.domain.search.toSearchableSong
import elovaire.music.droidbeauty.app.domain.search.buildSearchIndex
import elovaire.music.droidbeauty.app.domain.search.buildSearchResults
import elovaire.music.droidbeauty.app.domain.search.buildPickerArtistSearchIndex
import elovaire.music.droidbeauty.app.domain.search.buildPickerSongSearchIndex
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchIndexedArtistsForPicker
import elovaire.music.droidbeauty.app.ui.screens.distinctMusicArtistCount
import elovaire.music.droidbeauty.app.ui.screens.distinctMusicGenreCount
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.albumCollectionSortEntries
import elovaire.music.droidbeauty.app.ui.screens.sortAlbumCollection
import elovaire.music.droidbeauty.app.ui.screens.favoriteAlbumsFor
import elovaire.music.droidbeauty.app.ui.screens.recentlyAddedAlbumsFor
import kotlin.system.measureNanoTime
import java.util.Locale
import java.util.PriorityQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScaleInstrumentedTest {
    @Test
    fun playbackQueueMediaItemBuildAtLibraryScale() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        measure("playback_queue_items", songs.size, 3) {
            val items = songs.map { it.toPlaybackMediaItem() }
            assertEquals(songs.size, items.size)
            assertEquals(songs.last().id.toString(), items.last().mediaId)
        }
    }

    @Test
    fun libraryUiRevisionComparisonSkipsLargeUnchangedPrefix() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val previous = LibraryUiState(songs = songs, contentRevision = "before")
        val next = previous.copy(songs = songs.map { it.copy() }, contentRevision = "after")
        val baseline = {
            previous.permissionGranted == next.permissionGranted && previous.isLoading == next.isLoading &&
                java.lang.Float.compare(previous.scanProgress, next.scanProgress) == 0 &&
                previous.isAuthoritative == next.isAuthoritative && previous.songs == next.songs &&
                previous.albums == next.albums && previous.audiobooks == next.audiobooks &&
                previous.contentRevision == next.contentRevision && previous.removingSongIds == next.removingSongIds &&
                previous.removingAlbumIds == next.removingAlbumIds && previous.errorMessage == next.errorMessage
        }
        val candidate = { previous == next }
        assertEquals(baseline(), candidate())
        measurePair("library_ui_revision_comparison", songs.size, baseline, candidate)
    }

    @Test
    fun pickerAlbumSortingReusesCaseFoldedKeys() {
        val albums = List(10_000) { index ->
            val song = song(index.toLong() + 1L)
            Album(song.albumId, "ALBUM ${10_000 - index}", "ARTIST ${index % 100}", null, 1, song.durationMs, listOf(song))
        }
        val baseline = { albums.sortedWith(compareBy<Album> { it.artist.lowercase() }.thenBy { it.title.lowercase() }) }
        val candidate = { sortAlbumCollection(albumCollectionSortEntries(albums), AlbumSortMode.Artist) }
        assertEquals(baseline(), candidate())
        measurePair("picker_album_sort", albums.size, baseline, candidate)
    }

    @Test
    fun metadataInvalidationCombinesIdAndPathPasses() {
        val songs = List(10_000) { index ->
            song(index.toLong() + 1L).copy(libraryPath = "/music/${index + 1}.mp3")
        }
        fun caches() = List(5) { ScannerMetadataCache().apply { prime(songs) } }
        val baselineCaches = caches()
        val candidateCaches = caches()
        var baselineIndex = 0
        var candidateIndex = 0
        val ids = listOf(1L)
        val paths = listOf("/music/5000.mp3")
        val baseline = {
            baselineCaches[baselineIndex++].apply {
                invalidateSongIds(ids)
                invalidatePaths(paths)
            }
        }
        val candidate = { candidateCaches[candidateIndex++].apply { invalidate(ids, paths) } }
        measurePair("metadata_invalidation", songs.size, baseline, candidate)
        for (index in baselineCaches.indices) {
            for (song in listOf(songs.first(), songs[4_999], songs.last())) {
                val key = MediaIdentityResolver.sourceKey(song)
                assertEquals(baselineCaches[index][key], candidateCaches[index][key])
            }
        }
    }

    @Test
    fun bulkDeletionFailuresFindAlbumIdsInOnePass() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val failed = songs.asSequence().filterIndexed { index, _ -> index % 2 == 0 }
            .mapTo(linkedSetOf(), Song::id).apply { add(99_999L) }
        val baseline = {
            failed.map { id ->
                LibraryDeleteFailure(id, songs.firstOrNull { it.id == id }?.albumId, "The file is still present after deletion.")
            }
        }
        val candidate = { deletionFailures(songs, failed) }
        assertEquals(baseline(), candidate())
        measurePair("bulk_deletion_failures", songs.size, baseline, candidate)
    }

    @Test
    fun bulkAlbumDeletionFindsAlbumsInOnePass() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val albums = songs.map { song ->
            Album(song.id, song.album, song.artist, song.artUri, 1, song.durationMs, listOf(song))
        }
        val selected = albums.asSequence().filterIndexed { index, _ -> index % 2 == 0 }
            .mapTo(linkedSetOf(), Album::id)
        val songIds = songs.mapTo(hashSetOf(), Song::id)
        val baseline = {
            selected.filterTo(linkedSetOf()) { id ->
                albums.firstOrNull { it.id == id }?.songs?.all { it.id in songIds } == true
            }
        }
        val candidate = { fullyDeletedAlbumIds(albums, selected, songIds) }
        assertEquals(baseline().toList(), candidate().toList())
        measurePair("bulk_album_deletion", albums.size, baseline, candidate)

        val remaining = albums.filter { it.id !in selected }
        val baselineMissing = { selected.filterTo(linkedSetOf()) { id -> remaining.none { it.id == id } } }
        val candidateMissing = { missingAlbumIds(selected, remaining) }
        assertEquals(baselineMissing().toList(), candidateMissing().toList())
        measurePair("bulk_album_deletion_result", albums.size, baselineMissing, candidateMissing)
    }

    @Test
    fun deletionMarkerComparisonSkipsUnchangedLibraryTraversal() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val previous = LibraryContentState(songs = songs, contentRevision = "same")
        val next = previous.copy(songs = songs.map { it.copy() }, removingSongIds = setOf(1L))
        val baseline = {
            previous.contentRevision == next.contentRevision && previous.songs == next.songs &&
                previous.albums == next.albums && previous.audiobooks == next.audiobooks &&
                previous.removingSongIds == next.removingSongIds &&
                previous.removingAlbumIds == next.removingAlbumIds &&
                previous.portableMediaIdentityRevision == next.portableMediaIdentityRevision
        }
        val candidate = { previous == next }
        assertEquals(baseline(), candidate())
        measurePair("deletion_marker_comparison", songs.size, baseline, candidate)
    }

    @Test
    fun unrelatedDeletionMarkerDoesNotRebuildTheSnapshot() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val snapshot = LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs)
        val suppressed = setOf(90_000L)
        val filter = Class.forName("elovaire.music.droidbeauty.app.data.library.LibraryRepositoryKt")
            .getDeclaredMethod("songsVisibleAfterDeletionMarkers", List::class.java, Set::class.java)
            .apply { isAccessible = true }
        val baseline = {
            LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs.filterNot { it.id in suppressed })
        }
        val candidate = {
            @Suppress("UNCHECKED_CAST")
            val visible = filter.invoke(null, songs, suppressed) as List<Song>
            if (visible === songs) snapshot else LibrarySnapshotAssembler.assembleDeduplicatedSongs(visible)
        }
        assertEquals(baseline(), candidate())
        measurePair("unrelated_deletion_marker", songs.size, baseline, candidate)
    }

    @Test
    fun pickerArtistGroupsAvoidRepeatedCaseFolding() {
        val songs = List(10_000) { index ->
            song(index.toLong() + 1L).copy(artist = "Artist ${index % 100}", title = "TRACK ${10_000 - index}")
        }
        val groups = songs.groupBy(Song::artist)
        val baseline = {
            groups.map { (name, tracks) ->
                Triple(name, tracks.map(Song::albumId).distinct().size, tracks.sortedBy { it.title.lowercase() })
            }.sortedBy { it.first.lowercase() }
        }
        val candidate = {
            groups.map { (name, tracks) ->
                Triple(name, tracks.mapTo(hashSetOf(), Song::albumId).size,
                    tracks.map { it to it.title.lowercase() }.sortedBy { it.second }.map { it.first })
            }.map { it to it.first.lowercase() }.sortedBy { it.second }.map { it.first }
        }
        repeat(2) { assertEquals(baseline(), candidate()) }
        measurePair("picker_artist_groups", songs.size, baseline, candidate)
    }

    @Test
    fun collectionNameSortingCachesCaseFoldedKeys() {
        val names = List(10_000) { "Artist ${10_000 - it}" }
        val baseline = { names.sortedBy { it.lowercase() } }
        val candidate = { names.map { it to it.lowercase() }.sortedBy { it.second }.map { it.first } }
        repeat(2) { assertEquals(baseline(), candidate()) }
        measurePair("collection_name_sort", names.size, baseline, candidate)
    }

    @Test
    fun changedLibraryDiffSkipsPathsOfMatchedSongs() {
        val songs = List(10_000) { song(it.toLong() + 1L).copy(libraryPath = "/storage/emulated/0/Music/track-$it.mp3") }
        val next = songs.toMutableList().apply { this[lastIndex] = last().copy(title = "Changed") }
        val baseline = {
            songs.forEach { LibrarySongDuplicateResolver.normalizedRealPath(it.libraryPath) }
            next.forEach { LibrarySongDuplicateResolver.normalizedRealPath(it.libraryPath) }
            LibraryChangeSetCalculator.between(songs, next)
        }
        val candidate = { LibraryChangeSetCalculator.between(songs, next) }
        repeat(2) { assertEquals(baseline(), candidate()) }
        measurePair("changed_library_diff", songs.size, baseline, candidate)
    }

    @Test
    fun unchangedSnapshotReusesPortableIdentityAtScale() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val snapshot = LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs)
        val revision = MediaIdentityResolver.portableIdentityRevision(snapshot.songs)
        val current = LibraryContentState(
            songs = snapshot.songs,
            albums = snapshot.albums,
            contentRevision = snapshot.contentRevision,
            portableMediaIdentityRevision = revision,
        )
        val publisher = LibrarySnapshotPublisher({}, { current })
        val baseline = { MediaIdentityResolver.portableIdentityRevision(snapshot.songs) }
        val candidate = { publisher.stateForSnapshot(snapshot, emptySet(), emptySet()).portableMediaIdentityRevision }
        repeat(2) { assertEquals(baseline(), candidate()) }
        measurePair("unchanged_snapshot_identity", songs.size, baseline, candidate)
    }

    @Test
    fun unchangedLibraryDiffAvoidsBuildingIdentityIndexes() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val next = songs.map { it.copy() }
        val unoptimizedInput = object : AbstractList<Song>() {
            override val size: Int get() = songs.size
            override fun get(index: Int): Song = songs[index]
            override fun equals(other: Any?): Boolean = this === other
            override fun hashCode(): Int = System.identityHashCode(this)
        }
        val baseline = { LibraryChangeSetCalculator.between(unoptimizedInput, next) }
        val candidate = { LibraryChangeSetCalculator.between(songs, next) }
        repeat(2) {
            assertEquals(baseline(), candidate())
        }
        measurePair("unchanged_library_diff", songs.size, baseline, candidate)
    }

    @Test
    fun portableIdentityChecksAvoidNormalizingUnchangedFields() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val edited = songs.map { it.copy(audioQuality = "Lossless") }
        val whitespace = Regex("\\s+")
        fun normalize(value: String) = value.trim().lowercase(Locale.ROOT).replace(whitespace, " ")
        val baseline = {
            songs.indices.count { index ->
                val before = songs[index]
                val after = edited[index]
                before.id != after.id ||
                    before.durationMs.takeIf { it > 0L } != after.durationMs.takeIf { it > 0L } ||
                    normalize(before.title) != normalize(after.title) ||
                    normalize(before.artist) != normalize(after.artist) ||
                    normalize(before.album) != normalize(after.album) ||
                    normalize(before.albumArtist.orEmpty()) != normalize(after.albumArtist.orEmpty()) ||
                    normalize(before.fileName).takeIf(String::isNotBlank).orEmpty() !=
                    normalize(after.fileName).takeIf(String::isNotBlank).orEmpty() ||
                    before.trackNumber.takeIf { it > 0 } != after.trackNumber.takeIf { it > 0 } ||
                    before.discNumber.takeIf { it > 0 } != after.discNumber.takeIf { it > 0 }
            }
        }
        val candidate = { songs.indices.count { MediaIdentityResolver.portableIdentityChanged(songs[it], edited[it]) } }
        assertEquals(0, baseline())
        assertEquals(baseline(), candidate())
        repeat(2) {
            baseline()
            candidate()
        }
        measurePair("portable_identity_checks", songs.size, baseline, candidate)
    }

    @Test
    fun pickerSongIndexReusesRepeatedMetadata() {
        val songs = List(10_000) { song(it.toLong() + 1L) }
        val baseline = { songs.map(Song::toSearchableSong) }
        val candidate = { buildPickerSongSearchIndex(songs) }
        assertEquals(baseline(), candidate())
        repeat(2) {
            baseline()
            candidate()
        }
        measurePair("picker_song_index", songs.size, baseline, candidate)
        val uniqueMetadata = songs.map { song ->
            song.copy(artist = "Artist ${song.id}", album = "Album ${song.id}", albumArtist = "Album artist ${song.id}")
        }
        val uniqueBaseline = { uniqueMetadata.map(Song::toSearchableSong) }
        val uniqueCandidate = { buildPickerSongSearchIndex(uniqueMetadata) }
        assertEquals(uniqueBaseline(), uniqueCandidate())
        measurePair("picker_song_index_unique_metadata", songs.size, uniqueBaseline, uniqueCandidate)
    }

    @Test
    fun reusableArtistSearchIndexMatchesAndAvoidsRepeatedNormalization() {
        val artists = List(10_000) { index ->
            "Artist $index" to listOf(song(index.toLong() + 1L))
        }
        val index = buildPickerArtistSearchIndex(artists, { it.first }, { it.second }, { it.second.size })
        val queries = listOf("artist 10", "artist 100", "artist 1000").map(NormalizedSearchQuery::from)
        val baseline = {
            queries.map { query -> searchArtistsForPicker(artists, query, { it.first }, { it.second }, { it.second.size }) }
        }
        val candidate = { queries.map { query -> searchIndexedArtistsForPicker(index, query) { it.first } } }
        assertEquals(baseline(), candidate())
        repeat(2) {
            baseline()
            candidate()
        }
        measurePair("artist_search_reuse", artists.size, baseline, candidate)
    }

    @Test
    fun limitedSmartPlaylistsMatchFullSortAtScale() {
        val songs = List(50_000) { song(it.toLong() + 1) }.shuffled(kotlin.random.Random(17))
        for (field in listOf(SmartPlaylistSortField.Title, SmartPlaylistSortField.DateAdded, SmartPlaylistSortField.Random)) {
            val definition = SmartPlaylist(
                id = 42L,
                name = "Scale",
                sort = SmartPlaylistSort(field, SortDirection.Descending),
                createdAtMs = 1L,
                updatedAtMs = 1L,
            )
            val limited = definition.copy(limit = 25)
            val baseline = { SmartPlaylistEngine.resolve(definition, songs, emptySet(), emptyMap()).songs.take(25) }
            val candidate = { SmartPlaylistEngine.resolve(limited, songs, emptySet(), emptyMap()).songs }
            repeat(2) { assertEquals(baseline(), candidate()) }
            measurePair("smart_playlist_$field", songs.size, baseline, candidate)
        }
    }

    @Test
    fun playCountDecodingAvoidsRetainingSplitFieldsAtScale() {
        val counts = (1..50_000).joinToString(",") { "$it:${it % 37}" }
        val baseline = {
            val entries = counts.split(",", limit = 100_001)
            val decoded = LinkedHashMap<Long, Int>(entries.size)
            entries.forEach { entry ->
                val parts = entry.split(":", limit = 3)
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()?.takeIf(::isValidMediaId)
                    val count = parts[1].toIntOrNull()?.coerceAtLeast(0)
                    if (id != null && count != null) decoded[id] = count
                }
            }
            decoded
        }
        val candidate = { PreferenceCollectionCodec.deserializePlayCounts(counts) }
        repeat(2) { assertEquals(baseline(), candidate()) }
        measurePair("play_count_decode", 50_000, baseline, candidate)
    }

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

    @Test
    fun searchIndexBuildAvoidsIntermediateMusicSongProjectionAtScale() {
        val songs = List(30_000) { index ->
            val song = song(index.toLong() + 1L)
            if (index % 5 == 0) song.copy(mediaKind = AudioMediaKind.Audiobook) else song
        }
        val baseline = {
            SearchLibrarySnapshot(
                songs = songs.filter { it.mediaKind == AudioMediaKind.Music },
                albums = emptyList(),
                revision = "search-scale",
            ).toSearchIndex("search-scale")
        }
        val candidate = {
            SearchLibrarySnapshot(
                songs = songs,
                albums = emptyList(),
                revision = "search-scale",
            ).toSearchIndex("search-scale")
        }
        assertEquals(baseline(), candidate())
        repeat(2) {
            baseline()
            candidate()
        }
        val timingsMs = Array(2) { mutableListOf<Double>() }
        val allocations = Array(2) { mutableListOf<Long>() }
        repeat(7) { sample ->
            for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                lateinit var index: SearchIndex
                timingsMs[variant] += measureNanoTime {
                    index = if (variant == 0) baseline() else candidate()
                } / 1_000_000.0
                allocations[variant] += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
                assertEquals(24_000, index.songs.size)
            }
        }
        Log.i(
            "LibraryScale",
            "paired_search_projection songs=${songs.size} baselineMs=${timingsMs[0]} " +
                "candidateMs=${timingsMs[1]} baselineBytes=${allocations[0]} candidateBytes=${allocations[1]}",
        )
        assertTrue(
            "Streaming music selection should reduce index allocations: $allocations",
            medianLong(allocations[1]) < medianLong(allocations[0]),
        )
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

    private fun median(values: List<Double>): Double = values.sorted()[values.size / 2]

    private fun medianLong(values: List<Long>): Long = values.sorted()[values.size / 2]

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
