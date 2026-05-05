package elovaire.music.app.data.playback

import elovaire.music.app.domain.model.EqSettings
import org.junit.Test

class PlaybackEffectsControllerTest {
    @Test
    fun monoSettingCanBeAppliedWithoutNeedingAnAudioSession() {
        val controller = PlaybackEffectsController()
        controller.updateSettings(EqSettings(bass = 0.5f, treble = 0.25f, monoEnabled = true))
    }
}
