package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.SavedStateHandle
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.testing.FakeLibraryReader
import elovaire.music.droidbeauty.app.testing.FakePlaybackReader
import elovaire.music.droidbeauty.app.testing.FakeSearchSettingsStore
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.testing.testAlbum
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.collect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun queryStateAndResultsSurviveViewModelRecreation() =
        runTest(mainDispatcherRule.scheduler) {
            val song = testSong(title = "Northern Lights")
            val library = FakeLibraryReader(
                initialContent = LibraryContentState(
                    songs = listOf(song),
                    albums = listOf(testAlbum(songs = listOf(song))),
                    contentRevision = "revision-1",
                ),
            )
            val settings = FakeSearchSettingsStore()
            val handle = SavedStateHandle()
            val firstViewModel = SearchViewModel(
                libraryRepository = library,
                preferenceStore = settings,
                playbackReader = FakePlaybackReader(),
                defaultDispatcher = mainDispatcherRule.dispatcher,
                savedStateHandle = handle,
            )
            backgroundScope.launch { firstViewModel.uiState.collect {} }
            advanceUntilIdle()

            firstViewModel.onQueryChange("Northern")
            advanceTimeBy(200L)
            advanceUntilIdle()

            assertEquals("Northern", firstViewModel.uiState.value.query)
            assertEquals(1, firstViewModel.uiState.value.totalSongMatchCount)

            val recreatedViewModel = SearchViewModel(
                libraryRepository = library,
                preferenceStore = settings,
                playbackReader = FakePlaybackReader(),
                defaultDispatcher = mainDispatcherRule.dispatcher,
                savedStateHandle = handle,
            )
            backgroundScope.launch { recreatedViewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals("Northern", recreatedViewModel.uiState.value.query)
            assertTrue(recreatedViewModel.uiState.value.totalSongMatchCount >= 1)
        }
}
