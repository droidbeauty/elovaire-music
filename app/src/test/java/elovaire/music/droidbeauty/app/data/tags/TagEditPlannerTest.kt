package elovaire.music.droidbeauty.app.data.tags

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TagEditPlannerTest {
    private val planner = TagEditPlanner

    @Test
    fun plansFor_noOpRequestProducesNoPlans() {
        val request = request()

        assertTrue(planner.plansFor(request).isEmpty())
    }

    @Test
    fun plansFor_albumLevelEditAffectsAllTracks() {
        val request = request(
            albumTitle = TagFieldEdit.Value("New Album"),
        )

        assertEquals(listOf(1L, 2L), planner.plansFor(request).map { it.song.id })
    }

    @Test
    fun plansFor_trackLevelEditAffectsOnlyChangedTrack() {
        val request = request(
            tracks = listOf(track(songId = 2L, title = "Changed")),
        )

        assertEquals(listOf(2L), planner.plansFor(request).map { it.song.id })
    }

    @Test
    fun plansFor_artworkEditAffectsAllTracks() {
        val request = request(
            coverArtBytes = byteArrayOf(1, 2, 3),
        )

        assertEquals(listOf(1L, 2L), planner.plansFor(request).map { it.song.id })
    }

    @Test
    fun retryForFailuresKeepsOnlyFailedTracks() {
        val request = request(
            tracks = listOf(
                track(songId = 1L, title = "Changed 1"),
                track(songId = 2L, title = "Changed 2"),
            ),
        )

        val retry = request.retryForFailures(setOf(2L))

        assertEquals(listOf(2L), retry.album.songs.map { it.id })
        assertEquals(listOf(2L), retry.tracks.map { it.songId })
    }

    @Test
    fun validationRejectsDuplicateAndUnknownTrackSelections() {
        assertEquals(
            TagEditValidationFailure.InvalidTrackSelection,
            planner.validationFailure(
                request(tracks = listOf(track(1L, "One"), track(1L, "Duplicate"))),
            ),
        )
        assertEquals(
            TagEditValidationFailure.InvalidTrackSelection,
            planner.validationFailure(request(tracks = listOf(track(99L, "Unknown")))),
        )
    }

    @Test
    fun validationBoundsNumbersTextAndArtworkBeforeMutation() {
        assertEquals(
            TagEditValidationFailure.InvalidTrackNumber,
            planner.validationFailure(request(tracks = listOf(track(1L, "One", trackNumber = 0)))),
        )
        assertEquals(
            TagEditValidationFailure.TextTooLong,
            planner.validationFailure(request(albumTitle = TagFieldEdit.Value("a".repeat(4_097)))),
        )
        assertEquals(
            TagEditValidationFailure.ArtworkTooLarge,
            planner.validationFailure(request(coverArtBytes = ByteArray(MAX_TAG_ARTWORK_BYTES + 1))),
        )
    }

    private fun request(
        albumTitle: TagFieldEdit<String> = TagFieldEdit.Unchanged,
        albumArtist: TagFieldEdit<String> = TagFieldEdit.Unchanged,
        releaseYear: TagFieldEdit<Int> = TagFieldEdit.Unchanged,
        genre: TagFieldEdit<String> = TagFieldEdit.Unchanged,
        coverArtBytes: ByteArray? = null,
        tracks: List<EditableAlbumTrack> = emptyList(),
    ): AlbumTagEditRequest {
        return AlbumTagEditRequest(
            album = album(),
            albumTitle = albumTitle,
            albumArtist = albumArtist,
            releaseYear = releaseYear,
            genre = genre,
            coverArtUri = null,
            coverArtBytes = coverArtBytes,
            tracks = tracks,
        )
    }

    private fun album(): Album {
        val songs = listOf(song(1L), song(2L))
        return Album(
            id = 1L,
            title = "Album",
            artist = "Artist",
            artUri = null,
            songCount = songs.size,
            durationMs = songs.sumOf(Song::durationMs),
            songs = songs,
        )
    }

    private fun song(id: Long): Song {
        return Song(
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
            albumId = 1L,
            durationMs = 180_000L,
            trackNumber = id.toInt(),
            discNumber = 1,
            dateAddedSeconds = id,
            uri = TestUri(),
            artUri = null,
            albumArtist = "Artist",
        )
    }

    private fun track(
        songId: Long,
        title: String,
        trackNumber: Int = songId.toInt(),
    ): EditableAlbumTrack {
        return EditableAlbumTrack(
            songId = songId,
            title = title,
            artist = "Artist",
            trackNumber = trackNumber,
            discNumber = 1,
        )
    }
}
