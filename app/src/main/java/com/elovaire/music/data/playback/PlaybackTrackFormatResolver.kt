package elovaire.music.app.data.playback

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import androidx.annotation.VisibleForTesting
import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PlaybackTrackFormatResolver(
    private val context: Context,
) {
    suspend fun resolve(song: Song): TrackPlaybackFormat? {
        return withContext(Dispatchers.IO) {
            resolveBlocking(song)
        }
    }

    @VisibleForTesting
    internal fun resolveBlocking(song: Song): TrackPlaybackFormat? {
        return runCatching {
            val extractorMetadata = readExtractorMetadata(song)
            val bitDepth = readBitsPerSample(song) ?: parseBitDepth(song.audioQuality)
            val sampleRate = extractorMetadata.sampleRateHz
                ?: parseSampleRate(song.audioQuality)
                ?: return@runCatching null
            val channelCount = extractorMetadata.channelCount ?: DEFAULT_CHANNEL_COUNT
            val encoding = resolveEncoding(bitDepth) ?: return@runCatching null
            TrackPlaybackFormat(
                sampleRateHz = sampleRate,
                channelCount = channelCount,
                encoding = encoding,
                sourceBitDepth = bitDepth,
                sourceFormatLabel = song.audioFormat,
            )
        }.getOrNull()
    }

    private fun readExtractorMetadata(song: Song): ExtractorTrackMetadata {
        return runCatching {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(context, song.uri, emptyMap())
                val format = (0 until extractor.trackCount)
                    .asSequence()
                    .map(extractor::getTrackFormat)
                    .firstOrNull { trackFormat ->
                        trackFormat.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                    }
                ExtractorTrackMetadata(
                    sampleRateHz = format?.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE),
                    channelCount = format?.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                )
            } finally {
                runCatching { extractor.release() }
            }
        }.getOrDefault(ExtractorTrackMetadata())
    }

    private fun readBitsPerSample(song: Song): Int? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, song.uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                    ?.toIntOrNull()
            } finally {
                runCatching { retriever.release() }
            }
        }.getOrNull()
    }

    private fun resolveEncoding(bitDepth: Int?): UsbPcmEncoding? {
        return when (bitDepth) {
            16 -> UsbPcmEncoding.Pcm16Bit
            24 -> UsbPcmEncoding.Pcm24BitPacked
            32 -> UsbPcmEncoding.Pcm32Bit
            else -> null
        }
    }

    private fun parseBitDepth(audioQuality: String?): Int? {
        return QUALITY_REGEX.matchEntire(audioQuality.orEmpty())
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
    }

    private fun parseSampleRate(audioQuality: String?): Int? {
        val sampleRateText = QUALITY_REGEX.matchEntire(audioQuality.orEmpty())
            ?.groupValues
            ?.getOrNull(2)
            ?: return null
        return (sampleRateText.toFloatOrNull()?.times(1000f))?.toInt()
    }

    private fun MediaFormat.getIntegerOrNull(key: String): Int? {
        return if (containsKey(key)) getInteger(key) else null
    }

    private data class ExtractorTrackMetadata(
        val sampleRateHz: Int? = null,
        val channelCount: Int? = null,
    )

    private companion object {
        const val DEFAULT_CHANNEL_COUNT = 2
        val QUALITY_REGEX = Regex("""(\d{1,2})/(\d{1,3}(?:\.\d)?)kHz""", RegexOption.IGNORE_CASE)
    }
}
