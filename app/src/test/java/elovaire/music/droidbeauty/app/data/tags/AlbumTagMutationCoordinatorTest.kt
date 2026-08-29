package elovaire.music.droidbeauty.app.data.tags

import elovaire.music.droidbeauty.app.testing.FakeAlbumTagEditor
import elovaire.music.droidbeauty.app.testing.testAlbum
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumTagMutationCoordinatorTest {
    @Test
    fun artworkInvalidationIsOwnedByMutationCoordinator() = runTest {
        val song = testSong()
        val album = testAlbum(songs = listOf(song))
        val editor = FakeAlbumTagEditor(
            result = TagEditApplyResult(
                editedSongIds = listOf(song.id),
                editedUris = listOf(song.uri),
                editedFilePaths = emptyList(),
                editedSongs = listOf(song),
                artworkChanged = true,
            ),
        )
        var invalidatedUris: Collection<android.net.Uri?> = emptyList()
        val coordinator = AlbumTagMutationCoordinator(
            editor = editor,
            artworkInvalidator = AlbumTagArtworkInvalidator { invalidatedUris = it },
        )

        coordinator.applyEdits(
            request = AlbumTagEditRequest(
                album = album,
                albumTitle = TagFieldEdit.Unchanged,
                albumArtist = TagFieldEdit.Unchanged,
                releaseYear = TagFieldEdit.Unchanged,
                genre = TagFieldEdit.Unchanged,
                coverArtUri = null,
                tracks = emptyList(),
            ),
            writeConsentGranted = false,
        )

        assertEquals(listOf(song.artUri), invalidatedUris.filterNotNull())
    }
}
