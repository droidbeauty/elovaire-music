package elovaire.music.app.data.playback

import androidx.media3.common.audio.AudioProcessor
import elovaire.music.app.domain.model.EqSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PlaybackEffectsController(
    @Suppress("unused")
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val equalizerProcessor = EqualizerAudioProcessor()
    private var currentSettings: EqSettings = EqSettings()
    private var bypassedForBitPerfectUsb = false

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
            treble = settings.treble.coerceIn(-1f, 1f),
            spaciousness = settings.spaciousness.coerceIn(-1f, 1f),
            spaciousnessMode = settings.spaciousnessMode,
        )
        equalizerProcessor.updateSettings(currentSettings)
    }

    fun setBitPerfectBypass(enabled: Boolean) {
        bypassedForBitPerfectUsb = enabled
        equalizerProcessor.setBitPerfectBypass(enabled)
    }

    fun release() = Unit
}
