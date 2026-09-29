package elovaire.music.droidbeauty.app.ui.screens.tags

import androidx.lifecycle.SavedStateHandle
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.tags.TagEditApplyResult
import elovaire.music.droidbeauty.app.testing.FakeAlbumTagEditor
import elovaire.music.droidbeauty.app.testing.FakeLibraryReader
import elovaire.music.droidbeauty.app.testing.FakeLibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.testing.testAlbum
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumTagEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun albumDraftIsRestoredFromSavedStateAfterRecreation() =
        runTest(mainDispatcherRule.scheduler) {
            val song = testSong()
            val library = FakeLibraryReader(
                initialContent = LibraryContentState(
                    songs = listOf(song),
                    albums = listOf(testAlbum(songs = listOf(song))),
                ),
            )
            val handle = SavedStateHandle()
            val firstViewModel = AlbumTagEditorViewModel(
                libraryRepository = library,
                libraryTagUpdates = FakeLibraryTagUpdateWriter(),
                tagEditorService = FakeAlbumTagEditor(),
                savedStateHandle = handle,
            )
            firstViewModel.loadAlbum(1L)
            advanceUntilIdle()
            firstViewModel.onAlbumTitleChange("Edited album")
            firstViewModel.requestSave()
            assertTrue(firstViewModel.uiState.value.platformAction != null)

            val recreatedViewModel = AlbumTagEditorViewModel(
                libraryRepository = library,
                libraryTagUpdates = FakeLibraryTagUpdateWriter(),
                tagEditorService = FakeAlbumTagEditor(),
                savedStateHandle = handle,
            )
            recreatedViewModel.loadAlbum(1L)
            advanceUntilIdle()

            assertEquals("Edited album", recreatedViewModel.uiState.value.albumTitle)
            assertTrue(recreatedViewModel.uiState.value.hasUnsavedChanges)
            assertNull(recreatedViewModel.uiState.value.platformAction)
        }

    @Test
    fun savePermissionRequestIsExposedAsStateAndSuccessfulSaveAsOutcome() =
        runTest(mainDispatcherRule.scheduler) {
            val song = testSong()
            val library = FakeLibraryReader(
                initialContent = LibraryContentState(
                    songs = listOf(song),
                    albums = listOf(testAlbum(songs = listOf(song))),
                ),
            )
            val updates = FakeLibraryTagUpdateWriter()
            val editor = FakeAlbumTagEditor()
            val viewModel = AlbumTagEditorViewModel(library, updates, editor)
            viewModel.loadAlbum(1L)
            advanceUntilIdle()
            viewModel.onAlbumTitleChange("Edited album")
            viewModel.requestSave()

            val action = viewModel.uiState.value.platformAction
                as AlbumTagEditorPlatformAction.RequestWritePermission
            assertEquals(listOf(song.uri), action.uris)
            viewModel.consumePlatformAction(action.operationId)
            assertNull(viewModel.uiState.value.platformAction)

            editor.result = TagEditApplyResult(
                editedSongIds = listOf(song.id),
                editedUris = listOf(song.uri),
                editedFilePaths = emptyList(),
                editedSongs = listOf(song.copy(album = "Edited album")),
                artworkChanged = false,
            )
            viewModel.onWritePermissionNotRequired(action.operationId)
            advanceUntilIdle()

            assertEquals(1, editor.requests.size)
            assertEquals(listOf(song.id), updates.editedSongs.map { it.id })
            assertEquals(AlbumTagEditorSaveOutcome.Succeeded, viewModel.uiState.value.saveOutcome)
        }

    @Test
    fun deniedWritePermissionPreservesDraftAndCanRetry() = runTest(mainDispatcherRule.scheduler) {
        val song = testSong()
        val library = FakeLibraryReader(
            LibraryContentState(
                songs = listOf(song),
                albums = listOf(testAlbum(songs = listOf(song))),
            ),
        )
        val editor = FakeAlbumTagEditor()
        val viewModel = AlbumTagEditorViewModel(library, FakeLibraryTagUpdateWriter(), editor)
        viewModel.loadAlbum(1L)
        advanceUntilIdle()
        viewModel.onAlbumTitleChange("Edited album")
        viewModel.requestSave()
        val action = viewModel.uiState.value.platformAction as AlbumTagEditorPlatformAction.RequestWritePermission
        viewModel.consumePlatformAction(action.operationId)

        viewModel.onWritePermissionResult(action.operationId, granted = false)

        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("Edited album", viewModel.uiState.value.albumTitle)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertTrue(viewModel.uiState.value.canSave)
        assertTrue(editor.requests.isEmpty())
        viewModel.requestSave()
        assertTrue(viewModel.uiState.value.platformAction is AlbumTagEditorPlatformAction.RequestWritePermission)
    }

    @Test
    fun permissionLaunchFailureResetsSavingWithoutLosingDraft() = runTest(mainDispatcherRule.scheduler) {
        val song = testSong()
        val library = FakeLibraryReader(
            LibraryContentState(
                songs = listOf(song),
                albums = listOf(testAlbum(songs = listOf(song))),
            ),
        )
        val viewModel = AlbumTagEditorViewModel(library, FakeLibraryTagUpdateWriter(), FakeAlbumTagEditor())
        viewModel.loadAlbum(1L)
        advanceUntilIdle()
        viewModel.onAlbumTitleChange("Edited album")
        viewModel.requestSave()
        val action = viewModel.uiState.value.platformAction as AlbumTagEditorPlatformAction.RequestWritePermission

        viewModel.onWritePermissionLaunchFailed(action.operationId)

        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertTrue(viewModel.uiState.value.canSave)
        assertNull(viewModel.uiState.value.platformAction)
    }

}
