package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.provider.MediaStore
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal class AndroidChromaprintFingerprintProvider(
    context: Context,
    private val cache: TagMatchCache,
) : AudioFingerprintProvider {
    private val appContext = context.applicationContext
    private val formatDetector = AudioFormatDetector(appContext)

    override suspend fun fingerprint(song: Song): Result<AudioFingerprint> = withContext(Dispatchers.IO) {
        try {
            if (!AudioFormatPolicy.canFingerprint(song.fileName)) {
                throw UnsupportedFingerprintFormat()
            }
            val detected = formatDetector.detect(song.uri, song.fileName, null)
            if (
                !detected.hasAudioTrack ||
                detected.hasVideoTrack ||
                AudioFormatPolicy.playbackSupport(detected) == PlaybackSupport.Unsupported
            ) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Fingerprint skipped: unsupported decode path")
                }
                throw UnsupportedFingerprintFormat()
            }
            val signature = fileSignature(song)
            val cached = cache.getFingerprint(signature)
            if (!cached.isNullOrBlank()) {
                return@withContext Result.success(
                    AudioFingerprint(
                        songId = song.id,
                        durationSeconds = (song.durationMs / 1_000L).coerceAtLeast(1L).toInt(),
                        fingerprint = cached,
                        fileSignature = signature,
                    ),
                )
            }
            val generated = decodeFingerprint(song)
            cache.putFingerprint(signature, generated)
            Result.success(
                AudioFingerprint(
                    songId = song.id,
                    durationSeconds = (song.durationMs / 1_000L).coerceAtLeast(1L).toInt(),
                    fingerprint = generated,
                    fileSignature = signature,
                ),
            )
        } catch (throwable: CancellationException) {
            throw throwable
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

    private fun fileSignature(song: Song): String {
        var size = 0L
        var modified = 0L
        appContext.contentResolver.query(
            song.uri,
            arrayOf(MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATE_MODIFIED),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                size = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    .takeIf { it >= 0 }
                    ?.let(cursor::getLong)
                    ?: 0L
                modified = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                    .takeIf { it >= 0 }
                    ?.let(cursor::getLong)
                    ?: 0L
            }
        }
        return listOf(song.id, song.uri, size, modified, song.durationMs).joinToString(":")
    }

    private suspend fun decodeFingerprint(song: Song): String {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        val nativeSession = NativeChromaprintSession.open()
            ?: error("Chromaprint is unavailable on this device.")
        return try {
            extractor.setDataSource(appContext, song.uri, emptyMap())
            val trackIndex = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: error("No decodable audio track found.")
            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: error("Audio codec is unknown.")
            runCatching { inputFormat.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT) }
            codec = MediaCodec.createDecoderByType(mime).apply {
                configure(inputFormat, null, null, 0)
                start()
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var inputEnded = false
            var outputEnded = false
            var bridgeStarted = false
            var outputSampleRate = inputFormat.integerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: 44_100
            var outputChannels = inputFormat.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: 2
            var outputEncoding = AudioFormat.ENCODING_PCM_16BIT

            while (!outputEnded) {
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                if (!inputEnded) {
                    val inputIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex) ?: error("Decoder input buffer unavailable.")
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                extractor.sampleTime.coerceAtLeast(0L),
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                            )
                            inputEnded = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(bufferInfo, CODEC_TIMEOUT_US)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outputFormat = codec.outputFormat
                        outputSampleRate = outputFormat.integerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: outputSampleRate
                        outputChannels = outputFormat.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: outputChannels
                        outputEncoding = outputFormat.integerOrNull(MediaFormat.KEY_PCM_ENCODING) ?: outputEncoding
                    }

                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    else -> if (outputIndex >= 0) {
                        if (!bridgeStarted) {
                            check(nativeSession.start(outputSampleRate, outputChannels)) {
                                "Unable to initialize Chromaprint."
                            }
                            bridgeStarted = true
                        }
                        codec.getOutputBuffer(outputIndex)?.let { outputBuffer ->
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            val samples = outputBuffer.toPcm16(outputEncoding)
                            if (samples.isNotEmpty()) {
                                check(nativeSession.feed(samples, samples.size)) {
                                    "Unable to process decoded audio."
                                }
                            }
                        }
                        outputEnded = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                        codec.releaseOutputBuffer(outputIndex, false)
                    }
                }
            }
            check(bridgeStarted) { "No PCM audio was decoded." }
            nativeSession.finish()?.takeIf(String::isNotBlank)
                ?: error("Chromaprint did not produce a fingerprint.")
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor.release() }
            nativeSession.close()
        }
    }

    private fun java.nio.ByteBuffer.toPcm16(encoding: Int): ShortArray {
        order(ByteOrder.LITTLE_ENDIAN)
        return when (encoding) {
            AudioFormat.ENCODING_PCM_FLOAT -> {
                val values = asFloatBuffer()
                ShortArray(values.remaining()) { index ->
                    (values.get(index).coerceIn(-1f, 1f) * Short.MAX_VALUE).roundToInt().toShort()
                }
            }

            else -> {
                val values = asShortBuffer()
                ShortArray(values.remaining()).also(values::get)
            }
        }
    }

    private fun MediaFormat.integerOrNull(key: String): Int? {
        return runCatching { getInteger(key) }.getOrNull()
    }

    private companion object {
        const val CODEC_TIMEOUT_US = 10_000L
        const val TAG = "AudioFingerprint"
    }
}

internal class UnsupportedFingerprintFormat :
    IllegalArgumentException("Fingerprinting is unavailable for this audio file.")

private class NativeChromaprintBridge {
    init {
        System.loadLibrary("elovaire_chromaprint")
    }

    fun create(): Long = nativeCreate()
    fun start(handle: Long, sampleRate: Int, channels: Int): Boolean {
        if (!ChromaprintInputPolicy.acceptsStart(handle, sampleRate, channels)) return false
        return nativeStart(handle, sampleRate, channels)
    }

    fun feed(handle: Long, samples: ShortArray, length: Int): Boolean {
        if (!ChromaprintInputPolicy.acceptsFeed(handle, samples.size, length)) return false
        return nativeFeed(handle, samples, length)
    }

    fun finish(handle: Long): String? = if (handle > 0L) nativeFinish(handle) else null

    fun destroy(handle: Long) {
        if (handle > 0L) nativeDestroy(handle)
    }

    private external fun nativeCreate(): Long
    private external fun nativeStart(handle: Long, sampleRate: Int, channels: Int): Boolean
    private external fun nativeFeed(handle: Long, samples: ShortArray, length: Int): Boolean
    private external fun nativeFinish(handle: Long): String?
    private external fun nativeDestroy(handle: Long)
}

internal class NativeChromaprintSession private constructor(
    private val bridge: NativeChromaprintBridge,
    private var handle: Long,
) : AutoCloseable {
    fun start(sampleRate: Int, channels: Int): Boolean {
        val activeHandle = handle
        return activeHandle > 0L && bridge.start(activeHandle, sampleRate, channels)
    }

    fun feed(samples: ShortArray, length: Int): Boolean {
        val activeHandle = handle
        return activeHandle > 0L && bridge.feed(activeHandle, samples, length)
    }

    fun finish(): String? {
        val activeHandle = handle
        return if (activeHandle > 0L) bridge.finish(activeHandle) else null
    }

    override fun close() {
        val activeHandle = handle
        if (activeHandle <= 0L) return
        handle = 0L
        bridge.destroy(activeHandle)
    }

    companion object {
        fun open(): NativeChromaprintSession? {
            val bridge = NativeChromaprintBridge()
            val handle = bridge.create()
            return handle.takeIf { it > 0L }?.let { NativeChromaprintSession(bridge, it) }
        }
    }
}

internal object ChromaprintInputPolicy {
    fun acceptsStart(handle: Long, sampleRate: Int, channels: Int): Boolean {
        return handle > 0L && sampleRate in 1..MAX_SAMPLE_RATE && channels in 1..MAX_CHANNELS
    }

    fun acceptsFeed(handle: Long, arraySize: Int, length: Int): Boolean {
        return handle > 0L && length in 1..minOf(arraySize, MAX_SAMPLES_PER_FEED)
    }

    private const val MAX_SAMPLE_RATE = 768_000
    private const val MAX_CHANNELS = 32
    private const val MAX_SAMPLES_PER_FEED = 16 * 1024 * 1024
}
