package elovaire.music.droidbeauty.app.data.audio

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.util.Locale

internal data class DetectedAudioFormat(
    val container: AudioContainerFormat,
    val displayName: String,
    val mimeType: String?,
    val codecMimeType: String?,
    val detectionSucceeded: Boolean,
    val hasAudioTrack: Boolean,
    val hasVideoTrack: Boolean,
    val decoderAvailable: Boolean?,
    val sampleRate: Int?,
    val channelCount: Int?,
    val bitrate: Int?,
    val bitDepth: Int?,
    val durationMs: Long? = null,
    val evidence: DetectionEvidence = DetectionEvidence.ExtensionFallback,
)

internal enum class DetectionEvidence {
    Extractor,
    ExtensionFallback,
}

internal data class DeviceCodecProbeKey(
    val mimeType: String,
    val sampleRate: Int?,
    val channelCount: Int?,
)

internal class AudioFormatDetector(context: Context) {
    private val appContext = context.applicationContext
    private val decoderAvailabilityCache = mutableMapOf<DeviceCodecProbeKey, Boolean>()

    fun detect(uri: Uri, fileName: String, mediaStoreMimeType: String?): DetectedAudioFormat {
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(appContext, uri, emptyMap())
            var audioFormat: MediaFormat? = null
            var hasVideo = false
            for (trackIndex in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(trackIndex)
                val trackMimeType = trackFormat.mimeType()
                when {
                    audioFormat == null && trackMimeType?.startsWith("audio/") == true -> {
                        audioFormat = trackFormat
                    }
                    trackMimeType?.startsWith("video/") == true -> hasVideo = true
                }
            }
            val codecMime = audioFormat?.getString(MediaFormat.KEY_MIME)
            val container = AudioFormatPolicy.resolveContainer(extension, mediaStoreMimeType, codecMime)
            DetectedAudioFormat(
                container = container,
                displayName = AudioFormatPolicy.displayName(container, extension, codecMime),
                mimeType = mediaStoreMimeType,
                codecMimeType = codecMime,
                detectionSucceeded = true,
                hasAudioTrack = audioFormat != null,
                hasVideoTrack = hasVideo,
                decoderAvailable = codecMime?.let { mime ->
                    AudioDecoderAvailability.isImplicitlyAvailable(mime) || hasDecoder(audioFormat, mime)
                },
                sampleRate = audioFormat?.integerOrNull(MediaFormat.KEY_SAMPLE_RATE),
                channelCount = audioFormat?.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                bitrate = audioFormat?.integerOrNull(MediaFormat.KEY_BIT_RATE),
                bitDepth = null,
                durationMs = audioFormat?.longOrNull(MediaFormat.KEY_DURATION)
                    ?.takeIf { it > 0L }
                    ?.div(1_000L),
                evidence = DetectionEvidence.Extractor,
            )
        } catch (_: Throwable) {
            val container = AudioFormatPolicy.resolveContainer(extension, mediaStoreMimeType, null)
            DetectedAudioFormat(
                container = container,
                displayName = AudioFormatPolicy.displayName(container, extension),
                mimeType = mediaStoreMimeType,
                codecMimeType = null,
                detectionSucceeded = false,
                hasAudioTrack = false,
                hasVideoTrack = false,
                decoderAvailable = null,
                sampleRate = null,
                channelCount = null,
                bitrate = null,
                bitDepth = null,
                evidence = DetectionEvidence.ExtensionFallback,
            )
        } finally {
            runCatching { extractor.release() }
        }
    }

    @Synchronized
    private fun hasDecoder(format: MediaFormat, mimeType: String): Boolean {
        val key = DeviceCodecProbeKey(
            mimeType = mimeType.lowercase(Locale.ROOT),
            sampleRate = format.integerOrNull(MediaFormat.KEY_SAMPLE_RATE),
            channelCount = format.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
        )
        return decoderAvailabilityCache.getOrPut(key) {
            runCatching {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).findDecoderForFormat(format) != null
            }.getOrDefault(false)
        }
    }

    private fun MediaFormat.integerOrNull(key: String): Int? {
        return runCatching { if (containsKey(key)) getInteger(key) else null }.getOrNull()
    }

    private fun MediaFormat.longOrNull(key: String): Long? {
        return runCatching { if (containsKey(key)) getLong(key) else null }.getOrNull()
    }

    private fun MediaFormat.mimeType(): String? = getString(MediaFormat.KEY_MIME)
}

internal object AudioDecoderAvailability {
    fun isImplicitlyAvailable(mimeType: String): Boolean {
        return mimeType.equals("audio/raw", ignoreCase = true)
    }
}
