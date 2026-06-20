package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal class AndroidChromaprintFingerprintProvider(
    context: Context,
    private val cache: TagMatchCache,
) : AudioFingerprintProvider {
    private val appContext = context.applicationContext

    override suspend fun fingerprint(song: Song): Result<AudioFingerprint> = withContext(Dispatchers.IO) {
        runCatching {
            val signature = fileSignature(song)
            val cached = cache.getFingerprint(signature)
            if (!cached.isNullOrBlank()) {
                return@runCatching AudioFingerprint(
                    songId = song.id,
                    durationSeconds = (song.durationMs / 1_000L).coerceAtLeast(1L).toInt(),
                    fingerprint = cached,
                    fileSignature = signature,
                )
            }
            val generated = decodeFingerprint(song)
            cache.putFingerprint(signature, generated)
            AudioFingerprint(
                songId = song.id,
                durationSeconds = (song.durationMs / 1_000L).coerceAtLeast(1L).toInt(),
                fingerprint = generated,
                fileSignature = signature,
            )
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
        val bridge = NativeChromaprintBridge()
        val nativeHandle = bridge.create()
        check(nativeHandle != 0L) { "Chromaprint is unavailable on this device." }
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
                            check(bridge.start(nativeHandle, outputSampleRate, outputChannels)) {
                                "Unable to initialize Chromaprint."
                            }
                            bridgeStarted = true
                        }
                        codec.getOutputBuffer(outputIndex)?.let { outputBuffer ->
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            val samples = outputBuffer.toPcm16(outputEncoding)
                            if (samples.isNotEmpty()) {
                                check(bridge.feed(nativeHandle, samples, samples.size)) {
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
            bridge.finish(nativeHandle)?.takeIf(String::isNotBlank)
                ?: error("Chromaprint did not produce a fingerprint.")
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor.release() }
            bridge.destroy(nativeHandle)
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
    }
}

internal class NativeChromaprintBridge {
    init {
        System.loadLibrary("elovaire_chromaprint")
    }

    fun create(): Long = nativeCreate()
    fun start(handle: Long, sampleRate: Int, channels: Int): Boolean = nativeStart(handle, sampleRate, channels)
    fun feed(handle: Long, samples: ShortArray, length: Int): Boolean = nativeFeed(handle, samples, length)
    fun finish(handle: Long): String? = nativeFinish(handle)
    fun destroy(handle: Long) = nativeDestroy(handle)

    private external fun nativeCreate(): Long
    private external fun nativeStart(handle: Long, sampleRate: Int, channels: Int): Boolean
    private external fun nativeFeed(handle: Long, samples: ShortArray, length: Int): Boolean
    private external fun nativeFinish(handle: Long): String?
    private external fun nativeDestroy(handle: Long)
}
