package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.math.abs

class PlaybackEffectsController(
    @Suppress("unused")
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val equalizerProcessor = EqualizerAudioProcessor()
    private var currentSettings: EqSettings = EqSettings()

    fun audioProcessors(): Array<AudioProcessor> = arrayOf(equalizerProcessor)

    fun updateAudioSessionId(audioSessionId: Int) {
        // Kept as a no-op so the rest of the app does not need to care whether processing
        // happens in-app or through a platform audio session effect.
    }

    fun updateOutputSampleRate(sampleRateHz: Int) {
        // Sample-rate aware coefficient changes happen directly inside the audio processor whenever
        // the playback format changes, so this is intentionally a no-op now.
    }

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

    fun release() = Unit

    private companion object {
        const val EFFECT_BYPASS_EPSILON = 0.0001f
    }
}
