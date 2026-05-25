package elovaire.music.app.data.playback

import elovaire.music.app.domain.model.EqSettings
import elovaire.music.app.domain.model.SpaciousnessMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackEffectsControllerTest {
    @Test
    fun monoSettingCanBeAppliedWithoutNeedingAnAudioSession() {
        val controller = PlaybackEffectsController()
        controller.updateSettings(EqSettings(bass = 0.5f, treble = 0.25f, monoEnabled = true))
    }

    @Test
    fun defaultSettingsDoNotRequireSignalProcessing() {
        val controller = PlaybackEffectsController()

        assertFalse(controller.hasSignalAlteringEffects())
    }

    @Test
    fun nonFlatSettingsRequireSignalProcessing() {
        val controller = PlaybackEffectsController()
        controller.updateSettings(
            EqSettings(
                bass = 0.2f,
                spaciousness = 0.3f,
                spaciousnessMode = SpaciousnessMode.CrossfeedDepth,
            ),
        )

        assertTrue(controller.hasSignalAlteringEffects())
    }
}
