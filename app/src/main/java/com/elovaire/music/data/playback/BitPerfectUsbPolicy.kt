package elovaire.music.app.data.playback

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.os.Build

internal class BitPerfectUsbPolicy(
    // AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT is API 34+, so the policy keeps the raw
    // constant instead of referencing the class directly.
    private val bitPerfectBehavior: Int = BIT_PERFECT_MIXER_BEHAVIOR,
    private val minimumSupportedApi: Int = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
) {
    fun selectUsbDevice(
        devices: List<UsbAudioDeviceDescriptor>,
        currentDeviceId: Int? = null,
    ): UsbAudioDeviceDescriptor? {
        val candidates = devices
            .filter { descriptor ->
                descriptor.isSink && descriptor.type in USB_OUTPUT_DEVICE_TYPES
            }
            .sortedBy { it.id }
        return candidates.firstOrNull { it.id == currentDeviceId } ?: candidates.firstOrNull()
    }

    fun mapTrackFormat(trackFormat: TrackPlaybackFormat): PlatformAudioFormatData? {
        val channelMask = channelMaskForCount(trackFormat.channelCount) ?: return null
        val encoding = when (trackFormat.encoding) {
            UsbPcmEncoding.Pcm16Bit -> AudioFormat.ENCODING_PCM_16BIT
            UsbPcmEncoding.Pcm24BitPacked -> AudioFormat.ENCODING_PCM_24BIT_PACKED
            UsbPcmEncoding.Pcm32Bit -> AudioFormat.ENCODING_PCM_32BIT
            UsbPcmEncoding.PcmFloat -> AudioFormat.ENCODING_PCM_FLOAT
        }
        if (trackFormat.sampleRateHz <= 0) return null
        return PlatformAudioFormatData(
            sampleRateHz = trackFormat.sampleRateHz,
            channelMask = channelMask,
            encoding = encoding,
        )
    }

    fun selectSupportedProfile(
        requestedFormat: PlatformAudioFormatData,
        supportedProfiles: List<SupportedMixerProfile>,
    ): SupportedMixerProfile? {
        return supportedProfiles.firstOrNull { profile ->
            profile.mixerBehavior == bitPerfectBehavior && profile.format == requestedFormat
        }
    }

    fun deriveState(
        apiLevel: Int,
        selectedDevice: UsbAudioDeviceDescriptor?,
        trackFormatKnown: Boolean,
        requestedFormat: PlatformAudioFormatData?,
        matchedProfile: SupportedMixerProfile?,
        applySucceeded: Boolean,
        verifiedActive: Boolean,
        errorMessage: String? = null,
    ): BitPerfectUsbStatus {
        if (errorMessage != null) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.Error(errorMessage),
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice?.id,
                fallbackReason = errorMessage,
            )
        }
        if (apiLevel < minimumSupportedApi) {
            return BitPerfectUsbStatus(state = BitPerfectUsbState.UnsupportedAndroidVersion)
        }
        if (selectedDevice == null) {
            return BitPerfectUsbStatus(state = BitPerfectUsbState.NoUsbDevice)
        }
        if (requestedFormat == null) {
            return BitPerfectUsbStatus(
                state = if (trackFormatKnown) {
                    BitPerfectUsbState.UnsupportedDeviceOrFormat
                } else {
                    BitPerfectUsbState.UsbDetected
                },
                selectedDeviceId = selectedDevice.id,
                fallbackReason = if (trackFormatKnown) "Track format could not be mapped to PCM output" else null,
            )
        }
        if (matchedProfile == null) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.UnsupportedDeviceOrFormat,
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice.id,
                fallbackReason = "No matching USB mixer attributes",
            )
        }
        if (!applySucceeded) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.FallbackActive,
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice.id,
                fallbackReason = "Preferred mixer attribute request was rejected",
            )
        }
        return BitPerfectUsbStatus(
            state = if (verifiedActive) {
                BitPerfectUsbState.BitPerfectActive
            } else {
                BitPerfectUsbState.BitPerfectAvailable
            },
            requestedFormat = requestedFormat,
            selectedDeviceId = selectedDevice.id,
        )
    }

    private fun channelMaskForCount(channelCount: Int): Int? {
        return when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            4 -> AudioFormat.CHANNEL_OUT_QUAD
            6 -> AudioFormat.CHANNEL_OUT_5POINT1
            8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
            else -> null
        }
    }

    internal companion object {
        const val BIT_PERFECT_MIXER_BEHAVIOR = 1
        val USB_OUTPUT_DEVICE_TYPES = setOf(
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_ACCESSORY,
        )
    }
}
