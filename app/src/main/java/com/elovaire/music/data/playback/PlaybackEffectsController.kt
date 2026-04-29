package elovaire.music.app.data.playback

import android.media.audiofx.Equalizer
import elovaire.music.app.domain.model.EqSettings
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaybackEffectsController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private var currentAudioSessionId: Int = 0
    private var currentSettings: EqSettings = EqSettings()
    private var bypassedForBitPerfectUsb = false
    private var equalizer: Equalizer? = null
    private var currentSampleRateHz: Int = DEFAULT_SAMPLE_RATE_HZ
    private var currentBassAmount: Float = 0f
    private var bassSmoothingJob: Job? = null

    fun updateAudioSessionId(audioSessionId: Int) {
        if (currentAudioSessionId == audioSessionId) return
        currentAudioSessionId = audioSessionId
        rebuildEffects()
    }

    fun updateOutputSampleRate(sampleRateHz: Int) {
        val sanitized = sampleRateHz.coerceAtLeast(MIN_SAMPLE_RATE_HZ)
        if (currentSampleRateHz == sanitized) return
        currentSampleRateHz = sanitized
        applySettingsImmediately()
    }

    fun updateSettings(settings: EqSettings) {
        currentSettings = settings.copy(
            bass = settings.bass.coerceIn(0f, 1f),
            treble = settings.treble.coerceIn(-1f, 1f),
            spaciousness = settings.spaciousness.coerceIn(0f, 1f),
            bands = settings.bands.map { it.coerceIn(-1f, 1f) },
        )
        animateBassAmountTo(currentSettings.bass)
    }

    fun setBitPerfectBypass(enabled: Boolean) {
        if (bypassedForBitPerfectUsb == enabled) return
        bypassedForBitPerfectUsb = enabled
        if (enabled) {
            releaseEffects()
        } else {
            rebuildEffects()
        }
    }

    fun release() {
        bassSmoothingJob?.cancel()
        releaseEffects()
    }

    private fun rebuildEffects() {
        releaseEffects()
        if (currentAudioSessionId <= 0 || bypassedForBitPerfectUsb) return
        equalizer = createEqualizer(currentAudioSessionId)
        applySettingsImmediately()
    }

    private fun animateBassAmountTo(targetAmount: Float) {
        val clampedTarget = targetAmount.coerceIn(0f, 1f)
        bassSmoothingJob?.cancel()
        if (abs(currentBassAmount - clampedTarget) <= BASS_EPSILON) {
            currentBassAmount = clampedTarget
            applySettingsImmediately()
            return
        }
        val ramp = BassBoostProcessorModel.buildRamp(
            from = currentBassAmount,
            to = clampedTarget,
            smoothingTimeMs = BASS_BOOST_CONFIG.smoothingTimeMs,
        )
        bassSmoothingJob = scope.launch {
            ramp.forEach { value ->
                currentBassAmount = value
                applySettingsImmediately()
                delay(BASS_RAMP_FRAME_MS)
            }
            currentBassAmount = clampedTarget
            applySettingsImmediately()
        }
    }

    private fun applySettingsImmediately() {
        if (bypassedForBitPerfectUsb) {
            releaseEffects()
            return
        }
        val bandValues = currentSettings.bands.map { it.coerceIn(-1f, 1f) }
        val trebleValue = currentSettings.treble.coerceIn(-1f, 1f)
        val spaciousnessValue = currentSettings.spaciousness.coerceIn(0f, 1f)
        val bassConfig = BASS_BOOST_CONFIG.copy(
            enabled = currentBassAmount > BASS_EPSILON,
            amountNormalized = currentBassAmount,
        ).sanitized()
        equalizer?.let { effect ->
            runCatching {
                val levelRange = effect.bandLevelRange
                val minLevel = levelRange.firstOrNull()?.toInt() ?: -1500
                val maxLevel = levelRange.getOrNull(1)?.toInt() ?: 1500
                val hasBandActivity = bandValues.any { abs(it) > 0.01f } ||
                    currentBassAmount > BASS_EPSILON ||
                    abs(trebleValue) > 0.01f ||
                    spaciousnessValue > 0.01f
                effect.enabled = hasBandActivity
                repeat(effect.numberOfBands.toInt()) { index ->
                    val band = index.toShort()
                    val centerFrequencyHz = effect.getCenterFreq(band) / 1000f
                    val targetDb =
                        interpolatedEqBandValue(centerFrequencyHz, bandValues) * USER_EQ_MAX_DB +
                            bassContributionDb(centerFrequencyHz, bassConfig, currentSampleRateHz) +
                            trebleShelfContributionDb(centerFrequencyHz, trebleValue) +
                            spaciousnessContributionDb(centerFrequencyHz, spaciousnessValue)
                    val levelMb = (targetDb * 100f)
                        .roundToInt()
                        .coerceIn(minLevel, maxLevel)
                    effect.setBandLevel(band, levelMb.toShort())
                }
            }
        }
    }

    private fun releaseEffects() {
        equalizer?.release()
        equalizer = null
    }

    private fun createEqualizer(audioSessionId: Int): Equalizer? {
        return runCatching {
            Equalizer(0, audioSessionId)
        }.getOrNull()
    }

    private fun interpolatedEqBandValue(
        frequencyHz: Float,
        bandValues: List<Float>,
    ): Float {
        if (bandValues.isEmpty()) return 0f
        val nearestIndex = REFERENCE_BAND_FREQUENCIES_HZ
            .withIndex()
            .minByOrNull { (_, referenceHz) -> abs(referenceHz - frequencyHz) }
            ?.index
            ?: 0
        return bandValues.getOrElse(nearestIndex) { 0f }
    }

    private fun bassContributionDb(
        frequencyHz: Float,
        config: BassBoostConfig,
        sampleRateHz: Int,
    ): Float {
        return BassBoostProcessorModel.responseAt(
            frequencyHz = frequencyHz,
            sampleRateHz = sampleRateHz,
            config = config,
        ).totalDb
    }

    private fun trebleShelfContributionDb(
        frequencyHz: Float,
        trebleValue: Float,
    ): Float {
        if (trebleValue == 0f) return 0f
        val weight = when {
            frequencyHz >= 10000f -> 0.9f
            frequencyHz >= 7000f -> 0.7f
            frequencyHz >= 4500f -> 0.48f
            frequencyHz >= 2500f -> 0.24f
            else -> 0f
        }
        return trebleValue * weight * TREBLE_MAX_DB
    }

    private fun spaciousnessContributionDb(
        frequencyHz: Float,
        spaciousnessValue: Float,
    ): Float {
        if (spaciousnessValue <= 0f) return 0f
        val lowMidTrim = when {
            frequencyHz in 180f..280f -> -0.35f
            frequencyHz in 280f..520f -> -0.55f
            frequencyHz in 520f..900f -> -0.24f
            else -> 0f
        }
        val presenceLift = when {
            frequencyHz in 1200f..2200f -> 0.32f
            frequencyHz in 2200f..4800f -> 0.58f
            frequencyHz in 4800f..9200f -> 0.44f
            else -> 0f
        }
        val airLift = when {
            frequencyHz >= 10000f -> 0.26f
            else -> 0f
        }
        return spaciousnessValue * (lowMidTrim + presenceLift + airLift) * SPACIOUSNESS_MAX_DB
    }

    private companion object {
        const val DEFAULT_SAMPLE_RATE_HZ = 48_000
        const val MIN_SAMPLE_RATE_HZ = 8_000
        const val USER_EQ_MAX_DB = 7.5f
        const val TREBLE_MAX_DB = 4.25f
        const val SPACIOUSNESS_MAX_DB = 2.8f
        const val BASS_RAMP_FRAME_MS = 16L
        const val BASS_EPSILON = 0.0001f

        val BASS_BOOST_CONFIG = BassBoostConfig()

        val REFERENCE_BAND_FREQUENCIES_HZ = listOf(
            32f, 48f, 64f, 96f, 125f, 180f, 250f, 360f,
            500f, 700f, 1000f, 2000f, 4000f, 8000f, 12000f, 16000f,
        )
    }
}
