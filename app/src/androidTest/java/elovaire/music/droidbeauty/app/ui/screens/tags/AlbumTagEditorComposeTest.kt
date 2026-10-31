package elovaire.music.droidbeauty.app.ui.screens.tags

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumTagEditorComposeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun trackEditsSurviveCollapsingAndSaveStaysReachableWithKeyboard() {
        val state = mutableStateOf(testAlbum().toTagEditorUiState())
        val theme = mutableStateOf(ThemeMode.Dark)
        val textSize = mutableStateOf(TextSizePreset.Default)
        var submitted: AlbumTagEditorUiState? = null
        val coverRequests = AtomicInteger()
        val saveRequests = AtomicInteger()
        val scenario = ActivityScenario.launch(PlaylistTestActivity::class.java)
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    ElovaireTheme(theme.value, textSize.value) {
                        AlbumTagEditorScreen(
                            state = state.value,
                            appLanguage = AppLanguage.English,
                            onBack = {},
                            onSave = {
                                saveRequests.incrementAndGet()
                                submitted = state.value
                                state.value = state.value.copy(isSaving = true).recalculateFlags()
                            },
                            onPickCoverArt = { coverRequests.incrementAndGet() },
                            onAlbumTitleChange = { state.value = state.value.copy(albumTitle = it).recalculateFlags() },
                            onAlbumArtistChange = { state.value = state.value.copy(albumArtist = it).recalculateFlags() },
                            onReleaseYearChange = { state.value = state.value.copy(releaseYear = it).recalculateFlags() },
                            onGenreChange = { state.value = state.value.copy(genre = it).recalculateFlags() },
                            onTrackTitleChange = { id, value ->
                                state.value = state.value.copy(
                                    tracks = state.value.tracks.map { if (it.songId == id) it.copy(title = value) else it },
                                ).recalculateFlags()
                            },
                            onTrackArtistChange = { _, _ -> },
                            onTrackNumberChange = { _, _ -> },
                            onDiscNumberChange = { _, _ -> },
                        )
                    }
                }
            }
            node(By.text("Save")).click()
            assertEquals(0, saveRequests.get())
            assertTrue(device.findObject(By.text("Change cover")) == null)
            node(By.desc("Tap to choose new album artwork")).click()
            assertEquals(1, coverRequests.get())
            capture("album-editor-overview.png")

            val firstTrack = By.res("album_tag_editor_track_1")
            scrollTo(firstTrack).click()
            val titleField = By.clazz("android.widget.EditText").textContains("First Light")
            val field = scrollTo(titleField)
            field.click()
            field.text = ""
            node(By.textContains("track title cannot be empty."))
            node(By.text("Save")).click()
            assertEquals(0, saveRequests.get())
            field.text = "Edited First Light"
            node(By.text("Save"))
            device.pressBack()
            capture("album-editor-track.png")

            val editedTrack = By.res("album_tag_editor_track_1")
            scrollTo(editedTrack).click()
            assertTrue(device.wait(Until.gone(titleField), 3_000L))
            scrollTo(editedTrack).click()
            node(titleField)

            val lightFrameReady = CountDownLatch(1)
            scenario.onActivity { activity ->
                theme.value = ThemeMode.Light
                textSize.value = TextSizePreset.ExtraLarge
                activity.window.decorView.postOnAnimation {
                    activity.window.decorView.postOnAnimation { lightFrameReady.countDown() }
                }
            }
            assertTrue(lightFrameReady.await(3L, TimeUnit.SECONDS))
            capture("album-editor-light.png")
            scrollTo(titleField).click()
            node(By.text("Save")).click()
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                assertEquals("Edited First Light", submitted?.tracks?.first()?.title)
                assertTrue(state.value.isSaving)
            }
            node(By.text("Save")).click()
            assertEquals(1, saveRequests.get())
            assertFalse(node(titleField).isEnabled)
        } finally {
            scenario.close()
        }
    }

    private fun node(selector: BySelector): UiObject2 =
        requireNotNull(device.wait(Until.findObject(selector), 5_000L)) { "Missing UI node: $selector" }

    private fun scrollTo(selector: BySelector): UiObject2 {
        repeat(6) {
            device.findObject(selector)?.let { return it }
            node(By.scrollable(true)).scroll(Direction.DOWN, 0.45f)
        }
        return node(selector)
    }

    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        device.waitForIdle()
        assertTrue(device.takeScreenshot(instrumentation.targetContext.cacheDir.resolve(name)))
    }

    private fun testAlbum(): Album {
        val songs = listOf("First Light", "A Slow Tide", "Between Stars", "After Hours").mapIndexed { index, title ->
            Song(
                id = index + 1L,
                title = title,
                isExplicit = false,
                artist = "The Midnight Ensemble",
                album = "Moonlight Sessions",
                releaseYear = 2026,
                genre = "Ambient",
                audioFormat = "FLAC",
                audioQuality = null,
                fileName = "${index + 1} - $title.flac",
                albumId = 10L,
                durationMs = 210_000L,
                trackNumber = index + 1,
                discNumber = 1,
                dateAddedSeconds = 1L,
                uri = Uri.parse("content://test/song/${index + 1}"),
                artUri = null,
            )
        }
        return Album(10L, "Moonlight Sessions", "The Midnight Ensemble", null, songs.size, songs.sumOf(Song::durationMs), songs)
    }
}
