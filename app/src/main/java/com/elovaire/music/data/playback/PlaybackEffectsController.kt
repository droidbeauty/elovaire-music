package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlin.math.abs

@UnstableApi
class PlaybackEffectsController {
    private val equalizerProcessor = EqualizerAudioProcessor()
    private val audioProcessors = arrayOf<AudioProcessor>(equalizerProcessor)
    private var currentSettings: EqSettings = EqSettings()

    fun audioProcessors(): Array<AudioProcessor> = audioProcessors

    fun updateSettings(settings: EqSettings) {
        currentSettings = settings.copy(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
            },
            bass = settings.bass.coerceIn(-1f, 1f),
            midrange = settings.midrange.coerceIn(-1f, 1f),
            treble = settings.treble.coerceIn(-1f, 1f),
            spaciousness = settings.spaciousness.coerceIn(-1f, 1f),
            spaciousnessMode = settings.spaciousnessMode,
            monoEnabled = settings.monoEnabled,
            reverbDurationMs = normalizeReverbDurationMs(settings.reverbDurationMs),
            reverbProfile = settings.reverbProfile,
        )
        equalizerProcessor.updateSettings(currentSettings)
    }

    fun hasSignalAlteringEffects(): Boolean {
        return currentSettings.monoEnabled ||
            currentSettings.bands.any { abs(it) > EFFECT_BYPASS_EPSILON } ||
            abs(currentSettings.bass) > EFFECT_BYPASS_EPSILON ||
            abs(currentSettings.midrange) > EFFECT_BYPASS_EPSILON ||
            abs(currentSettings.treble) > EFFECT_BYPASS_EPSILON ||
            currentSettings.reverbDurationMs > 0 ||
            (
                currentSettings.spaciousnessMode != SpaciousnessMode.Off &&
                    abs(currentSettings.spaciousness) > EFFECT_BYPASS_EPSILON
                )
    }

    private companion object {
        const val EFFECT_BYPASS_EPSILON = 0.0001f
    }
}
