package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.LinkedHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal data class CrossfadeCue(
    val outgoingMixOutMs: Long,
    val incomingMixInMs: Long,
    val outgoingTrailingSilenceMs: Long,
    val incomingLeadingSilenceMs: Long,
    val outgoingAnalysisSucceeded: Boolean,
    val incomingAnalysisSucceeded: Boolean,
) {
    companion object {
        fun fallback(outgoingDurationMs: Long): CrossfadeCue = CrossfadeCue(
            outgoingMixOutMs = outgoingDurationMs.coerceAtLeast(0L),
            incomingMixInMs = 0L,
            outgoingTrailingSilenceMs = 0L,
            incomingLeadingSilenceMs = 0L,
            outgoingAnalysisSucceeded = false,
            incomingAnalysisSucceeded = false,
        )
    }
}

internal data class CrossfadeTransitionPlan(
    val outgoingMixOutMs: Long,
    val fadeStartMs: Long,
    val fadeDurationMs: Long,
    val incomingMixInMs: Long,
) {
    companion object {
        fun from(
            cue: CrossfadeCue,
            outgoingDurationMs: Long,
            incomingDurationMs: Long = 0L,
            fadeDurationMs: Long = CrossfadeDurationPolicy.DEFAULT_DURATION_MS,
        ): CrossfadeTransitionPlan {
            val duration = outgoingDurationMs.coerceAtLeast(0L)
            val mixOut = cue.outgoingMixOutMs.coerceIn(0L, duration)
            val incomingMixIn = cue.incomingMixInMs.coerceAtLeast(0L)
            val incomingAvailable = if (incomingDurationMs > 0L) {
                (incomingDurationMs - incomingMixIn).coerceAtLeast(0L)
            } else {
                Long.MAX_VALUE
            }
            val fadeDuration = min(
                min(CrossfadeDurationPolicy.sanitize(fadeDurationMs), mixOut),
                incomingAvailable,
            ).coerceAtLeast(0L)
            return CrossfadeTransitionPlan(
                outgoingMixOutMs = mixOut,
                fadeStartMs = (mixOut - fadeDuration).coerceAtLeast(0L),
                fadeDurationMs = fadeDuration,
                incomingMixInMs = incomingMixIn,
            )
        }
    }
}

internal object CrossfadeCuePolicy {
    const val TAIL_ANALYSIS_WINDOW_MS = 20_000L
    const val HEAD_ANALYSIS_WINDOW_MS = 5_000L
    const val LEVEL_WINDOW_MS = 20L
    const val MIN_TRAILING_SILENCE_MS = 100L
    const val PREWARM_LEAD_MS = 5_000L
}

internal data class CrossfadeLevelWindow(
    val startMs: Long,
    val endMs: Long,
    val maxChannelRms: Float,
)

internal object CrossfadeCueAlgorithm {
    fun trailingCue(
        windows: List<CrossfadeLevelWindow>,
        durationMs: Long,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): Pair<Long, Long> {
        val duration = durationMs.coerceAtLeast(0L)
        val lastAudible = windows.asReversed().firstOrNull { it.maxChannelRms >= silenceFloor }
            ?: return duration to 0L
        val trailingSilence = (duration - lastAudible.endMs).coerceAtLeast(0L)
        return if (trailingSilence >= minimumSilenceMs) {
            lastAudible.endMs.coerceIn(0L, duration) to trailingSilence
        } else {
            duration to 0L
        }
    }

    fun leadingCue(
        windows: List<CrossfadeLevelWindow>,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): Pair<Long, Long> {
        val firstAudible = windows.firstOrNull { it.maxChannelRms >= silenceFloor }
            ?: return 0L to 0L
        val leadingSilence = firstAudible.startMs.coerceAtLeast(0L)
        return if (leadingSilence >= minimumSilenceMs) {
            leadingSilence to leadingSilence
        } else {
            0L to 0L
        }
    }
}

internal class CrossfadeCueAnalyzer(
    context: Context,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val appContext = context.applicationContext
    private val cache = Collections.synchronizedMap(
        object : LinkedHashMap<CacheKey, SongCue>(MAX_CACHE_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, SongCue>?): Boolean {
                return size > MAX_CACHE_ENTRIES
            }
        },
    )
    private val inFlight = mutableMapOf<CacheKey, Deferred<SongCue>>()

    suspend fun analyzePair(
        outgoing: Song,
        incoming: Song,
        silenceLevelDb: Float = CrossfadeSilencePolicy.BASE_LEVEL_DB,
    ): CrossfadeCue {
        val normalizedSilenceLevelDb = CrossfadeSilencePolicy.sanitizeLevelDb(silenceLevelDb)
        val outgoingCue = analyzeSong(outgoing, normalizedSilenceLevelDb).await()
        val incomingCue = analyzeSong(incoming, normalizedSilenceLevelDb).await()
        val outgoingDurationMs = outgoing.durationMs.coerceAtLeast(0L)
        return CrossfadeCue(
            outgoingMixOutMs = outgoingCue.mixOutMs ?: outgoingDurationMs,
            incomingMixInMs = incomingCue.mixInMs ?: 0L,
            outgoingTrailingSilenceMs = outgoingCue.trailingSilenceMs,
            incomingLeadingSilenceMs = incomingCue.leadingSilenceMs,
            outgoingAnalysisSucceeded = outgoingCue.mixOutMs != null,
            incomingAnalysisSucceeded = incomingCue.mixInMs != null,
        )
    }

    @VisibleForTesting
    internal fun clearCache() {
        cache.clear()
    }

    private fun analyzeSong(song: Song, silenceLevelDb: Float): Deferred<SongCue> {
        val key = CacheKey(
            uri = song.uri.toString(),
            durationMs = song.durationMs,
            dateModifiedSeconds = song.dateModifiedSeconds,
            fileName = song.fileName,
            silenceLevelDb = silenceLevelDb.toInt(),
        )
        cache[key]?.let { return scope.async { it } }
        synchronized(inFlight) {
            cache[key]?.let { return scope.async { it } }
            inFlight[key]?.let { return it }
            return scope.async(dispatcher) {
                analyzeSongUncached(song, silenceLevelDb)
            }.also { deferred ->
                inFlight[key] = deferred
                deferred.invokeOnCompletion {
                    synchronized(inFlight) {
                        inFlight.remove(key)
                    }
                    if (!deferred.isCancelled) {
                        scope.launch {
                            try {
                                cache[key] = deferred.await()
                            } catch (_: CancellationException) {
                                // The owning playback transition was cancelled.
                            } catch (_: Exception) {
                                // An unsupported or corrupt source is not cacheable.
                            }
                        }
                    }
                }
            }
        }
    }

    private fun analyzeSongUncached(song: Song, silenceLevelDb: Float): SongCue {
        val fallbackDuration = song.durationMs.coerceAtLeast(0L)
        val durationMs = findDurationMs(song) ?: fallbackDuration
        if (durationMs <= 0L) return SongCue(null, null, 0L, 0L)

        val tailStartMs = (durationMs - CrossfadeCuePolicy.TAIL_ANALYSIS_WINDOW_MS).coerceAtLeast(0L)
        val headEndMs = min(durationMs, CrossfadeCuePolicy.HEAD_ANALYSIS_WINDOW_MS)
        val tailWindows = decodeRegion(song, tailStartMs, durationMs)
        val headWindows = decodeRegion(song, 0L, headEndMs)
        val silenceFloor = CrossfadeSilencePolicy.amplitudeThresholdForDb(silenceLevelDb)
        val (mixOutMs, trailingSilenceMs) = tailWindows?.let {
            CrossfadeCueAlgorithm.trailingCue(it, durationMs, silenceFloor)
        } ?: (null to 0L)
        val (mixInMs, leadingSilenceMs) = headWindows?.let {
            CrossfadeCueAlgorithm.leadingCue(it, silenceFloor)
        } ?: (null to 0L)
        return SongCue(mixOutMs, mixInMs, trailingSilenceMs, leadingSilenceMs)
    }

    private fun findDurationMs(song: Song): Long? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(appContext, song.uri, emptyMap())
            val durationUs = (0 until extractor.trackCount)
                .asSequence()
                .map(extractor::getTrackFormat)
                .firstOrNull { it.getStringOrNull(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
                ?.getLongOrNull(MediaFormat.KEY_DURATION)
            durationUs?.takeIf { it > 0L }?.div(1_000L)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            runCatching { extractor.release() }
        }
    }

    private fun decodeRegion(
        song: Song,
        startMs: Long,
        endMs: Long,
    ): List<CrossfadeLevelWindow>? {
        if (endMs <= startMs) return emptyList()
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(appContext, song.uri, emptyMap())
            val trackIndex = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getStringOrNull(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return null
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            extractor.seekTo(startMs * 1_000L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            if (format.getStringOrNull(MediaFormat.KEY_MIME) == MIME_AUDIO_RAW) {
                decodeRaw(extractor, format, startMs * 1_000L, endMs * 1_000L)
            } else {
                decodeCompressed(extractor, format, startMs * 1_000L, endMs * 1_000L)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            runCatching { extractor.release() }
        }
    }

    private fun decodeRaw(
        extractor: MediaExtractor,
        format: MediaFormat,
        startUs: Long,
        endUs: Long,
    ): List<CrossfadeLevelWindow>? {
        val sampleRate = format.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: return null
        val channelCount = format.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: return null
        val encoding = format.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: C.ENCODING_PCM_16BIT
        val accumulator = PcmEnvelopeAccumulator(sampleRate, channelCount, encoding, startUs, endUs)
        val buffer = ByteBuffer.allocateDirect(RAW_BUFFER_SIZE).order(ByteOrder.nativeOrder())
        while (true) {
            buffer.clear()
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) break
            val sampleTimeUs = extractor.sampleTime.takeIf { it >= 0L } ?: accumulator.nextTimeUs
            buffer.limit(size)
            accumulator.append(buffer, sampleTimeUs)
            if (sampleTimeUs >= endUs || !extractor.advance()) break
        }
        return accumulator.finish()
    }

    @Suppress("NestedBlockDepth")
    private fun decodeCompressed(
        extractor: MediaExtractor,
        inputFormat: MediaFormat,
        startUs: Long,
        endUs: Long,
    ): List<CrossfadeLevelWindow>? {
        val mimeType = inputFormat.getStringOrNull(MediaFormat.KEY_MIME) ?: return null
        val codec = MediaCodec.createDecoderByType(mimeType)
        val info = MediaCodec.BufferInfo()
        var outputFormat = inputFormat
        var inputEnded = false
        var outputEnded = false
        val deadline = SystemClock.elapsedRealtime() + MAX_DECODE_WALL_TIME_MS
        var accumulator: PcmEnvelopeAccumulator? = null
        try {
            codec.configure(inputFormat, null, null, 0)
            codec.start()
            while (!outputEnded) {
                if (SystemClock.elapsedRealtime() > deadline) return null
                if (!inputEnded) {
                    val inputIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex) ?: return null
                        inputBuffer.clear()
                        val size = extractor.readSampleData(inputBuffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                            )
                            inputEnded = true
                        } else {
                            val timeUs = extractor.sampleTime.takeIf { it >= 0L } ?: startUs
                            if (timeUs >= endUs) {
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    0,
                                    timeUs,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                                )
                                inputEnded = true
                            } else {
                                codec.queueInputBuffer(inputIndex, 0, size, timeUs, 0)
                                if (!extractor.advance()) inputEnded = true
                            }
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(info, CODEC_TIMEOUT_US)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        outputFormat = codec.outputFormat
                        accumulator = PcmEnvelopeAccumulator.from(outputFormat, startUs, endUs)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    else -> if (outputIndex >= 0) {
                        if (info.size > 0) {
                            val outputBuffer = codec.getOutputBuffer(outputIndex) ?: return null
                            outputBuffer.position(info.offset)
                            outputBuffer.limit(info.offset + info.size)
                            val currentAccumulator = accumulator
                                ?: PcmEnvelopeAccumulator.from(outputFormat, startUs, endUs)
                                    ?.also { accumulator = it }
                                ?: return null
                            currentAccumulator.append(
                                outputBuffer,
                                info.presentationTimeUs.takeIf { it >= 0L } ?: currentAccumulator.nextTimeUs,
                            )
                        }
                        outputEnded = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                        codec.releaseOutputBuffer(outputIndex, false)
                    }
                }
            }
            return accumulator?.finish()
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
        }
    }

    private data class CacheKey(
        val uri: String,
        val durationMs: Long,
        val dateModifiedSeconds: Long?,
        val fileName: String,
        val silenceLevelDb: Int,
    )

    private data class SongCue(
        val mixOutMs: Long?,
        val mixInMs: Long?,
        val trailingSilenceMs: Long,
        val leadingSilenceMs: Long,
    )

    private companion object {
        const val MAX_CACHE_ENTRIES = 16
        const val RAW_BUFFER_SIZE = 64 * 1024
        const val CODEC_TIMEOUT_US = 10_000L
        const val MAX_DECODE_WALL_TIME_MS = 10_000L
        const val MIME_AUDIO_RAW = "audio/raw"
    }
}

internal class PcmEnvelopeAccumulator(
    private val sampleRate: Int,
    private val channelCount: Int,
    private val encoding: Int,
    private val regionStartUs: Long,
    private val regionEndUs: Long,
) {
    private val windows = ArrayList<CrossfadeLevelWindow>()
    private val sumSquares = DoubleArray(channelCount.coerceAtLeast(1))
    private val windowFrames = max(1, sampleRate * CrossfadeCuePolicy.LEVEL_WINDOW_MS / 1_000)
    private var frameCount = 0
    private var windowStartUs = Long.MIN_VALUE
    var nextTimeUs: Long = regionStartUs
        private set

    companion object {
        fun from(format: MediaFormat, startUs: Long, endUs: Long): PcmEnvelopeAccumulator? {
            val sampleRate = format.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: return null
            val channels = format.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: return null
            val encoding = format.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: C.ENCODING_PCM_16BIT
            return PcmEnvelopeAccumulator(sampleRate, channels, encoding, startUs, endUs)
        }
    }

    fun append(buffer: ByteBuffer, presentationTimeUs: Long) {
        if (sampleRate <= 0 || channelCount <= 0 || presentationTimeUs >= regionEndUs) return
        val bytesPerSample = bytesPerSample(encoding) ?: return
        val bytesPerFrame = bytesPerSample * channelCount
        if (buffer.remaining() < bytesPerFrame) return
        val sampleBuffer = buffer.duplicate().order(ByteOrder.nativeOrder())
        var timestampUs = max(presentationTimeUs, regionStartUs)
        if (presentationTimeUs < regionStartUs) {
            val skipFrames = ((regionStartUs - presentationTimeUs) * sampleRate / 1_000_000L)
                .coerceAtLeast(0L)
                .coerceAtMost((sampleBuffer.remaining() / bytesPerFrame).toLong())
            repeat(skipFrames.toInt()) {
                repeat(channelCount) { readSample(sampleBuffer) }
            }
            timestampUs = regionStartUs
        }
        if (windowStartUs == Long.MIN_VALUE) windowStartUs = timestampUs
        while (sampleBuffer.remaining() >= bytesPerFrame && timestampUs < regionEndUs) {
            var channel = 0
            while (channel < channelCount) {
                val sample = readSample(sampleBuffer)
                sumSquares[channel] += sample * sample
                channel += 1
            }
            frameCount += 1
            timestampUs += 1_000_000L / sampleRate
            if (frameCount >= windowFrames) flushWindow(timestampUs)
        }
        nextTimeUs = max(nextTimeUs, timestampUs)
    }

    fun finish(): List<CrossfadeLevelWindow> {
        if (frameCount > 0) flushWindow(nextTimeUs)
        return windows
    }

    private fun flushWindow(endUs: Long) {
        val frames = frameCount
        if (frames <= 0 || windowStartUs == Long.MIN_VALUE) return
        var maxRms = 0f
        for (channel in 0 until channelCount) {
            maxRms = max(maxRms, sqrt(sumSquares[channel] / frames).toFloat())
            sumSquares[channel] = 0.0
        }
        windows += CrossfadeLevelWindow(
            startMs = windowStartUs / 1_000L,
            endMs = min(regionEndUs, endUs) / 1_000L,
            maxChannelRms = maxRms,
        )
        frameCount = 0
        windowStartUs = endUs
    }

    private fun bytesPerSample(encoding: Int): Int? = when (encoding) {
        C.ENCODING_PCM_8BIT -> 1
        C.ENCODING_PCM_16BIT -> 2
        C.ENCODING_PCM_24BIT -> 3
        C.ENCODING_PCM_32BIT, C.ENCODING_PCM_FLOAT -> 4
        else -> null
    }

    private fun readSample(buffer: ByteBuffer): Double = when (encoding) {
        C.ENCODING_PCM_8BIT -> ((buffer.get().toInt() and 0xff) - 128) / 128.0
        C.ENCODING_PCM_16BIT -> abs(buffer.short.toDouble() / 32_768.0)
        C.ENCODING_PCM_24BIT -> {
            val value = (buffer.get().toInt() and 0xff) or
                ((buffer.get().toInt() and 0xff) shl 8) or
                (buffer.get().toInt() shl 16)
            abs(value / 8_388_608.0)
        }
        C.ENCODING_PCM_32BIT -> abs(buffer.int.toDouble() / 2_147_483_648.0)
        C.ENCODING_PCM_FLOAT -> abs(buffer.float.toDouble())
        else -> 0.0
    }
}

private fun MediaFormat.getStringOrNull(key: String): String? = runCatching {
    if (containsKey(key)) getString(key) else null
}.getOrNull()

private fun MediaFormat.getIntegerOrNull(key: String): Int? = runCatching {
    if (containsKey(key)) getInteger(key) else null
}.getOrNull()

private fun MediaFormat.getLongOrNull(key: String): Long? = runCatching {
    if (containsKey(key)) getLong(key) else null
}.getOrNull()
