package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsReader
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsWriter
import elovaire.music.droidbeauty.app.data.lyrics.parseLrcOrPlain
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackSleepTimerState
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.settings.NowPlayingSettingsStore
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyricsNavigationInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun firstEntryShowsLoadingUntilLyricsArrive() {
        val fixture = Fixture()
        fixture.open().use {
            assertTrue(device.wait(Until.findObject(By.text("Loading lyrics...")), 4_000L) != null)
            assertTrue(device.findObject(By.desc("Add lyrics")) == null)
            fixture.answer(1, "Lyrics loaded on first entry")
            text("Lyrics loaded on first entry")
            assertEquals(listOf(1L), fixture.requests.toList())
        }
    }

    @Test
    fun nextPreviousAndRapidSkipsReplaceLyricsWithoutAnotherNavigationRoundTrip() {
        val fixture = Fixture()
        fixture.answer(1, "First song lyrics")
        fixture.open().use {
            text("First song lyrics")
            tap("Next")
            text("Track 2")
            assertTrue("Previous lyrics must leave while the next lookup is pending", device.wait(
                Until.gone(By.text("First song lyrics")), 2_000L,
            ))
            assertTrue(fixture.requests.contains(2L))
            // Another skip while a lookup is suspended must cancel it and fetch the newest song.
            tap("Next")
            text("Track 3")
            fixture.answer(3, "Third song lyrics")
            text("Third song lyrics")
            fixture.answer(2, "Second song lyrics")
            tap("Previous")
            text("Second song lyrics")
            tap("Previous")
            text("First song lyrics")
            tap("Next")
            text("Second song lyrics")
            assertEquals(listOf(1L, 2L, 3L, 2L, 1L, 2L), fixture.requests.toList())
        }
    }

    @Test
    fun changingAlbumOrSourceRevisionReloadsLyricsForTheCurrentSong() {
        val fixture = Fixture()
        fixture.answer(1, "Original album lyrics")
        fixture.open().use { scenario ->
            text("Original album lyrics")
            fixture.responses[1] = CompletableDeferred()
            scenario.onActivity {
                fixture.nowPlaying.value = PlaybackNowPlayingState(fixture.songs[0].copy(album = "Live"))
            }
            fixture.answer(1, "Live album lyrics")
            text("Live album lyrics")
            fixture.responses[1] = CompletableDeferred()
            scenario.onActivity {
                fixture.nowPlaying.value = PlaybackNowPlayingState(fixture.songs[0].copy(album = "Live", dateModifiedSeconds = 42))
            }
            fixture.answer(1, "Updated embedded lyrics")
            text("Updated embedded lyrics")
            assertEquals(listOf(1L, 1L, 1L), fixture.requests.toList())
        }
    }

    @Test
    fun scrollingOneSongDoesNotHideTheStartOfTheNextAndUnavailableLyricsCanRecover() {
        val fixture = Fixture()
        fixture.answer(1, (1..60).joinToString("\n") { "First song line $it" })
        fixture.responses[2] = CompletableDeferred(LyricsResult.NotFound)
        fixture.answer(3, "Third song opening line")
        fixture.open().use {
            text("First song line 1")
            repeat(3) { device.swipe(500, 1300, 500, 450, 12) }
            assertTrue(device.wait(Until.gone(By.text("First song line 1")), 2_000L))
            tap("Next")
            requireNotNull(device.wait(Until.findObject(By.desc("Add lyrics")), 4_000L))
            tap("Next")
            text("Third song opening line")
            tap("Previous")
            requireNotNull(device.wait(Until.findObject(By.desc("Add lyrics")), 4_000L))
            tap("Previous")
            text("First song line 1")
        }
    }

    @Test
    fun syncedLineTapsSeekTheCurrentTrackAndTheEditorClosesOnTrackChange() {
        val fixture = Fixture()
        fixture.answer(1, "[00:01]First synced line\n[00:10]Second synced line")
        fixture.answer(2, "[00:04]Next song synced line")
        fixture.open().use {
            text("Second synced line").click()
            instrumentation.waitForIdleSync()
            assertEquals(listOf(10_000L), fixture.seeks.toList())
            tap("Next")
            text("Next song synced line").click()
            instrumentation.waitForIdleSync()
            assertEquals(listOf(10_000L, 4_000L), fixture.seeks.toList())
            tap("Edit lyrics")
            text("[00:04]Next song synced line")
            instrumentation.runOnMainSync {
                fixture.nowPlaying.value = PlaybackNowPlayingState(fixture.songs[0])
            }
            text("First synced line")
            assertTrue(device.wait(Until.gone(By.text("[00:04]Next song synced line")), 2_000L))
            assertTrue(device.takeScreenshot(instrumentation.targetContext.cacheDir.resolve("lyrics-navigation.png")))
        }
    }

    @Test
    fun identicalCachedLyricsStillBelongToEachTrackAndStartAtTheTop() {
        val fixture = Fixture()
        val cached = LyricsResult.Found(requireNotNull(parseLrcOrPlain(
            (1..60).joinToString("\n") { "Shared cached line $it" },
        )))
        fixture.cached[1] = cached
        fixture.cached[2] = cached
        fixture.open().use {
            text("Shared cached line 1")
            repeat(3) { device.swipe(500, 1300, 500, 450, 12) }
            assertTrue(device.wait(Until.gone(By.text("Shared cached line 1")), 2_000L))
            tap("Next")
            text("Track 2")
            text("Shared cached line 1")
            assertTrue("Cache hits must not require network requests", fixture.requests.isEmpty())
        }
    }

    private fun text(value: String) = requireNotNull(device.wait(Until.findObject(By.text(value)), 4_000L)) {
        "Missing lyrics UI text: $value"
    }

    private fun tap(description: String) {
        requireNotNull(device.wait(Until.findObject(By.desc(description)), 4_000L)).click()
    }

    private inner class Fixture : LyricsReader, LyricsWriter, NowPlayingSettingsStore {
        val songs = (1L..3L).map { id -> Song(
            id = id, title = "Track $id", isExplicit = false, artist = "Lyrics fixture", album = "Album",
            releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
            fileName = "$id.mp3", albumId = 1L, durationMs = 180_000L, trackNumber = id.toInt(),
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("content://lyrics-navigation/$id"), artUri = null,
        ) }
        val nowPlaying = MutableStateFlow(PlaybackNowPlayingState(songs.first()))
        val requests = ConcurrentLinkedQueue<Long>()
        val seeks = ConcurrentLinkedQueue<Long>()
        val cached = ConcurrentHashMap<Long, LyricsResult>()
        val responses = ConcurrentHashMap<Long, CompletableDeferred<LyricsResult>>()
        private val playback = Proxy.newProxyInstance(
            NowPlayingPlayback::class.java.classLoader, arrayOf(NowPlayingPlayback::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Lyrics test playback"
                "getNowPlayingState" -> nowPlaying
                "getProgressState" -> MutableStateFlow(PlaybackProgressState())
                "getTransportState" -> MutableStateFlow(PlaybackTransportState())
                "getQueueState" -> MutableStateFlow(PlaybackQueueState())
                "getVolumeState" -> MutableStateFlow(PlaybackVolumeState())
                "getSleepTimerState" -> MutableStateFlow(PlaybackSleepTimerState())
                "setProgressConsumerActive" -> Unit
                "skipNext", "skipPrevious" -> {
                    val index = songs.indexOfFirst { it.id == nowPlaying.value.currentSong?.id }
                    val next = (index + if (method.name == "skipNext") 1 else -1).coerceIn(songs.indices)
                    nowPlaying.value = PlaybackNowPlayingState(songs[next])
                }
                "seekTo" -> { seeks.add(args?.first() as Long); Unit }
                else -> error("Unexpected playback call: ${method.name} ${args?.toList()}")
            }
        } as NowPlayingPlayback

        fun answer(id: Long, text: String) {
            responses.getOrPut(id) { CompletableDeferred() }.complete(
                LyricsResult.Found(requireNotNull(parseLrcOrPlain(text))),
            )
        }

        fun open(): ActivityScenario<PlaylistTestActivity> {
            val scenario = ActivityScenario.launch(PlaylistTestActivity::class.java)
            scenario.onActivity { activity ->
                val model = NowPlayingViewModel(playback, this, this, this)
                activity.viewModelStore.put("lyrics", model)
                model.setLyricsVisible(true)
                activity.setContent {
                    val track by nowPlaying.collectAsStateWithLifecycle()
                    val lyrics by model.lyricsUiState.collectAsStateWithLifecycle()
                    val activeLine by model.activeLyricsLineIndex.collectAsStateWithLifecycle()
                    ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                        LyricsOverlay(
                            playbackManager = playback, song = track.currentSong, lyricsUiState = lyrics,
                            lyricsEditorUiState = LyricsEditorUiState(), activeLyricsLineIndex = activeLine,
                            tintColor = Color.Black, contentColor = Color.White, secondaryContentColor = Color.LightGray,
                            isPlaying = false, onTogglePlayback = {}, onSkipPrevious = model::skipPrevious,
                            onSkipNext = model::skipNext, onSeekTo = playback::seekTo, onHideLyrics = {},
                            onSaveLyrics = {}, onClearLyricsEditorError = {},
                        )
                    }
                }
            }
            return scenario
        }

        override suspend fun cachedLyrics(song: Song, includeNotFound: Boolean): LyricsResult? = cached[song.id]
        override suspend fun localLyrics(song: Song): LyricsResult? = null
        override fun lyricsForSong(song: Song) = flow {
            requests.add(song.id)
            emit(responses.getOrPut(song.id) { CompletableDeferred() }.await())
        }
        override suspend fun saveEmbeddedLyrics(song: Song, lyrics: String, operationId: String?, approvedMediaUri: Uri?): EmbeddedLyricsWriteResult =
            error("Not used")
        override val crossfadeEnabled = MutableStateFlow(false)
        override fun setCrossfadeEnabled(enabled: Boolean) { crossfadeEnabled.value = enabled }
    }
}
