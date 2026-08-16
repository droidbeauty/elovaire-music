package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun cacheMatchRejectsSameTimestampWhenSizeOrDurationChanged() {
        val cached = CachedSongMetadata(
            songId = 1L,
            fileName = "song.flac",
            filePath = "/music/song.flac",
            dateAddedSeconds = 1L,
            dateModifiedSeconds = 2L,
            isEnriched = true,
            metadata = SongMetadata(null, null, null, null, null, null, "FLAC", null, null, null, null),
            fileSizeBytes = 100L,
            durationMs = 1_000L,
        )

        assertTrue(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 100L, 1_000L, true))
        assertFalse(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 101L, 1_000L, true))
        assertFalse(cached.matches("song.flac", "/music/song.flac", 1L, 2L, 100L, 2_000L, true))
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
