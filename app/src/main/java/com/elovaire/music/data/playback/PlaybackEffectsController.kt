package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.util.Collections
import java.util.WeakHashMap

@UnstableApi
class PlaybackEffectsController {
    private val equalizerProcessors = Collections.newSetFromMap(
        WeakHashMap<EqualizerAudioProcessor, Boolean>(),
    )
    private var currentSettings: EqSettings = EqSettings()

    @Synchronized
    fun audioProcessors(): Array<AudioProcessor> {
        val processor = EqualizerAudioProcessor().also { it.updateSettings(currentSettings) }
        equalizerProcessors += processor
        return arrayOf(processor)
    }

    @Synchronized
    fun applyEffectSettings(settings: EqSettings) {
        val sanitized = EqValuePolicy.sanitize(settings)
        if (sanitized == currentSettings) return
        currentSettings = sanitized
        equalizerProcessors.toList().forEach { it.updateSettings(currentSettings) }
    }

    fun hasSignalAlteringEffects(): Boolean = EqValuePolicy.hasSignalAlteringEffects(currentSettings)
}
