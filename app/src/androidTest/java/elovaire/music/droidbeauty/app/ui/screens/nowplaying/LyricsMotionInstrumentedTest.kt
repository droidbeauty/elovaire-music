package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.FrameMetrics
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.toLyricsIdentity
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyricsMotionInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun synchronizedLinesAnimateAndRemainSeekable() {
        val song = Song(
            id = 1L, title = "Motion test", isExplicit = false, artist = "Artist", album = "Album",
            releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
            fileName = "song.mp3", albumId = 1L, durationMs = 180_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("content://lyrics-motion/1"), artUri = null,
        )
        val payload = LyricsPayload((0..45).map { index ->
            LyricsLine("Lyric line $index with words that can wrap naturally on the screen", index * 1_000L, index = index)
        }, isSynced = true)
        val progress = MutableStateFlow(PlaybackProgressState())
        val playback = Proxy.newProxyInstance(NowPlayingPlayback::class.java.classLoader,
            arrayOf(NowPlayingPlayback::class.java)) { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Lyrics motion fixture"
                "getProgressState" -> progress
                "setProgressConsumerActive" -> Unit
                else -> error("Unexpected playback call: ${method.name}")
            }
        } as NowPlayingPlayback
        val activeIndex = mutableIntStateOf(0)
        var soughtPosition: Long? = null
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    LyricsOverlay(
                        playbackManager = playback, song = song,
                        lyricsUiState = LyricsUiState.Ready(payload, song.toLyricsIdentity().cacheKeys.first()),
                        lyricsEditorUiState = LyricsEditorUiState(), activeLyricsLineIndex = activeIndex,
                        tintColor = Color.Black, contentColor = Color.White, secondaryContentColor = Color.LightGray,
                        isPlaying = true, onTogglePlayback = {}, onSkipPrevious = {}, onSkipNext = {},
                        onSeekTo = { soughtPosition = it }, onHideLyrics = {}, onSaveLyrics = {},
                        onClearLyricsEditorError = {},
                    )
                }
            }
        }
        rule.onNodeWithText(payload.lines[0].text).assertIsDisplayed()
        val frames = ConcurrentLinkedQueue<Long>()
        val thread = HandlerThread("lyrics-frame-metrics").apply { start() }
        val listener = android.view.Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            frames.add(metrics.getMetric(FrameMetrics.TOTAL_DURATION))
        }
        try {
            rule.runOnUiThread { rule.activity.window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper)) }
            rule.mainClock.autoAdvance = false
            repeat(8) { index ->
                rule.runOnUiThread { activeIndex.intValue = index + 1 }
                repeat(24) {
                    rule.mainClock.advanceTimeByFrame()
                    Thread.sleep(16)
                }
            }
            rule.mainClock.autoAdvance = true
            rule.waitForIdle()
            val samples = frames.toList().sorted()
            if (samples.isNotEmpty()) {
                Log.i("LyricsMotionTest", "frames=${samples.size} p90Ms=${samples[(samples.size * 9 / 10).coerceAtMost(samples.lastIndex)] / 1_000_000.0} over16Ms=${samples.count { it > 16_666_667L }}")
            }
            rule.onNodeWithText(payload.lines[8].text).assertIsDisplayed().performClick()
            rule.runOnIdle { assertEquals(8_000L, soughtPosition) }
            repeat(2) { rule.onRoot().performTouchInput { swipeUp() } }
            val target = (12..30).firstOrNull { index ->
                rule.onNodeWithText(payload.lines[index].text).isDisplayed()
            }
            assertTrue("Expected a later lyric after manual scrolling", target != null)
            val selected = requireNotNull(target)
            rule.mainClock.autoAdvance = false
            rule.onNodeWithText(payload.lines[selected].text).performClick()
            rule.mainClock.advanceTimeBy(500)
            rule.onNodeWithText(payload.lines[selected].text).assertIsDisplayed()
            rule.runOnIdle { assertEquals(selected * 1_000L, soughtPosition) }
        } finally {
            rule.mainClock.autoAdvance = true
            rule.runOnUiThread { rule.activity.window.removeOnFrameMetricsAvailableListener(listener) }
            thread.quitSafely()
        }
    }
}
