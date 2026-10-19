package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.testing.FakeEqualizerSettingsStore
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun editingAnyEffectClearsPresetSelectionButNoOpKeepsIt() = runTest(mainDispatcherRule.scheduler) {
        val store = FakeEqualizerSettingsStore()
        val viewModel = EqualizerViewModel(store)
        runCurrent()
        val preset = EqSettings(preampDb = -2f, bass = 0.2f)
        viewModel.applyPreset("Saved", preset)
        viewModel.updatePreamp(-2f)
        assertEquals("Saved", viewModel.uiState.value.presetName)
        viewModel.updatePreamp(-1.5f)
        assertNull(viewModel.uiState.value.presetName)
        viewModel.applyPreset("Saved", preset)
        viewModel.updateBass(0.4f)
        assertNull(viewModel.uiState.value.presetName)
        viewModel.applyPreset("Saved", preset)
        viewModel.updateReverbDuration(100)
        assertNull(viewModel.uiState.value.presetName)
    }

    @Test
    fun customPresetRoundTripPreservesAllEffectsAndDeleteDoesNotResetSound() = runTest(mainDispatcherRule.scheduler) {
        val store = FakeEqualizerSettingsStore()
        val viewModel = EqualizerViewModel(store)
        runCurrent()
        viewModel.updatePreamp(-3f)
        viewModel.updateBand(2, 0.5f)
        viewModel.updateSpaciousnessMode(SpaciousnessMode.StereoWidth)
        viewModel.updateReverbDuration(150)
        val expected = viewModel.uiState.value.toEqSettings()
        viewModel.saveCustomPreset("  Evening  ")
        assertEquals("Evening", store.eqCustomPresets.value.single().name)
        assertEquals(expected, store.eqCustomPresets.value.single().settings)
        viewModel.resetEffects()
        assertFalse(viewModel.uiState.value.enabled)
        viewModel.applyPreset("Evening", store.eqCustomPresets.value.single().settings)
        assertEquals(expected, viewModel.uiState.value.toEqSettings())
        viewModel.deleteCustomPreset("Evening")
        assertTrue(store.eqCustomPresets.value.isEmpty())
        assertNull(viewModel.uiState.value.presetName)
        assertEquals(expected, viewModel.uiState.value.toEqSettings())
    }

    @Test
    fun repeatedSliderValuesDoNotScheduleSettingsWrites() = runTest(mainDispatcherRule.scheduler) {
        val store = FakeEqualizerSettingsStore()
        val viewModel = EqualizerViewModel(store)
        runCurrent()
        repeat(30) { viewModel.updatePreamp(0.1f) }
        advanceTimeBy(100)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun fullPresetLibraryNeverSilentlyEvictsAnExistingPreset() = runTest(mainDispatcherRule.scheduler) {
        val store = FakeEqualizerSettingsStore()
        store.eqCustomPresets.value = List(elovaire.music.droidbeauty.app.domain.model.EqCustomPreset.MAX_COUNT) {
            elovaire.music.droidbeauty.app.domain.model.EqCustomPreset("Preset $it", EqSettings())
        }
        val viewModel = EqualizerViewModel(store)
        runCurrent()
        viewModel.saveCustomPreset("One too many")
        assertEquals(24, store.eqCustomPresets.value.size)
        assertEquals("Preset 0", store.eqCustomPresets.value.first().name)
        viewModel.updatePreamp(-2f)
        viewModel.saveCustomPreset("Preset 0")
        assertEquals(24, store.eqCustomPresets.value.size)
        assertEquals(-2f, store.eqCustomPresets.value.first { it.name == "Preset 0" }.settings.preampDb, 0f)
    }

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
