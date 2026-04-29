package elovaire.music.app.data.playback

import elovaire.music.app.domain.model.EqSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackEffectsControllerTest {
    @Test
    fun bitPerfectBypassTogglesEffectSuppressionWithoutNeedingAnAudioSession() {
        val controller = PlaybackEffectsController()

        controller.updateSettings(EqSettings(bass = 0.5f, treble = 0.25f))
        controller.setBitPerfectBypass(true)

        assertTrue(controller.readBypassFlag())

        controller.setBitPerfectBypass(false)

        assertFalse(controller.readBypassFlag())
    }

    private fun PlaybackEffectsController.readBypassFlag(): Boolean {
        val field = PlaybackEffectsController::class.java.getDeclaredField("bypassedForBitPerfectUsb")
        field.isAccessible = true
        return field.getBoolean(this)
    }
}
