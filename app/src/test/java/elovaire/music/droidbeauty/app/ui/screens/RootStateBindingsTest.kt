package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RootStateBindingsTest {
    @Test
    fun libraryUiStatePreservesWhetherTheLibraryScanIsAuthoritative() {
        assertFalse(
            libraryUiStateOf(
                content = LibraryContentState(),
                scan = LibraryScanState(permissionGranted = true),
            ).isAuthoritative,
        )
        assertTrue(
            libraryUiStateOf(
                content = LibraryContentState(),
                scan = LibraryScanState(permissionGranted = true, isAuthoritative = true),
            ).isAuthoritative,
        )
    }

    @Test
    fun homeDoesNotClaimLibraryIsEmptyWhenMusicExistsOrScanIsIncomplete() {
        assertFalse(
            shouldShowHomeEmptyLibraryState(
                isLibraryLoading = false,
                isLibraryAuthoritative = true,
                hasMusic = true,
                hasHomeContent = false,
            ),
        )
        assertFalse(
            shouldShowHomeEmptyLibraryState(
                isLibraryLoading = false,
                isLibraryAuthoritative = false,
                hasMusic = false,
                hasHomeContent = false,
            ),
        )
        assertTrue(
            shouldShowHomeEmptyLibraryState(
                isLibraryLoading = false,
                isLibraryAuthoritative = true,
                hasMusic = false,
                hasHomeContent = false,
            ),
        )
    }

    @Test
    fun buildRootSongIndexes_preservesSourceOrderInBothIndexes() {
        val first = song(1L, 42L)
        val second = song(2L, 42L)
        val third = song(3L, 7L)

        val indexes = buildRootSongIndexes(listOf(second, first, third))

        assertEquals(listOf(2L, 1L, 3L), indexes.songsById.keys.toList())
        assertEquals(listOf(2L, 1L), indexes.songsByAlbumId.getValue(42L).map(Song::id))
        assertSame(second, indexes.songsByAlbumId.getValue(42L).first())
    }

    @Test
    fun resolveRoutedAlbumUsesIndexesAndPreservesAlbumOrderForSongFallback() {
        val firstSong = song(1L, 10L)
        val secondSong = song(2L, 20L)
        val firstAlbum = album(10L, firstSong)
        val secondAlbum = album(20L, secondSong)
        val albums = listOf(firstAlbum, secondAlbum)

        assertSame(
            firstAlbum,
            resolveRoutedAlbum(
                albumId = 99L,
                albums = albums,
                albumsById = albums.associateBy(Album::id),
                routedSongIds = linkedSetOf(secondSong.id, firstSong.id),
            ),
        )
        assertSame(
            secondAlbum,
            resolveRoutedAlbum(
                albumId = 20L,
                albums = albums,
                albumsById = albums.associateBy(Album::id),
                routedSongIds = emptySet(),
            ),
        )
    }

    private fun album(id: Long, song: Song) = Album(
        id = id,
        title = "Album $id",
        artist = "Artist",
        artUri = null,
        songCount = 1,
        durationMs = song.durationMs,
        songs = listOf(song),
    )

    private fun song(id: Long, albumId: Long) = Song(
        id = id,
        title = "Song $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = albumId,
        durationMs = 1L,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = id,
        uri = TestUri("content://root/$id"),
        artUri = null,
    )
}
