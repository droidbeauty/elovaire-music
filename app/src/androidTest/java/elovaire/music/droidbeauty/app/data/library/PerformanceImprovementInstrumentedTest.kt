package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.SearchableAlbum
import elovaire.music.droidbeauty.app.domain.search.buildSuggestedAlbums
import elovaire.music.droidbeauty.app.domain.search.toSearchableAlbum
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgressStore
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.resolveMediaButtonResumption
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.data.library.network.NetworkFileEntry
import elovaire.music.droidbeauty.app.data.library.network.networkArtworkCacheKey
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.PreferenceCollectionCodec
import elovaire.music.droidbeauty.app.ui.screens.AudiobookSortMode
import elovaire.music.droidbeauty.app.ui.screens.sortAudiobooksForDisplay
import elovaire.music.droidbeauty.app.ui.screens.artistAlbumsFor
import elovaire.music.droidbeauty.app.ui.screens.formatDuration
import elovaire.music.droidbeauty.app.ui.screens.AudiobookDescriptionPreview
import elovaire.music.droidbeauty.app.ui.screens.audiobookDescriptionPreview
import java.util.Locale
import java.util.PriorityQueue
import kotlin.system.measureNanoTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerformanceImprovementInstrumentedTest {
    @Test
    fun legacyMediaKindsAvoidExceptionBasedDecoding() {
        val values = List(3_000) { index -> listOf("", "unknown", "Music", "Audiobook")[index % 4] }
        val baseline = { values.map { runCatching { AudioMediaKind.valueOf(it) }.getOrNull() == AudioMediaKind.Audiobook } }
        val candidate = { values.map { it == AudioMediaKind.Audiobook.name } }
        assertEquals(baseline(), candidate())
        measurePair("legacy_media_kind", baseline, candidate)
    }

    @Test
    fun mediaButtonResumptionIndexesOnlyRequestedSongs() {
        val songs = (1L..10_000L).map(::song)
        val ids = (1L..20L).toList()
        val persisted = PersistedPlaybackSession(ids, 1L, 0, 0L, PlaybackRepeatMode.Off, false, null, false, 1L)
        val baseline = {
            val songsById = songs.associateBy(Song::id)
            ids.mapNotNull(songsById::get)
        }
        val candidate = { resolveMediaButtonResumption(persisted, songs)?.queue?.queue }
        assertEquals(baseline(), candidate())
        measurePair("media_button_resumption", baseline, candidate)
    }

    @Test
    fun malformedPreferenceRecordsStopSplittingAfterTheFieldLimit() {
        val value = "key\u001fAlbum\u001ftitle\u001fsubtitle\u001f\u001f\u001f" + "\u001f".repeat(20_000)
        val baseline = {
            val parts = value.split("\u001f").map { it }
            if (parts.size == 7) parts else null
        }
        val candidate = { PreferenceCollectionCodec.deserializeSearchHistory(value) }
        assertEquals(null, baseline())
        assertEquals(null, candidate())
        measurePair("malformed_preferences", baseline, candidate)
    }

    @Test
    fun audiobookDescriptionPreviewStopsAfterTheVisibleSentences() {
        val description = (1..10_000).joinToString(" ") { "Sentence $it." }
        val baseline = {
            val normalized = description.trim()
            val preview = normalized.split(Regex("(?<=[.!?])\\s+"))
                .filter(String::isNotBlank).take(5).joinToString(" ")
                .ifBlank { normalized }.take(900).trim()
            AudiobookDescriptionPreview(preview, preview.length < normalized.length)
        }
        val candidate = { audiobookDescriptionPreview(description) }
        assertEquals(baseline(), candidate())
        measurePair("audiobook_description_preview", baseline, candidate)
    }

    @Test
    fun durationLabelsPreserveLocalizedDigitsWithLessAllocation() {
        val durations = List(1_000) { index -> (index + 1L) * 7_321L } + Long.MAX_VALUE
        fun baseline(duration: Long): String {
            val seconds = duration / 1_000L
            return if (seconds >= 3_600L) {
                "%d:%02d:%02d".format(seconds / 3_600L, seconds % 3_600L / 60L, seconds % 60L)
            } else {
                "%d:%02d".format(seconds / 60L, seconds % 60L)
            }
        }
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            val boundaryDurations = listOf(1L, 61_000L, 3_661_000L, Long.MAX_VALUE)
            Locale.getAvailableLocales().forEach { locale ->
                Locale.setDefault(Locale.Category.FORMAT, locale)
                assertEquals("$locale", boundaryDurations.map(::baseline), boundaryDurations.map(::formatDuration))
            }
            listOf("en-US", "ar-EG", "fa-IR", "bn-BD", "ar-EG-u-nu-latn").forEach { tag ->
                Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag(tag))
                assertEquals(durations.map(::baseline), durations.map(::formatDuration))
            }
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            measurePair("duration_labels", { durations.map(::baseline) }, { durations.map(::formatDuration) })
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun networkArtworkKeysPreserveCacheIdentityWithLessAllocation() {
        val entries = List(256) { index ->
            NetworkFileEntry("album-$index/cover.jpg", false, sizeBytes = 1024L, modifiedAtMs = index.toLong())
        }
        val baseline = {
            entries.map { entry ->
                val fields = listOf("source", entry.path, entry.sizeBytes?.toString(), entry.modifiedAtMs?.toString(), entry.etag)
                val identity = fields.joinToString("") { value -> value?.let { "${it.length}:$it" } ?: "-:" }
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(identity.toByteArray(Charsets.UTF_8))
                    .joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }
            }
        }
        val candidate = { entries.map { networkArtworkCacheKey("source", it) } }
        assertEquals(baseline(), candidate())
        measurePair("network_artwork_keys", baseline, candidate)
    }

    @Test
    fun emptyAudiobookProgressLookupsAvoidHashingTheCatalog() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, ElovaireDatabase::class.java).build()
        val store = RoomUserDataStore(context, database.userDataDao(), database = database)
        try {
            withTimeout(10_000L) { store.userDataReadiness.first { it == UserDataReadiness.Ready } }
            val keys = List(1_000) { "book-$it" }
            val baseline = { keys.map { AudiobookProgressStore.storageKey(it); null } }
            val candidate = { keys.map(store::load) }
            assertEquals(baseline(), candidate())
            measurePair("empty_audiobook_progress", baseline, candidate)
        } finally {
            store.release().await()
            database.close()
        }
    }

    @Test
    fun audiobookProgressKeysPreserveStoredHashesWithLessAllocation() {
        val keys = List(1_000) { index -> "book-$index-作者" }
        val baseline = {
            keys.map { key ->
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(key.toByteArray(Charsets.UTF_8))
                    .joinToString("") { byte -> "%02x".format(byte) }
            }
        }
        val candidate = { keys.map(AudiobookProgressStore::storageKey) }
        assertEquals(baseline(), candidate())
        measurePair("audiobook_progress_keys", baseline, candidate)
    }

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

    @Test
    fun audiobookDisplaySortPreservesEveryOrderingAndAvoidsRepeatedNormalization() {
        val books = List(6_000) { index ->
            Audiobook("key-$index", "BOOK ${index % 211}", "AUTHOR ${index % 71}", null, 0, emptyList())
        }.shuffled(kotlin.random.Random(123))
        val progress = books.filterIndexed { index, _ -> index % 3 != 0 }.associate { book ->
            val index = book.stableKey.substringAfter('-').toInt()
            book.stableKey to AudiobookProgress(index.toLong(), if (index % 2 == 0) 1L else 0L, index % 5 == 0, 1L)
        }
        fun baseline(mode: AudiobookSortMode): List<Audiobook> {
            val byTitle = compareBy<Audiobook> { it.title.lowercase(Locale.ROOT) }
            val comparator = when (mode) {
                AudiobookSortMode.Author -> compareBy<Audiobook> { it.author.lowercase(Locale.ROOT) }.then(byTitle)
                AudiobookSortMode.Title -> byTitle.thenBy { it.author.lowercase(Locale.ROOT) }
                AudiobookSortMode.AlreadyListened ->
                    compareBy<Audiobook> { if (progress[it.stableKey]?.completed == true) 0 else 1 }.then(byTitle)
                AudiobookSortMode.NeverListened ->
                    compareBy<Audiobook> { if ((progress[it.stableKey]?.positionMs ?: 0L) == 0L) 0 else 1 }.then(byTitle)
            }
            return books.sortedWith(comparator)
        }
        AudiobookSortMode.entries.forEach { mode ->
            assertEquals(baseline(mode), sortAudiobooksForDisplay(books, progress, mode))
        }
        measurePair(
            "audiobook_display_sort",
            { baseline(AudiobookSortMode.Title) },
            { sortAudiobooksForDisplay(books, progress, AudiobookSortMode.Title) },
        )
    }

    @Test
    fun artistAlbumSortingPreservesStableOrderAndNormalizesTitlesOnce() {
        val albums = List(6_000) { index ->
            Album(index.toLong(), "ALBUM ${index % 383}", if (index % 5 == 0) "Other" else "Artist", null, 0, 0, emptyList())
        }.shuffled(kotlin.random.Random(234))
        val baseline = { albums.filter { it.artist.equals("artist", ignoreCase = true) }.sortedBy { it.title.lowercase() } }
        val candidate = { artistAlbumsFor(albums, "artist") }
        assertEquals(baseline(), candidate())
        assertEquals(emptyList<Album>(), artistAlbumsFor(albums, "Missing"))
        measurePair("artist_album_sort", baseline, candidate)
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
