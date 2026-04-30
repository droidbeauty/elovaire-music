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
            bandGainsDb.getOrElse(index) { 0f }.coerceIn(-config.maxBandGainDb, config.maxBandGainDb)
        }
        return copy(
            bandGainsDb = safeBands,
            preampDb = preampDb.coerceIn(config.minPreampDb, config.maxPreampDb),
        )
    }
}

internal data class EqualizerDspConfig(
    val bandCenterFrequenciesHz: FloatArray = EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ,
    val maxBandGainDb: Float = 12f,
    val minPreampDb: Float = -12f,
    val maxPreampDb: Float = 6f,
    val smoothingTimeMs: Int = 56,
    val bandQ: Float = 0.98f,
    val bandBandwidthStretch: Float = 1.08f,
    val bassConfig: BassBoostConfig = BassBoostConfig(),
    val trebleShelfFrequencyHz: Float = 8_200f,
    val trebleShelfSlope: Float = 0.66f,
    val trebleMaxBoostDb: Float = 3.8f,
    val spaciousnessMidCutDb: Float = 1.25f,
    val spaciousnessPresenceBoostDb: Float = 1.1f,
    val spaciousnessAirBoostDb: Float = 0.75f,
    val spaciousnessMidCutFrequencyHz: Float = 290f,
    val spaciousnessPresenceFrequencyHz: Float = 2_350f,
    val spaciousnessAirFrequencyHz: Float = 8_500f,
    val headroomSafetyMarginDb: Float = 0.85f,
    val headroomBlend: Float = 0.92f,
    val limiterThreshold: Float = 0.988f,
    val limiterKnee: Float = 0.028f,
    val updateStrideFrames: Int = 24,
) {
    fun sanitized(): EqualizerDspConfig {
        return copy(
            maxBandGainDb = maxBandGainDb.coerceIn(3f, 15f),
            minPreampDb = minPreampDb.coerceIn(-24f, 0f),
            maxPreampDb = maxPreampDb.coerceIn(0f, 12f),
            smoothingTimeMs = smoothingTimeMs.coerceIn(20, 140),
            bandQ = bandQ.coerceIn(0.55f, 2.2f),
            bandBandwidthStretch = bandBandwidthStretch.coerceIn(0.8f, 1.4f),
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
            limiterThreshold = limiterThreshold.coerceIn(0.94f, 0.999f),
            limiterKnee = limiterKnee.coerceIn(0.008f, 0.08f),
            updateStrideFrames = updateStrideFrames.coerceIn(8, 96),
        )
    }
}

internal object EqualizerDspModel {
    const val BAND_COUNT = 24
    val BAND_CENTER_FREQUENCIES_HZ: FloatArray = FloatArray(BAND_COUNT) { index ->
        val fraction = index.toDouble() / (BAND_COUNT - 1).coerceAtLeast(1).toDouble()
        (20.0 * (16_000.0 / 20.0).pow(fraction)).toFloat()
    }
    private const val FLAT_EPSILON = 0.0005f

    fun normalizedBandToDb(
        normalized: Float,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        return normalized.coerceIn(-1f, 1f) * safeConfig.maxBandGainDb
    }

    fun activeBandFrequencies(sampleRateHz: Int): FloatArray {
        val nyquistSafeLimit = sampleRateHz.coerceAtLeast(8_000) / 2f * 0.9f
        return BAND_CENTER_FREQUENCIES_HZ.map { frequencyHz ->
            if (frequencyHz < nyquistSafeLimit) frequencyHz else -1f
        }.toFloatArray()
    }

    fun isFlat(settings: EqSettings): Boolean {
        return settings.bands.none { abs(it) > FLAT_EPSILON } &&
            abs(settings.treble) <= FLAT_EPSILON &&
            settings.bass.coerceAtLeast(0f) <= FLAT_EPSILON &&
            settings.spaciousness.coerceAtLeast(0f) <= FLAT_EPSILON
    }

    fun automaticHeadroomDb(
        bandGainsDb: FloatArray,
        bassBoostDb: Float,
        bassPregainDb: Float,
        trebleBoostDb: Float,
        spaciousnessAmount: Float,
        sampleRateHz: Int,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val peakResponseDb = estimatePeakResponseDb(
            bandGainsDb = bandGainsDb,
            bassBoostDb = bassBoostDb,
            trebleBoostDb = trebleBoostDb,
            spaciousnessAmount = spaciousnessAmount,
            sampleRateHz = sampleRateHz,
            config = safeConfig,
        )
        val combinedPositiveBoostDb = (peakResponseDb + bassPregainDb).coerceAtLeast(0f)
        if (combinedPositiveBoostDb <= 0f) return 0f
        return -((combinedPositiveBoostDb * safeConfig.headroomBlend) + safeConfig.headroomSafetyMarginDb)
            .coerceIn(0f, 12f)
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

    fun bandBandwidthOctaves(
        bandIndex: Int,
        config: EqualizerDspConfig = EqualizerDspConfig(),
    ): Float {
        val safeConfig = config.sanitized()
        val centers = safeConfig.bandCenterFrequenciesHz
        if (centers.isEmpty()) return 0.5f
        val currentIndex = bandIndex.coerceIn(0, centers.lastIndex)
        val center = centers[currentIndex].coerceAtLeast(10f)
        val previous = centers.getOrNull(currentIndex - 1) ?: (center / (centers.getOrNull(currentIndex + 1)?.div(center) ?: 1.25f))
        val next = centers.getOrNull(currentIndex + 1) ?: (center * (center / previous.coerceAtLeast(1f)))
        val lowerBoundary = sqrt((center * previous).toDouble()).toFloat().coerceAtLeast(1f)
        val upperBoundary = sqrt((center * next).toDouble()).toFloat().coerceAtLeast(lowerBoundary + 0.0001f)
        val octaves = (ln((upperBoundary / lowerBoundary).toDouble()) / ln(2.0)).toFloat()
        return (octaves * safeConfig.bandBandwidthStretch).coerceIn(0.18f, 0.9f)
    }

    private fun estimatePeakResponseDb(
        bandGainsDb: FloatArray,
        bassBoostDb: Float,
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
                BiquadCoefficients.peakingByBandwidth(
                    sampleRateHz = safeSampleRate.toFloat(),
                    centerFrequencyHz = frequencyHz,
                    bandwidthOctaves = bandBandwidthOctaves(index, config),
                    gainDb = gainDb,
                    fallbackQ = config.bandQ,
                )
            } else {
                BiquadCoefficients.identity()
            }
        }
        val bassCoefficient = BiquadCoefficients.lowShelf(
            sampleRateHz = safeSampleRate.toFloat(),
            cornerFrequencyHz = config.bassConfig.shelfFrequencyHz,
            slope = config.bassConfig.shelfSlope,
            gainDb = bassBoostDb,
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
            totalDb += trebleCoefficient.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousMid.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousPresence.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            totalDb += spaciousAir.magnitudeResponseDb(frequencyHz, safeSampleRate.toFloat())
            peakDb = max(peakDb, totalDb)
        }
        return peakDb
    }
}

internal class EqualizerAudioProcessor(
    private val config: EqualizerDspConfig = EqualizerDspConfig(),
) : BaseAudioProcessor() {
    @Volatile
    private var currentSettings: EqSettings = EqSettings()

    @Volatile
    private var bitPerfectBypassed = false

    @Volatile
    private var manualPreampDb = 0f

    private var channelCount = 0
    private var sampleRateHz = 48_000
    private var currentWetMix = 0f
    private var targetWetMix = 0f
    private var currentBassBoostDb = 0f
    private var targetBassBoostDb = 0f
    private var currentBassPregainDb = 0f
    private var targetBassPregainDb = 0f
    private var currentTrebleDb = 0f
    private var targetTrebleDb = 0f
    private var currentAutoHeadroomDb = 0f
    private var targetAutoHeadroomDb = 0f
    private var currentBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
    private var targetBandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT)
    private var activeBandFrequenciesHz = EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ.copyOf()
    private var bandFilters: Array<Array<BiquadFilterState>> = emptyArray()
    private var bassFilters: Array<BiquadFilterState> = emptyArray()
    private var trebleFilters: Array<BiquadFilterState> = emptyArray()
    private val spaciousnessProcessor = SpaciousnessProcessor()
    private var smoothingAlpha = 1f
    private var configInitialized = false
    private var targetsDirty = true
    private var limiterPeakReduction = 0f
    private var limiterEvents = 0L
    private var scratchDryFrame = FloatArray(2)
    private var scratchWetFrame = FloatArray(2)

    fun updateSettings(settings: EqSettings) {
        currentSettings = settings.copy(
            bands = List(EqualizerDspModel.BAND_COUNT) { index ->
                settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
            },
            bass = settings.bass.coerceIn(-1f, 1f),
            treble = settings.treble.coerceIn(-1f, 1f),
            spaciousness = settings.spaciousness.coerceIn(-1f, 1f),
            spaciousnessMode = settings.spaciousnessMode,
        )
        targetsDirty = true
    }

    fun setBitPerfectBypass(enabled: Boolean) {
        bitPerfectBypassed = enabled
        spaciousnessProcessor.setBitPerfectBypass(enabled)
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
        if (bitPerfectBypassed) {
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            resetRuntimeStates()
            return
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
                    scratchWetFrame[channelIndex] = processChannelSample(channelIndex, drySample)
                }
                spaciousnessProcessor.processFrame(scratchWetFrame, channelCount)
                for (channelIndex in 0 until channelCount) {
                    val mixed = mixDryWet(scratchDryFrame[channelIndex], scratchWetFrame[channelIndex], currentWetMix)
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
        bassFilters = Array(channelCount) { BiquadFilterState() }
        trebleFilters = Array(channelCount) { BiquadFilterState() }
        scratchDryFrame = FloatArray(channelCount)
        scratchWetFrame = FloatArray(channelCount)
        spaciousnessProcessor.configure(sampleRateHz = sampleRateHz, channelCount = channelCount)
        resetRuntimeStates()
        targetsDirty = true
        configInitialized = true
    }

    private fun resetRuntimeStates() {
        currentWetMix = 0f
        targetWetMix = 0f
        currentBassBoostDb = 0f
        targetBassBoostDb = 0f
        currentBassPregainDb = 0f
        targetBassPregainDb = 0f
        currentTrebleDb = 0f
        targetTrebleDb = 0f
        currentAutoHeadroomDb = 0f
        targetAutoHeadroomDb = 0f
        limiterPeakReduction = 0f
        currentBandGainsDb.fill(0f)
        targetBandGainsDb.fill(0f)
        bandFilters.forEach { channelFilters -> channelFilters.forEach(BiquadFilterState::reset) }
        bassFilters.forEach(BiquadFilterState::reset)
        trebleFilters.forEach(BiquadFilterState::reset)
        spaciousnessProcessor.reset()
    }

    private fun updateTargets() {
        if (!targetsDirty) return
        val settingsSnapshot = currentSettings
        val safeConfig = config.sanitized()
        val flat = EqualizerDspModel.isFlat(settingsSnapshot)
        targetWetMix = if (flat) 0f else 1f
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
        targetBassBoostDb = BassBoostProcessorModel.normalizedAmountToBoostDb(bassAmount, bassConfig)
        targetBassPregainDb = BassBoostProcessorModel.pregainDbForBoost(targetBassBoostDb, bassConfig)
        targetTrebleDb = settingsSnapshot.treble.coerceIn(-1f, 1f) * safeConfig.trebleMaxBoostDb
        val spaciousnessAmount = settingsSnapshot.spaciousness.coerceAtLeast(0f).coerceIn(0f, 1f)
        spaciousnessProcessor.setConfig(
            SpaciousnessConfig(
                enabled = spaciousnessAmount > 0.0005f,
                mode = settingsSnapshot.spaciousnessMode,
                amountNormalized = spaciousnessAmount,
                preserveBassMono = true,
                smoothingTimeMs = safeConfig.smoothingTimeMs,
            ),
        )
        targetAutoHeadroomDb = EqualizerDspModel.automaticHeadroomDb(
            bandGainsDb = targetBandGainsDb,
            bassBoostDb = targetBassBoostDb,
            bassPregainDb = targetBassPregainDb,
            trebleBoostDb = targetTrebleDb,
            spaciousnessAmount = spaciousnessAmount,
            sampleRateHz = sampleRateHz,
            config = safeConfig,
        )
        targetsDirty = false
    }

    private fun stepTowardsTargets() {
        currentWetMix = smooth(currentWetMix, targetWetMix, smoothingAlpha)
        currentBassBoostDb = smooth(currentBassBoostDb, targetBassBoostDb, smoothingAlpha)
        currentBassPregainDb = smooth(currentBassPregainDb, targetBassPregainDb, smoothingAlpha)
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
                    BiquadCoefficients.peakingByBandwidth(
                        sampleRateHz = sampleRateHz.toFloat(),
                        centerFrequencyHz = frequencyHz,
                        bandwidthOctaves = EqualizerDspModel.bandBandwidthOctaves(bandIndex, safeConfig),
                        gainDb = currentBandGainsDb[bandIndex],
                        fallbackQ = safeConfig.bandQ,
                    )
                } else {
                    BiquadCoefficients.identity()
                }
                bandFilters[channelIndex][bandIndex].setCoefficients(coefficients)
            }
            bassFilters[channelIndex].setCoefficients(
                BiquadCoefficients.lowShelf(
                    sampleRateHz = sampleRateHz.toFloat(),
                    cornerFrequencyHz = safeConfig.bassConfig.shelfFrequencyHz,
                    slope = safeConfig.bassConfig.shelfSlope,
                    gainDb = currentBassBoostDb,
                ),
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
        processed = bassFilters[channelIndex].process(processed)
        processed = trebleFilters[channelIndex].process(processed)
        return processed
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
        return if (sampleAbs <= threshold) {
            limiterPeakReduction = 0f
            sample
        } else {
            val overshoot = (sampleAbs - threshold).coerceAtLeast(0f)
            val shapedAbs = threshold + (1f - exp(-(overshoot / knee).coerceAtLeast(0f).toDouble())).toFloat() * knee
            val limited = shapedAbs.coerceAtMost(1f) * if (sample >= 0f) 1f else -1f
            limiterPeakReduction = max(limiterPeakReduction, sampleAbs - abs(limited))
            limiterEvents += 1
            limited.coerceIn(-1f, 1f)
        }
    }

    internal fun debugSnapshot(): EqualizerDiagnosticsSnapshot {
        return EqualizerDiagnosticsSnapshot(
            sampleRateHz = sampleRateHz,
            computedHeadroomDb = currentAutoHeadroomDb,
            activeFilterCount = currentBandGainsDb.count { abs(it) > 0.01f } +
                if (abs(currentBassBoostDb) > 0.01f) 1 else 0 +
                if (abs(currentTrebleDb) > 0.01f) 1 else 0 +
                if (!spaciousnessProcessor.isBypassed()) 1 else 0,
            limiterPeakReduction = limiterPeakReduction,
            limiterEvents = limiterEvents,
            dspBypassed = bitPerfectBypassed || targetWetMix == 0f,
        )
    }
}

internal data class EqualizerDiagnosticsSnapshot(
    val sampleRateHz: Int,
    val computedHeadroomDb: Float,
    val activeFilterCount: Int,
    val limiterPeakReduction: Float,
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
