package elovaire.music.droidbeauty.app.ui.screens.tags

import androidx.lifecycle.SavedStateHandle
import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditor
import elovaire.music.droidbeauty.app.data.tags.TagEditApplyResult
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.testing.FakeLibraryReader
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudiobookTagEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saveUsesPhysicalSongsAndPublishesVerifiedChanges() = runTest(mainDispatcherRule.scheduler) {
        val song = song()
        val book = Audiobook("book", "Book", "Author", null, song.durationMs, listOf(AudiobookPart(song, 1)))
        val library = FakeLibraryReader(LibraryContentState(songs = listOf(song), audiobooks = listOf(book)))
        val updates = RecordingUpdates()
        val editor = RecordingEditor(song.copy(album = "Renamed"))
        val viewModel = AudiobookTagEditorViewModel(library, updates, editor, { _, _ -> }, ioDispatcher = mainDispatcherRule.dispatcher)
        viewModel.loadBook(book.stableKey)
        advanceUntilIdle()

        viewModel.onBookTitleChange("Renamed")
        viewModel.requestSave()
        val action = viewModel.uiState.value.platformAction as AudiobookTagEditorPlatformAction.RequestWritePermission
        viewModel.consumePlatformAction(action.operationId)
        viewModel.onWritePermissionNotRequired(action.operationId)
        advanceUntilIdle()

        assertEquals(listOf(song.id), editor.requests.single().songs.map(Song::id))
        assertEquals(listOf(song.id), updates.editedSongs.map(Song::id))
        assertTrue(viewModel.uiState.value.saveOutcome is AudiobookTagEditorSaveOutcome.Succeeded)
    }

    @Test
    fun draftRestoresAcrossViewModelRecreation() = runTest(mainDispatcherRule.scheduler) {
        val song = song()
        val book = Audiobook("book", "Book", "Author", null, song.durationMs, listOf(AudiobookPart(song, 1)))
        val library = FakeLibraryReader(LibraryContentState(songs = listOf(song), audiobooks = listOf(book)))
        val handle = SavedStateHandle()
        val first = AudiobookTagEditorViewModel(
            library,
            RecordingUpdates(),
            RecordingEditor(song),
            { _, _ -> },
            ioDispatcher = mainDispatcherRule.dispatcher,
            savedStateHandle = handle,
        )
        first.loadBook(book.stableKey)
        advanceUntilIdle()
        first.onAuthorChange("New Author")

        val recreated = AudiobookTagEditorViewModel(
            library,
            RecordingUpdates(),
            RecordingEditor(song),
            { _, _ -> },
            ioDispatcher = mainDispatcherRule.dispatcher,
            savedStateHandle = handle,
        )
        recreated.loadBook(book.stableKey)
        advanceUntilIdle()

        assertEquals("New Author", recreated.uiState.value.author)
        assertTrue(recreated.uiState.value.hasUnsavedChanges)
    }

    private class RecordingEditor(private val editedSong: Song) : AudiobookTagEditor {
        val requests = mutableListOf<AudiobookTagEditRequest>()
        override suspend fun applyEdits(request: AudiobookTagEditRequest, writeConsentGranted: Boolean): TagEditApplyResult {
            requests += request
            return TagEditApplyResult(listOf(editedSong.id), listOf(editedSong.uri), emptyList(), listOf(editedSong), false)
        }
    }

    private class RecordingUpdates : LibraryTagUpdateWriter {
        val editedSongs = mutableListOf<Song>()
        override suspend fun applyVerifiedTagEdits(editedSongs: List<Song>) {
            this.editedSongs += editedSongs
        }
    }

    private fun song() = Song(
        id = 1L,
        title = "Part",
        isExplicit = false,
        artist = "Author",
        album = "Book",
        releaseYear = 2020,
        genre = "Audio",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = "book.m4b",
        albumId = 1L,
        durationMs = 100L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("file:///book.m4b"),
        artUri = null,
        albumArtist = "Author",
        mediaKind = AudioMediaKind.Audiobook,
    )
}
