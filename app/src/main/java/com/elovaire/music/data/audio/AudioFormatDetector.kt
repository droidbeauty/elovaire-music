package elovaire.music.droidbeauty.app.data.audio

import android.content.Context
import android.media.MediaCodecList
import android.media.AudioFormat
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
    val codecProfile: Int? = null,
    val pcmEncoding: Int? = null,
    val isProtected: Boolean = false,
    val evidence: DetectionEvidence = DetectionEvidence.ExtensionFallback,
)

internal enum class DetectionEvidence {
    Extractor,
    Signature,
    ProviderMime,
    ExtensionFallback,
}

internal data class DeviceCodecProbeKey(
    val mimeType: String,
    val sampleRate: Int?,
    val channelCount: Int?,
    val profile: Int?,
)

internal data class AudioProbeCacheKey(
    val uri: String,
    val revisionKey: String,
)

internal class AudioFormatDetector(context: Context) {
    private val appContext = context.applicationContext
    private val decoderAvailabilityCache = object : LinkedHashMap<DeviceCodecProbeKey, Boolean>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<DeviceCodecProbeKey, Boolean>?): Boolean {
            return size > MAX_DECODER_CACHE_ENTRIES
        }
    }
    private val detectedFormatCache = object : LinkedHashMap<AudioProbeCacheKey, DetectedAudioFormat>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<AudioProbeCacheKey, DetectedAudioFormat>?): Boolean {
            return size > MAX_PROBE_CACHE_ENTRIES
        }
    }

    fun detect(
        uri: Uri,
        fileName: String,
        mediaStoreMimeType: String?,
        revisionKey: String? = null,
    ): DetectedAudioFormat {
        val cacheKey = revisionKey?.let { AudioProbeCacheKey(uri.toString(), it) }
        cacheKey?.let { key -> synchronized(detectedFormatCache) { detectedFormatCache[key] } }?.let { return it }
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val signature = readSignature(uri)
        val extractor = MediaExtractor()
        val detected = try {
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
            val container = signature?.toContainer() ?: AudioFormatPolicy.resolveContainer(extension, mediaStoreMimeType, codecMime)
            val pcmEncoding = audioFormat?.integerOrNull(MediaFormat.KEY_PCM_ENCODING)
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
                bitDepth = pcmEncoding.toBitDepth(),
                durationMs = audioFormat?.longOrNull(MediaFormat.KEY_DURATION)
                    ?.takeIf { it > 0L }
                    ?.div(1_000L),
                codecProfile = audioFormat?.integerOrNull(MediaFormat.KEY_PROFILE),
                pcmEncoding = pcmEncoding,
                isProtected = extractor.drmInitData != null,
                evidence = DetectionEvidence.Extractor,
            )
        } catch (_: Exception) {
            val container = signature?.toContainer()
                ?: AudioFormatPolicy.resolveContainer(extension, mediaStoreMimeType, null)
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
                evidence = when {
                    signature != null -> DetectionEvidence.Signature
                    !mediaStoreMimeType.isNullOrBlank() -> DetectionEvidence.ProviderMime
                    else -> DetectionEvidence.ExtensionFallback
                },
            )
        } finally {
            runCatching { extractor.release() }
        }
        cacheKey?.let { key -> synchronized(detectedFormatCache) { detectedFormatCache[key] = detected } }
        return detected
    }

    @Synchronized
    private fun hasDecoder(format: MediaFormat, mimeType: String): Boolean {
        val key = DeviceCodecProbeKey(
            mimeType = mimeType.lowercase(Locale.ROOT),
            sampleRate = format.integerOrNull(MediaFormat.KEY_SAMPLE_RATE),
            channelCount = format.integerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
            profile = format.integerOrNull(MediaFormat.KEY_PROFILE),
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

    private fun readSignature(uri: Uri): AudioContainerSignature? {
        return runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = ByteArray(MAX_SIGNATURE_BYTES)
                var total = 0
                while (total < bytes.size) {
                    val read = input.read(bytes, total, bytes.size - total)
                    if (read <= 0) break
                    total += read
                }
                AudioContainerSignatureDetector.detect(bytes, total)
            }
        }.getOrNull()
    }

    private fun AudioContainerSignature.toContainer(): AudioContainerFormat = when (this) {
        AudioContainerSignature.Mp3 -> AudioContainerFormat.Mp3
        AudioContainerSignature.Mp4 -> AudioContainerFormat.Mp4Audio
        AudioContainerSignature.ThreeGp -> AudioContainerFormat.ThreeGpAudio
        AudioContainerSignature.AacAdts -> AudioContainerFormat.AacAdts
        AudioContainerSignature.Flac -> AudioContainerFormat.Flac
        AudioContainerSignature.Wav -> AudioContainerFormat.Wav
        AudioContainerSignature.OggVorbis -> AudioContainerFormat.OggVorbis
        AudioContainerSignature.OggOpus -> AudioContainerFormat.OggOpus
        AudioContainerSignature.OggFlac -> AudioContainerFormat.OggFlac
        AudioContainerSignature.Amr -> AudioContainerFormat.Amr
        AudioContainerSignature.Matroska -> AudioContainerFormat.MatroskaAudio
    }

    private fun Int?.toBitDepth(): Int? = when (this) {
        AudioFormat.ENCODING_PCM_16BIT -> 16
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_FLOAT -> 32
        0x15 -> 24 // AudioFormat.ENCODING_PCM_24BIT_PACKED on API 31+
        0x16 -> 32 // AudioFormat.ENCODING_PCM_32BIT on API 31+
        else -> null
    }

    private companion object {
        const val MAX_DECODER_CACHE_ENTRIES = 64
        const val MAX_PROBE_CACHE_ENTRIES = 128
        const val MAX_SIGNATURE_BYTES = 64 * 1024
    }
}

internal object AudioDecoderAvailability {
    fun isImplicitlyAvailable(mimeType: String): Boolean {
        return mimeType.equals("audio/raw", ignoreCase = true)
    }
}
