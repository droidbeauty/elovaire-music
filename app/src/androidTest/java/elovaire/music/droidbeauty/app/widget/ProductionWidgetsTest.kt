package elovaire.music.droidbeauty.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.testing.unit.assertHasRunCallbackClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.assertHasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasTestTag
import androidx.glance.testing.unit.hasText
import androidx.glance.testing.unit.assertHasText
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionWidgetsTest {
    @Test
    fun externalAudioFallbackUsesSelectedLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val snapshot = WidgetPlaybackSnapshot(
            contentRevision = 1L,
            currentSongId = -8_000_000_000_000L,
            title = "External audio",
            artist = "Unknown Artist",
            album = "External audio",
            artworkIdentity = null,
            isPlaying = false,
            transportShowsPause = false,
            repeatMode = WidgetRepeatMode.Off,
            shuffleEnabled = false,
            durationMs = null,
            capturedAtElapsedRealtimeMs = 0L,
        )
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(300.dp, 100.dp))
            provideComposable { WidgetContent(WidgetDesign.Compact, snapshot, null, widgetCopy(AppLanguage.Polish), context) }
            onNode(hasText("Zewnętrzny plik audio")).assertHasText("Zewnętrzny plik audio")
        }
    }
    @Test
    fun everyDesignIsRegisteredWithLauncherMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = AppWidgetManager.getInstance(context)
        val providers = manager.installedProviders.map { it.provider }.toSet()
        listOf(
            CompactWidgetReceiver::class.java, ClassicWidgetReceiver::class.java,
            CoverWidgetReceiver::class.java, StudioWidgetReceiver::class.java,
            ControlsWidgetReceiver::class.java,
        ).forEach { receiver ->
            val component = ComponentName(context, receiver)
            assertNotNull(manager.installedProviders.firstOrNull { it.provider == component && it.previewImage != 0 })
            assertTrue(providers.contains(component))
        }
    }

    @Test
    fun allDesignsExposeLocalizedPlaybackAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (design in WidgetDesign.entries) {
            runGlanceAppWidgetUnitTest {
                setAppWidgetSize(DpSize(300.dp, 240.dp))
                provideComposable { WidgetContent(design, null, null, widgetCopy(AppLanguage.Polish), context) }
                onNode(hasTestTag("widget-playback")).assertHasContentDescriptionEqualTo("Odtwórz")
                onNode(hasTestTag("widget-playback")).assertHasRunCallbackClickAction<WidgetPlaybackActionCallback>(
                    actionParametersOf(WIDGET_PLAYBACK_ACTION_PARAMETER to "toggle_playback"),
                )
            }
        }
    }
}
