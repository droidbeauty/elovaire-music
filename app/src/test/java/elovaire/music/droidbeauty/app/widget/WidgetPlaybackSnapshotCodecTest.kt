package elovaire.music.droidbeauty.app.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import elovaire.music.droidbeauty.app.domain.model.AppLanguage

class WidgetPlaybackSnapshotCodecTest {
    @Test
    fun widgetTitleLocalizesOnlyExternalFallback() {
        val snapshot = WidgetPlaybackSnapshot(
            contentRevision = 1L,
            currentSongId = -8_000_000_000_000L,
            title = "External audio",
            artist = "",
            album = null,
            artworkIdentity = null,
            isPlaying = false,
            transportShowsPause = false,
            repeatMode = WidgetRepeatMode.Off,
            shuffleEnabled = false,
            durationMs = null,
            capturedAtElapsedRealtimeMs = 0L,
        )
        val copy = widgetCopy(AppLanguage.Polish)
        assertEquals("Zewnętrzny plik audio", widgetTitle(snapshot, copy))
        assertEquals("External audio", widgetTitle(snapshot.copy(currentSongId = 1L), copy))
        assertEquals("Real title", widgetTitle(snapshot.copy(title = "Real title"), copy))
        assertEquals("Elovaire", widgetTitle(null, copy))
    }
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
            artworkUri = "content://artwork/10",
            mediaUri = "content://media/10",
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
