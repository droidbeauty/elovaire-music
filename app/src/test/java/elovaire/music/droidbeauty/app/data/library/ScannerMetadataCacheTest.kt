package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScannerMetadataCacheTest {
    @Test
    fun prime_keepsMetadataSeparateForDifferentUrisWithTheSameMediaStoreId() {
        val first = song(uri = "content://media/external/audio/media/7", title = "First")
        val second = song(uri = "content://media/secondary/audio/media/7", title = "Second")
        val cache = ScannerMetadataCache()

        cache.prime(listOf(first, second))

        assertEquals("First", cache[first.uri.toString()]?.metadata?.title)
        assertEquals("Second", cache[second.uri.toString()]?.metadata?.title)
    }

    @Test
    fun invalidateSongIds_removesEveryCachedUriForTheSong() {
        val first = song(uri = "content://media/external/audio/media/7", title = "First")
        val second = song(uri = "content://media/secondary/audio/media/7", title = "Second")
        val cache = ScannerMetadataCache()
        cache.prime(listOf(first, second))

        cache.invalidateSongIds(listOf(7L))

        assertNull(cache[first.uri.toString()])
        assertNull(cache[second.uri.toString()])
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
