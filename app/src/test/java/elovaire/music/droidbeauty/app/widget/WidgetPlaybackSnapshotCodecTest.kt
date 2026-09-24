package elovaire.music.droidbeauty.app.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetPlaybackSnapshotCodecTest {
    @Test
    fun currentSnapshotRoundTripsWithoutDomainObjects() {
        val snapshot = WidgetPlaybackSnapshot(
            contentRevision = 4L,
            currentSongId = 10L,
            title = "Track",
            artist = "Artist",
            album = "Album",
            artworkIdentity = WidgetArtworkIdentity(10L, 9L),
            isPlaying = true,
            transportShowsPause = true,
            repeatMode = WidgetRepeatMode.One,
            shuffleEnabled = true,
            durationMs = 50_000L,
            capturedAtElapsedRealtimeMs = 2_000L,
        )

        assertEquals(snapshot, WidgetPlaybackSnapshotCodec.decode(WidgetPlaybackSnapshotCodec.encode(snapshot)))
    }

    @Test
    fun corruptOrUnsupportedSnapshotIsRejected() {
        val valid = WidgetPlaybackSnapshotCodec.encode(
            WidgetPlaybackSnapshot(
                contentRevision = 1L,
                currentSongId = null,
                title = "",
                artist = "",
                album = null,
                artworkIdentity = null,
                isPlaying = false,
                transportShowsPause = false,
                repeatMode = WidgetRepeatMode.Off,
                shuffleEnabled = false,
                durationMs = null,
                capturedAtElapsedRealtimeMs = 0L,
            ),
        )

        assertNull(WidgetPlaybackSnapshotCodec.decode(byteArrayOf(1, 2, 3)))
        assertNull(WidgetPlaybackSnapshotCodec.decode(valid + byteArrayOf(0)))
        assertNull(
            WidgetPlaybackSnapshotCodec.decode(
                WidgetPlaybackSnapshotCodec.encode(
                    WidgetPlaybackSnapshot(
                        schemaVersion = 99,
                        contentRevision = 1L,
                        currentSongId = null,
                        title = "",
                        artist = "",
                        album = null,
                        artworkIdentity = null,
                        isPlaying = false,
                        transportShowsPause = false,
                        repeatMode = WidgetRepeatMode.Off,
                        shuffleEnabled = false,
                        durationMs = null,
                        capturedAtElapsedRealtimeMs = 0L,
                    ),
                ),
            ),
        )
    }
}
