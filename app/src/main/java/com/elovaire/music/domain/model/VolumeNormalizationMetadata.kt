package elovaire.music.droidbeauty.app.domain.model

import kotlin.math.abs
import kotlin.math.pow

data class VolumeNormalizationMetadata(
    val trackGainDb: Float? = null,
    val albumGainDb: Float? = null,
    val trackPeak: Float? = null,
    val albumPeak: Float? = null,
) {
    val hasUsableGain: Boolean
        get() = trackGainDb != null || albumGainDb != null
}

internal object VolumeNormalizationPolicy {
    fun effectivePlayerGain(
        baseGain: Float,
        metadata: VolumeNormalizationMetadata?,
        enabled: Boolean,
        softwareGainAllowed: Boolean,
    ): Float {
        if (!softwareGainAllowed) return 1f
        val normalizedBaseGain = baseGain.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 1f
        val normalizationGain = if (enabled) multiplierFor(metadata) else 1f
        return (normalizedBaseGain * normalizationGain).coerceIn(0f, PLAYER_VOLUME_MAX)
    }

    fun isSignalAltering(
        metadata: VolumeNormalizationMetadata?,
        enabled: Boolean,
    ): Boolean {
        return enabled && abs(multiplierFor(metadata) - 1f) > NEUTRAL_GAIN_EPSILON
    }

    fun multiplierFor(metadata: VolumeNormalizationMetadata?): Float {
        val normalizedMetadata = metadata ?: return 1f
        val gainDb = normalizedMetadata.trackGainDb ?: normalizedMetadata.albumGainDb ?: return 1f
        if (!gainDb.isFinite()) return 1f
        val unclippedMultiplier = 10f.pow(gainDb / 20f)
            .coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER)
        val peak = normalizedMetadata.trackPeak ?: normalizedMetadata.albumPeak
        val peakLimited = peak
            ?.takeIf { it.isFinite() && it > 0f }
            ?.let { peakValue -> minOf(unclippedMultiplier, 1f / peakValue) }
            ?: unclippedMultiplier
        return peakLimited.coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER)
    }

    fun parseGain(rawValue: String?, r128: Boolean = false): Float? {
        val numericValue = parseFirstNumber(rawValue) ?: return null
        val gainDb = if (r128 && kotlin.math.abs(numericValue) > R128_DB_THRESHOLD) {
            numericValue / R128_FIXED_POINT_SCALE
        } else {
            numericValue
        }
        return gainDb.takeIf { it.isFinite() && it in MIN_GAIN_DB..MAX_GAIN_DB }
    }

    fun parsePeak(rawValue: String?): Float? {
        val peak = parseFirstNumber(rawValue) ?: return null
        return peak.takeIf { it.isFinite() && it > 0f && it <= MAX_PEAK }
    }

    private fun parseFirstNumber(rawValue: String?): Float? {
        val value = rawValue?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return NUMBER_REGEX.find(value)?.value?.toFloatOrNull()
    }

    private const val MIN_MULTIPLIER = 0.25f
    private const val MAX_MULTIPLIER = 1.5f
    private const val PLAYER_VOLUME_MAX = 1f
    private const val NEUTRAL_GAIN_EPSILON = 0.001f
    private const val MIN_GAIN_DB = -60f
    private const val MAX_GAIN_DB = 24f
    private const val MAX_PEAK = 8f
    private const val R128_DB_THRESHOLD = 64f
    private const val R128_FIXED_POINT_SCALE = 256f
    private val NUMBER_REGEX = Regex("""[-+]?(?:\d+(?:\.\d+)?|\.\d+)""")
}
