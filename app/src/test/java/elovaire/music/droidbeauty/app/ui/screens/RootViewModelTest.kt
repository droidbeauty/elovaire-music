package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.core.RootViewModelDependencies
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.testing.FakeLibraryReader
import elovaire.music.droidbeauty.app.testing.FakePlaybackReader
import elovaire.music.droidbeauty.app.testing.FakeRootSettingsReader
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.testing.testAlbum
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun appStateAggregatesLibraryPlaybackAndAppearanceChanges() =
        runTest(mainDispatcherRule.scheduler) {
            val library = FakeLibraryReader()
            val playback = FakePlaybackReader()
            val settings = FakeRootSettingsReader()
            val viewModel = RootViewModel(
                object : RootViewModelDependencies {
                    override val libraryReader = library
                    override val rootSettingsReader: elovaire.music.droidbeauty.app.data.settings.RootSettingsReader = settings
                    override val playbackReader = playback
                },
            )
            backgroundScope.launch { viewModel.appState.collect {} }
            advanceUntilIdle()

            val song = testSong()
            library.mutableContentState.value = LibraryContentState(
                songs = listOf(song),
                albums = listOf(testAlbum(songs = listOf(song))),
                contentRevision = "revision-1",
            )
            playback.transportState.value = playback.transportState.value.copy(isPlaying = true)
            settings.onlineLyricsEnabled.value = false
            advanceUntilIdle()

            assertEquals(listOf(song), viewModel.appState.value.library.songs)
            assertTrue(viewModel.appState.value.playback.isPlaying)
            assertEquals(false, viewModel.appState.value.onlineLyricsEnabled)
        }
}
