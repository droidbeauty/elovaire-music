package elovaire.music.droidbeauty.app.widget

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetPlaybackSnapshotTest {
    @Test
    fun emptyQueueProjectsSafeEmptyPlaybackState() {
        val snapshot = projectWidgetPlaybackSnapshot(
            nowPlaying = PlaybackNowPlayingState(),
            transport = PlaybackTransportState(),
            queue = PlaybackQueueState(),
            previous = null,
            elapsedRealtimeMs = 40L,
        )

        assertNull(snapshot.currentSongId)
        assertEquals("", snapshot.title)
        assertNull(snapshot.artworkIdentity)
        assertFalse(snapshot.isPlaying)
        assertEquals(1L, snapshot.contentRevision)
    }

    @Test
    fun snapshotUsesSelectedQueueSongAndOnlyPersistsWidgetFields() {
        val song = song()
        val snapshot = projectWidgetPlaybackSnapshot(
            nowPlaying = PlaybackNowPlayingState(currentSong = song),
            transport = PlaybackTransportState(isPlaying = true, transportShowsPause = true),
            queue = PlaybackQueueState(queue = listOf(song), currentIndex = 0, sourcePlaylistId = 92L),
            previous = null,
            elapsedRealtimeMs = 100L,
        )

        assertEquals(song.id, snapshot.currentSongId)
        assertEquals("Track", snapshot.title)
        assertEquals("Artist", snapshot.artist)
        assertEquals("Album", snapshot.album)
        assertEquals(WidgetArtworkIdentity(song.id, 7L), snapshot.artworkIdentity)
        assertEquals(2_000L, snapshot.durationMs)
        assertTrue(snapshot.isPlaying)
        assertTrue(snapshot.transportShowsPause)
    }

    @Test
    fun sameWidgetVisibleStateKeepsRevisionAndTimestamp() {
        val song = song()
        val initial = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song, sourceLabel = "Album", audioSessionId = 1),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song), 0),
            previous = null,
            elapsedRealtimeMs = 100L,
        )
        val next = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song, sourceLabel = "Changed", audioSessionId = 2),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song), 0),
            previous = initial,
            elapsedRealtimeMs = 200L,
        )

        assertEquals(initial.contentRevision, next.contentRevision)
        assertEquals(initial.capturedAtElapsedRealtimeMs, next.capturedAtElapsedRealtimeMs)
    }

    @Test
    fun transportAndRepeatChangesAdvanceRevisionButIgnoreQueueChanges() {
        val song = song()
        val initial = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song), 0),
            previous = null,
            elapsedRealtimeMs = 100L,
        )
        val queueChanged = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song, song.copy(id = 2L, title = "Other")), 0),
            previous = initial,
            elapsedRealtimeMs = 200L,
        )
        assertEquals(initial.contentRevision, queueChanged.contentRevision)

        val playing = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song),
            PlaybackTransportState(isPlaying = true, transportShowsPause = true, repeatMode = PlaybackRepeatMode.All, shuffleEnabled = true),
            PlaybackQueueState(listOf(song), 0),
            previous = queueChanged,
            elapsedRealtimeMs = 300L,
        )
        assertEquals(initial.contentRevision + 1L, playing.contentRevision)
        assertEquals(WidgetRepeatMode.All, playing.repeatMode)
        assertTrue(playing.shuffleEnabled)
    }

    @Test
    fun metadataAndArtworkRevisionChangesAdvanceContentRevision() {
        val song = song()
        val initial = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song), 0),
            previous = null,
            elapsedRealtimeMs = 100L,
        )
        val edited = song.copy(title = "Updated", dateModifiedSeconds = 8L)
        val updated = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = edited),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(edited), 0),
            previous = initial,
            elapsedRealtimeMs = 200L,
        )

        assertEquals(initial.contentRevision + 1L, updated.contentRevision)
        assertEquals("Updated", updated.title)
        assertEquals(WidgetArtworkIdentity(song.id, 8L), updated.artworkIdentity)
    }

    @Test
    fun textAndInvalidDurationAreBounded() {
        val song = song().copy(title = "x".repeat(WidgetPlaybackSnapshot.MAX_TEXT_LENGTH + 5), durationMs = -1L)
        val snapshot = projectWidgetPlaybackSnapshot(
            PlaybackNowPlayingState(currentSong = song),
            PlaybackTransportState(),
            PlaybackQueueState(listOf(song), 0),
            previous = null,
            elapsedRealtimeMs = -1L,
        )

        assertEquals(WidgetPlaybackSnapshot.MAX_TEXT_LENGTH, snapshot.title.length)
        assertEquals(0L, snapshot.durationMs)
        assertEquals(0L, snapshot.capturedAtElapsedRealtimeMs)
    }

    private fun song() = Song(
        id = 10L,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "private-name.mp3",
        albumId = 3L,
        durationMs = 2_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        dateModifiedSeconds = 7L,
        libraryPath = "/private/path.mp3",
        uri = TestUri("content://media/10"),
        artUri = null,
        description = "must not reach the snapshot",
    )
}
