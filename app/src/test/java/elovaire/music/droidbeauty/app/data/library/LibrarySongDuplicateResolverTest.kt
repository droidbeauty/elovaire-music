package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySongDuplicateResolverTest {
    @Test
    fun mediaStoreSongWinsOverSafSongWithSameRealPath() {
        val result = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = listOf(song(id = 1L, path = "/storage/emulated/0/Music/Album/Track.flac")),
            safSongs = listOf(song(id = -1L, path = "/storage/emulated/0/Music/Album/Track.flac")),
        )

        assertEquals(listOf(1L), result.map(Song::id))
    }

    @Test
    fun safSyntheticPathDoesNotMatchRealPathByAccident() {
        val result = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = listOf(song(id = 1L, artist = "Media artist")),
            safSongs = listOf(song(id = -1L, path = "saf/abc/Album/Track.flac", artist = "Saf artist")),
        )

        assertEquals(listOf(1L, -1L), result.map(Song::id))
    }

    @Test
    fun metadataAloneDoesNotMergeWhenPathUnavailable() {
        val result = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = listOf(song(id = 1L, path = null)),
            safSongs = listOf(song(id = -1L, path = "saf/abc/Album/Track.flac")),
        )

        assertEquals(listOf(1L, -1L), result.map(Song::id))
    }

    @Test
    fun differentFoldersWithSameMetadataRemainWhenRealPathsDiffer() {
        val result = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = listOf(song(id = 1L, path = "/storage/emulated/0/Music/One/Track.flac")),
            safSongs = listOf(song(id = -1L, path = "/storage/emulated/0/Music/Two/Track.flac")),
        )

        assertEquals(listOf(1L, -1L), result.map(Song::id))
    }

    @Test
    fun distinctSafSongsWithSameMetadataRemain() {
        val result = LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(
            mediaStoreSongs = emptyList(),
            safSongs = listOf(
                song(id = -1L, path = "saf/one/Album/Track.flac"),
                song(id = -2L, path = "saf/two/Album/Track.flac"),
            ),
        )

        assertEquals(listOf(-1L, -2L), result.map(Song::id))
    }

    @Test
    fun loadedSnapshotKeepsAmbiguousSourcesSeparate() {
        val result = LibrarySongDuplicateResolver.dedupeLoadedSnapshotSongs(
            listOf(
                song(id = -1L, path = "saf/abc/Album/Track.flac"),
                song(id = 1L, path = null),
            ),
        )

        assertEquals(listOf(1L, -1L), result.map(Song::id))
    }

    @Test
    fun documentIdentityUsesAuthorityAndDocumentId() {
        val first = LibrarySongDuplicateResolver.documentIdentity(
            "com.android.externalstorage.documents",
            "primary:Music/Album/Track.flac",
        )
        val second = LibrarySongDuplicateResolver.documentIdentity(
            "COM.ANDROID.EXTERNALSTORAGE.DOCUMENTS",
            "PRIMARY:MUSIC/ALBUM/TRACK.FLAC",
        )

        assertTrue(first?.contains("primary:Music/Album/Track.flac") == true)
        assertTrue(second?.contains("PRIMARY:MUSIC/ALBUM/TRACK.FLAC") == true)
    }

    @Test
    fun matchingCheapEvidenceIsOnlyProbableUntilContentIsProven() {
        val evidence = LibrarySongDuplicateResolver.duplicateEvidence(
            song(id = 1L, path = "/storage/emulated/0/Music/One/Track.flac"),
            song(id = -1L, path = "/storage/emulated/0/Music/Two/Track.flac"),
        )

        assertEquals(
            LibrarySongDuplicateResolver.DuplicateConfidence.ProbableDuplicate,
            evidence.confidence,
        )
    }

    @Test
    fun sameSourceProducesLogicalTrackEvidence() {
        val evidence = LibrarySongDuplicateResolver.duplicateEvidence(
            song(id = 1L),
            song(id = 1L),
        )

        assertEquals(
            LibrarySongDuplicateResolver.DuplicateConfidence.SameSource,
            evidence.confidence,
        )
        assertEquals(1L, evidence.logicalTrackId?.value)
    }

    private fun song(
        id: Long,
        path: String? = "/storage/emulated/0/Music/Album/Track.flac",
        artist: String = "Artist",
    ): Song {
        return Song(
            id = id,
            title = "Track",
            isExplicit = false,
            artist = artist,
            album = "Album",
            releaseYear = null,
            genre = "",
            audioFormat = "FLAC",
            audioQuality = null,
            fileName = "Track.flac",
            albumId = if (id > 0L) 1L else -1L,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            dateModifiedSeconds = null,
            libraryPath = path,
            uri = TestUri("content://song/$id"),
            artUri = null,
            metadataResolved = true,
            albumArtist = artist,
            volumeNormalization = null,
        )
    }
}
