package elovaire.music.app.data.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import elovaire.music.app.domain.model.EqSettings
import kotlin.math.abs

class PlaybackEffectsController {
    private var currentAudioSessionId: Int = 0
    private var currentSettings: EqSettings = EqSettings()
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    fun updateAudioSessionId(audioSessionId: Int) {
        if (currentAudioSessionId == audioSessionId) return
        currentAudioSessionId = audioSessionId
        rebuildEffects()
    }

    fun updateSettings(settings: EqSettings) {
        currentSettings = settings
        applySettings()
    }

    fun release() {
        releaseEffects()
    }

    private fun rebuildEffects() {
        releaseEffects()
        if (currentAudioSessionId <= 0) return

        equalizer = createEqualizer(currentAudioSessionId)
        bassBoost = createBassBoost(currentAudioSessionId)
        applySettings()
    }

    private fun applySettings() {
        val bandValues = currentSettings.bands.map { it.coerceIn(-1f, 1f) }
        val bassValue = currentSettings.bass.coerceIn(-1f, 1f)
        val trebleValue = currentSettings.treble.coerceIn(-1f, 1f)
        val spaciousnessValue = currentSettings.spaciousness.coerceAtLeast(0f).coerceIn(0f, 1f)
        equalizer?.let { effect ->
            runCatching {
                val levelRange = effect.bandLevelRange
                val minLevel = levelRange.firstOrNull()?.toInt() ?: -1500
                val maxLevel = levelRange.getOrNull(1)?.toInt() ?: 1500
                val symmetricMax = minOf(abs(minLevel), abs(maxLevel)).toFloat().coerceAtLeast(1f)
                val hasBandActivity = bandValues.any { abs(it) > 0.01f } ||
                    abs(bassValue) > 0.01f ||
                    abs(trebleValue) > 0.01f ||
                    spaciousnessValue > 0.01f
                effect.enabled = hasBandActivity
                repeat(effect.numberOfBands.toInt()) { index ->
                    val band = index.toShort()
                    val centerFrequencyHz = effect.getCenterFreq(band) / 1000f
                    val targetLevel = (
                        interpolatedEqBandValue(centerFrequencyHz, bandValues) * 0.9f +
                            bassShelfContribution(centerFrequencyHz, bassValue) +
                            trebleShelfContribution(centerFrequencyHz, trebleValue) +
                            spaciousnessCompensationContribution(centerFrequencyHz, spaciousnessValue)
                        ).coerceIn(-1f, 1f)
                    val levelMb = (targetLevel * symmetricMax * 0.78f)
                        .toInt()
                        .coerceIn(minLevel, maxLevel)
                    effect.setBandLevel(band, levelMb.toShort())
                }
            }
        }

        bassBoost?.let { effect ->
            runCatching {
                val positiveBass = bassValue.coerceAtLeast(0f)
                effect.enabled = positiveBass > 0f
                if (positiveBass > 0f && effect.strengthSupported) {
                    effect.setStrength((positiveBass * MAX_BASS_STRENGTH).toInt().toShort())
                }
            }
        }

    }

    private fun releaseEffects() {
        equalizer?.release()
        bassBoost?.release()
        equalizer = null
        bassBoost = null
    }

    private fun createEqualizer(audioSessionId: Int): Equalizer? {
        return runCatching {
            Equalizer(0, audioSessionId)
        }.getOrNull()
    }

    private fun createBassBoost(audioSessionId: Int): BassBoost? {
        return runCatching {
            BassBoost(0, audioSessionId)
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

    private fun bassShelfContribution(
        frequencyHz: Float,
        bassValue: Float,
    ): Float {
        if (bassValue == 0f) return 0f
        val weight = when {
            frequencyHz <= 90f -> 0.9f
            frequencyHz <= 180f -> 0.72f
            frequencyHz <= 320f -> 0.44f
            frequencyHz <= 520f -> 0.18f
            else -> 0f
        }
        return bassValue * weight
    }

    private fun trebleShelfContribution(
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
        return trebleValue * weight
    }

    private fun spaciousnessCompensationContribution(
        frequencyHz: Float,
        spaciousnessValue: Float,
    ): Float {
        if (spaciousnessValue <= 0f) return 0f
        val lowMidTrim = when {
            frequencyHz in 160f..280f -> -0.05f
            frequencyHz in 280f..520f -> -0.08f
            frequencyHz in 520f..900f -> -0.04f
            else -> 0f
        }
        val presenceLift = when {
            frequencyHz in 1200f..2200f -> 0.08f
            frequencyHz in 2200f..4800f -> 0.14f
            frequencyHz in 4800f..9200f -> 0.11f
            else -> 0f
        }
        val airLift = when {
            frequencyHz >= 10000f -> 0.08f
            else -> 0f
        }
        return spaciousnessValue * (lowMidTrim + presenceLift + airLift)
    }

    private companion object {
        const val MAX_BASS_STRENGTH = 820f
        val REFERENCE_BAND_FREQUENCIES_HZ = listOf(
            32f, 48f, 64f, 96f, 125f, 180f, 250f, 360f,
            500f, 700f, 1000f, 2000f, 4000f, 8000f, 12000f, 16000f,
        )
    }
}
