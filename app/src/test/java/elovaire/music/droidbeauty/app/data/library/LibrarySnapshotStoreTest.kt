package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibrarySnapshotStoreTest {
    @Test
    fun songSignatureChecksum_includesDateModifiedSeconds() {
        val baseline = songSignatureChecksum(
            id = 1L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 111L,
        )

        val modified = songSignatureChecksum(
            id = 1L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 222L,
        )

        assertNotEquals(baseline, modified)
    }

    @Test
    fun songSignatureChecksum_treatsMissingDateModifiedAsZero() {
        val withoutModified = songSignatureChecksum(
            id = 7L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = null,
        )

        val zeroModified = songSignatureChecksum(
            id = 7L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 0L,
        )

        assertEquals(withoutModified, zeroModified)
    }

    @Test
    fun finiteFloatOrNull_rejectsInvalidSnapshotNumbers() {
        assertNull(finiteFloatOrNull(Double.NaN))
        assertNull(finiteFloatOrNull(Double.POSITIVE_INFINITY))
        assertEquals(0.92f, finiteFloatOrNull(0.92) ?: 0f, 0.001f)
    }

    @Test
    fun finiteFloatOrNull_acceptsFiniteSnapshotNumbers() {
        assertEquals(-4f, finiteFloatOrNull(-4.0) ?: 0f, 0.001f)
    }

    @Test
    fun signatureValidation_rejectsChangedCachedSongs() {
        val song = snapshotSong(id = 1L, modifiedSeconds = 10L)
        val signature = signatureFromSongs(listOf(song), "music")

        assertEquals(true, isLibrarySignatureValid(signature, listOf(song)))
        assertEquals(false, isLibrarySignatureValid(signature, listOf(song.copy(dateModifiedSeconds = 11L))))
    }

    @Test
    fun snapshotSongValidation_rejectsUnplayableCachedRows() {
        assertEquals(true, isValidSnapshotSong(snapshotSong(id = 1L, modifiedSeconds = 10L)))
        assertEquals(false, isValidSnapshotSong(snapshotSong(id = 0L, modifiedSeconds = 10L)))
        assertEquals(
            false,
            isValidSnapshotSong(snapshotSong(id = 1L, modifiedSeconds = 10L).copy(durationMs = 0L)),
        )
    }

    private fun snapshotSong(id: Long, modifiedSeconds: Long): Song {
        return Song(
            id = id,
            title = "Track",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = "Genre",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "track.mp3",
            albumId = 2L,
            durationMs = 1_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            dateModifiedSeconds = modifiedSeconds,
            uri = TestUri("content://media/external/audio/media/$id"),
            artUri = null,
        )
    }
}
