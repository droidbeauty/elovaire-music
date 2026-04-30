package elovaire.music.app.data.playback

internal sealed interface BitPerfectUsbState {
    data object NoUsbDevice : BitPerfectUsbState
    data object UsbDetected : BitPerfectUsbState
    data object ProbingCapabilities : BitPerfectUsbState
    data object ExactFormatSupported : BitPerfectUsbState
    data object BitPerfectAvailable : BitPerfectUsbState
    data object BitPerfectActive : BitPerfectUsbState
    data object ExactFormatUsbActive : BitPerfectUsbState
    data object BestEffortUsbActive : BitPerfectUsbState
    data object UnsupportedAndroidVersion : BitPerfectUsbState
    data object UnsupportedDevice : BitPerfectUsbState
    data object UnsupportedFormat : BitPerfectUsbState
    data object FallbackNormalPlayback : BitPerfectUsbState
    data class ErrorRecoverable(val message: String) : BitPerfectUsbState
}

internal enum class UsbPcmEncoding {
    Pcm16Bit,
    Pcm24BitPacked,
    Pcm32Bit,
    PcmFloat,
}

internal data class TrackPlaybackFormat(
    val sampleRateHz: Int,
    val channelCount: Int,
    val encoding: UsbPcmEncoding,
    val sourceBitDepth: Int? = null,
    val sourceFormatLabel: String? = null,
)

internal data class PlatformAudioFormatData(
    val sampleRateHz: Int,
    val channelMask: Int,
    val encoding: Int,
    val channelCount: Int,
)

internal data class UsbAudioDeviceDescriptor(
    val id: Int,
    val type: Int,
    val isSink: Boolean,
    val productName: String? = null,
    val sampleRates: IntArray = intArrayOf(),
    val encodings: IntArray = intArrayOf(),
)

internal data class SupportedMixerProfile(
    val format: PlatformAudioFormatData,
    val mixerBehavior: Int,
)

internal enum class UsbBitPerfectRouteStrategy {
    None,
    MixerAttributesBitPerfect,
    ExactFormatDirect,
    BestEffortPreferredDevice,
}

internal data class UsbFormatProbeResult(
    val requestedFormat: PlatformAudioFormatData,
    val exactFormatSupported: Boolean,
    val routeStrategy: UsbBitPerfectRouteStrategy,
    val directPlaybackSupported: Boolean = false,
    val mixerAttributesSupported: Boolean = false,
    val probedSuccessfully: Boolean = false,
    val fallbackReason: String? = null,
)

internal data class BitPerfectUsbStatus(
    val state: BitPerfectUsbState,
    val requestedFormat: PlatformAudioFormatData? = null,
    val selectedDeviceId: Int? = null,
    val fallbackReason: String? = null,
    val routeStrategy: UsbBitPerfectRouteStrategy = UsbBitPerfectRouteStrategy.None,
) {
    val shouldBypassProcessing: Boolean
        get() = state == BitPerfectUsbState.BitPerfectActive ||
            state == BitPerfectUsbState.ExactFormatUsbActive
}
