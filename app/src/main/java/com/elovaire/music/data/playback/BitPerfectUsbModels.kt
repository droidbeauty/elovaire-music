package elovaire.music.app.data.playback

internal sealed interface BitPerfectUsbState {
    data object NoUsbDevice : BitPerfectUsbState
    data object UsbDetected : BitPerfectUsbState
    data object BitPerfectAvailable : BitPerfectUsbState
    data object BitPerfectActive : BitPerfectUsbState
    data object UnsupportedAndroidVersion : BitPerfectUsbState
    data object UnsupportedDeviceOrFormat : BitPerfectUsbState
    data object FallbackActive : BitPerfectUsbState
    data class Error(val message: String) : BitPerfectUsbState
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
)

internal data class UsbAudioDeviceDescriptor(
    val id: Int,
    val type: Int,
    val isSink: Boolean,
    val productName: String? = null,
)

internal data class SupportedMixerProfile(
    val format: PlatformAudioFormatData,
    val mixerBehavior: Int,
)

internal data class BitPerfectUsbStatus(
    val state: BitPerfectUsbState,
    val requestedFormat: PlatformAudioFormatData? = null,
    val selectedDeviceId: Int? = null,
    val fallbackReason: String? = null,
) {
    val shouldBypassProcessing: Boolean
        get() = state == BitPerfectUsbState.BitPerfectActive
}
