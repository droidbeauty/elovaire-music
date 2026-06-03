package elovaire.music.droidbeauty.app.data.playback

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal data class HighQualityBassConfig(
    val enabled: Boolean = true,
    val amountNormalized: Float = 0f,
    val highPassFrequencyHz: Float = 24f,
    val shelfFrequencyHz: Float = 82f,
    val shelfSlope: Float = 0.74f,
    val maxShelfBoostDb: Float = 8.6f,
    val punchCenterHz: Float = 64f,
    val punchQ: Float = 0.82f,
    val maxPunchDb: Float = 2.35f,
    val mudTrimCenterHz: Float = 255f,
    val mudTrimQ: Float = 0.78f,
    val maxMudTrimDb: Float = 0.48f,
    val dynamicControlThreshold: Float = 0.26f,
    val maxDynamicReductionDb: Float = 1.65f,
    val smoothingTimeMs: Int = 90,
) {
    fun sanitized(): HighQualityBassConfig {
        return copy(
            amountNormalized = amountNormalized.coerceIn(0f, 1f),
            highPassFrequencyHz = highPassFrequencyHz.coerceIn(18f, 32f),
            shelfFrequencyHz = shelfFrequencyHz.coerceIn(65f, 110f),
            shelfSlope = shelfSlope.coerceIn(0.5f, 1.05f),
            maxShelfBoostDb = maxShelfBoostDb.coerceIn(0f, 9f),
            punchCenterHz = punchCenterHz.coerceIn(45f, 78f),
            punchQ = punchQ.coerceIn(0.55f, 1.25f),
            maxPunchDb = maxPunchDb.coerceIn(0f, 2.7f),
            mudTrimCenterHz = mudTrimCenterHz.coerceIn(170f, 300f),
            mudTrimQ = mudTrimQ.coerceIn(0.55f, 1.25f),
            maxMudTrimDb = maxMudTrimDb.coerceIn(0f, 1.8f),
            dynamicControlThreshold = dynamicControlThreshold.coerceIn(0.08f, 0.45f),
            maxDynamicReductionDb = maxDynamicReductionDb.coerceIn(0f, 4f),
            smoothingTimeMs = smoothingTimeMs.coerceIn(45, 160),
        )
    }
}

internal data class HighQualityBassCurve(
    val amount: Float,
    val shelfDb: Float,
    val punchDb: Float,
    val mudTrimDb: Float,
    val automaticHeadroomDb: Float,
) {
    val isBypassed: Boolean
        get() = amount <= 0.0005f &&
            abs(shelfDb) <= 0.0005f &&
            abs(punchDb) <= 0.0005f &&
            abs(mudTrimDb) <= 0.0005f &&
            abs(automaticHeadroomDb) <= 0.0005f
}

internal data class HighQualityBassResponse(
    val shelfDb: Float,
    val punchDb: Float,
    val mudTrimDb: Float,
    val highPassDb: Float,
    val automaticHeadroomDb: Float,
    val totalDb: Float,
)

internal object HighQualityBassProcessorModel {
    fun curveFor(
        amountNormalized: Float,
        config: HighQualityBassConfig = HighQualityBassConfig(),
        lowBandEqBoostSafetyDb: Float = 0f,
        trebleBoostDb: Float = 0f,
        spaciousnessAmount: Float = 0f,
    ): HighQualityBassCurve {
        val safeConfig = config.sanitized()
        val amount = amountNormalized.coerceIn(0f, 1f)
        if (!safeConfig.enabled || amount <= 0.0005f) {
            return HighQualityBassCurve(
                amount = 0f,
                shelfDb = 0f,
                punchDb = 0f,
                mudTrimDb = 0f,
                automaticHeadroomDb = 0f,
            )
        }

        val shelfCurve = amount.toDouble().pow(1.22).toFloat()
        val punchCurve = amount.toDouble().pow(1.72).toFloat()
        val mudCurve = amount.toDouble().pow(1.9).toFloat()
        val shelfDb = safeConfig.maxShelfBoostDb * shelfCurve
        val punchDb = safeConfig.maxPunchDb * punchCurve
        val mudTrimDb = -(safeConfig.maxMudTrimDb * mudCurve)
        val automaticHeadroomDb = automaticHeadroomDb(
            shelfDb = shelfDb,
            punchDb = punchDb,
            lowBandEqBoostSafetyDb = lowBandEqBoostSafetyDb,
            trebleBoostDb = trebleBoostDb,
            spaciousnessAmount = spaciousnessAmount,
        )
        return HighQualityBassCurve(
            amount = amount,
            shelfDb = shelfDb,
            punchDb = punchDb,
            mudTrimDb = mudTrimDb,
            automaticHeadroomDb = automaticHeadroomDb,
        )
    }

    fun automaticHeadroomDb(
        shelfDb: Float,
        punchDb: Float,
        lowBandEqBoostSafetyDb: Float = 0f,
        trebleBoostDb: Float = 0f,
        spaciousnessAmount: Float = 0f,
    ): Float {
        val bassSafety = (shelfDb.coerceAtLeast(0f) * 0.65f) +
            (punchDb.coerceAtLeast(0f) * 0.5f)
        val eqSafety = lowBandEqBoostSafetyDb.coerceAtLeast(0f) * 0.42f
        val trebleSafety = trebleBoostDb.coerceAtLeast(0f) * 0.08f
        val spatialSafety = spaciousnessAmount.coerceIn(0f, 1f) * 0.35f
        return -(bassSafety + eqSafety + trebleSafety + spatialSafety).coerceIn(0f, 12f)
    }

    fun dynamicReductionDb(
        lowBandEnvelope: Float,
        amountNormalized: Float,
        config: HighQualityBassConfig = HighQualityBassConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val amount = amountNormalized.coerceIn(0f, 1f)
        if (amount <= 0.0005f || lowBandEnvelope <= safeConfig.dynamicControlThreshold) return 0f
        val excess = ((lowBandEnvelope - safeConfig.dynamicControlThreshold) / (1f - safeConfig.dynamicControlThreshold))
            .coerceIn(0f, 1f)
        val shaped = 1f - exp(-(excess * 3.1f).toDouble()).toFloat()
        return -(safeConfig.maxDynamicReductionDb * amount * shaped).coerceIn(0f, safeConfig.maxDynamicReductionDb)
    }

    fun responseAt(
        frequencyHz: Float,
        sampleRateHz: Int,
        config: HighQualityBassConfig,
        lowBandEqBoostSafetyDb: Float = 0f,
        trebleBoostDb: Float = 0f,
        spaciousnessAmount: Float = 0f,
    ): HighQualityBassResponse {
        val safeConfig = config.sanitized()
        val curve = curveFor(
            amountNormalized = safeConfig.amountNormalized,
            config = safeConfig,
            lowBandEqBoostSafetyDb = lowBandEqBoostSafetyDb,
            trebleBoostDb = trebleBoostDb,
            spaciousnessAmount = spaciousnessAmount,
        )
        if (curve.isBypassed) {
            return HighQualityBassResponse(
                shelfDb = 0f,
                punchDb = 0f,
                mudTrimDb = 0f,
                highPassDb = 0f,
                automaticHeadroomDb = 0f,
                totalDb = 0f,
            )
        }
        val safeSampleRate = sampleRateHz.coerceAtLeast(8_000).toFloat()
        val safeFrequency = frequencyHz.coerceAtLeast(10f)
        val highPass = SimpleBassBiquad.highPass(
            sampleRateHz = safeSampleRate,
            cutoffFrequencyHz = safeConfig.highPassFrequencyHz,
            q = 0.707f,
        ).magnitudeResponseDb(safeFrequency, safeSampleRate)
        val shelf = SimpleBassBiquad.lowShelf(
            sampleRateHz = safeSampleRate,
            cornerFrequencyHz = safeConfig.shelfFrequencyHz,
            slope = safeConfig.shelfSlope,
            gainDb = curve.shelfDb,
        ).magnitudeResponseDb(safeFrequency, safeSampleRate)
        val punch = bellResponseDb(
            frequencyHz = safeFrequency,
            centerFrequencyHz = safeConfig.punchCenterHz,
            q = safeConfig.punchQ,
            gainDb = curve.punchDb,
        )
        val mud = bellResponseDb(
            frequencyHz = safeFrequency,
            centerFrequencyHz = safeConfig.mudTrimCenterHz,
            q = safeConfig.mudTrimQ,
            gainDb = curve.mudTrimDb,
        )
        return HighQualityBassResponse(
            shelfDb = shelf,
            punchDb = punch,
            mudTrimDb = mud,
            highPassDb = highPass,
            automaticHeadroomDb = curve.automaticHeadroomDb,
            totalDb = highPass + shelf + punch + mud + curve.automaticHeadroomDb,
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

    private fun bellResponseDb(
        frequencyHz: Float,
        centerFrequencyHz: Float,
        q: Float,
        gainDb: Float,
    ): Float {
        if (abs(gainDb) <= 0.0001f) return 0f
        val logDistance = log2(frequencyHz / centerFrequencyHz.coerceAtLeast(1f))
        val sigma = (1f / q.coerceAtLeast(0.35f)).coerceIn(0.2f, 1.8f).toDouble()
        val gaussian = exp(-0.5 * ((logDistance / sigma).pow(2.0))).toFloat()
        return gaussian * gainDb
    }

    private fun log2(value: Float): Float {
        return (ln(value.toDouble()) / ln(2.0)).toFloat()
    }

    private const val DEFAULT_RAMP_FRAME_MS = 16
}

private data class SimpleBassBiquad(
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
        fun highPass(
            sampleRateHz: Float,
            cutoffFrequencyHz: Float,
            q: Float,
        ): SimpleBassBiquad {
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val safeCutoff = cutoffFrequencyHz.coerceIn(10f, safeSampleRate / 2.4f)
            val safeQ = q.coerceIn(0.2f, 4f)
            val w0 = 2.0 * PI * safeCutoff.toDouble() / safeSampleRate.toDouble()
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * safeQ)
            val b0 = (1.0 + cosW0) / 2.0
            val b1 = -(1.0 + cosW0)
            val b2 = (1.0 + cosW0) / 2.0
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosW0
            val a2 = 1.0 - alpha
            return SimpleBassBiquad(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }

        fun lowShelf(
            sampleRateHz: Float,
            cornerFrequencyHz: Float,
            slope: Float,
            gainDb: Float,
        ): SimpleBassBiquad {
            if (abs(gainDb) <= 0.0001f) {
                return SimpleBassBiquad(1.0, 0.0, 0.0, 0.0, 0.0)
            }
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val safeCorner = cornerFrequencyHz.coerceIn(20f, safeSampleRate / 2.4f)
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
            return SimpleBassBiquad(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }
    }
}
