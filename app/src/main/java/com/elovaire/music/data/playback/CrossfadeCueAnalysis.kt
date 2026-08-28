package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import elovaire.music.droidbeauty.app.BuildConfig
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
    val outgoingFallbackReason: String? = null,
    val incomingFallbackReason: String? = null,
) {
    companion object {
        fun fallback(outgoingDurationMs: Long): CrossfadeCue = CrossfadeCue(
            outgoingMixOutMs = outgoingDurationMs.coerceAtLeast(0L),
            incomingMixInMs = 0L,
            outgoingTrailingSilenceMs = 0L,
            incomingLeadingSilenceMs = 0L,
            outgoingAnalysisSucceeded = false,
            incomingAnalysisSucceeded = false,
            outgoingFallbackReason = "controller_analysis_exception",
            incomingFallbackReason = "controller_analysis_exception",
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
    const val ANALYSIS_LEAD_MS = 60_000L
    const val STARTUP_ANALYSIS_DELAY_MS = 500L
    const val MAX_EXPANDED_ANALYSIS_MS = 60_000L
    const val COVERAGE_TOLERANCE_MS = 100L
}

internal fun crossfadeAnalysisDelayMs(
    outgoingDurationMs: Long,
    currentPositionMs: Long,
): Long {
    val durationMs = outgoingDurationMs.coerceAtLeast(0L)
    val positionMs = currentPositionMs.coerceIn(0L, durationMs)
    val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)
    if (positionMs == 0L && remainingMs <= CrossfadeCuePolicy.ANALYSIS_LEAD_MS) {
        return CrossfadeCuePolicy.STARTUP_ANALYSIS_DELAY_MS
    }
    return (remainingMs - CrossfadeCuePolicy.ANALYSIS_LEAD_MS).coerceAtLeast(0L)
}

internal data class CrossfadeLevelWindow(
    val startMs: Long,
    val endMs: Long,
    val maxChannelRms: Float,
)

internal enum class CrossfadeCueEvidence {
    Audible,
    AllSilent,
    NoUsableWindows,
}

internal data class CrossfadeCueDecision(
    val cueMs: Long,
    val silenceMs: Long,
    val evidence: CrossfadeCueEvidence,
)

internal object CrossfadeCueAlgorithm {
    fun trailingCueDecision(
        windows: List<CrossfadeLevelWindow>,
        durationMs: Long,
        analyzedStartMs: Long = 0L,
        analyzedEndMs: Long = durationMs,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): CrossfadeCueDecision {
        val duration = durationMs.coerceAtLeast(0L)
        val usableWindows = windows.filter(::isUsableWindow)
        if (usableWindows.isEmpty()) {
            return CrossfadeCueDecision(
                cueMs = duration,
                silenceMs = 0L,
                evidence = CrossfadeCueEvidence.NoUsableWindows,
            )
        }
        val lastAudible = usableWindows.asReversed().firstOrNull { it.maxChannelRms >= silenceFloor }
            ?: run {
                val silentStart = analyzedStartMs.coerceIn(0L, duration)
                return CrossfadeCueDecision(
                    cueMs = silentStart,
                    silenceMs = (duration - silentStart).coerceAtLeast(0L),
                    evidence = CrossfadeCueEvidence.AllSilent,
                )
            }
        val trailingSilence = (duration - lastAudible.endMs).coerceAtLeast(0L)
        return if (trailingSilence >= minimumSilenceMs) {
            CrossfadeCueDecision(
                cueMs = lastAudible.endMs.coerceIn(0L, duration),
                silenceMs = trailingSilence,
                evidence = CrossfadeCueEvidence.Audible,
            )
        } else {
            CrossfadeCueDecision(
                cueMs = duration,
                silenceMs = 0L,
                evidence = CrossfadeCueEvidence.Audible,
            )
        }
    }

    fun trailingCue(
        windows: List<CrossfadeLevelWindow>,
        durationMs: Long,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): Pair<Long, Long> {
        val decision = trailingCueDecision(
            windows = windows,
            durationMs = durationMs,
            silenceFloor = silenceFloor,
            minimumSilenceMs = minimumSilenceMs,
        )
        return decision.cueMs to decision.silenceMs
    }

    fun leadingCueDecision(
        windows: List<CrossfadeLevelWindow>,
        analyzedStartMs: Long = 0L,
        analyzedEndMs: Long,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): CrossfadeCueDecision {
        val usableWindows = windows.filter(::isUsableWindow)
        if (usableWindows.isEmpty()) {
            return CrossfadeCueDecision(
                cueMs = analyzedStartMs.coerceAtLeast(0L),
                silenceMs = 0L,
                evidence = CrossfadeCueEvidence.NoUsableWindows,
            )
        }
        val firstAudible = usableWindows.firstOrNull { it.maxChannelRms >= silenceFloor }
            ?: run {
                val silentEnd = analyzedEndMs.coerceAtLeast(analyzedStartMs)
                return CrossfadeCueDecision(
                    cueMs = silentEnd,
                    silenceMs = (silentEnd - analyzedStartMs).coerceAtLeast(0L),
                    evidence = CrossfadeCueEvidence.AllSilent,
                )
            }
        val leadingSilence = (firstAudible.startMs - analyzedStartMs).coerceAtLeast(0L)
        return if (leadingSilence >= minimumSilenceMs) {
            CrossfadeCueDecision(
                cueMs = firstAudible.startMs.coerceAtLeast(0L),
                silenceMs = leadingSilence,
                evidence = CrossfadeCueEvidence.Audible,
            )
        } else {
            CrossfadeCueDecision(
                cueMs = analyzedStartMs.coerceAtLeast(0L),
                silenceMs = 0L,
                evidence = CrossfadeCueEvidence.Audible,
            )
        }
    }

    fun leadingCue(
        windows: List<CrossfadeLevelWindow>,
        silenceFloor: Float = CrossfadeSilencePolicy.BASE_AMPLITUDE_THRESHOLD,
        minimumSilenceMs: Long = CrossfadeCuePolicy.MIN_TRAILING_SILENCE_MS,
    ): Pair<Long, Long> {
        val decision = leadingCueDecision(
            windows = windows,
            analyzedEndMs = windows.maxOfOrNull(CrossfadeLevelWindow::endMs) ?: 0L,
            silenceFloor = silenceFloor,
            minimumSilenceMs = minimumSilenceMs,
        )
        return decision.cueMs to decision.silenceMs
    }

    private fun isUsableWindow(window: CrossfadeLevelWindow): Boolean {
        return window.endMs > window.startMs &&
            window.maxChannelRms.isFinite() &&
            window.maxChannelRms >= 0f
    }
}

private data class RegionCue(
    val cueMs: Long?,
    val silenceMs: Long,
    val failureReason: String? = null,
)

private data class DecodeRegionResult(
    val requestedStartMs: Long,
    val requestedEndMs: Long,
    val windows: List<CrossfadeLevelWindow> = emptyList(),
    val decodedFrameCount: Long = 0L,
    val firstExtractorSampleTimeUs: Long? = null,
    val firstDecoderOutputTimeUs: Long? = null,
    val lastDecoderOutputTimeUs: Long? = null,
    val inputMime: String? = null,
    val outputSampleRate: Int? = null,
    val outputChannelCount: Int? = null,
    val outputEncoding: Int? = null,
    val endOfStreamReached: Boolean = false,
    val failureReason: String? = null,
) {
    val isUsable: Boolean
        get() = failureReason == null &&
            decodedFrameCount > 0L &&
            windows.isNotEmpty() &&
            windows.last().endMs >= requestedEndMs - CrossfadeCuePolicy.COVERAGE_TOLERANCE_MS
}

private class CrossfadeDecodeFailure(
    val reason: String,
    cause: Throwable? = null,
) : RuntimeException(reason, cause)

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
        logDebug(
            "analyze_pair analyzer_silence_db=$normalizedSilenceLevelDb " +
                "detected_mix_out_ms=${outgoingCue.mixOutMs} " +
                "detected_mix_in_ms=${incomingCue.mixInMs} " +
                "trailing_silence_ms=${outgoingCue.trailingSilenceMs} " +
                "leading_silence_ms=${incomingCue.leadingSilenceMs} " +
                "analysis_success=${outgoingCue.mixOutMs != null}/${incomingCue.mixInMs != null} " +
                "fallback_reason=${outgoingCue.failureReason ?: incomingCue.failureReason ?: "none"}",
        )
        val outgoingDurationMs = outgoing.durationMs.coerceAtLeast(0L)
        return CrossfadeCue(
            outgoingMixOutMs = outgoingCue.mixOutMs ?: outgoingDurationMs,
            incomingMixInMs = incomingCue.mixInMs ?: 0L,
            outgoingTrailingSilenceMs = outgoingCue.trailingSilenceMs,
            incomingLeadingSilenceMs = incomingCue.leadingSilenceMs,
            outgoingAnalysisSucceeded = outgoingCue.mixOutMs != null,
            incomingAnalysisSucceeded = incomingCue.mixInMs != null,
            outgoingFallbackReason = outgoingCue.failureReason,
            incomingFallbackReason = incomingCue.failureReason,
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
        val durationMs = fallbackDuration.takeIf { it > 0L } ?: findDurationMs(song) ?: 0L
        if (durationMs <= 0L) {
            return SongCue(
                mixOutMs = null,
                mixInMs = null,
                trailingSilenceMs = 0L,
                leadingSilenceMs = 0L,
                failureReason = "invalid_duration",
            )
        }
        val silenceFloor = CrossfadeSilencePolicy.amplitudeThresholdForDb(silenceLevelDb)
        val tailCue = analyzeTrailingRegion(song, durationMs, silenceFloor)
        val headCue = analyzeLeadingRegion(song, durationMs, silenceFloor)
        return SongCue(
            mixOutMs = tailCue.cueMs,
            mixInMs = headCue.cueMs,
            trailingSilenceMs = tailCue.silenceMs,
            leadingSilenceMs = headCue.silenceMs,
            failureReason = tailCue.failureReason ?: headCue.failureReason,
        )
    }

    private fun analyzeTrailingRegion(
        song: Song,
        durationMs: Long,
        silenceFloor: Float,
    ): RegionCue {
        var endMs = durationMs
        var startMs = (endMs - CrossfadeCuePolicy.TAIL_ANALYSIS_WINDOW_MS).coerceAtLeast(0L)
        while (true) {
            val decoded = decodeRegion(song, startMs, endMs)
            logRegion("tail", decoded)
            if (!decoded.isUsable) {
                return RegionCue(
                    cueMs = null,
                    silenceMs = 0L,
                    failureReason = decoded.failureReason ?: "zero_usable_pcm_windows",
                )
            }
            val decision = CrossfadeCueAlgorithm.trailingCueDecision(
                windows = decoded.windows,
                durationMs = durationMs,
                analyzedStartMs = startMs,
                analyzedEndMs = endMs,
                silenceFloor = silenceFloor,
            )
            if (decision.evidence != CrossfadeCueEvidence.AllSilent) {
                return RegionCue(decision.cueMs, decision.silenceMs)
            }
            if (startMs <= 0L || durationMs - startMs >= CrossfadeCuePolicy.MAX_EXPANDED_ANALYSIS_MS) {
                logDebug(
                    "tail_all_silent analyzed_start_ms=$startMs analyzed_end_ms=$endMs " +
                        "bounded_budget_ms=${CrossfadeCuePolicy.MAX_EXPANDED_ANALYSIS_MS}",
                )
                return RegionCue(decision.cueMs, decision.silenceMs)
            }
            endMs = startMs
            startMs = (endMs - CrossfadeCuePolicy.TAIL_ANALYSIS_WINDOW_MS).coerceAtLeast(0L)
        }
    }

    private fun analyzeLeadingRegion(
        song: Song,
        durationMs: Long,
        silenceFloor: Float,
    ): RegionCue {
        var startMs = 0L
        var endMs = min(durationMs, CrossfadeCuePolicy.HEAD_ANALYSIS_WINDOW_MS)
        while (true) {
            val decoded = decodeRegion(song, startMs, endMs)
            logRegion("head", decoded)
            if (!decoded.isUsable) {
                return RegionCue(
                    cueMs = null,
                    silenceMs = 0L,
                    failureReason = decoded.failureReason ?: "zero_usable_pcm_windows",
                )
            }
            val decision = CrossfadeCueAlgorithm.leadingCueDecision(
                windows = decoded.windows,
                analyzedStartMs = 0L,
                analyzedEndMs = endMs,
                silenceFloor = silenceFloor,
            )
            if (decision.evidence != CrossfadeCueEvidence.AllSilent) {
                return RegionCue(decision.cueMs, decision.silenceMs)
            }
            if (endMs >= durationMs || endMs >= CrossfadeCuePolicy.MAX_EXPANDED_ANALYSIS_MS) {
                logDebug(
                    "head_all_silent analyzed_start_ms=$startMs analyzed_end_ms=$endMs " +
                        "bounded_budget_ms=${CrossfadeCuePolicy.MAX_EXPANDED_ANALYSIS_MS}",
                )
                return RegionCue(decision.cueMs, decision.silenceMs)
            }
            startMs = endMs
            endMs = min(
                durationMs,
                endMs + CrossfadeCuePolicy.HEAD_ANALYSIS_WINDOW_MS,
            )
        }
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
    ): DecodeRegionResult {
        if (endMs <= startMs) {
            return DecodeRegionResult(
                requestedStartMs = startMs,
                requestedEndMs = endMs,
                failureReason = "invalid_analysis_region",
            )
        }
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(appContext, song.uri, emptyMap())
            val trackIndex = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getStringOrNull(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return DecodeRegionResult(
                requestedStartMs = startMs,
                requestedEndMs = endMs,
                failureReason = "no_audio_track",
            )
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mimeType = format.getStringOrNull(MediaFormat.KEY_MIME)
                ?: return DecodeRegionResult(
                    requestedStartMs = startMs,
                    requestedEndMs = endMs,
                    failureReason = "missing_audio_mime",
                )
            extractor.seekTo(startMs * 1_000L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            val result = if (mimeType == MIME_AUDIO_RAW) {
                decodeRaw(extractor, format, startMs * 1_000L, endMs * 1_000L)
            } else {
                decodeCompressed(extractor, format, startMs * 1_000L, endMs * 1_000L)
            }
            result.copy(
                requestedStartMs = startMs,
                requestedEndMs = endMs,
                inputMime = result.inputMime ?: mimeType,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: CrossfadeDecodeFailure) {
            DecodeRegionResult(
                requestedStartMs = startMs,
                requestedEndMs = endMs,
                failureReason = failure.reason,
            )
        } catch (_: Exception) {
            DecodeRegionResult(
                requestedStartMs = startMs,
                requestedEndMs = endMs,
                failureReason = "extractor_exception",
            )
        } finally {
            runCatching { extractor.release() }
        }
    }

    private fun decodeRaw(
        extractor: MediaExtractor,
        format: MediaFormat,
        startUs: Long,
        endUs: Long,
    ): DecodeRegionResult {
        val sampleRate = format.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE)
            ?: throw CrossfadeDecodeFailure("missing_sample_rate")
        val channelCount = format.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT)
            ?: throw CrossfadeDecodeFailure("missing_channel_count")
        val encoding = format.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: C.ENCODING_PCM_16BIT
        if (!PcmEnvelopeAccumulator.supportsEncoding(encoding)) {
            throw CrossfadeDecodeFailure("unsupported_pcm_encoding")
        }
        val accumulator = PcmEnvelopeAccumulator(sampleRate, channelCount, encoding, startUs, endUs)
        val buffer = ByteBuffer.allocateDirect(RAW_BUFFER_SIZE).order(ByteOrder.nativeOrder())
        var firstSampleTimeUs: Long? = null
        var lastSampleTimeUs: Long? = null
        var endOfStreamReached = false
        while (true) {
            buffer.clear()
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) {
                endOfStreamReached = true
                break
            }
            val sampleTimeUs = extractor.sampleTime.takeIf { it >= 0L } ?: accumulator.nextTimeUs
            if (firstSampleTimeUs == null) firstSampleTimeUs = sampleTimeUs
            lastSampleTimeUs = sampleTimeUs
            buffer.limit(size)
            accumulator.append(buffer, sampleTimeUs)
            if (sampleTimeUs >= endUs || !extractor.advance()) break
        }
        return DecodeRegionResult(
            requestedStartMs = startUs / 1_000L,
            requestedEndMs = endUs / 1_000L,
            windows = accumulator.finish(),
            decodedFrameCount = accumulator.decodedFrameCount,
            firstExtractorSampleTimeUs = firstSampleTimeUs,
            lastDecoderOutputTimeUs = lastSampleTimeUs,
            outputSampleRate = sampleRate,
            outputChannelCount = channelCount,
            outputEncoding = encoding,
            endOfStreamReached = endOfStreamReached,
        )
    }

    @Suppress("NestedBlockDepth", "TooGenericExceptionCaught", "LongMethod")
    private fun decodeCompressed(
        extractor: MediaExtractor,
        inputFormat: MediaFormat,
        startUs: Long,
        endUs: Long,
    ): DecodeRegionResult {
        val mimeType = inputFormat.getStringOrNull(MediaFormat.KEY_MIME)
            ?: throw CrossfadeDecodeFailure("missing_decoder_mime")
        val codec = try {
            MediaCodec.createDecoderByType(mimeType)
        } catch (error: RuntimeException) {
            throw CrossfadeDecodeFailure("decoder_creation_failure", error)
        }
        val info = MediaCodec.BufferInfo()
        var outputFormat = inputFormat
        var inputEnded = false
        var outputEnded = false
        val deadline = SystemClock.elapsedRealtime() + MAX_DECODE_WALL_TIME_MS
        var accumulator: PcmEnvelopeAccumulator? = null
        var firstInputSampleTimeUs: Long? = null
        var firstOutputTimeUs: Long? = null
        var lastOutputTimeUs: Long? = null
        try {
            try {
                codec.configure(inputFormat, null, null, 0)
                codec.start()
            } catch (error: RuntimeException) {
                throw CrossfadeDecodeFailure("decoder_configure_or_start_failure", error)
            }
            while (!outputEnded) {
                if (SystemClock.elapsedRealtime() > deadline) {
                    throw CrossfadeDecodeFailure("decoder_timeout")
                }
                if (!inputEnded) {
                    val inputIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                            ?: throw CrossfadeDecodeFailure("missing_decoder_input_buffer")
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
                            if (firstInputSampleTimeUs == null) firstInputSampleTimeUs = timeUs
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
                                val hasMoreSamples = extractor.advance()
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    size,
                                    timeUs,
                                    if (hasMoreSamples) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                                )
                                if (!hasMoreSamples) inputEnded = true
                            }
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(info, CODEC_TIMEOUT_US)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        outputFormat = codec.outputFormat
                        accumulator = PcmEnvelopeAccumulator.from(outputFormat, startUs, endUs)
                            ?: throw CrossfadeDecodeFailure("missing_decoder_output_format")
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    else -> if (outputIndex >= 0) {
                        if (info.size > 0) {
                            val outputBuffer = codec.getOutputBuffer(outputIndex)
                                ?: throw CrossfadeDecodeFailure("missing_decoder_output_buffer")
                            outputBuffer.position(info.offset)
                            outputBuffer.limit(info.offset + info.size)
                            val currentAccumulator = accumulator
                                ?: PcmEnvelopeAccumulator.from(outputFormat, startUs, endUs)
                                    ?.also { accumulator = it }
                                ?: throw CrossfadeDecodeFailure("missing_decoder_output_format")
                            if (firstOutputTimeUs == null) firstOutputTimeUs = info.presentationTimeUs
                            lastOutputTimeUs = info.presentationTimeUs
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
            val decodedAccumulator = accumulator
                ?: throw CrossfadeDecodeFailure("zero_decoder_output")
            val windows = decodedAccumulator.finish()
            if (decodedAccumulator.decodedFrameCount <= 0L) {
                throw CrossfadeDecodeFailure("zero_decoded_pcm_frames")
            }
            return DecodeRegionResult(
                requestedStartMs = startUs / 1_000L,
                requestedEndMs = endUs / 1_000L,
                windows = windows,
                decodedFrameCount = decodedAccumulator.decodedFrameCount,
                firstExtractorSampleTimeUs = firstInputSampleTimeUs,
                firstDecoderOutputTimeUs = firstOutputTimeUs,
                lastDecoderOutputTimeUs = lastOutputTimeUs,
                inputMime = mimeType,
                outputSampleRate = outputFormat.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE),
                outputChannelCount = outputFormat.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                outputEncoding = outputFormat.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING)
                    ?: C.ENCODING_PCM_16BIT,
                endOfStreamReached = outputEnded,
            )
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
        }
    }

    private fun logRegion(label: String, result: DecodeRegionResult) {
        logDebug(
            "region=$label requested_start_ms=${result.requestedStartMs} " +
                "requested_end_ms=${result.requestedEndMs} first_extractor_sample_us=${result.firstExtractorSampleTimeUs} " +
                "first_decoder_output_us=${result.firstDecoderOutputTimeUs} " +
                "last_decoder_output_us=${result.lastDecoderOutputTimeUs} " +
                "input_mime=${result.inputMime ?: "unknown"} sample_rate=${result.outputSampleRate} " +
                "channels=${result.outputChannelCount} encoding=${result.outputEncoding} " +
                "first_accumulator_window_ms=${result.windows.firstOrNull()?.startMs} " +
                "last_accumulator_window_ms=${result.windows.lastOrNull()?.endMs} " +
                "windows=${result.windows.size} decoded_frames=${result.decodedFrameCount} " +
                "eos=${result.endOfStreamReached} usable=${result.isUsable} " +
                "failure_reason=${result.failureReason ?: "none"}",
        )
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
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
        val failureReason: String? = null,
    )

    private companion object {
        const val TAG = "CrossfadeCueAnalyzer"
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
    private var totalFrameCount = 0L
    private var windowStartUs = Long.MIN_VALUE
    var nextTimeUs: Long = regionStartUs
        private set
    val decodedFrameCount: Long
        get() = totalFrameCount

    companion object {
        fun supportsEncoding(encoding: Int): Boolean {
            return encoding == C.ENCODING_PCM_8BIT ||
                encoding == C.ENCODING_PCM_16BIT ||
                encoding == C.ENCODING_PCM_24BIT ||
                encoding == C.ENCODING_PCM_32BIT ||
                encoding == C.ENCODING_PCM_FLOAT
        }

        fun from(format: MediaFormat, startUs: Long, endUs: Long): PcmEnvelopeAccumulator? {
            val sampleRate = format.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: return null
            val channels = format.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: return null
            val encoding = format.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: C.ENCODING_PCM_16BIT
            if (!supportsEncoding(encoding)) return null
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
        var framesProcessed = 0L
        while (sampleBuffer.remaining() >= bytesPerFrame &&
            timestampUs + (framesProcessed * 1_000_000L / sampleRate) < regionEndUs
        ) {
            val frameEndUs = timestampUs + ((framesProcessed + 1L) * 1_000_000L / sampleRate)
            var channel = 0
            while (channel < channelCount) {
                val sample = readSample(sampleBuffer)
                sumSquares[channel] += sample * sample
                channel += 1
            }
            frameCount += 1
            totalFrameCount += 1L
            framesProcessed += 1L
            if (frameCount >= windowFrames) flushWindow(frameEndUs)
        }
        nextTimeUs = max(nextTimeUs, timestampUs + (framesProcessed * 1_000_000L / sampleRate))
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
        C.ENCODING_PCM_FLOAT -> buffer.float.toDouble()
            .takeIf(Double::isFinite)
            ?.coerceIn(-1.0, 1.0)
            ?.let(::abs)
            ?: 0.0
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
