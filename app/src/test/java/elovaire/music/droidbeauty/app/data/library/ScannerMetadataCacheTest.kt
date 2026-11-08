package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

class ScannerMetadataCacheTest {
    @Test
    fun prime_keepsMetadataSeparateForDifferentUrisWithTheSameMediaStoreId() {
        val first = song(uri = "content://media/external/audio/media/7", title = "First")
        val second = song(uri = "content://media/secondary/audio/media/7", title = "Second")
        val cache = ScannerMetadataCache()

        cache.prime(listOf(first, second))

        assertEquals("First", cache[MediaIdentityResolver.sourceKey(first)]?.metadata?.title)
        assertEquals("Second", cache[MediaIdentityResolver.sourceKey(second)]?.metadata?.title)
    }

    @Test
    fun invalidateSongIds_removesEveryCachedUriForTheSong() {
        val first = song(uri = "content://media/external/audio/media/7", title = "First")
        val second = song(uri = "content://media/secondary/audio/media/7", title = "Second")
        val cache = ScannerMetadataCache()
        cache.prime(listOf(first, second))

        cache.invalidateSongIds(listOf(7L))

        assertNull(cache[MediaIdentityResolver.sourceKey(first)])
        assertNull(cache[MediaIdentityResolver.sourceKey(second)])
    }

    @Test
    fun retainOnly_dropsRowsThatDisappearedFromTheScan() {
        val first = song(uri = "content://media/external/audio/media/7", title = "First")
        val second = song(uri = "content://media/secondary/audio/media/7", title = "Second")
        val cache = ScannerMetadataCache()
        cache.prime(listOf(first, second))

        cache.retainOnly(setOf(MediaIdentityResolver.sourceKey(first)))

        assertTrue(cache[MediaIdentityResolver.sourceKey(first)] != null)
        assertNull(cache[MediaIdentityResolver.sourceKey(second)])
    }

    @Test
    fun memoryPressure_clearsReconstructableMetadataCache() {
        val cached = song(uri = "content://media/external/audio/media/7", title = "First")
        val cache = ScannerMetadataCache()
        cache.prime(listOf(cached))

        cache.onMemoryPressure(MemoryPressure.Critical)

        assertNull(cache[MediaIdentityResolver.sourceKey(cached)])
    }

    @Test
    fun concurrentScanAndInvalidationOperationsRemainSafe() {
        val cache = ScannerMetadataCache()
        val failures = ConcurrentLinkedQueue<Throwable>()
        val workers = List(6) { worker ->
            thread {
                try {
                    repeat(500) { iteration ->
                        val song = song(
                            uri = "content://media/external/audio/media/${worker * 500 + iteration + 1}",
                            title = "Track $iteration",
                        )
                        val key = MediaIdentityResolver.sourceKey(song)
                        when (worker % 4) {
                            0 -> cache.prime(listOf(song))
                            1 -> cache[key]
                            2 -> cache.retainOnly(setOf(key))
                            else -> cache.invalidateSongIds(listOf(song.id))
                        }
                        if (iteration % 100 == 0) cache.prime(listOf(song))
                    }
                } catch (failure: Throwable) {
                    failures += failure
                }
            }
        }

        workers.forEach(Thread::join)

        assertTrue("Concurrent cache operations failed: $failures", failures.isEmpty())
    }

    @Test
    fun cacheMatchRejectsSameTimestampWhenSizeOrDurationChanged() {
        val cached = CachedSongMetadata(
            songId = 1L,
            fileName = "song.flac",
            filePath = "/music/song.flac",
            dateAddedSeconds = 1L,
            dateModifiedSeconds = 2L,
            isEnriched = true,
            metadata = SongMetadata(null, null, null, null, null, null, null, "FLAC", null, null, null, null),
            fileSizeBytes = 100L,
            durationMs = 1_000L,
        )

        assertTrue(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 100L, 1_000L, true))
        assertFalse(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 101L, 1_000L, true))
        assertFalse(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 100L, 2_000L, true))
    }

    @Test
    fun lightweightRefreshDoesNotEraseKnownAudioQuality() {
        val cache = ScannerMetadataCache()
        val key = "media|external|7"
        val enriched = CachedSongMetadata(
            songId = 7L,
            fileName = "song.mp3",
            filePath = "/music/song.mp3",
            dateAddedSeconds = 1L,
            dateModifiedSeconds = 2L,
            isEnriched = true,
            metadata = SongMetadata(null, null, null, null, null, null, null, "MP3", "320/44.1kHz", null, null, null),
        )
        cache.put(key, enriched)

        val lightweight = enriched.copy(
            isEnriched = false,
            metadata = enriched.metadata.copy(quality = null),
        )
        val stored = cache.put(key, lightweight)

        assertEquals("320/44.1kHz", stored.metadata.quality)
        assertEquals("320/44.1kHz", cache[key]?.metadata?.quality)
    }

    private fun song(
        uri: String,
        title: String,
    ): Song {
        return Song(
            id = 7L,
            title = title,
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = "Genre",
            audioFormat = "FLAC",
            audioQuality = null,
            fileName = "$title.flac",
            albumId = 1L,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            dateModifiedSeconds = null,
            libraryPath = null,
            uri = TestUri(uri),
            artUri = null,
            metadataResolved = true,
            albumArtist = "Artist",
            volumeNormalization = null,
        )
    }
}
