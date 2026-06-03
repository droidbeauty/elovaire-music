package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

private const val MAX_REVERB_DURATION_MS = 500
private const val REVERB_STEP_MS = 50

internal data class ReverbConfig(
    val enabled: Boolean = false,
    val profile: ReverbProfile = ReverbProfile.Dry,
    val decayMs: Int = 0,
    val smoothingTimeMs: Int = 72,
) {
    fun sanitized(): ReverbConfig {
        return copy(
            decayMs = normalizeReverbDurationMs(decayMs),
            smoothingTimeMs = smoothingTimeMs.coerceIn(24, 140),
        )
    }
}

internal object ReverbProcessorModel {
    private const val FLAT_EPSILON = 0.0005f

    fun isBypassed(config: ReverbConfig): Boolean {
        val safeConfig = config.sanitized()
        return !safeConfig.enabled || safeConfig.decayMs <= 0
    }

    fun normalizedAmount(decayMs: Int): Float {
        val safeDecayMs = normalizeReverbDurationMs(decayMs)
        if (safeDecayMs <= 0) return 0f
        return (safeDecayMs / MAX_REVERB_DURATION_MS.toFloat()).toDouble().pow(0.96).toFloat().coerceIn(0f, 1f)
    }

    fun automaticHeadroomDb(
        profile: ReverbProfile,
        decayMs: Int,
    ): Float {
        val amount = normalizedAmount(decayMs)
        if (amount <= FLAT_EPSILON) return 0f
        val compensationDb = when (profile) {
            ReverbProfile.Dry -> 0.42f + (amount * 1.38f)
            ReverbProfile.Wet -> 0.82f + (amount * 2.42f)
        }
        return -compensationDb.coerceAtMost(3.45f)
    }

    fun wetMix(
        profile: ReverbProfile,
        decayMs: Int,
    ): Float {
        val amount = normalizedAmount(decayMs)
        if (amount <= FLAT_EPSILON) return 0f
        return when (profile) {
            ReverbProfile.Dry -> 0.12f + (amount * 0.28f)
            ReverbProfile.Wet -> 0.20f + (amount * 0.42f)
        }.coerceIn(0f, 0.64f)
    }

    fun feedback(
        profile: ReverbProfile,
        decayMs: Int,
    ): Float {
        val amount = normalizedAmount(decayMs)
        return when (profile) {
            ReverbProfile.Dry -> 0.32f + (amount * 0.30f)
            ReverbProfile.Wet -> 0.44f + (amount * 0.32f)
        }.coerceIn(0.2f, 0.79f)
    }

    fun crossMix(
        profile: ReverbProfile,
        decayMs: Int,
    ): Float {
        val amount = normalizedAmount(decayMs)
        return when (profile) {
            ReverbProfile.Dry -> 0.08f + (amount * 0.16f)
            ReverbProfile.Wet -> 0.16f + (amount * 0.24f)
        }.coerceIn(0f, 0.36f)
    }

    fun dampingFrequencyHz(
        profile: ReverbProfile,
        decayMs: Int,
    ): Float {
        val amount = normalizedAmount(decayMs)
        return when (profile) {
            ReverbProfile.Dry -> 6_400f - (amount * 1_550f)
            ReverbProfile.Wet -> 5_200f - (amount * 2_250f)
        }.coerceIn(2_600f, 8_400f)
    }

    fun tapDurationsMs(decayMs: Int): ReverbTapDurations {
        val safeDecay = normalizeReverbDurationMs(decayMs).coerceAtLeast(REVERB_STEP_MS)
        return ReverbTapDurations(
            primaryMs = (24f + (safeDecay * 0.18f)).toInt().coerceAtLeast(16),
            secondaryMs = (42f + (safeDecay * 0.37f)).toInt().coerceAtLeast(28),
            diffuseMs = (78f + (safeDecay * 0.66f)).toInt().coerceAtLeast(48),
            crossMs = (32f + (safeDecay * 0.24f)).toInt().coerceAtLeast(22),
        )
    }
}

internal class ReverbProcessor {
    @Volatile
    private var pendingConfig = ReverbConfig()

    @Volatile
    private var activeConfig = pendingConfig.sanitized()
    private var sampleRateHz = 48_000
    private var channelCount = 2
    private var currentWetMix = 0f
    private var targetWetMix = 0f
    private var currentFeedback = 0f
    private var targetFeedback = 0f
    private var currentCrossMix = 0f
    private var targetCrossMix = 0f
    private var currentDampingFrequencyHz = 6_400f
    private var targetDampingFrequencyHz = 6_400f
    private var smoothingAlpha = 1f
    private var leftDelay = FloatArray(1)
    private var rightDelay = FloatArray(1)
    private var monoDelay = FloatArray(1)
    private var writeIndex = 0
    private val leftDamping = ReverbLowPassState()
    private val rightDamping = ReverbLowPassState()
    private val monoDamping = ReverbLowPassState()

    fun setConfig(config: ReverbConfig) {
        pendingConfig = config.sanitized()
        if (ReverbProcessorModel.isBypassed(pendingConfig)) {
            activeConfig = pendingConfig
            targetWetMix = 0f
            currentWetMix = 0f
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
        val maxDelaySamples = ((this.sampleRateHz / 1_000f) * (MAX_REVERB_DURATION_MS + 96)).toInt().coerceAtLeast(128)
        leftDelay = FloatArray(maxDelaySamples)
        rightDelay = FloatArray(maxDelaySamples)
        monoDelay = FloatArray(maxDelaySamples)
        reset()
    }

    fun reset() {
        leftDelay.fill(0f)
        rightDelay.fill(0f)
        monoDelay.fill(0f)
        writeIndex = 0
        leftDamping.reset()
        rightDamping.reset()
        monoDamping.reset()
    }

    fun processFrame(
        frame: FloatArray,
        channels: Int,
    ) {
        syncConfig()
        if (frame.isEmpty() || channels <= 0 || ReverbProcessorModel.isBypassed(activeConfig)) return
        currentWetMix = smooth(currentWetMix, targetWetMix, smoothingAlpha)
        currentFeedback = smooth(currentFeedback, targetFeedback, smoothingAlpha)
        currentCrossMix = smooth(currentCrossMix, targetCrossMix, smoothingAlpha)
        currentDampingFrequencyHz = smooth(currentDampingFrequencyHz, targetDampingFrequencyHz, smoothingAlpha)
        if (currentWetMix <= 0.0005f) return

        val taps = ReverbProcessorModel.tapDurationsMs(activeConfig.decayMs)
        if (channels == 1 || frame.size == 1) {
            val dry = sanitize(frame[0])
            val primary = readDelay(monoDelay, taps.primaryMs)
            val secondary = readDelay(monoDelay, taps.secondaryMs)
            val diffuse = readDelay(monoDelay, taps.diffuseMs)
            val wet = shapeWetSignal(monoDamping.process(
                (primary * 0.34f) + (secondary * 0.28f) + (diffuse * 0.22f),
                sampleRateHz,
                currentDampingFrequencyHz,
            ))
            monoDelay[writeIndex] = sanitize(dry + (wet * currentFeedback * 0.72f))
            frame[0] = sanitize(dry + (wet * currentWetMix))
            advanceWriteIndex()
            return
        }

        val dryLeft = sanitize(frame[0])
        val dryRight = sanitize(frame[1])
        val leftPrimary = readDelay(leftDelay, taps.primaryMs)
        val leftSecondary = readDelay(leftDelay, taps.secondaryMs)
        val leftDiffuse = readDelay(leftDelay, taps.diffuseMs)
        val rightPrimary = readDelay(rightDelay, taps.primaryMs)
        val rightSecondary = readDelay(rightDelay, taps.secondaryMs)
        val rightDiffuse = readDelay(rightDelay, taps.diffuseMs)
        val leftCross = readDelay(rightDelay, taps.crossMs)
        val rightCross = readDelay(leftDelay, taps.crossMs)

        val wetLeft = shapeWetSignal(leftDamping.process(
            (leftPrimary * 0.34f) +
                (leftSecondary * 0.24f) +
                (leftDiffuse * 0.20f) +
                (leftCross * (0.06f + (currentCrossMix * 0.72f))),
            sampleRateHz,
            currentDampingFrequencyHz,
        ))
        val wetRight = shapeWetSignal(rightDamping.process(
            (rightPrimary * 0.34f) +
                (rightSecondary * 0.24f) +
                (rightDiffuse * 0.20f) +
                (rightCross * (0.06f + (currentCrossMix * 0.72f))),
            sampleRateHz,
            currentDampingFrequencyHz,
        ))

        leftDelay[writeIndex] = sanitize(dryLeft + (wetLeft * currentFeedback * 0.76f) + (wetRight * currentCrossMix * 0.62f))
        rightDelay[writeIndex] = sanitize(dryRight + (wetRight * currentFeedback * 0.76f) + (wetLeft * currentCrossMix * 0.62f))
        frame[0] = sanitize(dryLeft + (wetLeft * currentWetMix))
        frame[1] = sanitize(dryRight + (wetRight * currentWetMix))
        advanceWriteIndex()
    }

    private fun syncConfig() {
        if (pendingConfig == activeConfig) return
        activeConfig = pendingConfig
        smoothingAlpha = smoothingAlpha(
            sampleRateHz = sampleRateHz,
            smoothingTimeMs = activeConfig.smoothingTimeMs,
        )
        targetWetMix = ReverbProcessorModel.wetMix(activeConfig.profile, activeConfig.decayMs)
        targetFeedback = ReverbProcessorModel.feedback(activeConfig.profile, activeConfig.decayMs)
        targetCrossMix = ReverbProcessorModel.crossMix(activeConfig.profile, activeConfig.decayMs)
        targetDampingFrequencyHz = ReverbProcessorModel.dampingFrequencyHz(activeConfig.profile, activeConfig.decayMs)
        if (ReverbProcessorModel.isBypassed(activeConfig)) {
            currentWetMix = 0f
            reset()
        }
    }

    private fun readDelay(
        buffer: FloatArray,
        delayMs: Int,
    ): Float {
        val delaySamples = ((sampleRateHz / 1_000f) * delayMs).toInt().coerceIn(1, buffer.lastIndex.coerceAtLeast(1))
        val readIndex = (writeIndex - delaySamples).mod(buffer.size)
        return buffer[readIndex]
    }

    private fun advanceWriteIndex() {
        writeIndex += 1
        if (writeIndex >= leftDelay.size) {
            writeIndex = 0
        }
    }

    private fun shapeWetSignal(value: Float): Float {
        val safeValue = if (value.isFinite()) value else 0f
        return sanitize(safeValue / (1f + (abs(safeValue) * 0.85f)))
    }

    private fun smooth(
        current: Float,
        target: Float,
        alpha: Float,
    ): Float {
        if (abs(current - target) <= 0.0001f) return target
        return current + ((target - current) * alpha.coerceIn(0f, 1f))
    }

    private fun sanitize(sample: Float): Float {
        return if (sample.isFinite()) sample.coerceIn(-1f, 1f) else 0f
    }

    private companion object {
    }
}

internal data class ReverbTapDurations(
    val primaryMs: Int,
    val secondaryMs: Int,
    val diffuseMs: Int,
    val crossMs: Int,
)

internal fun normalizeReverbDurationMs(valueMs: Int): Int {
    return ((valueMs.coerceIn(0, MAX_REVERB_DURATION_MS) + (REVERB_STEP_MS / 2)) / REVERB_STEP_MS) * REVERB_STEP_MS
}

private fun smoothingAlpha(
    sampleRateHz: Int,
    smoothingTimeMs: Int,
): Float {
    val safeSampleRate = sampleRateHz.coerceAtLeast(8_000).toDouble()
    val safeTimeSeconds = smoothingTimeMs.coerceAtLeast(20) / 1_000.0
    return (1.0 - exp(-1.0 / (safeSampleRate * safeTimeSeconds))).toFloat().coerceIn(0.01f, 1f)
}

private class ReverbLowPassState {
    private var output = 0f

    fun reset() {
        output = 0f
    }

    fun process(
        input: Float,
        sampleRateHz: Int,
        cutoffFrequencyHz: Float,
    ): Float {
        val safeSampleRate = sampleRateHz.coerceAtLeast(8_000)
        val safeCutoff = cutoffFrequencyHz.coerceIn(400f, safeSampleRate / 2f * 0.8f)
        val alpha = (1f - exp((-2f * Math.PI.toFloat() * safeCutoff) / safeSampleRate.toFloat()))
            .coerceIn(0.01f, 1f)
        output += (input - output) * alpha
        return output
    }
}
