package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.domain.model.EqSettings

@UnstableApi
class PlaybackEffectsController {
    private val equalizerProcessor = EqualizerAudioProcessor()
    private val audioProcessors = arrayOf<AudioProcessor>(equalizerProcessor)
    private var currentSettings: EqSettings = EqSettings()

    fun audioProcessors(): Array<AudioProcessor> = audioProcessors

    fun applyEffectSettings(settings: EqSettings) {
        val sanitized = EqValuePolicy.sanitize(settings)
        if (sanitized == currentSettings) return
        currentSettings = sanitized
        equalizerProcessor.updateSettings(currentSettings)
    }

    fun hasSignalAlteringEffects(): Boolean = EqValuePolicy.hasSignalAlteringEffects(currentSettings)
}
