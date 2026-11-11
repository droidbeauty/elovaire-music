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
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerBackdropInstrumentedTest {
    @get:Rule val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun audiobookChapterQueueSeeksToSelectedChapter() {
        val song = Song(
            id = 1L, title = "A Book", isExplicit = false, artist = "Author", album = "Book",
            releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
            fileName = "book.m4b", albumId = 1L, durationMs = 180_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("content://backdrop-test/book"),
            artUri = null, mediaKind = AudioMediaKind.Audiobook,
        )
        val progress = MutableStateFlow(PlaybackProgressState(positionMs = 65_000, displayPositionMs = 65_000,
            durationMs = 180_000, currentMediaId = song.id))
        var seekPosition: Long? = null
        val playback = Proxy.newProxyInstance(NowPlayingPlayback::class.java.classLoader,
            arrayOf(NowPlayingPlayback::class.java)) { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Audiobook player fixture"
                "getProgressState" -> progress
                "setProgressConsumerActive", "cancelScrub" -> Unit
                "seekTo" -> { seekPosition = args?.firstOrNull() as Long; Unit }
                else -> error("Unexpected playback call: ${method.name}")
            }
        } as NowPlayingPlayback
        val chapters = object : AudiobookChapterReader {
            override suspend fun chapters(song: Song): List<AudiobookChapter> = listOf(
                AudiobookChapter("First chapter", 0L, 60_000L),
                AudiobookChapter("Second chapter", 70_000L, 120_000L),
                AudiobookChapter("Third chapter", 130_000L, 180_000L),
            )
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    NowPlayingScreen(
                        playbackManager = playback, audiobookChapterReader = chapters, isPlayerVisible = true,
                        playerUiState = PlayerUiState(currentSong = song, queue = listOf(song), currentIndex = 0),
                        enrichedSongsById = mapOf(song.id to song), audiobookSettings = AudiobookSettings(),
                        isFavorite = false, playlists = emptyList(),
                        lyricsUiState = LyricsUiState.Ready(LyricsPayload(emptyList(), false), song.toLyricsIdentity().cacheKeys.first()),
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
        composeRule.onNodeWithContentDescription("Rewind").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Forward").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Queue").performClick()
        composeRule.onNodeWithText("Chapters").assertIsDisplayed()
        composeRule.onNodeWithText("Second chapter").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(70_000L, seekPosition) }
    }

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
