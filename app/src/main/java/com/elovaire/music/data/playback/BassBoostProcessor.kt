package elovaire.music.app.data.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal data class BassBoostConfig(
    val enabled: Boolean = true,
    val amountNormalized: Float = 0f,
    val maxBoostDb: Float = 7.5f,
    val shelfFrequencyHz: Float = 96f,
    val shelfSlope: Float = 0.85f,
    val mudTrimCenterHz: Float = 220f,
    val mudTrimOctaves: Float = 1.05f,
    val maxMudTrimDb: Float = 1.4f,
    val pregainRatio: Float = 0.56f,
    val limiterEnabled: Boolean = false,
    val smoothingTimeMs: Int = 140,
) {
    fun sanitized(): BassBoostConfig {
        return copy(
            amountNormalized = amountNormalized.coerceIn(0f, 1f),
            maxBoostDb = maxBoostDb.coerceIn(0f, 9f),
            shelfFrequencyHz = shelfFrequencyHz.coerceIn(40f, 180f),
            shelfSlope = shelfSlope.coerceIn(0.35f, 1.2f),
            mudTrimCenterHz = mudTrimCenterHz.coerceIn(140f, 320f),
            mudTrimOctaves = mudTrimOctaves.coerceIn(0.4f, 2.5f),
            maxMudTrimDb = maxMudTrimDb.coerceIn(0f, 3f),
            pregainRatio = pregainRatio.coerceIn(0.2f, 0.8f),
            smoothingTimeMs = smoothingTimeMs.coerceIn(24, 320),
        )
    }
}

internal data class BassBoostResponse(
    val boostDb: Float,
    val pregainDb: Float,
    val shelfDb: Float,
    val mudTrimDb: Float,
    val totalDb: Float,
)

internal object BassBoostProcessorModel {
    fun normalizedAmountToBoostDb(
        amountNormalized: Float,
        config: BassBoostConfig = BassBoostConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val normalized = amountNormalized.coerceIn(0f, 1f)
        if (!safeConfig.enabled || normalized <= 0f || safeConfig.maxBoostDb <= 0f) return 0f
        val curved = normalized.toDouble().pow(1.7).toFloat()
        return safeConfig.maxBoostDb * curved
    }

    fun pregainDbForBoost(
        boostDb: Float,
        config: BassBoostConfig = BassBoostConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        return -(boostDb.coerceAtLeast(0f) * safeConfig.pregainRatio)
    }

    fun responseAt(
        frequencyHz: Float,
        sampleRateHz: Int,
        config: BassBoostConfig,
    ): BassBoostResponse {
        val safeConfig = config.sanitized()
        if (!safeConfig.enabled || safeConfig.amountNormalized <= 0f) {
            return BassBoostResponse(
                boostDb = 0f,
                pregainDb = 0f,
                shelfDb = 0f,
                mudTrimDb = 0f,
                totalDb = 0f,
            )
        }

        val safeFrequencyHz = frequencyHz.coerceAtLeast(10f)
        val safeSampleRateHz = sampleRateHz.coerceAtLeast(8_000)
        val boostDb = normalizedAmountToBoostDb(safeConfig.amountNormalized, safeConfig)
        val pregainDb = pregainDbForBoost(boostDb, safeConfig)
        val shelf = LowShelfBiquad.design(
            sampleRateHz = safeSampleRateHz.toFloat(),
            cornerFrequencyHz = safeConfig.shelfFrequencyHz,
            gainDb = boostDb,
            slope = safeConfig.shelfSlope,
        )
        val shelfDb = shelf.magnitudeResponseDb(safeFrequencyHz, safeSampleRateHz.toFloat())
        val mudTrimDb = mudTrimDb(
            frequencyHz = safeFrequencyHz,
            amountNormalized = safeConfig.amountNormalized,
            centerFrequencyHz = safeConfig.mudTrimCenterHz,
            widthInOctaves = safeConfig.mudTrimOctaves,
            maxTrimDb = safeConfig.maxMudTrimDb,
        )
        val totalDb = shelfDb + mudTrimDb + pregainDb
        return BassBoostResponse(
            boostDb = boostDb,
            pregainDb = pregainDb,
            shelfDb = shelfDb,
            mudTrimDb = mudTrimDb,
            totalDb = totalDb,
        )
    }

    fun buildRamp(
        from: Float,
        to: Float,
        smoothingTimeMs: Int,
        frameTimeMs: Int = DEFAULT_RAMP_FRAME_MS,
    ): List<Float> {
        val safeFrom = from.coerceIn(0f, 1f)
        val safeTo = to.coerceIn(0f, 1f)
        val frames = max(1, smoothingTimeMs.coerceAtLeast(frameTimeMs) / frameTimeMs)
        return buildList(frames) {
            repeat(frames) { frameIndex ->
                val t = (frameIndex + 1).toFloat() / frames.toFloat()
                val eased = 1f - (1f - t) * (1f - t)
                add(safeFrom + ((safeTo - safeFrom) * eased))
            }
        }
    }

    private fun mudTrimDb(
        frequencyHz: Float,
        amountNormalized: Float,
        centerFrequencyHz: Float,
        widthInOctaves: Float,
        maxTrimDb: Float,
    ): Float {
        if (amountNormalized <= 0f) return 0f
        val logDistance = log2(frequencyHz / centerFrequencyHz.coerceAtLeast(1f))
        val sigma = (widthInOctaves.coerceAtLeast(0.1f) / 2f).toDouble()
        val gaussian = exp(-0.5 * ((logDistance / sigma).pow(2.0))).toFloat()
        val trimAmount = amountNormalized.toDouble().pow(1.25).toFloat()
        return -(gaussian * maxTrimDb * trimAmount)
    }

    private fun log2(value: Float): Float {
        return (ln(value.toDouble()) / ln(2.0)).toFloat()
    }

    private const val DEFAULT_RAMP_FRAME_MS = 16
}

internal data class LowShelfBiquad(
    val b0: Double,
    val b1: Double,
    val b2: Double,
    val a1: Double,
    val a2: Double,
) {
    fun magnitudeResponseDb(
        frequencyHz: Float,
        sampleRateHz: Float,
    ): Float {
        val omega = 2.0 * PI * frequencyHz.coerceAtLeast(0.1f).toDouble() / sampleRateHz.coerceAtLeast(1f).toDouble()
        val cos1 = cos(omega)
        val sin1 = sin(omega)
        val cos2 = cos(omega * 2.0)
        val sin2 = sin(omega * 2.0)

        val numeratorReal = b0 + (b1 * cos1) + (b2 * cos2)
        val numeratorImag = -(b1 * sin1) - (b2 * sin2)
        val denominatorReal = 1.0 + (a1 * cos1) + (a2 * cos2)
        val denominatorImag = -(a1 * sin1) - (a2 * sin2)

        val numeratorMagnitude = sqrt((numeratorReal * numeratorReal) + (numeratorImag * numeratorImag))
        val denominatorMagnitude = sqrt((denominatorReal * denominatorReal) + (denominatorImag * denominatorImag))
        val magnitude = (numeratorMagnitude / denominatorMagnitude.coerceAtLeast(1e-12)).coerceAtLeast(1e-12)
        return (20.0 * log10(magnitude)).toFloat()
    }

    companion object {
        fun design(
            sampleRateHz: Float,
            cornerFrequencyHz: Float,
            gainDb: Float,
            slope: Float,
        ): LowShelfBiquad {
            if (gainDb == 0f) {
                return LowShelfBiquad(
                    b0 = 1.0,
                    b1 = 0.0,
                    b2 = 0.0,
                    a1 = 0.0,
                    a2 = 0.0,
                )
            }
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val safeCorner = cornerFrequencyHz.coerceIn(20f, safeSampleRate / 2.2f)
            val safeSlope = slope.coerceIn(0.35f, 1.2f)
            val a = 10.0.pow(gainDb / 40.0)
            val w0 = 2.0 * PI * safeCorner.toDouble() / safeSampleRate.toDouble()
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = (sinW0 / 2.0) * sqrt((a + (1.0 / a)) * ((1.0 / safeSlope) - 1.0) + 2.0)
            val beta = 2.0 * sqrt(a) * alpha

            val b0 = a * ((a + 1.0) - ((a - 1.0) * cosW0) + beta)
            val b1 = 2.0 * a * ((a - 1.0) - ((a + 1.0) * cosW0))
            val b2 = a * ((a + 1.0) - ((a - 1.0) * cosW0) - beta)
            val a0 = (a + 1.0) + ((a - 1.0) * cosW0) + beta
            val a1 = -2.0 * ((a - 1.0) + ((a + 1.0) * cosW0))
            val a2 = (a + 1.0) + ((a - 1.0) * cosW0) - beta

            return LowShelfBiquad(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }
    }
}
