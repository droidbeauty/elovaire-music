package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlin.math.abs
import kotlin.math.round

internal object EqValuePolicy {
    const val MIN_GAIN_DB = -8f
    const val MAX_GAIN_DB = 8f
    const val GAIN_STEP_DB = 0.5f
    const val MIN_NORMALIZED = -1f
    const val MAX_NORMALIZED = 1f
    const val MACRO_STEP = 0.01f
    const val EFFECT_BYPASS_EPSILON = 0.01f

    fun clampBandNormalized(value: Float): Float {
        val db = normalizedToDb(value)
        return dbToNormalized(round(db / GAIN_STEP_DB) * GAIN_STEP_DB)
    }

    fun clampMacro(value: Float): Float {
        val clamped = value.coerceIn(MIN_NORMALIZED, MAX_NORMALIZED)
        return (round(clamped / MACRO_STEP) * MACRO_STEP).coerceIn(MIN_NORMALIZED, MAX_NORMALIZED)
    }

    fun clampPositiveMacro(value: Float): Float = clampMacro(value).coerceAtLeast(0f)

    fun normalizedToDb(value: Float): Float {
        val normalized = value.coerceIn(MIN_NORMALIZED, MAX_NORMALIZED)
        return if (normalized >= 0f) normalized * MAX_GAIN_DB else -normalized * MIN_GAIN_DB
    }

    fun dbToNormalized(valueDb: Float): Float {
        val clamped = valueDb.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
        return if (clamped >= 0f) clamped / MAX_GAIN_DB else clamped / -MIN_GAIN_DB
    }

    fun sanitize(settings: EqSettings): EqSettings {
        val spaciousness = clampPositiveMacro(settings.spaciousness)
        val spaciousnessMode = if (spaciousness <= EFFECT_BYPASS_EPSILON) {
            SpaciousnessMode.Off
        } else {
            settings.spaciousnessMode
        }
        return settings.copy(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                clampBandNormalized(settings.bands.getOrElse(index) { 0f })
            },
            bass = clampPositiveMacro(settings.bass),
            midrange = clampMacro(settings.midrange),
            treble = clampMacro(settings.treble),
            spaciousness = if (spaciousnessMode == SpaciousnessMode.Off) 0f else spaciousness,
            spaciousnessMode = spaciousnessMode,
            reverbDurationMs = normalizeReverbDurationMs(settings.reverbDurationMs),
        )
    }

    fun hasSignalAlteringEffects(settings: EqSettings): Boolean {
        val sanitized = sanitize(settings)
        return sanitized.monoEnabled ||
            sanitized.bands.any { abs(it) >= EFFECT_BYPASS_EPSILON } ||
            abs(sanitized.bass) >= EFFECT_BYPASS_EPSILON ||
            abs(sanitized.midrange) >= EFFECT_BYPASS_EPSILON ||
            abs(sanitized.treble) >= EFFECT_BYPASS_EPSILON ||
            sanitized.reverbDurationMs > 0 ||
            (sanitized.spaciousnessMode != SpaciousnessMode.Off &&
                abs(sanitized.spaciousness) >= EFFECT_BYPASS_EPSILON)
    }
}
