package elovaire.music.droidbeauty.app.data.audio

import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

internal object AudioQualityFormatter {
    fun format(
        container: AudioContainerFormat,
        bitDepth: Int?,
        sampleRate: Int?,
        bitrate: Int?,
        codecMimeType: String? = null,
    ): String? {
        val sampleRateText = sampleRate?.takeIf { it > 0 }?.let(::formatSampleRate)
        val codec = codecMimeType.orEmpty().lowercase(Locale.ROOT)
        val lossless = container == AudioContainerFormat.Flac ||
            container == AudioContainerFormat.Wav ||
            codec in setOf("audio/flac", "audio/raw")
        return when {
            lossless && bitDepth != null && bitDepth in 8..64 && sampleRateText != null -> "$bitDepth/$sampleRateText"
            !lossless && bitrate != null && bitrate in 8_000..10_000_000 && sampleRateText != null ->
                "${(bitrate / 1000f).rounded()}/$sampleRateText"
            lossless && sampleRateText != null -> sampleRateText
            !lossless && sampleRateText != null -> sampleRateText
            !lossless && bitrate != null && bitrate in 8_000..10_000_000 -> "${(bitrate / 1000f).rounded()}kbps"
            else -> null
        }
    }

    private fun formatSampleRate(sampleRate: Int): String {
        val khz = sampleRate / 1000f
        val value = if (khz % 1f == 0f) khz.toInt().toString() else String.format(Locale.ROOT, "%.1f", khz)
        return "${value}kHz"
    }

    private fun Float.rounded(): String {
        val whole = round(this)
        return if (abs(this - whole) < 0.05f) whole.toInt().toString() else String.format(Locale.ROOT, "%.1f", this)
    }
}
