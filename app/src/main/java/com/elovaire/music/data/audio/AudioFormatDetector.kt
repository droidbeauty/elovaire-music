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
)

internal class AudioFormatDetector(context: Context) {
    private val appContext = context.applicationContext
    private val decoderAvailabilityCache = mutableMapOf<String, Boolean>()

    fun detect(uri: Uri, fileName: String, mediaStoreMimeType: String?): DetectedAudioFormat {
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(appContext, uri, emptyMap())
            val trackFormats = (0 until extractor.trackCount).map(extractor::getTrackFormat)
            val audioFormat = trackFormats.firstOrNull {
                it.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            }
            val hasVideo = trackFormats.any {
                it.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
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
                    mime.equals("audio/raw", true) || hasDecoder(mime)
                },
                sampleRate = audioFormat?.integerOrNull(MediaFormat.KEY_SAMPLE_RATE),
                channelCount = audioFormat?.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                bitrate = audioFormat?.integerOrNull(MediaFormat.KEY_BIT_RATE),
                bitDepth = null,
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
            )
        } finally {
            runCatching { extractor.release() }
        }
    }

    @Synchronized
    private fun hasDecoder(mimeType: String): Boolean {
        val key = mimeType.lowercase(Locale.ROOT)
        return decoderAvailabilityCache.getOrPut(key) {
            runCatching {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { codecInfo ->
                    !codecInfo.isEncoder && codecInfo.supportedTypes.any { it.equals(key, true) }
                }
            }.getOrDefault(false)
        }
    }

    private fun MediaFormat.integerOrNull(key: String): Int? {
        return runCatching { if (containsKey(key)) getInteger(key) else null }.getOrNull()
    }
}
