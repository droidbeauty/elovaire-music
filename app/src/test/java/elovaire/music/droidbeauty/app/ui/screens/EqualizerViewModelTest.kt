package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.testing.FakeEqualizerSettingsStore
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun stateChangesArePersistedThroughTheDebouncedSettingsBoundary() =
        runTest(mainDispatcherRule.scheduler) {
            val settings = FakeEqualizerSettingsStore()
            val viewModel = EqualizerViewModel(settings)

            runCurrent()
            viewModel.updateBass(0.75f)
            runCurrent()

            assertTrue(viewModel.uiState.value.isDirty)
            assertEquals(0, settings.writes.size)

            advanceTimeBy(39L)
            runCurrent()
            assertEquals(0, settings.writes.size)

            advanceTimeBy(1L)
            runCurrent()
            assertEquals(1, settings.writes.size)
            assertEquals(0.75f, settings.writes.single().bass)
        }
}
