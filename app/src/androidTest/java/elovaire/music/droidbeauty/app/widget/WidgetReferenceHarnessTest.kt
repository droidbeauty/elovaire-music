package elovaire.music.droidbeauty.app.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.testing.unit.assertHasRunCallbackClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.assertHasText
import androidx.glance.testing.unit.assertHasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasTestTag
import androidx.glance.testing.unit.hasText
import org.junit.Test

class WidgetReferenceHarnessTest {
    @Test
    fun compactSizeKeepsTrackAndTransportAccessible() = runGlanceAppWidgetUnitTest {
        setAppWidgetSize(DpSize(width = 120.dp, height = 80.dp))
        provideComposable { WidgetReferenceContent(playingSnapshot()) }

        onNode(hasTestTag("widget-title")).assertHasText("Track")
        onNode(hasTestTag("widget-transport")).assertHasText("Playing")
        onNode(hasTestTag("widget-action-skip_next")).assertHasRunCallbackClickAction<WidgetPlaybackActionCallback>(
            actionParametersOf(WIDGET_PLAYBACK_ACTION_PARAMETER to "skip_next"),
        )
        onNode(hasTestTag("widget-action-skip_next")).assertHasContentDescriptionEqualTo("Skip next")
    }

    @Test
    fun expandedSizeIncludesArtistAndPlaybackAction() = runGlanceAppWidgetUnitTest {
        setAppWidgetSize(DpSize(width = 240.dp, height = 160.dp))
        provideComposable { WidgetReferenceContent(playingSnapshot()) }

        onNode(hasText("Artist")).assertHasText("Artist")
        onNode(hasTestTag("widget-action-toggle_playback")).assertHasRunCallbackClickAction<WidgetPlaybackActionCallback>(
            actionParametersOf(WIDGET_PLAYBACK_ACTION_PARAMETER to "toggle_playback"),
        )
    }

    @Test
    fun emptySnapshotRendersFallbackText() = runGlanceAppWidgetUnitTest {
        val empty = WidgetPlaybackSnapshot(
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
        )
        provideComposable { WidgetReferenceContent(empty) }

        onNode(hasTestTag("widget-title")).assertHasText("Nothing is playing")
    }

    private fun playingSnapshot() = WidgetPlaybackSnapshot(
        contentRevision = 1L,
        currentSongId = 1L,
        title = "Track",
        artist = "Artist",
        album = "Album",
        artworkIdentity = WidgetArtworkIdentity(1L, 1L),
        isPlaying = true,
        transportShowsPause = true,
        repeatMode = WidgetRepeatMode.Off,
        shuffleEnabled = false,
        durationMs = 90_000L,
        capturedAtElapsedRealtimeMs = 1_000L,
    )
}
