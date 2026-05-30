package elovaire.music.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import elovaire.music.app.domain.model.EqSettings

internal data class EqualizerPreset(
    val name: String,
    val bandGainsDb: List<Float>,
    val preampDb: Float = 0f,
) {
    fun sanitized(config: EqualizerDspConfig = EqualizerDspConfig()): EqualizerPreset {
        val safeBands = List(config.bandCenterFrequenciesHz.size) { index ->
            bandGainsDb.getOrElse(index) { 0f }.coerceIn(config.minBandGainDb, config.maxBandGainDb)
        }
        return copy(
            bandGainsDb = safeBands,
            preampDb = preampDb.coerceIn(config.minPreampDb, config.maxPreampDb),
        )
    }
}

internal data class EqualizerDspConfig(
    val bandCenterFrequenciesHz: FloatArray = EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ,
    val minBandGainDb: Float = -10f,
    val maxBandGainDb: Float = 8f,
    val minPreampDb: Float = -12f,
    val maxPreampDb: Float = 6f,
    val smoothingTimeMs: Int = 42,
    val bassConfig: HighQualityBassConfig = HighQualityBassConfig(),
    val trebleShelfFrequencyHz: Float = 7_600f,
    val trebleShelfSlope: Float = 0.72f,
    val trebleMaxBoostDb: Float = 4.4f,
    val spaciousnessMidCutDb: Float = 1.25f,
    val spaciousnessPresenceBoostDb: Float = 1.1f,
    val spaciousnessAirBoostDb: Float = 0.75f,
    val spaciousnessMidCutFrequencyHz: Float = 290f,
    val spaciousnessPresenceFrequencyHz: Float = 2_350f,
    val spaciousnessAirFrequencyHz: Float = 8_500f,
    val headroomSafetyMarginDb: Float = 0.5f,
    val headroomBlend: Float = 0.76f,
    val limiterThreshold: Float = 0.891f,
    val limiterKnee: Float = 0.035f,
    val updateStrideFrames: Int = 16,
) {
    fun sanitized(): EqualizerDspConfig {
        return copy(
            minBandGainDb = minBandGainDb.coerceIn(-18f, -3f),
            maxBandGainDb = maxBandGainDb.coerceIn(3f, 15f),
            minPreampDb = minPreampDb.coerceIn(-24f, 0f),
            maxPreampDb = maxPreampDb.coerceIn(0f, 12f),
            smoothingTimeMs = smoothingTimeMs.coerceIn(20, 140),
            trebleShelfFrequencyHz = trebleShelfFrequencyHz.coerceIn(3_500f, 14_000f),
            trebleShelfSlope = trebleShelfSlope.coerceIn(0.35f, 1.4f),
            trebleMaxBoostDb = trebleMaxBoostDb.coerceIn(0f, 8f),
            spaciousnessMidCutDb = spaciousnessMidCutDb.coerceIn(0f, 4f),
            spaciousnessPresenceBoostDb = spaciousnessPresenceBoostDb.coerceIn(0f, 4f),
            spaciousnessAirBoostDb = spaciousnessAirBoostDb.coerceIn(0f, 3f),
            spaciousnessMidCutFrequencyHz = spaciousnessMidCutFrequencyHz.coerceIn(180f, 600f),
            spaciousnessPresenceFrequencyHz = spaciousnessPresenceFrequencyHz.coerceIn(1_200f, 4_200f),
            spaciousnessAirFrequencyHz = spaciousnessAirFrequencyHz.coerceIn(4_500f, 12_000f),
            headroomSafetyMarginDb = headroomSafetyMarginDb.coerceIn(0.2f, 2.5f),
            headroomBlend = headroomBlend.coerceIn(0.4f, 1.1f),
            limiterThreshold = limiterThreshold.coerceIn(0.82f, 0.98f),
            limiterKnee = limiterKnee.coerceIn(0.008f, 0.08f),
            updateStrideFrames = updateStrideFrames.coerceIn(8, 96),
        )
    }
}

internal data class EqBandDefinition(
    val frequencyHz: Float,
    val q: Float,
)

internal object EqualizerDspModel {
    private val BAND_FREQUENCIES_HZ = listOf(
        30f,
        60f,
        120f,
        250f,
        350f,
        500f,
        750f,
        1_000f,
        1_500f,
        2_000f,
        3_000f,
        4_000f,
        5_000f,
        6_000f,
        8_000f,
        10_000f,
        12_000f,
        16_000f,
    )
    val BAND_DEFINITIONS: List<EqBandDefinition> = BAND_FREQUENCIES_HZ.mapIndexed { index, frequencyHz ->
        EqBandDefinition(
            frequencyHz = frequencyHz,
            q = deriveBandQ(index, BAND_FREQUENCIES_HZ),
        )
    }
    const val BAND_COUNT = 18
    val BAND_CENTER_FREQUENCIES_HZ: FloatArray = BAND_DEFINITIONS.map { it.frequencyHz }.toFloatArray()
    private const val FLAT_EPSILON = 0.0005f

    fun bandDefinition(index: Int): EqBandDefinition =
        BAND_DEFINITIONS.getOrElse(index.coerceIn(0, BAND_DEFINITIONS.lastIndex)) { BAND_DEFINITIONS.last() }

    fun normalizedBandToDb(
        normalized: Float,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val clamped = normalized.coerceIn(-1f, 1f)
        val curvedMagnitude = abs(clamped).toDouble().pow(0.86).toFloat()
        return if (clamped >= 0f) {
            curvedMagnitude * safeConfig.maxBandGainDb
        } else {
            curvedMagnitude * safeConfig.minBandGainDb
        }
    }

    fun bandGraphFraction(
        normalized: Float,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val dbValue = normalizedBandToDb(normalized, safeConfig)
        return ((dbValue - safeConfig.minBandGainDb) / (safeConfig.maxBandGainDb - safeConfig.minBandGainDb))
            .coerceIn(0f, 1f)
    }

    fun graphFractionToNormalized(
        fraction: Float,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val clampedFraction = fraction.coerceIn(0f, 1f)
        val dbValue = safeConfig.minBandGainDb +
            ((safeConfig.maxBandGainDb - safeConfig.minBandGainDb) * clampedFraction)
        val magnitude = if (dbValue >= 0f) {
            (dbValue / safeConfig.maxBandGainDb).coerceIn(0f, 1f)
        } else {
            (dbValue / safeConfig.minBandGainDb).coerceIn(0f, 1f)
        }
        val normalizedMagnitude = magnitude.toDouble().pow(1.0 / 0.86).toFloat().coerceIn(0f, 1f)
        return if (dbValue >= 0f) normalizedMagnitude else -normalizedMagnitude
    }

    fun activeBandFrequencies(sampleRateHz: Int): FloatArray {
        val nyquistSafeLimit = sampleRateHz.coerceAtLeast(8_000) / 2f * 0.9f
        return BAND_DEFINITIONS.map { definition ->
            if (definition.frequencyHz < nyquistSafeLimit) definition.frequencyHz else -1f
        }.toFloatArray()
    }

    fun lowBandBoostSafetyDb(
        bandGainsDb: FloatArray,
    ): Float {
        var weightedBoost = 0f
        BAND_DEFINITIONS.forEachIndexed { index, definition ->
            val gain = bandGainsDb.getOrElse(index) { 0f }.coerceAtLeast(0f)
            val weight = when {
                definition.frequencyHz <= 120f -> 1f
                definition.frequencyHz <= 250f -> 0.55f
                definition.frequencyHz <= 350f -> 0.24f
                else -> 0f
            }
            weightedBoost += gain * weight
        }
        return weightedBoost.coerceIn(0f, 10f)
    }

    fun isFlat(settings: EqSettings): Boolean {
        return settings.bands.none { abs(it) > FLAT_EPSILON } &&
            abs(settings.treble) <= FLAT_EPSILON &&
            settings.bass.coerceAtLeast(0f) <= FLAT_EPSILON &&
            settings.spaciousness.coerceAtLeast(0f) <= FLAT_EPSILON &&
            settings.reverbDurationMs <= 0
    }

    fun automaticHeadroomDb(
        bandGainsDb: FloatArray,
        bassShelfDb: Float,
        bassAccentDb: Float,
        bassTrimDb: Float,
        bassPregainDb: Float,
        trebleBoostDb: Float,
        spaciousnessAmount: Float,
        sampleRateHz: Int,
        reverbHeadroomDb: Float = 0f,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val peakResponseDb = estimatePeakResponseDb(
            bandGainsDb = bandGainsDb,
            bassShelfDb = bassShelfDb,
            bassAccentDb = bassAccentDb,
            bassTrimDb = bassTrimDb,
            trebleBoostDb = trebleBoostDb,
            spaciousnessAmount = spaciousnessAmount,
            sampleRateHz = sampleRateHz,
            config = safeConfig,
        )
        val combinedPositiveBoostDb = (peakResponseDb + bassPregainDb).coerceAtLeast(0f)
        val responseHeadroomDb = if (combinedPositiveBoostDb <= 0f) {
            0f
        } else {
            -((combinedPositiveBoostDb * safeConfig.headroomBlend) + safeConfig.headroomSafetyMarginDb)
                .coerceIn(0f, abs(safeConfig.minPreampDb))
        }
        return min(responseHeadroomDb, reverbHeadroomDb)
    }

    fun smoothingAlpha(
        sampleRateHz: Int,
        smoothingTimeMs: Int,
        strideFrames: Int,
    ): Float {
        val safeSampleRate = sampleRateHz.coerceAtLeast(8_000).toDouble()
        val safeTimeSeconds = smoothingTimeMs.coerceAtLeast(20) / 1_000.0
        val stride = strideFrames.coerceAtLeast(1).toDouble()
        return (1.0 - exp(-stride / (safeSampleRate * safeTimeSeconds))).toFloat().coerceIn(0.01f, 1f)
    }

    private fun deriveBandQ(
        index: Int,
        frequenciesHz: List<Float>,
    ): Float {
        val center = frequenciesHz[index].toDouble()
        val lowerBoundary = if (index == 0) {
            val nextRatio = frequenciesHz[index + 1] / frequenciesHz[index]
            center / sqrt(nextRatio.toDouble())
        } else {
            sqrt((frequenciesHz[index - 1] * frequenciesHz[index]).toDouble())
        }
        val upperBoundary = if (index == frequenciesHz.lastIndex) {
            val previousRatio = frequenciesHz[index] / frequenciesHz[index - 1]
            center * sqrt(previousRatio.toDouble())
        } else {
            sqrt((frequenciesHz[index] * frequenciesHz[index + 1]).toDouble())
        }
        val bandwidth = (upperBoundary - lowerBoundary).coerceAtLeast(center * 0.16)
        return (center / bandwidth).toFloat().coerceIn(0.9f, 4.2f)
    }

    private fun estimatePeakResponseDb(
        bandGainsDb: FloatArray,
        bassShelfDb: Float,
        bassAccentDb: Float,
        bassTrimDb: Float,
        trebleBoostDb: Float,
        spaciousnessAmount: Float,
        sampleRateHz: Int,
        config: EqualizerDspConfig,
    ): Float {
        val safeSampleRate = sampleRateHz.coerceAtLeast(8_000)
        val activeFrequencies = activeBandFrequencies(safeSampleRate)
        val bandCoefficients = bandGainsDb.mapIndexed { index, gainDb ->
            val frequencyHz = activeFrequencies.getOrElse(index) { -1f }
            if (frequencyHz > 0f && abs(gainDb) > 0.01f) {
                BiquadCoefficients.peaking(
                    sampleRateHz = safeSampleRate.toFloat(),
                    centerFrequencyHz = frequencyHz,
                    q = bandDefinition(index).q,
                    gainDb = gainDb,
                )
            } else {
                BiquadCoefficients.identity()
            }
        }
        val bassCoefficient = BiquadCoefficients.lowShelf(
            sampleRateHz = safeSampleRate.toFloat(),
            cornerFrequencyHz = config.bassConfig.shelfFrequencyHz,
            slope = config.bassConfig.shelfSlope,
            gainDb = bassShelfDb,
        )
        val bassAccentCoefficient = BiquadCoefficients.peaking(
            sampleRateHz = safeSampleRate.toFloat(),
            centerFrequencyHz = config.bassConfig.punchCenterHz,
            q = config.bassConfig.punchQ,
            gainDb = bassAccentDb,
        )
        val bassTrimCoefficient = BiquadCoefficients.peaking(
            sampleRateHz = safeSampleRate.toFloat(),
            centerFrequencyHz = config.bassConfig.mudTrimCenterHz,
            q = config.bassConfig.mudTrimQ,
            gainDb = bassTrimDb,
        )
        val trebleCoefficient = BiquadCoefficients.highShelf(
            sampleRateHz = safeSampleRate.toFloat(),
            cornerFrequencyHz = config.trebleShelfFrequencyHz,
            slope = config.trebleShelfSlope,
            gainDb = trebleBoostDb,
        )
        val spaciousMid = BiquadCoefficients.peaking(
            sampleRateHz = safeSampleRate.toFloat(),
            centerFrequencyHz = config.spaciousnessMidCutFrequencyHz,
            q = 0.7f,
            gainDb = -(config.spaciousnessMidCutDb * spaciousnessAmount),
        )
        val spaciousPresence = BiquadCoefficients.peaking(
            sampleRateHz = safeSampleRate.toFloat(),
            centerFrequencyHz = config.spaciousnessPresenceFrequencyHz,
            q = 0.78f,
            gainDb = config.spaciousnessPresenceBoostDb * spaciousnessAmount,
        )
        val spaciousAir = BiquadCoefficients.highShelf(
            sampleRateHz = safeSampleRate.toFloat(),
            cornerFrequencyHz = config.spaciousnessAirFrequencyHz,
            slope = 0.72f,
            gainDb = config.spaciousnessAirBoostDb * spaciousnessAmount,
        )
        val maxFrequencyHz = (safeSampleRate / 2f * 0.88f).coerceAtLeast(4_000f)
        val responseSampleCount = 72
        var peakDb = 0f
        repeat(responseSampleCount) { sampleIndex ->
            val t = sampleIndex.toFloat() / (responseSampleCount - 1).coerceAtLeast(1).toFloat()
            val frequencyHz = 20f * ((maxFrequencyHz / 20f).toDouble().pow(t.toDouble())).toFloat()
            var totalDb = 0f
            bandCoefficients.forEach { totalDb += it.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat()) }
            totalDb += bassCoefficient.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += bassAccentCoefficient.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += bassTrimCoefficient.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += trebleCoefficient.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousMid.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousPresence.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousAir.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            peakDb = max(peakDb, totalDb)
        }
        return peakDb
    }
}

internal object MonoDownmixProcessor {
    fun downmixFrame(
        frame: FloatArray,
        channelCount: Int,
    ): Float {
        return when {
            channelCount <= 0 || frame.isEmpty() -> 0f
            channelCount == 1 -> frame[0].sanitizeAudioSample()
            channelCount == 2 -> {
                val left = frame[0].sanitizeAudioSample()
                val right = frame[1].sanitizeAudioSample()
                ((left + right) * 0.5f).coerceIn(-1f, 1f)
            }
            else -> {
                val safeChannels = min(channelCount, frame.size)
                var sum = 0f
                for (index in 0 until safeChannels) {
                    sum += frame[index].sanitizeAudioSample()
                }
                (sum / safeChannels.toFloat()).coerceIn(-1f, 1f)
            }
        }
    }

    private fun Float.sanitizeAudioSample(): Float {
        return if (isFinite()) this else 0f
    }
}

internal class EqualizerAudioProcessor(
    private val config: EqualizerDspConfig = EqualizerDspConfig(),
) : BaseAudioProcessor() {
    @Volatile
    private var currentSettings: EqSettings = EqSettings()

    @Volatile
    private var manualPreampDb = 0f

    private var channelCount = 0
    private var sampleRateHz = 48_000
    private var currentWetMix = 0f
    private var targetWetMix = 0f
    private var currentBassShelfDb = 0f
    private var targetBassShelfDb = 0f
    private var currentBassAccentDb = 0f
    private var targetBassAccentDb = 0f
    private var currentBassTrimDb = 0f
    private var targetBassTrimDb = 0f
    private var currentBassPregainDb = 0f
    private var targetBassPregainDb = 0f
    private var currentBassAmount = 0f
    private var targetBassAmount = 0f
    private var currentBassDynamicReductionDb = 0f
    private var currentTrebleDb = 0f
    private var targetTrebleDb = 0f
    private var currentAutoHeadroomDb = 0f
    private var targetAutoHeadroomDb = 0f
    private var currentBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
    private var targetBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
    private var activeBandFrequenciesHz = EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ.copyOf()
    private var bandFilters: Array<Array<BiquadFilterState>> = emptyArray()
    private var bassHighPassFilters: Array<BiquadFilterState> = emptyArray()
    private var bassFilters: Array<BiquadFilterState> = emptyArray()
    private var bassAccentFilters: Array<BiquadFilterState> = emptyArray()
    private var bassTrimFilters: Array<BiquadFilterState> = emptyArray()
    private var trebleFilters: Array<BiquadFilterState> = emptyArray()
    private val spaciousnessProcessor = SpaciousnessProcessor()
    private val reverbProcessor = ReverbProcessor()
    private var smoothingAlpha = 1f
    private var configInitialized = false
    private var targetsDirty = true
    private var limiterPeakReduction = 0f
    private var limiterEvents = 0L
    private var limiterGain = 1f
    private var limiterGainReductionDb = 0f
    private var bassEnergyEnvelope = FloatArray(0)
    private var scratchDryFrame = FloatArray(2)
    private var scratchWetFrame = FloatArray(2)

    fun updateSettings(settings: EqSettings) {
        val monoToggled = settings.monoEnabled != currentSettings.monoEnabled
        currentSettings = settings.copy(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
            },
            bass = settings.bass.coerceIn(-1f, 1f),
            treble = settings.treble.coerceIn(-1f, 1f),
            spaciousness = settings.spaciousness.coerceIn(-1f, 1f),
            spaciousnessMode = settings.spaciousnessMode,
            monoEnabled = settings.monoEnabled,
            reverbDurationMs = normalizeReverbDurationMs(settings.reverbDurationMs),
            reverbProfile = settings.reverbProfile,
        )
        if (monoToggled && configInitialized) {
            resetFilterMemory()
        }
        targetsDirty = true
    }

    fun setManualPreampDb(value: Float) {
        manualPreampDb = value.coerceIn(config.minPreampDb, config.maxPreampDb)
        targetsDirty = true
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT,
            C.ENCODING_PCM_24BIT,
            C.ENCODING_PCM_32BIT,
            C.ENCODING_PCM_FLOAT,
            -> inputAudioFormat
            else -> throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
    }

    override fun queueInput(inputBuffer: java.nio.ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return
        val inputSize = inputBuffer.remaining()
        val outputBuffer = replaceOutputBuffer(inputSize)
        val encoding = inputAudioFormat.encoding
        if (!configInitialized) {
            resetProcessingState()
        }
        updateTargets()
        val frameSize = inputAudioFormat.bytesPerFrame
        if (frameSize <= 0) {
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }
        val totalFrames = inputSize / frameSize
        var processedFrames = 0
        while (processedFrames < totalFrames) {
            stepTowardsTargets()
            val blockFrames = min(
                config.sanitized().updateStrideFrames,
                totalFrames - processedFrames,
            )
            repeat(blockFrames) {
                for (channelIndex in 0 until channelCount) {
                    val drySample = readSample(inputBuffer, encoding)
                    scratchDryFrame[channelIndex] = drySample
                }
                if (currentSettings.monoEnabled && channelCount >= 2) {
                    val monoInputSample = MonoDownmixProcessor.downmixFrame(scratchDryFrame, channelCount)
                    val monoWet = processChannelSample(channelIndex = 0, sample = monoInputSample)
                    scratchWetFrame[0] = monoWet
                    reverbProcessor.processFrame(scratchWetFrame, 1)
                    val monoOutput = transparentSafetyLimit(scratchWetFrame[0])
                    for (channelIndex in 0 until channelCount) {
                        scratchDryFrame[channelIndex] = monoInputSample
                        scratchWetFrame[channelIndex] = scratchWetFrame[0]
                        writeSample(outputBuffer, encoding, monoOutput)
                    }
                    return@repeat
                }
                for (channelIndex in 0 until channelCount) {
                    scratchWetFrame[channelIndex] = processChannelSample(channelIndex, scratchDryFrame[channelIndex])
                }
                if (!currentSettings.monoEnabled) {
                    spaciousnessProcessor.processFrame(scratchWetFrame, channelCount)
                }
                reverbProcessor.processFrame(scratchWetFrame, channelCount)
                for (channelIndex in 0 until channelCount) {
                    val mixed = if (currentSettings.monoEnabled) {
                        scratchWetFrame[channelIndex]
                    } else {
                        mixDryWet(scratchDryFrame[channelIndex], scratchWetFrame[channelIndex], currentWetMix)
                    }
                    writeSample(outputBuffer, encoding, transparentSafetyLimit(mixed))
                }
            }
            processedFrames += blockFrames
        }
        outputBuffer.flip()
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        resetProcessingState()
    }

    override fun onReset() {
        resetRuntimeStates()
        configInitialized = false
        channelCount = 0
        sampleRateHz = 48_000
        currentBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
        targetBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
        activeBandFrequenciesHz = EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ.copyOf()
    }

    private fun resetProcessingState() {
        channelCount = inputAudioFormat.channelCount.coerceAtLeast(1)
        sampleRateHz = inputAudioFormat.sampleRate.coerceAtLeast(8_000)
        activeBandFrequenciesHz = EqualizerDspModel.activeBandFrequencies(sampleRateHz)
        smoothingAlpha = EqualizerDspModel.smoothingAlpha(
            sampleRateHz = sampleRateHz,
            smoothingTimeMs = config.sanitized().smoothingTimeMs,
            strideFrames = config.sanitized().updateStrideFrames,
        )
        bandFilters = Array(channelCount) {
            Array(EqualizerDspModel.BAND_COUNT) { BiquadFilterState() }
        }
        bassHighPassFilters = Array(channelCount) { BiquadFilterState() }
        bassFilters = Array(channelCount) { BiquadFilterState() }
        bassAccentFilters = Array(channelCount) { BiquadFilterState() }
        bassTrimFilters = Array(channelCount) { BiquadFilterState() }
        trebleFilters = Array(channelCount) { BiquadFilterState() }
        bassEnergyEnvelope = FloatArray(channelCount)
        scratchDryFrame = FloatArray(channelCount)
        scratchWetFrame = FloatArray(channelCount)
        spaciousnessProcessor.configure(sampleRateHz = sampleRateHz, channelCount = channelCount)
        reverbProcessor.configure(sampleRateHz = sampleRateHz, channelCount = channelCount)
        resetRuntimeStates()
        targetsDirty = true
        configInitialized = true
    }

    private fun resetRuntimeStates() {
        currentWetMix = 0f
        targetWetMix = 0f
        currentBassShelfDb = 0f
        targetBassShelfDb = 0f
        currentBassAccentDb = 0f
        targetBassAccentDb = 0f
        currentBassTrimDb = 0f
        targetBassTrimDb = 0f
        currentBassPregainDb = 0f
        targetBassPregainDb = 0f
        currentBassAmount = 0f
        targetBassAmount = 0f
        currentBassDynamicReductionDb = 0f
        currentTrebleDb = 0f
        targetTrebleDb = 0f
        currentAutoHeadroomDb = 0f
        targetAutoHeadroomDb = 0f
        limiterPeakReduction = 0f
        limiterGain = 1f
        limiterGainReductionDb = 0f
        bassEnergyEnvelope.fill(0f)
        currentBandGainsDb.fill(0f)
        targetBandGainsDb.fill(0f)
        resetFilterMemory()
    }

    private fun resetFilterMemory() {
        bandFilters.forEach { channelFilters -> channelFilters.forEach(BiquadFilterState::reset) }
        bassHighPassFilters.forEach(BiquadFilterState::reset)
        bassFilters.forEach(BiquadFilterState::reset)
        bassAccentFilters.forEach(BiquadFilterState::reset)
        bassTrimFilters.forEach(BiquadFilterState::reset)
        trebleFilters.forEach(BiquadFilterState::reset)
        spaciousnessProcessor.reset()
        reverbProcessor.reset()
    }

    private fun updateTargets() {
        if (!targetsDirty) return
        val settingsSnapshot = currentSettings
        val safeConfig = config.sanitized()
        val flat = EqualizerDspModel.isFlat(settingsSnapshot)
        targetWetMix = if (flat && !settingsSnapshot.monoEnabled) 0f else 1f
        settingsSnapshot.bands.forEachIndexed { index, normalized ->
            val bandFrequencyHz = activeBandFrequenciesHz.getOrElse(index) { -1f }
            targetBandGainsDb[index] = if (bandFrequencyHz > 0f) {
                EqualizerDspModel.normalizedBandToDb(normalized, safeConfig)
            } else {
                0f
            }
        }
        val bassAmount = settingsSnapshot.bass.coerceAtLeast(0f).coerceIn(0f, 1f)
        val bassConfig = safeConfig.bassConfig.copy(
            enabled = bassAmount > 0.0005f,
            amountNormalized = bassAmount,
        ).sanitized()
        targetTrebleDb = settingsSnapshot.treble.coerceIn(-1f, 1f) * safeConfig.trebleMaxBoostDb
        val spaciousnessAmount = settingsSnapshot.spaciousness.coerceAtLeast(0f).coerceIn(0f, 1f)
        val lowBandEqBoostSafetyDb = EqualizerDspModel.lowBandBoostSafetyDb(targetBandGainsDb)
        val bassCurve = HighQualityBassProcessorModel.curveFor(
            amountNormalized = bassAmount,
            config = bassConfig,
            lowBandEqBoostSafetyDb = lowBandEqBoostSafetyDb,
            trebleBoostDb = targetTrebleDb,
            spaciousnessAmount = spaciousnessAmount,
        )
        targetBassAmount = bassCurve.amount
        targetBassShelfDb = bassCurve.shelfDb
        targetBassAccentDb = bassCurve.punchDb
        targetBassTrimDb = bassCurve.mudTrimDb
        targetBassPregainDb = 0f
        spaciousnessProcessor.setConfig(
            SpaciousnessConfig(
                enabled = spaciousnessAmount > 0.0005f,
                mode = settingsSnapshot.spaciousnessMode,
                amountNormalized = spaciousnessAmount,
                preserveBassMono = true,
                smoothingTimeMs = safeConfig.smoothingTimeMs,
            ),
        )
        val reverbDurationMs = normalizeReverbDurationMs(settingsSnapshot.reverbDurationMs)
        reverbProcessor.setConfig(
            ReverbConfig(
                enabled = reverbDurationMs > 0,
                profile = settingsSnapshot.reverbProfile,
                decayMs = reverbDurationMs,
                smoothingTimeMs = safeConfig.smoothingTimeMs,
            ),
        )
        val reverbHeadroomDb = ReverbProcessorModel.automaticHeadroomDb(
            profile = settingsSnapshot.reverbProfile,
            decayMs = reverbDurationMs,
        )
        targetAutoHeadroomDb = EqualizerDspModel.automaticHeadroomDb(
            bandGainsDb = targetBandGainsDb,
            bassShelfDb = targetBassShelfDb,
            bassAccentDb = targetBassAccentDb,
            bassTrimDb = targetBassTrimDb,
            bassPregainDb = targetBassPregainDb,
            trebleBoostDb = targetTrebleDb,
            spaciousnessAmount = spaciousnessAmount,
            sampleRateHz = sampleRateHz,
            reverbHeadroomDb = reverbHeadroomDb,
            config = safeConfig,
        ).coerceAtMost(bassCurve.automaticHeadroomDb)
        targetsDirty = false
    }

    private fun stepTowardsTargets() {
        currentWetMix = smooth(currentWetMix, targetWetMix, smoothingAlpha)
        currentBassShelfDb = smooth(currentBassShelfDb, targetBassShelfDb, smoothingAlpha)
        currentBassAccentDb = smooth(currentBassAccentDb, targetBassAccentDb, smoothingAlpha)
        currentBassTrimDb = smooth(currentBassTrimDb, targetBassTrimDb, smoothingAlpha)
        currentBassPregainDb = smooth(currentBassPregainDb, targetBassPregainDb, smoothingAlpha)
        currentBassAmount = smooth(currentBassAmount, targetBassAmount, smoothingAlpha)
        val averageBassEnvelope = if (bassEnergyEnvelope.isNotEmpty()) {
            bassEnergyEnvelope.sum() / bassEnergyEnvelope.size.toFloat()
        } else {
            0f
        }
        val targetDynamicReductionDb = HighQualityBassProcessorModel.dynamicReductionDb(
            lowBandEnvelope = averageBassEnvelope,
            amountNormalized = currentBassAmount,
            config = config.sanitized().bassConfig,
        )
        currentBassDynamicReductionDb = smooth(currentBassDynamicReductionDb, targetDynamicReductionDb, smoothingAlpha * 0.55f)
        currentTrebleDb = smooth(currentTrebleDb, targetTrebleDb, smoothingAlpha)
        currentAutoHeadroomDb = smooth(currentAutoHeadroomDb, targetAutoHeadroomDb, smoothingAlpha)
        currentBandGainsDb.indices.forEach { index ->
            currentBandGainsDb[index] = smooth(currentBandGainsDb[index], targetBandGainsDb[index], smoothingAlpha)
        }
        rebuildCoefficients()
    }

    private fun rebuildCoefficients() {
        val safeConfig = config.sanitized()
        for (channelIndex in 0 until channelCount) {
            for (bandIndex in currentBandGainsDb.indices) {
                val frequencyHz = activeBandFrequenciesHz.getOrElse(bandIndex) { -1f }
                val coefficients = if (frequencyHz > 0f && abs(currentBandGainsDb[bandIndex]) > 0.01f) {
                    BiquadCoefficients.peaking(
                        sampleRateHz = sampleRateHz.toFloat(),
                        centerFrequencyHz = frequencyHz,
                        q = EqualizerDspModel.bandDefinition(bandIndex).q,
                        gainDb = currentBandGainsDb[bandIndex],
                    )
                } else {
                    BiquadCoefficients.identity()
                }
                bandFilters[channelIndex][bandIndex].setCoefficients(coefficients)
            }
            bassHighPassFilters[channelIndex].setCoefficients(
                if (currentBassAmount > 0.0005f) {
                    BiquadCoefficients.highPass(
                        sampleRateHz = sampleRateHz.toFloat(),
                        cutoffFrequencyHz = safeConfig.bassConfig.highPassFrequencyHz,
                        q = 0.707f,
                    )
                } else {
                    BiquadCoefficients.identity()
                },
            )
            val effectiveShelfDb = (currentBassShelfDb + currentBassDynamicReductionDb).coerceAtLeast(0f)
            val effectivePunchDb = (currentBassAccentDb + (currentBassDynamicReductionDb * 0.5f)).coerceAtLeast(0f)
            bassFilters[channelIndex].setCoefficients(
                BiquadCoefficients.lowShelf(
                    sampleRateHz = sampleRateHz.toFloat(),
                    cornerFrequencyHz = safeConfig.bassConfig.shelfFrequencyHz,
                    slope = safeConfig.bassConfig.shelfSlope,
                    gainDb = effectiveShelfDb,
                ),
            )
            bassAccentFilters[channelIndex].setCoefficients(
                if (abs(effectivePunchDb) > 0.01f) {
                    BiquadCoefficients.peaking(
                        sampleRateHz = sampleRateHz.toFloat(),
                        centerFrequencyHz = safeConfig.bassConfig.punchCenterHz,
                        q = safeConfig.bassConfig.punchQ,
                        gainDb = effectivePunchDb,
                    )
                } else {
                    BiquadCoefficients.identity()
                },
            )
            bassTrimFilters[channelIndex].setCoefficients(
                if (abs(currentBassTrimDb) > 0.01f) {
                    BiquadCoefficients.peaking(
                        sampleRateHz = sampleRateHz.toFloat(),
                        centerFrequencyHz = safeConfig.bassConfig.mudTrimCenterHz,
                        q = safeConfig.bassConfig.mudTrimQ,
                        gainDb = currentBassTrimDb,
                    )
                } else {
                    BiquadCoefficients.identity()
                },
            )
            trebleFilters[channelIndex].setCoefficients(
                BiquadCoefficients.highShelf(
                    sampleRateHz = sampleRateHz.toFloat(),
                    cornerFrequencyHz = safeConfig.trebleShelfFrequencyHz,
                    slope = safeConfig.trebleShelfSlope,
                    gainDb = currentTrebleDb,
                ),
            )
        }
    }

    private fun processChannelSample(
        channelIndex: Int,
        sample: Float,
    ): Float {
        var processed = sample * dbToLinear(manualPreampDb + currentBassPregainDb + currentAutoHeadroomDb)
        bandFilters[channelIndex].forEach { filter -> processed = filter.process(processed) }
        val bassDetectorInput = processed
        processed = bassHighPassFilters[channelIndex].process(processed)
        processed = bassFilters[channelIndex].process(processed)
        processed = bassAccentFilters[channelIndex].process(processed)
        processed = bassTrimFilters[channelIndex].process(processed)
        processed = trebleFilters[channelIndex].process(processed)
        updateBassEnergyEnvelope(channelIndex, bassDetectorInput)
        return processed
    }

    private fun updateBassEnergyEnvelope(
        channelIndex: Int,
        sample: Float,
    ) {
        if (channelIndex !in bassEnergyEnvelope.indices || currentBassAmount <= 0.0005f) return
        val detectorSample = abs(sample).coerceIn(0f, 1f)
        val attack = 1f - exp(-1.0 / (sampleRateHz.coerceAtLeast(8_000) * 0.028)).toFloat()
        val release = 1f - exp(-1.0 / (sampleRateHz.coerceAtLeast(8_000) * 0.16)).toFloat()
        val current = bassEnergyEnvelope[channelIndex]
        val alpha = if (detectorSample > current) attack else release
        bassEnergyEnvelope[channelIndex] = current + ((detectorSample - current) * alpha)
    }

    private fun mixDryWet(
        dry: Float,
        wet: Float,
        wetMix: Float,
    ): Float {
        return dry + ((wet - dry) * wetMix.coerceIn(0f, 1f))
    }

    private fun readSample(
        inputBuffer: java.nio.ByteBuffer,
        encoding: Int,
    ): Float {
        return when (encoding) {
            C.ENCODING_PCM_16BIT -> (inputBuffer.short.toInt() / 32768f).coerceIn(-1f, 1f)
            C.ENCODING_PCM_24BIT -> {
                val b0 = inputBuffer.get().toInt() and 0xFF
                val b1 = inputBuffer.get().toInt() and 0xFF
                val b2 = inputBuffer.get().toInt() and 0xFF
                var value = b0 or (b1 shl 8) or (b2 shl 16)
                if (value and 0x00800000 != 0) {
                    value = value or -0x01000000
                }
                (value / 8_388_608f).coerceIn(-1f, 1f)
            }
            C.ENCODING_PCM_32BIT -> (inputBuffer.int / 2_147_483_648f).coerceIn(-1f, 1f)
            C.ENCODING_PCM_FLOAT -> inputBuffer.float.coerceIn(-1f, 1f)
            else -> 0f
        }
    }

    private fun writeSample(
        outputBuffer: java.nio.ByteBuffer,
        encoding: Int,
        sample: Float,
    ) {
        val clamped = sample.coerceIn(-1f, 1f)
        when (encoding) {
            C.ENCODING_PCM_16BIT -> {
                outputBuffer.putShort((clamped * Short.MAX_VALUE).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
            }
            C.ENCODING_PCM_24BIT -> {
                val value = (clamped * 8_388_607f).roundToInt().coerceIn(-8_388_608, 8_388_607)
                outputBuffer.put((value and 0xFF).toByte())
                outputBuffer.put(((value shr 8) and 0xFF).toByte())
                outputBuffer.put(((value shr 16) and 0xFF).toByte())
            }
            C.ENCODING_PCM_32BIT -> {
                outputBuffer.putInt((clamped * Int.MAX_VALUE.toFloat()).roundToInt())
            }
            C.ENCODING_PCM_FLOAT -> outputBuffer.putFloat(clamped)
        }
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

    private fun transparentSafetyLimit(sample: Float): Float {
        val safeConfig = config.sanitized()
        val threshold = safeConfig.limiterThreshold
        val knee = safeConfig.limiterKnee
        val sampleAbs = abs(sample)
        val targetGain = if (sampleAbs <= threshold) {
            1f
        } else {
            val overshoot = (sampleAbs - threshold).coerceAtLeast(0f)
            val shapedAbs = threshold + (1f - exp(-(overshoot / knee).coerceAtLeast(0f).toDouble())).toFloat() * knee
            shapedAbs.coerceAtMost(threshold) / sampleAbs.coerceAtLeast(1e-9f)
        }
        val attack = 0.42f
        val release = 1f - exp(-1.0 / (sampleRateHz.coerceAtLeast(8_000) * 0.11)).toFloat()
        limiterGain = if (targetGain < limiterGain) {
            limiterGain + ((targetGain - limiterGain) * attack)
        } else {
            limiterGain + ((targetGain - limiterGain) * release)
        }.coerceIn(0f, 1f)
        val limited = (sample * limiterGain).coerceIn(-threshold, threshold)
        val reduction = sampleAbs - abs(limited)
        if (reduction > 0.00001f) {
            limiterPeakReduction = max(limiterPeakReduction, reduction)
            limiterEvents += 1
        }
        limiterGainReductionDb = if (limiterGain < 0.9999f) 20f * kotlin.math.log10(limiterGain.coerceAtLeast(1e-6f)) else 0f
        return limited
    }

    internal fun debugSnapshot(): EqualizerDiagnosticsSnapshot {
        return EqualizerDiagnosticsSnapshot(
            sampleRateHz = sampleRateHz,
            computedHeadroomDb = currentAutoHeadroomDb,
            activeFilterCount = currentBandGainsDb.count { abs(it) > 0.01f } +
                if (currentBassAmount > 0.0005f) 1 else 0 +
                if (abs(currentBassShelfDb) > 0.01f) 1 else 0 +
                if (abs(currentBassAccentDb) > 0.01f) 1 else 0 +
                if (abs(currentBassTrimDb) > 0.01f) 1 else 0 +
                if (abs(currentTrebleDb) > 0.01f) 1 else 0 +
                if (currentSettings.reverbDurationMs > 0) 1 else 0 +
                if (!spaciousnessProcessor.isBypassed()) 1 else 0,
            bassShelfGainDb = currentBassShelfDb,
            bassPunchGainDb = currentBassAccentDb,
            bassMudTrimDb = currentBassTrimDb,
            bassDynamicReductionDb = currentBassDynamicReductionDb,
            limiterPeakReduction = limiterPeakReduction,
            limiterGainReductionDb = limiterGainReductionDb,
            limiterEvents = limiterEvents,
            dspBypassed = targetWetMix == 0f,
        )
    }
}

internal data class EqualizerDiagnosticsSnapshot(
    val sampleRateHz: Int,
    val computedHeadroomDb: Float,
    val activeFilterCount: Int,
    val bassShelfGainDb: Float,
    val bassPunchGainDb: Float,
    val bassMudTrimDb: Float,
    val bassDynamicReductionDb: Float,
    val limiterPeakReduction: Float,
    val limiterGainReductionDb: Float,
    val limiterEvents: Long,
    val dspBypassed: Boolean,
)

private data class BiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float,
) {
    companion object {
        fun identity(): BiquadCoefficients = BiquadCoefficients(
            b0 = 1f,
            b1 = 0f,
            b2 = 0f,
            a1 = 0f,
            a2 = 0f,
        )

        fun peaking(
            sampleRateHz: Float,
            centerFrequencyHz: Float,
            q: Float,
            gainDb: Float,
        ): BiquadCoefficients {
            if (abs(gainDb) <= 0.0001f) return identity()
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val nyquist = safeSampleRate / 2f
            val safeFrequency = centerFrequencyHz.coerceIn(10f, nyquist * 0.92f)
            val safeQ = q.coerceIn(0.2f, 12f)
            val a = 10.0.pow(gainDb / 40.0).toFloat()
            val w0 = (2.0 * PI * safeFrequency / safeSampleRate).toFloat()
            val alpha = (sin(w0) / (2f * safeQ)).coerceAtLeast(1e-6f)
            val cosW0 = kotlin.math.cos(w0)
            val b0 = 1f + (alpha * a)
            val b1 = -2f * cosW0
            val b2 = 1f - (alpha * a)
            val a0 = 1f + (alpha / a)
            val a1 = -2f * cosW0
            val a2 = 1f - (alpha / a)
            return BiquadCoefficients(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }

        fun peakingByBandwidth(
            sampleRateHz: Float,
            centerFrequencyHz: Float,
            bandwidthOctaves: Float,
            gainDb: Float,
            fallbackQ: Float,
        ): BiquadCoefficients {
            if (abs(gainDb) <= 0.0001f) return identity()
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val nyquist = safeSampleRate / 2f
            val safeFrequency = centerFrequencyHz.coerceIn(10f, nyquist * 0.92f)
            val safeBandwidth = bandwidthOctaves.coerceIn(0.15f, 1.2f)
            val a = 10.0.pow(gainDb / 40.0).toFloat()
            val w0 = (2.0 * PI * safeFrequency / safeSampleRate).toFloat()
            val sinW0 = sin(w0)
            val alpha = if (abs(sinW0) > 1e-6f) {
                (sinW0 * sinh((ln(2.0) / 2.0) * safeBandwidth * (w0 / sinW0))).toFloat().coerceAtLeast(1e-6f)
            } else {
                (sin(w0) / (2f * fallbackQ.coerceIn(0.2f, 12f))).coerceAtLeast(1e-6f)
            }
            val cosW0 = kotlin.math.cos(w0)
            val b0 = 1f + (alpha * a)
            val b1 = -2f * cosW0
            val b2 = 1f - (alpha * a)
            val a0 = 1f + (alpha / a)
            val a1 = -2f * cosW0
            val a2 = 1f - (alpha / a)
            return BiquadCoefficients(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }

        fun highPass(
            sampleRateHz: Float,
            cutoffFrequencyHz: Float,
            q: Float,
        ): BiquadCoefficients {
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val nyquist = safeSampleRate / 2f
            val safeCutoff = cutoffFrequencyHz.coerceIn(10f, nyquist * 0.72f)
            val safeQ = q.coerceIn(0.2f, 4f)
            val w0 = (2.0 * PI * safeCutoff / safeSampleRate).toFloat()
            val cosW0 = kotlin.math.cos(w0)
            val sinW0 = sin(w0)
            val alpha = (sinW0 / (2f * safeQ)).coerceAtLeast(1e-6f)
            val b0 = (1f + cosW0) / 2f
            val b1 = -(1f + cosW0)
            val b2 = (1f + cosW0) / 2f
            val a0 = 1f + alpha
            val a1 = -2f * cosW0
            val a2 = 1f - alpha
            return BiquadCoefficients(
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
        ): BiquadCoefficients {
            if (abs(gainDb) <= 0.0001f) return identity()
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val safeCorner = cornerFrequencyHz.coerceIn(20f, safeSampleRate / 2.4f)
            val safeSlope = slope.coerceIn(0.35f, 1.4f)
            val a = 10.0.pow(gainDb / 40.0)
            val w0 = 2.0 * PI * safeCorner / safeSampleRate
            val cosW0 = kotlin.math.cos(w0).toFloat()
            val sinW0 = kotlin.math.sin(w0).toFloat()
            val alpha = (sinW0 / 2f) * sqrt(((a + (1.0 / a)) * ((1f / safeSlope) - 1f)) + 2.0).toFloat()
            val beta = 2f * sqrt(a).toFloat() * alpha
            val b0 = (a * ((a + 1.0) - ((a - 1.0) * cosW0) + beta)).toFloat()
            val b1 = (2.0 * a * ((a - 1.0) - ((a + 1.0) * cosW0))).toFloat()
            val b2 = (a * ((a + 1.0) - ((a - 1.0) * cosW0) - beta)).toFloat()
            val a0 = ((a + 1.0) + ((a - 1.0) * cosW0) + beta).toFloat()
            val a1 = (-2.0 * ((a - 1.0) + ((a + 1.0) * cosW0))).toFloat()
            val a2 = ((a + 1.0) + ((a - 1.0) * cosW0) - beta).toFloat()
            return BiquadCoefficients(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }

        fun highShelf(
            sampleRateHz: Float,
            cornerFrequencyHz: Float,
            slope: Float,
            gainDb: Float,
        ): BiquadCoefficients {
            if (abs(gainDb) <= 0.0001f) return identity()
            val safeSampleRate = sampleRateHz.coerceAtLeast(8_000f)
            val safeCorner = cornerFrequencyHz.coerceIn(400f, safeSampleRate / 2.4f)
            val safeSlope = slope.coerceIn(0.35f, 1.4f)
            val a = 10.0.pow(gainDb / 40.0)
            val w0 = 2.0 * PI * safeCorner / safeSampleRate
            val cosW0 = kotlin.math.cos(w0).toFloat()
            val sinW0 = kotlin.math.sin(w0).toFloat()
            val alpha = (sinW0 / 2f) * sqrt(((a + (1.0 / a)) * ((1f / safeSlope) - 1f)) + 2.0).toFloat()
            val beta = 2f * sqrt(a).toFloat() * alpha
            val b0 = (a * ((a + 1.0) + ((a - 1.0) * cosW0) + beta)).toFloat()
            val b1 = (-2.0 * a * ((a - 1.0) + ((a + 1.0) * cosW0))).toFloat()
            val b2 = (a * ((a + 1.0) + ((a - 1.0) * cosW0) - beta)).toFloat()
            val a0 = ((a + 1.0) - ((a - 1.0) * cosW0) + beta).toFloat()
            val a1 = (2.0 * ((a - 1.0) - ((a + 1.0) * cosW0))).toFloat()
            val a2 = ((a + 1.0) - ((a - 1.0) * cosW0) - beta).toFloat()
            return BiquadCoefficients(
                b0 = b0 / a0,
                b1 = b1 / a0,
                b2 = b2 / a0,
                a1 = a1 / a0,
                a2 = a2 / a0,
            )
        }
    }

    fun magnitudeResponseDb(
        frequencyHz: Float,
        sampleRateHz: Float,
    ): Float {
        if (isIdentity()) return 0f
        val omega = 2.0 * PI * frequencyHz.coerceAtLeast(0.1f).toDouble() / sampleRateHz.coerceAtLeast(1f).toDouble()
        val cos1 = kotlin.math.cos(omega)
        val sin1 = kotlin.math.sin(omega)
        val cos2 = kotlin.math.cos(omega * 2.0)
        val sin2 = kotlin.math.sin(omega * 2.0)
        val numeratorReal = b0 + (b1 * cos1).toFloat() + (b2 * cos2).toFloat()
        val numeratorImag = (-(b1 * sin1) - (b2 * sin2)).toFloat()
        val denominatorReal = 1f + (a1 * cos1).toFloat() + (a2 * cos2).toFloat()
        val denominatorImag = (-(a1 * sin1) - (a2 * sin2)).toFloat()
        val numeratorMagnitude = sqrt((numeratorReal * numeratorReal) + (numeratorImag * numeratorImag))
        val denominatorMagnitude = sqrt((denominatorReal * denominatorReal) + (denominatorImag * denominatorImag))
        val magnitude = (numeratorMagnitude / denominatorMagnitude.coerceAtLeast(1e-12f)).coerceAtLeast(1e-12f)
        return (20.0 * kotlin.math.log10(magnitude.toDouble())).toFloat()
    }

    fun isIdentity(): Boolean {
        return abs(b0 - 1f) <= 0.00001f &&
            abs(b1) <= 0.00001f &&
            abs(b2) <= 0.00001f &&
            abs(a1) <= 0.00001f &&
            abs(a2) <= 0.00001f
    }
}

private class BiquadFilterState {
    private var b0 = 1f
    private var b1 = 0f
    private var b2 = 0f
    private var a1 = 0f
    private var a2 = 0f
    private var z1 = 0f
    private var z2 = 0f

    fun setCoefficients(coefficients: BiquadCoefficients) {
        b0 = coefficients.b0
        b1 = coefficients.b1
        b2 = coefficients.b2
        a1 = coefficients.a1
        a2 = coefficients.a2
    }

    fun process(sample: Float): Float {
        val output = (b0 * sample) + z1
        z1 = (b1 * sample) - (a1 * output) + z2
        z2 = (b2 * sample) - (a2 * output)
        return output
    }

    fun reset() {
        z1 = 0f
        z2 = 0f
    }
}
