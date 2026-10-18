package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.util.Collections
import java.util.WeakHashMap

internal interface PlaybackEffects {
    fun applyUpscalerSettings(settings: UpscalerSettings)

    fun applyEffectSettings(settings: EqSettings)

    fun hasSignalAlteringEffects(): Boolean
}

@UnstableApi
class PlaybackEffectsController : PlaybackEffects {
    private val equalizerProcessors = Collections.newSetFromMap(
        WeakHashMap<EqualizerAudioProcessor, Boolean>(),
    )
    @Volatile
    var upscalerSettings: UpscalerSettings = UpscalerSettings()
        private set

    @Synchronized
    override fun applyUpscalerSettings(settings: UpscalerSettings) {
        upscalerSettings = settings.sanitized()
    }

    private var currentSettings: EqSettings = EqSettings()

    @Synchronized
    fun audioProcessors(): Array<AudioProcessor> {
        val processor = EqualizerAudioProcessor().also { it.updateSettings(currentSettings) }
        equalizerProcessors += processor
        return if (upscalerSettings.enabled) arrayOf(processor, UpscalerAudioProcessor(upscalerSettings)) else arrayOf(processor)
    }

    @Synchronized
    override fun applyEffectSettings(settings: EqSettings) {
        val sanitized = EqValuePolicy.sanitize(settings)
        if (sanitized == currentSettings) return
        currentSettings = sanitized
        equalizerProcessors.toList().forEach { it.updateSettings(currentSettings) }
    }

    override fun hasSignalAlteringEffects(): Boolean = upscalerSettings.enabled || EqValuePolicy.hasSignalAlteringEffects(currentSettings)
}
