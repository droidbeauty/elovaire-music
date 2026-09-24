package elovaire.music.droidbeauty.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.semantics.semantics
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.testTag
import androidx.glance.text.Text

/** Test-only reference surface; it is deliberately absent from the application manifest. */
internal class WidgetReferenceHarness(
    private val snapshot: WidgetPlaybackSnapshot,
) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetReferenceContent(snapshot) }
    }
}

@Composable
internal fun WidgetReferenceContent(snapshot: WidgetPlaybackSnapshot) {
    Column {
        Text(
            text = snapshot.title.ifBlank { "Nothing is playing" },
            modifier = GlanceModifier.semantics { testTag = "widget-title" },
        )
        Text(
            text = if (snapshot.isPlaying) "Playing" else "Paused",
            modifier = GlanceModifier.semantics { testTag = "widget-transport" },
        )
        if (LocalSize.current.width >= 150.dp) {
            Text(
                text = snapshot.artist,
                modifier = GlanceModifier.semantics { testTag = "widget-artist" },
            )
        }
        Text(
            text = "Previous",
            modifier = GlanceModifier
                .clickable(onClick = widgetPlaybackAction(WidgetPlaybackAction.SkipPrevious))
                .semantics {
                    testTag = "widget-action-skip_previous"
                    contentDescription = "Skip previous"
                },
        )
        Text(
            text = if (snapshot.transportShowsPause) "Pause" else "Play",
            modifier = GlanceModifier
                .clickable(onClick = widgetPlaybackAction(WidgetPlaybackAction.TogglePlayback))
                .semantics {
                    testTag = "widget-action-toggle_playback"
                    contentDescription = if (snapshot.transportShowsPause) "Pause playback" else "Play playback"
                },
        )
        Text(
            text = "Next",
            modifier = GlanceModifier
                .clickable(onClick = widgetPlaybackAction(WidgetPlaybackAction.SkipNext))
                .semantics {
                    testTag = "widget-action-skip_next"
                    contentDescription = "Skip next"
                },
        )
    }
}
