package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorScreen
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorUiState
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudiobookDetailInteractionTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun detailActionsRemainUsableWithLargeTextInLightTheme() {
        val song = Song(
            id = 1L, title = "A particularly long chapter title", isExplicit = false,
            artist = "An author with a long name", album = "A long audiobook title",
            releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
            fileName = "book.m4b", albumId = 1L, durationMs = 100_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("file:///book.m4b"),
            artUri = null, mediaKind = AudioMediaKind.Audiobook,
        )
        val part = AudiobookPart(song, 1)
        val book = Audiobook("book", song.album, song.artist, null, song.durationMs, listOf(part))
        var restarted = false
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.English,
                LocalDensity provides Density(density.density, 1.5f),
            ) {
                ElovaireTheme(ThemeMode.Light, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobookDetailScreen(
                            book = book, currentSongId = null, isPlaying = false, progressMs = 0L,
                            savedProgress = null, descriptionState = AudiobookDescriptionLoadState.Unavailable,
                            bottomPadding = 0.dp, onBack = {}, onOpenTagEditor = {},
                            onPlay = { _, _ -> }, onStartOver = { restarted = true },
                        )
                    }
                }
            }
        }
        val playBounds = rule.onNodeWithText("Play").assertIsDisplayed().getUnclippedBoundsInRoot()
        val restartBounds = rule.onNodeWithText("Start over").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue(playBounds.right <= restartBounds.left)
        rule.onNodeWithText("Start over").performClick()
        rule.runOnIdle { assertTrue(restarted) }
    }

    @Test
    fun audiobookSearchResultOpensTheSelectedBook() {
        val book = Audiobook("search-book", "Search Book", "Search Author", null, 100_000L, emptyList())
        var opened: Audiobook? = null
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        SearchAudiobooksResults(books = listOf(book), onBookSelected = { opened = it })
                    }
                }
            }
        }
        rule.onNodeWithText("Audiobooks").assertIsDisplayed()
        rule.onNodeWithText("Search Author").assertIsDisplayed()
        rule.onNodeWithText("Search Book").performClick()
        rule.runOnIdle { assertEquals(book, opened) }
    }

    @Test
    fun galleryDoesNotMarkBookFinishedWhenOnlyAnEarlierPartFinished() {
        val song = Song(
            id = 1L, title = "Part One", isExplicit = false, artist = "Author", album = "Book",
            releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
            fileName = "one.m4b", albumId = 1L, durationMs = 100_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("file:///one.m4b"),
            artUri = null, mediaKind = AudioMediaKind.Audiobook,
        )
        val second = song.copy(id = 2L, title = "Part Two", uri = Uri.parse("file:///two.m4b"))
        val book = Audiobook("book", "Book", "Author", null, 200_000L, listOf(AudiobookPart(song, 1), AudiobookPart(second, 2)))
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobookMiniGallery(
                            books = listOf(book),
                            progressByBookKey = mapOf("book" to AudiobookProgress(1L, 100_000L, true, 1L)),
                            onOpenCollection = {}, onBookSelected = {},
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Continue listening").assertIsDisplayed()
        rule.onNodeWithText("Completed").assertDoesNotExist()
    }

    @Test
    fun galleryShowsListeningOnlyWhileTheBookIsActivelyPlaying() {
        val song = Song(
            id = 1L, title = "Book", isExplicit = false, artist = "Author", album = "Book",
            releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
            fileName = "book.m4b", albumId = 1L, durationMs = 100_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("file:///book.m4b"),
            artUri = null, mediaKind = AudioMediaKind.Audiobook,
        )
        val book = Audiobook("book", "Book", "Author", null, 100_000L, listOf(AudiobookPart(song, 1)))
        var activeSongId by mutableStateOf<Long?>(song.id)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobookMiniGallery(
                            books = listOf(book),
                            progressByBookKey = mapOf("book" to AudiobookProgress(1L, 100_000L, true, 1L)),
                            currentSongId = activeSongId,
                            onOpenCollection = {}, onBookSelected = {},
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Listening").assertIsDisplayed()
        rule.runOnIdle { activeSongId = null }
        rule.onNodeWithText("Completed").assertIsDisplayed()
    }

    @Test
    fun tagEditorDisablesFieldsDuringSaveAndRestoresEditingAfterward() {
        val book = Audiobook("book", "Book", "Author", null, 120_000L, emptyList())
        var state by mutableStateOf(
            AudiobookTagEditorUiState(originalBook = book, bookTitle = "Book", author = "Author", isLoading = false),
        )
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobookTagEditorScreen(
                            state = state, appLanguage = AppLanguage.English,
                            onBack = {}, onSave = {}, onPickCoverArt = {},
                            onBookTitleChange = { state = state.copy(bookTitle = it) },
                            onAuthorChange = {}, onDescriptionChange = {}, onReleaseYearChange = {}, onGenreChange = {},
                            onPartTitleChange = { _, _ -> }, onPartArtistChange = { _, _ -> },
                            onPartTrackNumberChange = { _, _ -> }, onPartDiscNumberChange = { _, _ -> },
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Book title").assertIsEnabled().performTextReplacement("Revised")
        rule.runOnIdle { assertEquals("Revised", state.bookTitle); state = state.copy(isSaving = true) }
        rule.onNodeWithText("Book title").assertIsNotEnabled()
        rule.runOnIdle { state = state.copy(isSaving = false) }
        rule.onNodeWithText("Book title").assertIsEnabled()
    }

    @Test
    fun tagEditorKeepsBackAvailableWhenLoadingOrBookIsMissing() {
        var state by mutableStateOf(AudiobookTagEditorUiState())
        var backCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    AudiobookTagEditorScreen(
                        state = state, appLanguage = AppLanguage.English,
                        onBack = { backCount++ }, onSave = {}, onPickCoverArt = {},
                        onBookTitleChange = {}, onAuthorChange = {}, onDescriptionChange = {},
                        onReleaseYearChange = {}, onGenreChange = {}, onPartTitleChange = { _, _ -> },
                        onPartArtistChange = { _, _ -> }, onPartTrackNumberChange = { _, _ -> },
                        onPartDiscNumberChange = { _, _ -> },
                    )
                }
            }
        }
        rule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, backCount); state = state.copy(isLoading = false) }
        rule.onNodeWithText("Audiobook not found").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(2, backCount) }
    }

    @Test
    fun collectionDisplaysProgressAndKeepsSelectionSeparateFromOpening() {
        fun book(id: Long, title: String): Audiobook {
            val song = Song(
                id = id, title = title, isExplicit = false, artist = "Author", album = title,
                releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
                fileName = "$id.m4b", albumId = id, durationMs = 120_000L, trackNumber = 1,
                discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("file:///$id.m4b"),
                artUri = null, mediaKind = AudioMediaKind.Audiobook,
            )
            return Audiobook("book-$id", title, "Author", null, 120_000L, listOf(AudiobookPart(song, 1)))
        }
        val first = book(1L, "Book A")
        val second = book(2L, "Book B")
        var opened: Audiobook? = null
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobooksScreen(
                            books = listOf(first, second),
                            progressByBookKey = mapOf(
                                first.stableKey to AudiobookProgress(1L, 60_000L, false, 1L),
                                second.stableKey to AudiobookProgress(2L, 120_000L, true, 1L),
                            ),
                            bottomPadding = 0.dp, onBack = {}, onBookSelected = { opened = it },
                            playlists = emptyList(), playlistSongsById = emptyMap(),
                            onAddSongsToPlaylist = { _, _ -> CompletableDeferred(PlaylistMutationResult.NotAllowed) },
                            onCreatePlaylist = { CompletableDeferred(PlaylistMutationResult.NotAllowed) },
                            onDeleteSongsFromDevice = {},
                        )
                    }
                }
            }
        }
        rule.onNodeWithContentDescription("Finished").assertIsDisplayed()
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertCountEquals(1)
        rule.onNodeWithText("Book A").performTouchInput { longClick() }
        rule.onNodeWithText("Add to playlist").assertIsDisplayed()
        rule.onNodeWithText("Book B").performClick()
        rule.runOnIdle { assertEquals(null, opened) }
        rule.onNodeWithText("Book A").performClick()
        rule.onNodeWithText("Book B").performClick()
        rule.onNodeWithContentDescription("Grid").performClick()
        rule.onNodeWithText("Book A").performClick()
        rule.runOnIdle { assertEquals(first, opened) }
    }

    @Test
    fun shortDescriptionAndChaptersRemainInteractiveWhileAnotherSongPlays() {
        val song = Song(
            id = 1L, title = "Book file", isExplicit = false, artist = "Author", album = "Book",
            releaseYear = null, genre = "", audioFormat = "M4B", audioQuality = null,
            fileName = "book.m4b", albumId = 1L, durationMs = 120_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.parse("file:///book.m4b"),
            artUri = null, mediaKind = AudioMediaKind.Audiobook,
        )
        val chapters = listOf(
            AudiobookPart(song, 1, "Chapter One", 0L, 60_000L),
            AudiobookPart(song, 2, "Chapter Two", 60_000L, 120_000L),
        )
        val book = Audiobook("book", "Book", "Author", null, 120_000L, chapters)
        var playedPart: AudiobookPart? = null
        var resume = false
        var restarted = false
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                        AudiobookDetailScreen(
                            book = book, currentSongId = 99L, isPlaying = true, progressMs = 10_000L,
                            savedProgress = AudiobookProgress(1L, 30_000L, false, 1L),
                            descriptionState = AudiobookDescriptionLoadState.Loaded("A short description"),
                            bottomPadding = 0.dp, onBack = {}, onOpenTagEditor = {},
                            onPlay = { part, shouldResume -> playedPart = part; resume = shouldResume },
                            onStartOver = { restarted = true },
                        )
                    }
                }
            }
        }
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertCountEquals(2)
        rule.onNodeWithText("A short description").performClick()
        rule.onNodeWithContentDescription("Close description").assertIsDisplayed().performClick()
        rule.onNodeWithText("Chapter Two").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(chapters[1], playedPart); assertFalse(resume) }
        rule.onNodeWithText("Resume").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(chapters[0], playedPart); assertTrue(resume) }
        rule.onNodeWithText("Start over").performClick()
        rule.runOnIdle { assertTrue(restarted) }
    }
}
