package elovaire.music.app.data.playback

import elovaire.music.app.domain.model.SpaciousnessMode
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

internal data class SpaciousnessConfig(
    val enabled: Boolean = true,
    val mode: SpaciousnessMode = SpaciousnessMode.StereoWidth,
    val amountNormalized: Float = 0f,
    val preserveBassMono: Boolean = true,
    val smoothingTimeMs: Int = 56,
    val safetyLimiterEnabled: Boolean = false,
) {
    fun sanitized(): SpaciousnessConfig {
        return copy(
            amountNormalized = amountNormalized.coerceIn(0f, 1f),
            smoothingTimeMs = smoothingTimeMs.coerceIn(20, 120),
        )
    }
}

internal data class SpaciousnessDiagnosticsSnapshot(
    val mode: SpaciousnessMode,
    val amountNormalized: Float,
    val sampleRateHz: Int,
    val channelCount: Int,
    val bypassed: Boolean,
    val peakIn: Float,
    val peakOut: Float,
    val headroomCompensationDb: Float,
    val resetEvents: Long,
    val bitPerfectBypassEvents: Long,
)

internal object SpaciousnessProcessorModel {
    private const val FLAT_EPSILON = 0.0005f

    fun isBypassed(config: SpaciousnessConfig): Boolean {
        val safeConfig = config.sanitized()
        return !safeConfig.enabled ||
            safeConfig.mode == SpaciousnessMode.Off ||
            safeConfig.amountNormalized <= FLAT_EPSILON
    }

    fun mappedAmount(amountNormalized: Float): Float {
        return amountNormalized.coerceIn(0f, 1f).toDouble().pow(1.35).toFloat()
    }

    fun automaticHeadroomDb(
        mode: SpaciousnessMode,
        amountNormalized: Float,
    ): Float {
        val amount = mappedAmount(amountNormalized)
        if (amount <= 0f || mode == SpaciousnessMode.Off) return 0f
        val compensationDb = when (mode) {
            SpaciousnessMode.Off -> 0f
            SpaciousnessMode.StereoWidth -> 0.6f + (amount * 0.9f)
            SpaciousnessMode.CrossfeedDepth -> 0.25f + (amount * 0.45f)
            SpaciousnessMode.EarlyReflectionRoom -> 0.35f + (amount * 0.7f)
            SpaciousnessMode.HaasSpace -> 0.4f + (amount * 0.8f)
            SpaciousnessMode.HarmonicAir -> 0.22f + (amount * 0.5f)
        }
        return -compensationDb.coerceAtMost(2.4f)
    }
}

internal class SpaciousnessProcessor {
    @Volatile
    private var pendingConfig = SpaciousnessConfig()

    @Volatile
    private var bitPerfectBypassed = false

    private var activeConfig = pendingConfig.sanitized()
    private var activeMode = activeConfig.mode
    private var sampleRateHz = 48_000
    private var channelCount = 2
    private var currentAmount = 0f
    private var targetAmount = 0f
    private var smoothingAlpha = 1f
    private var leftDelay = FloatArray(1)
    private var rightDelay = FloatArray(1)
    private var delayWriteIndex = 0
    private var peakIn = 0f
    private var peakOut = 0f
    private var resetEvents = 0L
    private var bitPerfectBypassEvents = 0L

    private val sideLowPass = OnePoleLowPassState()
    private val crossfeedLowPassL = OnePoleLowPassState()
    private val crossfeedLowPassR = OnePoleLowPassState()
    private val reflectionLowPassL = OnePoleLowPassState()
    private val reflectionLowPassR = OnePoleLowPassState()
    private val haasHighPassL = OnePoleHighPassState()
    private val haasHighPassR = OnePoleHighPassState()
    private val airHighPassL = OnePoleHighPassState()
    private val airHighPassR = OnePoleHighPassState()
    private val airWetLowPassL = OnePoleLowPassState()
    private val airWetLowPassR = OnePoleLowPassState()

    fun setConfig(config: SpaciousnessConfig) {
        val sanitized = config.sanitized()
        pendingConfig = sanitized
        if (SpaciousnessProcessorModel.isBypassed(sanitized)) {
            activeConfig = sanitized
            activeMode = sanitized.mode
            targetAmount = 0f
            currentAmount = 0f
            reset()
        }
    }

    fun configure(
        sampleRateHz: Int,
        channelCount: Int,
    ) {
        this.sampleRateHz = sampleRateHz.coerceAtLeast(8_000)
        this.channelCount = channelCount.coerceAtLeast(1)
        smoothingAlpha = smoothingAlpha(
            sampleRateHz = this.sampleRateHz,
            smoothingTimeMs = activeConfig.smoothingTimeMs,
        )
        val maxDelaySamples = ((this.sampleRateHz / 1_000f) * MAX_DELAY_MS).toInt().coerceAtLeast(32)
        leftDelay = FloatArray(maxDelaySamples)
        rightDelay = FloatArray(maxDelaySamples)
        delayWriteIndex = 0
        reset()
    }

    fun setBitPerfectBypass(enabled: Boolean) {
        if (enabled && !bitPerfectBypassed) {
            bitPerfectBypassEvents += 1
        }
        bitPerfectBypassed = enabled
        if (enabled) {
            reset()
        }
    }

    fun isBypassed(): Boolean {
        return bitPerfectBypassed ||
            channelCount != 2 ||
            SpaciousnessProcessorModel.isBypassed(activeConfig) ||
            targetAmount <= 0.0005f
    }

    fun reset() {
        delayWriteIndex = 0
        leftDelay.fill(0f)
        rightDelay.fill(0f)
        sideLowPass.reset()
        crossfeedLowPassL.reset()
        crossfeedLowPassR.reset()
        reflectionLowPassL.reset()
        reflectionLowPassR.reset()
        haasHighPassL.reset()
        haasHighPassR.reset()
        airHighPassL.reset()
        airHighPassR.reset()
        airWetLowPassL.reset()
        airWetLowPassR.reset()
        peakIn = 0f
        peakOut = 0f
        resetEvents += 1
    }

    fun processFrame(
        frame: FloatArray,
        channels: Int,
    ) {
        syncConfig()
        if (channels != 2 || frame.size < 2 || isBypassed()) return

        currentAmount = smooth(currentAmount, targetAmount, smoothingAlpha)
        if (currentAmount <= 0.0005f) {
            return
        }

        val dryLeft = frame[0]
        val dryRight = frame[1]
        peakIn = max(peakIn, max(abs(dryLeft), abs(dryRight)))

        val amount = SpaciousnessProcessorModel.mappedAmount(currentAmount)
        val headroomGain = dbToLinear(SpaciousnessProcessorModel.automaticHeadroomDb(activeMode, currentAmount))

        val processed = when (activeMode) {
            SpaciousnessMode.Off -> StereoPair(dryLeft, dryRight)
            SpaciousnessMode.StereoWidth -> processStereoWidth(dryLeft, dryRight, amount)
            SpaciousnessMode.CrossfeedDepth -> processCrossfeedDepth(dryLeft, dryRight, amount)
            SpaciousnessMode.EarlyReflectionRoom -> processEarlyReflectionRoom(dryLeft, dryRight, amount)
            SpaciousnessMode.HaasSpace -> processHaasSpace(dryLeft, dryRight, amount)
            SpaciousnessMode.HarmonicAir -> processHarmonicAir(dryLeft, dryRight, amount)
        }

        val outLeft = sanitizeSample(processed.left * headroomGain)
        val outRight = sanitizeSample(processed.right * headroomGain)
        frame[0] = outLeft
        frame[1] = outRight
        peakOut = max(peakOut, max(abs(outLeft), abs(outRight)))

        writeDelay(dryLeft, dryRight)
    }

    fun diagnosticsSnapshot(): SpaciousnessDiagnosticsSnapshot {
        return SpaciousnessDiagnosticsSnapshot(
            mode = activeMode,
            amountNormalized = targetAmount,
            sampleRateHz = sampleRateHz,
            channelCount = channelCount,
            bypassed = isBypassed(),
            peakIn = peakIn,
            peakOut = peakOut,
            headroomCompensationDb = SpaciousnessProcessorModel.automaticHeadroomDb(activeMode, targetAmount),
            resetEvents = resetEvents,
            bitPerfectBypassEvents = bitPerfectBypassEvents,
        )
    }

    private fun syncConfig() {
        if (pendingConfig == activeConfig) return
        val previousMode = activeMode
        activeConfig = pendingConfig
        activeMode = activeConfig.mode
        targetAmount = activeConfig.amountNormalized
        smoothingAlpha = smoothingAlpha(
            sampleRateHz = sampleRateHz,
            smoothingTimeMs = activeConfig.smoothingTimeMs,
        )
        if (previousMode != activeMode) {
            currentAmount = 0f
            reset()
        }
    }

    private fun processStereoWidth(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val mid = (left + right) * 0.5f
        val side = (left - right) * 0.5f
        val lowSide = if (activeConfig.preserveBassMono) {
            sideLowPass.process(side, sampleRateHz, 140f)
        } else {
            0f
        }
        val highSide = side - lowSide
        val sideGain = 1f + (amount * 0.74f)
        val widenedSide = if (activeConfig.preserveBassMono) {
            lowSide * (1f + (amount * 0.06f)) + highSide * sideGain
        } else {
            side * sideGain
        }
        val centerGain = 1f - (amount * 0.02f)
        return StereoPair(
            left = (mid * centerGain) + widenedSide,
            right = (mid * centerGain) - widenedSide,
        )
    }

    private fun processCrossfeedDepth(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val crossDelay = delaySamples(0.18f + (0.42f * amount))
        val lowPassedRight = crossfeedLowPassL.process(readDelay(rightDelay, crossDelay), sampleRateHz, 920f)
        val lowPassedLeft = crossfeedLowPassR.process(readDelay(leftDelay, crossDelay), sampleRateHz, 920f)
        val crossGain = 0.03f + (amount * 0.11f)
        val directTrim = 1f - (amount * 0.055f)
        return StereoPair(
            left = (left * directTrim) + (lowPassedRight * crossGain),
            right = (right * directTrim) + (lowPassedLeft * crossGain),
        )
    }

    private fun processEarlyReflectionRoom(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val tap1 = delaySamples(2.6f + (0.8f * amount))
        val tap2 = delaySamples(5.3f + (1.2f * amount))
        val tap3 = delaySamples(8.4f + (1.4f * amount))
        val reflectionLeft = reflectionLowPassL.process(
            (readDelay(leftDelay, tap1) * 0.54f) +
                (readDelay(rightDelay, tap2) * 0.33f) +
                (readDelay(leftDelay, tap3) * 0.18f),
            sampleRateHz,
            4_800f,
        )
        val reflectionRight = reflectionLowPassR.process(
            (readDelay(rightDelay, tap1 + 1) * 0.54f) +
                (readDelay(leftDelay, tap2 + 2) * 0.33f) +
                (readDelay(rightDelay, tap3 + 1) * 0.18f),
            sampleRateHz,
            4_800f,
        )
        val wetGain = 0.06f + (amount * 0.14f)
        val dryGain = 1f - (amount * 0.045f)
        return StereoPair(
            left = (left * dryGain) + (reflectionLeft * wetGain),
            right = (right * dryGain) + (reflectionRight * wetGain),
        )
    }

    private fun processHaasSpace(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val delayLeft = delaySamples(5.5f + (3.5f * amount))
        val delayRight = delaySamples(7.2f + (4.2f * amount))
        val delayedRightHigh = haasHighPassL.process(readDelay(rightDelay, delayRight), sampleRateHz, 240f)
        val delayedLeftHigh = haasHighPassR.process(readDelay(leftDelay, delayLeft), sampleRateHz, 240f)
        val wetGain = 0.045f + (amount * 0.09f)
        return StereoPair(
            left = left + (delayedRightHigh * wetGain),
            right = right + (delayedLeftHigh * wetGain),
        )
    }

    private fun processHarmonicAir(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val highLeft = airHighPassL.process(left, sampleRateHz, 2_600f)
        val highRight = airHighPassR.process(right, sampleRateHz, 2_600f)
        val sideAir = (highLeft - highRight) * 0.5f
        val delayLeft = delaySamples(2.4f + (1.2f * amount))
        val delayRight = delaySamples(3.2f + (1.4f * amount))
        val wetLeft = airWetLowPassL.process(readDelay(rightDelay, delayRight), sampleRateHz, 9_500f)
        val wetRight = airWetLowPassR.process(readDelay(leftDelay, delayLeft), sampleRateHz, 9_500f)
        val sideGain = 0.06f + (amount * 0.11f)
        val decorrelatedGain = 0.028f + (amount * 0.068f)
        return StereoPair(
            left = left + (sideAir * sideGain) + (wetLeft * decorrelatedGain),
            right = right - (sideAir * sideGain) + (wetRight * decorrelatedGain),
        )
    }

    private fun delaySamples(delayMs: Float): Int {
        return ((sampleRateHz / 1_000f) * delayMs).toInt().coerceIn(1, (leftDelay.size - 1).coerceAtLeast(1))
    }

    private fun readDelay(
        buffer: FloatArray,
        delaySamples: Int,
    ): Float {
        if (buffer.isEmpty()) return 0f
        val index = (delayWriteIndex - delaySamples).floorMod(buffer.size)
        return buffer[index]
    }

    private fun writeDelay(
        left: Float,
        right: Float,
    ) {
        if (leftDelay.isEmpty() || rightDelay.isEmpty()) return
        leftDelay[delayWriteIndex] = left
        rightDelay[delayWriteIndex] = right
        delayWriteIndex = (delayWriteIndex + 1) % leftDelay.size
    }

    private fun smooth(
        current: Float,
        target: Float,
        alpha: Float,
    ): Float {
        if (abs(current - target) <= 0.0001f) return target
        return current + ((target - current) * alpha.coerceIn(0f, 1f))
    }

    private fun dbToLinear(db: Float): Float {
        return 10f.pow(db / 20f)
    }

    private fun sanitizeSample(sample: Float): Float {
        return if (sample.isFinite()) {
            sample.coerceIn(-1.8f, 1.8f)
        } else {
            0f
        }
    }

    private fun smoothingAlpha(
        sampleRateHz: Int,
        smoothingTimeMs: Int,
    ): Float {
        val safeSampleRate = sampleRateHz.coerceAtLeast(8_000).toDouble()
        val safeTimeSeconds = smoothingTimeMs.coerceAtLeast(20) / 1_000.0
        return (1.0 - exp(-1.0 / (safeSampleRate * safeTimeSeconds))).toFloat().coerceIn(0.002f, 0.25f)
    }

    private data class StereoPair(
        val left: Float,
        val right: Float,
    )

    private fun Int.floorMod(modulus: Int): Int {
        return ((this % modulus) + modulus) % modulus
    }

    private companion object {
        const val MAX_DELAY_MS = 20f
    }
}

private class OnePoleLowPassState {
    private var previous = 0f

    fun process(
        input: Float,
        sampleRateHz: Int,
        cutoffHz: Float,
    ): Float {
        val alpha = lowPassAlpha(sampleRateHz, cutoffHz)
        previous += alpha * (input - previous)
        return previous
    }

    fun reset() {
        previous = 0f
    }
}

private class OnePoleHighPassState {
    private var previousInput = 0f
    private var previousOutput = 0f

    fun process(
        input: Float,
        sampleRateHz: Int,
        cutoffHz: Float,
    ): Float {
        val alpha = highPassAlpha(sampleRateHz, cutoffHz)
        val output = alpha * (previousOutput + input - previousInput)
        previousInput = input
        previousOutput = output
        return output
    }

    fun reset() {
        previousInput = 0f
        previousOutput = 0f
    }
}

private fun lowPassAlpha(
    sampleRateHz: Int,
    cutoffHz: Float,
): Float {
    val dt = 1f / sampleRateHz.coerceAtLeast(8_000).toFloat()
    val rc = 1f / (2f * Math.PI.toFloat() * cutoffHz.coerceAtLeast(20f))
    return (dt / (rc + dt)).coerceIn(0.0001f, 0.99f)
}

private fun highPassAlpha(
    sampleRateHz: Int,
    cutoffHz: Float,
): Float {
    val dt = 1f / sampleRateHz.coerceAtLeast(8_000).toFloat()
    val rc = 1f / (2f * Math.PI.toFloat() * cutoffHz.coerceAtLeast(20f))
    return (rc / (rc + dt)).coerceIn(0.0001f, 0.999f)
}
