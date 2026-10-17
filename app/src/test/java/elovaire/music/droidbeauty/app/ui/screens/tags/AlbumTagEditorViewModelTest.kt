package elovaire.music.droidbeauty.app.ui.screens.tags

import androidx.lifecycle.SavedStateHandle
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.tags.TagEditApplyResult
import elovaire.music.droidbeauty.app.data.tags.TagEditFailure
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
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
            firstViewModel.onTrackTitleChange(song.id, "Edited | track\nżółć")
            firstViewModel.onTrackArtistChange(song.id, "Edited artist")
            firstViewModel.onTrackNumberChange(song.id, "7")
            firstViewModel.onDiscNumberChange(song.id, "2")
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
            val restoredTrack = recreatedViewModel.uiState.value.tracks.single()
            assertEquals("Edited | track\nżółć", restoredTrack.title)
            assertEquals("Edited artist", restoredTrack.artist)
            assertEquals("7", restoredTrack.trackNumber)
            assertEquals("2", restoredTrack.discNumber)
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
    fun partialSaveKeepsCollectionEditsAvailableForRetry() = runTest(mainDispatcherRule.scheduler) {
        val first = testSong(1L)
        val second = testSong(2L)
        val library = FakeLibraryReader(LibraryContentState(
            songs = listOf(first, second),
            albums = listOf(testAlbum(songs = listOf(first, second))),
        ))
        val editor = FakeAlbumTagEditor()
        val viewModel = AlbumTagEditorViewModel(library, FakeLibraryTagUpdateWriter(), editor)
        viewModel.loadAlbum(1L)
        advanceUntilIdle()
        viewModel.onAlbumTitleChange("New album")
        viewModel.requestSave()
        val action = viewModel.uiState.value.platformAction as AlbumTagEditorPlatformAction.RequestWritePermission
        editor.result = TagEditApplyResult(
            editedSongIds = listOf(first.id),
            editedUris = listOf(first.uri),
            editedFilePaths = emptyList(),
            editedSongs = listOf(first.copy(album = "New album")),
            artworkChanged = false,
            failures = listOf(TagEditFailure(second.id, second.fileName, "Temporary failure")),
        )

        viewModel.onWritePermissionNotRequired(action.operationId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canSave)
        assertEquals(TagFieldEdit.Value("New album"), viewModel.uiState.value.toAlbumTagEditRequest()?.albumTitle)
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
