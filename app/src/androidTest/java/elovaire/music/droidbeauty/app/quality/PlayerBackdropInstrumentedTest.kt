package elovaire.music.droidbeauty.app.quality

import android.net.Uri
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.toLyricsIdentity
import elovaire.music.droidbeauty.app.data.playback.AudiobookChapterReader
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.AudiobookChapter
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.LyricsEditorUiState
import elovaire.music.droidbeauty.app.ui.screens.LyricsUiState
import elovaire.music.droidbeauty.app.ui.screens.NowPlayingScreen
import elovaire.music.droidbeauty.app.ui.screens.PlayerUiState
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import java.lang.reflect.Proxy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerBackdropInstrumentedTest {
    @get:Rule val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun queueTimerAndLyricsCanRenderTheirBackdropsWithoutCapturingThemselves() {
        val songs = (1L..20L).map { id -> Song(
            id = id, title = "Backdrop track $id", isExplicit = false, artist = "Backdrop artist", album = "Album",
            releaseYear = null, genre = "", audioFormat = "FLAC", audioQuality = null,
            fileName = "$id.flac", albumId = 1L, durationMs = 180_000L, trackNumber = id.toInt(),
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("content://backdrop-test/$id"), artUri = null,
        ) }
        val progress = MutableStateFlow(PlaybackProgressState(positionMs = 90_000, displayPositionMs = 90_000,
            durationMs = 180_000, currentMediaId = 1L))
        val playback = Proxy.newProxyInstance(NowPlayingPlayback::class.java.classLoader,
            arrayOf(NowPlayingPlayback::class.java)) { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Backdrop fixture playback"
                "getProgressState" -> progress
                "setProgressConsumerActive", "cancelScrub" -> Unit
                else -> error("Unexpected playback call: ${method.name}")
            }
        } as NowPlayingPlayback
        val chapters = object : AudiobookChapterReader {
            override suspend fun chapters(song: Song): List<AudiobookChapter> = emptyList()
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Box(Modifier.fillMaxSize().testTag("full_player_fixture")) {
                        NowPlayingScreen(
                            playbackManager = playback, audiobookChapterReader = chapters, isPlayerVisible = true,
                            playerUiState = PlayerUiState(currentSong = songs.first(), queue = songs, currentIndex = 0),
                            enrichedSongsById = songs.associateBy(Song::id), audiobookSettings = AudiobookSettings(),
                            isFavorite = false, playlists = emptyList(),
                            lyricsUiState = LyricsUiState.Ready(
                                LyricsPayload((0..40).map { LyricsLine("Backdrop lyric line $it", null, index = it) }, false),
                                songs.first().toLyricsIdentity().cacheKeys.first(),
                            ),
                            lyricsEditorUiState = LyricsEditorUiState(), activeLyricsLineIndex = -1,
                            onLyricsVisibilityChanged = {}, onSaveLyrics = {}, onClearLyricsEditorError = {},
                            onBack = {}, onOpenCurrentAlbum = {}, onTogglePlayback = {}, onSkipPrevious = {}, onSkipNext = {},
                            onCycleRepeatMode = {}, onToggleShuffle = {}, onToggleFavorite = {},
                            onAddCurrentSongToPlaylist = { _, _ -> CompletableDeferred(PlaylistMutationResult.InvalidInput) },
                            onCreatePlaylist = { CompletableDeferred(PlaylistMutationResult.InvalidInput) },
                            onQueueItemSelected = {}, onQueueItemRemoved = {}, onOpenEqualizer = {},
                            onToggleCrossfade = {}, onSleepTimerSelected = {}, onVolumeChanged = {}, transitionSnapshot = null,
                        )
                    }
                }
            }
        }
        composeRule.onNodeWithContentDescription("Queue").performClick()
        composeRule.waitForIdle()
        capture("player-queue-blur.png")
        composeRule.onNodeWithContentDescription("Sleep timer").performClick()
        composeRule.onNodeWithText("min", substring = true).assertIsDisplayed()
        capture("player-timer-blur.png")
        composeRule.onNodeWithContentDescription("Close sleep timer").performClick()
        composeRule.onNodeWithContentDescription("Close queue").performClick()
        composeRule.onNodeWithContentDescription("Lyrics").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Backdrop lyric line 0").assertIsDisplayed()
        capture("player-lyrics-blur.png")
        composeRule.onNodeWithContentDescription("Hide lyrics").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Queue").assertIsDisplayed()
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onNodeWithTag("full_player_fixture").captureToImage().asAndroidBitmap()
        composeRule.activity.cacheDir.resolve(name).outputStream().use {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
