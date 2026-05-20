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
        return amountNormalized.coerceIn(0f, 1f).toDouble().pow(1.18).toFloat()
    }

    fun automaticHeadroomDb(
        mode: SpaciousnessMode,
        amountNormalized: Float,
    ): Float {
        val amount = mappedAmount(amountNormalized)
        if (amount <= 0f || mode == SpaciousnessMode.Off) return 0f
        val compensationDb = when (mode) {
            SpaciousnessMode.Off -> 0f
            SpaciousnessMode.StereoWidth -> 0.34f + (amount * 0.62f)
            SpaciousnessMode.CrossfeedDepth -> 0.12f + (amount * 0.26f)
            SpaciousnessMode.EarlyReflectionRoom -> 0.18f + (amount * 0.42f)
            SpaciousnessMode.HaasSpace -> 0.24f + (amount * 0.5f)
            SpaciousnessMode.HarmonicAir -> 0.16f + (amount * 0.3f)
        }
        return -compensationDb.coerceAtMost(1.35f)
    }
}

internal class SpaciousnessProcessor {
    @Volatile
    private var pendingConfig = SpaciousnessConfig()

    @Volatile
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
    // Short all-pass decorrelators keep width cues airy without punching a hole in the center image.
    private val widthDecorrelator = CascadedAllPassState(
        firstCoefficient = 0.57f,
        secondCoefficient = 0.33f,
    )
    private val reflectionDecorrelatorL = CascadedAllPassState(
        firstCoefficient = 0.51f,
        secondCoefficient = 0.29f,
    )
    private val reflectionDecorrelatorR = CascadedAllPassState(
        firstCoefficient = -0.47f,
        secondCoefficient = 0.31f,
    )
    private val haasDecorrelator = CascadedAllPassState(
        firstCoefficient = 0.49f,
        secondCoefficient = -0.27f,
    )
    private val airDecorrelatorL = CascadedAllPassState(
        firstCoefficient = 0.43f,
        secondCoefficient = 0.25f,
    )
    private val airDecorrelatorR = CascadedAllPassState(
        firstCoefficient = -0.41f,
        secondCoefficient = 0.23f,
    )

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

    fun isBypassed(): Boolean {
        return channelCount != 2 ||
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
        widthDecorrelator.reset()
        reflectionDecorrelatorL.reset()
        reflectionDecorrelatorR.reset()
        haasDecorrelator.reset()
        airDecorrelatorL.reset()
        airDecorrelatorR.reset()
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
            sideLowPass.process(side, sampleRateHz, 150f)
        } else {
            0f
        }
        val highSide = side - lowSide
        val decorrelatedHighSide = widthDecorrelator.process(highSide)
        val sideGain = 1f + (amount * 0.78f)
        val decorrelatedGain = 0.02f + (amount * 0.11f)
        val widenedSide = if (activeConfig.preserveBassMono) {
            lowSide * (1f + (amount * 0.015f)) +
                highSide * sideGain +
                decorrelatedHighSide * decorrelatedGain
        } else {
            side * sideGain + (decorrelatedHighSide * decorrelatedGain)
        }
        val centerGain = 1f - (amount * 0.018f)
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
        val crossDelayMs = 0.18f + (0.42f * amount)
        val lowPassedRight = crossfeedLowPassL.process(readDelayInterpolated(rightDelay, crossDelayMs), sampleRateHz, 1_050f)
        val lowPassedLeft = crossfeedLowPassR.process(readDelayInterpolated(leftDelay, crossDelayMs), sampleRateHz, 1_050f)
        val mid = (left + right) * 0.5f
        val side = (left - right) * 0.5f
        val sideBlend = 1f + (amount * 0.12f)
        val crossGain = 0.022f + (amount * 0.055f)
        val presenceLift = 1f + (amount * 0.02f)
        return StereoPair(
            left = (mid * presenceLift) + (side * sideBlend) + (lowPassedRight * crossGain),
            right = (mid * presenceLift) - (side * sideBlend) + (lowPassedLeft * crossGain),
        )
    }

    private fun processEarlyReflectionRoom(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val tap1Ms = 2.2f + (0.8f * amount)
        val tap2Ms = 4.9f + (1.3f * amount)
        val tap3Ms = 7.6f + (1.8f * amount)
        val rawReflectionLeft = reflectionLowPassL.process(
            (readDelayInterpolated(leftDelay, tap1Ms) * 0.46f) +
                (readDelayInterpolated(rightDelay, tap2Ms + 0.4f) * 0.31f) +
                (readDelayInterpolated(leftDelay, tap3Ms + 0.8f) * 0.16f),
            sampleRateHz,
            5_600f,
        )
        val rawReflectionRight = reflectionLowPassR.process(
            (readDelayInterpolated(rightDelay, tap1Ms + 0.25f) * 0.46f) +
                (readDelayInterpolated(leftDelay, tap2Ms + 0.7f) * 0.31f) +
                (readDelayInterpolated(rightDelay, tap3Ms + 1.1f) * 0.16f),
            sampleRateHz,
            5_600f,
        )
        val reflectionLeft = reflectionDecorrelatorL.process(rawReflectionLeft)
        val reflectionRight = reflectionDecorrelatorR.process(rawReflectionRight)
        val wetGain = 0.028f + (amount * 0.078f)
        val dryGain = 1f - (amount * 0.012f)
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
        val delayLeftMs = 4.8f + (2.1f * amount)
        val delayRightMs = 6.1f + (2.5f * amount)
        val mid = (left + right) * 0.5f
        val side = (left - right) * 0.5f
        val delayedLeftHigh = haasHighPassL.process(readDelayInterpolated(leftDelay, delayLeftMs), sampleRateHz, 650f)
        val delayedRightHigh = haasHighPassR.process(readDelayInterpolated(rightDelay, delayRightMs), sampleRateHz, 650f)
        val delayedSide = haasDecorrelator.process((delayedLeftHigh - delayedRightHigh) * 0.5f)
        val wetGain = 0.045f + (amount * 0.12f)
        val sideGain = 1f + (amount * 0.12f)
        return StereoPair(
            left = mid + (side * sideGain) + (delayedSide * wetGain),
            right = mid - (side * sideGain) - (delayedSide * wetGain),
        )
    }

    private fun processHarmonicAir(
        left: Float,
        right: Float,
        amount: Float,
    ): StereoPair {
        val mid = (left + right) * 0.5f
        val side = (left - right) * 0.5f
        val highLeft = airHighPassL.process(left, sampleRateHz, 3_200f)
        val highRight = airHighPassR.process(right, sampleRateHz, 3_200f)
        val sideAir = (highLeft - highRight) * 0.5f
        val delayLeftMs = 2.1f + (0.9f * amount)
        val delayRightMs = 2.8f + (1.1f * amount)
        val wetLeft = airWetLowPassL.process(readDelayInterpolated(leftDelay, delayLeftMs), sampleRateHz, 11_500f)
        val wetRight = airWetLowPassR.process(readDelayInterpolated(rightDelay, delayRightMs), sampleRateHz, 11_500f)
        val decorrelatedSide = (wetLeft - wetRight) * 0.5f
        val shapedLeft = airDecorrelatorL.process(wetLeft)
        val shapedRight = airDecorrelatorR.process(wetRight)
        val decorrelatedAir = (shapedLeft - shapedRight) * 0.5f
        val sideGain = 0.06f + (amount * 0.14f)
        val decorrelatedGain = 0.035f + (amount * 0.075f)
        return StereoPair(
            left = mid + side + (sideAir * sideGain) + ((decorrelatedSide + decorrelatedAir) * decorrelatedGain),
            right = mid - side - (sideAir * sideGain) - ((decorrelatedSide + decorrelatedAir) * decorrelatedGain),
        )
    }

    private fun delaySamples(delayMs: Float): Int {
        return ((sampleRateHz / 1_000f) * delayMs).toInt().coerceIn(1, (leftDelay.size - 1).coerceAtLeast(1))
    }

    private fun readDelayInterpolated(
        buffer: FloatArray,
        delayMs: Float,
    ): Float {
        if (buffer.isEmpty()) return 0f
        val delaySamples = ((sampleRateHz / 1_000f) * delayMs).coerceIn(
            1f,
            (buffer.size - 1).coerceAtLeast(1).toFloat(),
        )
        val integerDelay = delaySamples.toInt()
        val fractionalDelay = delaySamples - integerDelay
        val newerIndex = (delayWriteIndex - integerDelay).floorMod(buffer.size)
        val olderIndex = (newerIndex - 1).floorMod(buffer.size)
        val newerSample = buffer[newerIndex]
        val olderSample = buffer[olderIndex]
        return newerSample + ((olderSample - newerSample) * fractionalDelay)
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

private class CascadedAllPassState(
    private val firstCoefficient: Float,
    private val secondCoefficient: Float,
) {
    private val firstStage = FirstOrderAllPassState(firstCoefficient)
    private val secondStage = FirstOrderAllPassState(secondCoefficient)

    fun process(input: Float): Float {
        return secondStage.process(firstStage.process(input))
    }

    fun reset() {
        firstStage.reset()
        secondStage.reset()
    }
}

private class FirstOrderAllPassState(
    private val coefficient: Float,
) {
    private var previousInput = 0f
    private var previousOutput = 0f

    fun process(input: Float): Float {
        val safeCoefficient = coefficient.coerceIn(-0.82f, 0.82f)
        val output = (-safeCoefficient * input) + previousInput + (safeCoefficient * previousOutput)
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
