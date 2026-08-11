package elovaire.music.droidbeauty.app.ui.screens.tags

import android.net.TestUri
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.data.tags.mutatedUris
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class AlbumTagWritePermissionStateTest {
    @Test
    fun begin_createsOnePendingOperationWithTheMutatedUri() {
        val request = request()
        val state = AlbumTagWritePermissionState(OperationIdGenerator { "operation-1" })

        val pending = state.begin(request)

        assertEquals("operation-1", pending?.operationId)
        val mutatedUris = pending?.request?.mutatedUris().orEmpty()
        assertEquals(1, mutatedUris.size)
        assertSame(request.album.songs.single().uri, mutatedUris.single())
        assertNull(state.begin(request))
    }

    @Test
    fun consume_ignoresStaleAndDuplicateResults() {
        val request = request()
        val state = AlbumTagWritePermissionState(OperationIdGenerator { "operation-1" })
        state.begin(request)

        assertNull(state.consume("stale-operation"))
        assertSame(request, state.consume("operation-1")?.request)
        assertNull(state.consume("operation-1"))
    }

    @Test
    fun consumeForDeniedOrLaunchFailureLeavesNoWritableRequest() {
        val request = request()
        val state = AlbumTagWritePermissionState(OperationIdGenerator { "operation-1" })
        state.begin(request)

        assertSame(request, state.consume("operation-1")?.request)
        assertNull(state.pending("operation-1"))
    }

    private fun request(): AlbumTagEditRequest {
        val song = Song(
            id = 1L,
            title = "Original",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = 2024,
            genre = "Genre",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "song.mp3",
            albumId = 10L,
            durationMs = 60_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            uri = TestUri("content://media/external/audio/media/1"),
            artUri = null,
            albumArtist = "Artist",
        )
        val album = Album(
            id = 10L,
            title = "Album",
            artist = "Artist",
            artUri = null,
            songCount = 1,
            durationMs = song.durationMs,
            songs = listOf(song),
        )
        return AlbumTagEditRequest(
            album = album,
            albumTitle = TagFieldEdit.Unchanged,
            albumArtist = TagFieldEdit.Unchanged,
            releaseYear = TagFieldEdit.Unchanged,
            genre = TagFieldEdit.Unchanged,
            coverArtUri = null,
            tracks = listOf(
                EditableAlbumTrack(
                    songId = song.id,
                    title = "Changed",
                    artist = song.artist,
                    trackNumber = 1,
                    discNumber = 1,
                ),
            ),
        )
    }
}
