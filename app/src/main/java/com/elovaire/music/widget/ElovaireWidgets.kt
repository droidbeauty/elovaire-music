package elovaire.music.droidbeauty.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import elovaire.music.droidbeauty.app.ElovaireApp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmapAwaitingAdmission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

internal enum class WidgetDesign { Compact, Classic, Cover, Studio, Controls }

private val Panel = Color(0xFF181818)
private val Muted = Color(0xFFB4B4BB)
private val White = Color.White

internal object WidgetRenderUpdates {
    val revision = MutableStateFlow(0L)
    fun notifyChanged() { revision.update { it + 1L } }
}

private data class WidgetRenderData(val snapshot: WidgetPlaybackSnapshot?, val artwork: Bitmap?)

private suspend fun loadWidgetArtwork(context: Context, snapshot: WidgetPlaybackSnapshot?): Bitmap? =
    withTimeoutOrNull(5_000L) {
        listOfNotNull(snapshot?.artworkUri, snapshot?.mediaUri).distinct()
            .firstNotNullOfOrNull { source ->
                loadArtworkBitmapAwaitingAdmission(context, Uri.parse(source), 384, ArtworkPurpose.UiGrid)
            }
    }

abstract class ElovaireWidget : GlanceAppWidget() {
    internal abstract val design: WidgetDesign
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initialData = WidgetRenderData(AtomicWidgetSnapshotStore(context).readLatest(), null)
        val app = context.applicationContext as ElovaireApp
        val language = app.container.rootSettingsReader.appLanguage
        provideContent {
            val revision by WidgetRenderUpdates.revision.collectAsState()
            val currentLanguage by language.collectAsState()
            val data by produceState(initialData, revision) {
                val snapshot = AtomicWidgetSnapshotStore(context).readLatest()
                value = WidgetRenderData(snapshot, null)
                value = WidgetRenderData(snapshot, loadWidgetArtwork(context, snapshot))
            }
            WidgetContent(design, data.snapshot, data.artwork, widgetCopy(currentLanguage), context)
        }
    }
}

class CompactWidget : ElovaireWidget() { override val design = WidgetDesign.Compact }
class ClassicWidget : ElovaireWidget() { override val design = WidgetDesign.Classic }
class CoverWidget : ElovaireWidget() { override val design = WidgetDesign.Cover }
class StudioWidget : ElovaireWidget() { override val design = WidgetDesign.Studio }
class ControlsWidget : ElovaireWidget() { override val design = WidgetDesign.Controls }

abstract class ElovaireWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        (context.applicationContext as ElovaireApp).onWidgetProvidersChanged()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        (context.applicationContext as ElovaireApp).onWidgetProvidersChanged()
    }
}

class CompactWidgetReceiver : ElovaireWidgetReceiver() { override val glanceAppWidget = CompactWidget() }
class ClassicWidgetReceiver : ElovaireWidgetReceiver() { override val glanceAppWidget = ClassicWidget() }
class CoverWidgetReceiver : ElovaireWidgetReceiver() { override val glanceAppWidget = CoverWidget() }
class StudioWidgetReceiver : ElovaireWidgetReceiver() { override val glanceAppWidget = StudioWidget() }
class ControlsWidgetReceiver : ElovaireWidgetReceiver() { override val glanceAppWidget = ControlsWidget() }

@Composable
internal fun WidgetContent(design: WidgetDesign, state: WidgetPlaybackSnapshot?, artwork: Bitmap?, copy: WidgetCopy, context: Context) {
    val open = actionStartActivity(WidgetLaunchIntentFactory.openNowPlaying(context))
    val title = state?.title?.ifBlank { "Elovaire" } ?: "Elovaire"
    val artist = state?.artist.orEmpty()
    val playing = state?.transportShowsPause == true
    val inset = if (design == WidgetDesign.Compact || design == WidgetDesign.Controls) 8.dp else 12.dp
    val background = if (design == WidgetDesign.Controls) R.drawable.widget_panel_background else R.drawable.widget_loading_background
    val root = GlanceModifier.fillMaxSize().cornerRadius(24.dp).background(ImageProvider(background)).padding(inset)
    when (design) {
        WidgetDesign.Compact -> Row(root, verticalAlignment = Alignment.CenterVertically) {
            Artwork(artwork, 48, GlanceModifier.clickable(open).semantics { contentDescription = title })
            Spacer(GlanceModifier.width(12.dp))
            TrackText(title, artist, GlanceModifier.defaultWeight().clickable(open))
            TransportButton(playing, copy, prominent = true, size = 48)
            if (LocalSize.current.width >= 260.dp) SkipButton(true, copy, 44)
        }
        WidgetDesign.Classic -> Column(root, verticalAlignment = Alignment.CenterVertically) {
            Row(GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                Artwork(artwork, 60, GlanceModifier.clickable(open).semantics { contentDescription = title })
                Spacer(GlanceModifier.width(14.dp))
                TrackText(title, artist, GlanceModifier.defaultWeight().clickable(open))
            }
            Row(GlanceModifier.fillMaxWidth().height(50.dp), horizontalAlignment = Alignment.End, verticalAlignment = Alignment.CenterVertically) {
                Brand(GlanceModifier.defaultWeight().clickable(open))
                SkipButton(false, copy, 44)
                TransportButton(playing, copy, prominent = true, size = 44)
                SkipButton(true, copy, 44)
            }
        }
        WidgetDesign.Cover -> Column(root, verticalAlignment = Alignment.CenterVertically) {
            Row(GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                Box(GlanceModifier.defaultWeight().fillMaxHeight().clickable(open).semantics { contentDescription = title }) { ArtworkFill(artwork) }
            }
            Spacer(GlanceModifier.height(8.dp))
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TrackText(title, artist, GlanceModifier.defaultWeight().clickable(open))
                TransportButton(playing, copy, prominent = true, size = 44)
            }
        }
        WidgetDesign.Studio -> Column(root) {
            Row(GlanceModifier.fillMaxWidth().height(28.dp), verticalAlignment = Alignment.CenterVertically) {
                Brand(GlanceModifier.defaultWeight().clickable(open))
                Image(ImageProvider(R.drawable.ic_lucide_audio_waveform), null, GlanceModifier.size(20.dp), colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Muted)))
            }
            Row(GlanceModifier.fillMaxWidth().defaultWeight().clickable(open), verticalAlignment = Alignment.CenterVertically) {
                Artwork(artwork, if (LocalSize.current.height >= 220.dp) 112 else 72, GlanceModifier)
                Spacer(GlanceModifier.width(16.dp))
                TrackText(title, artist, GlanceModifier.defaultWeight(), large = true)
            }
            Row(GlanceModifier.fillMaxWidth().height(54.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                SkipButton(false, copy, 52)
                Spacer(GlanceModifier.width(16.dp))
                TransportButton(playing, copy, prominent = true, size = 52)
                Spacer(GlanceModifier.width(16.dp))
                SkipButton(true, copy, 52)
            }
        }
        WidgetDesign.Controls -> Row(root, verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
            SkipButton(false, copy, 40)
            TransportButton(playing, copy, prominent = true, size = 44)
            SkipButton(true, copy, 40)
        }
    }
}

@Composable
private fun Brand(modifier: GlanceModifier) {
    Text("ELOVAIRE", modifier, style = TextStyle(color = ColorProvider(Muted), fontSize = 10.sp, fontWeight = FontWeight.Bold), maxLines = 1)
}

@Composable
private fun TrackText(title: String, artist: String, modifier: GlanceModifier, large: Boolean = false) {
    Column(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = TextStyle(color = ColorProvider(White), fontSize = if (large) 18.sp else 15.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        if (artist.isNotBlank()) {
            Spacer(GlanceModifier.height(3.dp))
            Text(artist, style = TextStyle(color = ColorProvider(Muted), fontSize = 12.sp), maxLines = 1)
        }
    }
}

@Composable
private fun Artwork(bitmap: Bitmap?, size: Int, modifier: GlanceModifier) {
    Box(modifier.size(size.dp)) { ArtworkFill(bitmap) }
}

@Composable
private fun ArtworkFill(bitmap: Bitmap?) {
    Box(GlanceModifier.fillMaxSize().cornerRadius(14.dp).background(Panel), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(ImageProvider(bitmap), null, GlanceModifier.fillMaxSize().cornerRadius(14.dp), contentScale = androidx.glance.layout.ContentScale.Crop)
        } else {
            Image(ImageProvider(R.drawable.ic_lucide_disc_3), null, GlanceModifier.size(30.dp), colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Muted)))
        }
    }
}

@Composable
private fun TransportButton(playing: Boolean, copy: WidgetCopy, prominent: Boolean, size: Int) {
    val label = if (playing) copy.pause else copy.play
    Box(GlanceModifier.size(size.dp).cornerRadius((size / 2).dp).background(ImageProvider(if (prominent) R.drawable.widget_play_background else R.drawable.widget_panel_background))
        .clickable(widgetPlaybackAction(WidgetPlaybackAction.TogglePlayback)).semantics { contentDescription = label; testTag = "widget-playback" }, contentAlignment = Alignment.Center) {
        Image(ImageProvider(if (playing) R.drawable.ic_elovaire_pause_filled else R.drawable.ic_lucide_play), null, GlanceModifier.size(22.dp), colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(White)))
    }
}

@Composable
private fun SkipButton(next: Boolean, copy: WidgetCopy, size: Int) {
    val action = if (next) WidgetPlaybackAction.SkipNext else WidgetPlaybackAction.SkipPrevious
    val label = if (next) copy.next else copy.previous
    Box(GlanceModifier.size(size.dp).clickable(widgetPlaybackAction(action)).semantics { contentDescription = label; testTag = if (next) "widget-next" else "widget-previous" }, contentAlignment = Alignment.Center) {
        Image(ImageProvider(if (next) R.drawable.ic_elovaire_forward_filled else R.drawable.ic_elovaire_backward_filled), null, GlanceModifier.size(20.dp), colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(White)))
    }
}
