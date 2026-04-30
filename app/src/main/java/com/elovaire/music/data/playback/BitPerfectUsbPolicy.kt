package elovaire.music.app.data.playback

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.os.Build

internal class BitPerfectUsbPolicy(
    // AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT is API 34+, so the policy keeps the raw
    // constant instead of referencing the class directly.
    private val bitPerfectBehavior: Int = BIT_PERFECT_MIXER_BEHAVIOR,
    private val minimumPreferredDeviceApi: Int = Build.VERSION_CODES.M,
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

    @SuppressLint("InlinedApi")
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
            channelCount = trackFormat.channelCount,
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

    fun shouldAttemptPreferredDeviceRouting(apiLevel: Int): Boolean {
        return apiLevel >= minimumPreferredDeviceApi
    }

    fun evaluateExactFormatSupport(
        selectedDevice: UsbAudioDeviceDescriptor,
        requestedFormat: PlatformAudioFormatData,
        directPlaybackSupported: Boolean,
        audioTrackProbeSucceeded: Boolean,
    ): UsbFormatProbeResult {
        val encodingSupported = selectedDevice.encodings.isEmpty() || selectedDevice.encodings.contains(requestedFormat.encoding)
        val sampleRateSupported = selectedDevice.sampleRates.isEmpty() || selectedDevice.sampleRates.contains(requestedFormat.sampleRateHz)
        val descriptorCompatible = encodingSupported && sampleRateSupported
        val exactFormatSupported = audioTrackProbeSucceeded && descriptorCompatible
        val routeStrategy = when {
            exactFormatSupported && directPlaybackSupported -> UsbBitPerfectRouteStrategy.ExactFormatDirect
            exactFormatSupported -> UsbBitPerfectRouteStrategy.BestEffortPreferredDevice
            else -> UsbBitPerfectRouteStrategy.None
        }
        val fallbackReason = when {
            !encodingSupported -> "USB device does not advertise requested PCM encoding"
            !sampleRateSupported -> "USB device does not advertise requested sample rate"
            !audioTrackProbeSucceeded -> "Exact-format AudioTrack initialization failed"
            else -> null
        }
        return UsbFormatProbeResult(
            requestedFormat = requestedFormat,
            exactFormatSupported = exactFormatSupported,
            routeStrategy = routeStrategy,
            directPlaybackSupported = directPlaybackSupported,
            mixerAttributesSupported = false,
            probedSuccessfully = audioTrackProbeSucceeded,
            fallbackReason = fallbackReason,
        )
    }

    fun deriveState(
        apiLevel: Int,
        selectedDevice: UsbAudioDeviceDescriptor?,
        trackFormatKnown: Boolean,
        requestedFormat: PlatformAudioFormatData?,
        probeResult: UsbFormatProbeResult?,
        applySucceeded: Boolean,
        verifiedActive: Boolean,
        errorMessage: String? = null,
    ): BitPerfectUsbStatus {
        if (errorMessage != null) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.ErrorRecoverable(errorMessage),
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice?.id,
                fallbackReason = errorMessage,
            )
        }
        if (!shouldAttemptPreferredDeviceRouting(apiLevel)) {
            return BitPerfectUsbStatus(state = BitPerfectUsbState.UnsupportedAndroidVersion)
        }
        if (selectedDevice == null) {
            return BitPerfectUsbStatus(state = BitPerfectUsbState.NoUsbDevice)
        }
        if (requestedFormat == null) {
            return BitPerfectUsbStatus(
                state = if (trackFormatKnown) {
                    BitPerfectUsbState.UnsupportedFormat
                } else {
                    BitPerfectUsbState.UsbDetected
                },
                selectedDeviceId = selectedDevice.id,
                fallbackReason = if (trackFormatKnown) "Track format could not be mapped to PCM output" else null,
            )
        }
        if (probeResult == null) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.ProbingCapabilities,
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice.id,
                routeStrategy = UsbBitPerfectRouteStrategy.None,
            )
        }
        if (!probeResult.exactFormatSupported) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.FallbackNormalPlayback,
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice.id,
                fallbackReason = probeResult.fallbackReason ?: "Exact USB output format not supported",
                routeStrategy = UsbBitPerfectRouteStrategy.None,
            )
        }
        if (probeResult.mixerAttributesSupported && !applySucceeded) {
            return BitPerfectUsbStatus(
                state = BitPerfectUsbState.ExactFormatSupported,
                requestedFormat = requestedFormat,
                selectedDeviceId = selectedDevice.id,
                fallbackReason = "Preferred mixer attributes rejected, keeping exact-format USB path",
                routeStrategy = UsbBitPerfectRouteStrategy.ExactFormatDirect,
            )
        }
        return BitPerfectUsbStatus(
            state = when {
                probeResult.mixerAttributesSupported && verifiedActive && applySucceeded -> BitPerfectUsbState.BitPerfectActive
                probeResult.mixerAttributesSupported && applySucceeded -> BitPerfectUsbState.BitPerfectAvailable
                probeResult.routeStrategy == UsbBitPerfectRouteStrategy.ExactFormatDirect && verifiedActive -> BitPerfectUsbState.ExactFormatUsbActive
                probeResult.routeStrategy == UsbBitPerfectRouteStrategy.ExactFormatDirect -> BitPerfectUsbState.ExactFormatSupported
                probeResult.routeStrategy == UsbBitPerfectRouteStrategy.BestEffortPreferredDevice -> BitPerfectUsbState.BestEffortUsbActive
                else -> BitPerfectUsbState.FallbackNormalPlayback
            },
            requestedFormat = requestedFormat,
            selectedDeviceId = selectedDevice.id,
            fallbackReason = probeResult.fallbackReason,
            routeStrategy = probeResult.routeStrategy,
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
