package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.annotation.SuppressLint
import androidx.annotation.RequiresApi

internal enum class AudioOutputRouteKind {
    BuiltIn,
    Wired,
    Usb,
    Bluetooth,
    Hdmi,
    Other,
    Unknown,
}

internal enum class AudioOutputCapabilitySource {
    ActiveRoute,
    AvailableDevices,
    Unknown,
}

internal data class AudioOutputDeviceCapability(
    val id: Int,
    val type: Int,
    val route: AudioOutputRouteKind,
    val sampleRates: List<Int>,
    val channelCounts: List<Int>,
    val channelMasks: List<Int>,
    val encodings: List<Int>,
)

internal data class AudioOutputCapabilitySnapshot(
    val devices: List<AudioOutputDeviceCapability>,
    val platformSdk: Int,
    val source: AudioOutputCapabilitySource = AudioOutputCapabilitySource.ActiveRoute,
) {
    val hasActiveUsbOutput: Boolean
        get() = source == AudioOutputCapabilitySource.ActiveRoute &&
            devices.any { it.route == AudioOutputRouteKind.Usb }

    val hasAvailableUsbOutput: Boolean
        get() = devices.any { it.route == AudioOutputRouteKind.Usb }

    val routeSignature: Int
        get() = devices.sortedWith(compareBy(AudioOutputDeviceCapability::id, AudioOutputDeviceCapability::type))
            .fold(31 * platformSdk + source.ordinal) { result, device ->
            var value = 31 * result + device.id
            value = 31 * value + device.type
            value = 31 * value + device.route.ordinal
            value = 31 * value + device.sampleRates.sorted().hashCode()
            value = 31 * value + device.channelCounts.sorted().hashCode()
            value = 31 * value + device.channelMasks.sorted().hashCode()
            31 * value + device.encodings.sorted().hashCode()
        }

    companion object {
        val Unknown = AudioOutputCapabilitySnapshot(
            devices = emptyList(),
            platformSdk = Build.VERSION.SDK_INT,
            source = AudioOutputCapabilitySource.Unknown,
        )
    }
}

internal object AudioOutputCapabilityReader {
    @SuppressLint("NewApi")
    fun read(
        audioManager: AudioManager?,
        attributes: AudioAttributes,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): AudioOutputCapabilitySnapshot {
        if (audioManager == null) {
            return AudioOutputCapabilitySnapshot(
                devices = emptyList(),
                platformSdk = sdkInt,
                source = AudioOutputCapabilitySource.Unknown,
            )
        }
        val (rawDevices, source) = if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            val routed = runCatching { readRoutedOutputDevices(audioManager, attributes) }
                .getOrElse {
                    return AudioOutputCapabilitySnapshot(
                        devices = emptyList(),
                        platformSdk = sdkInt,
                        source = AudioOutputCapabilitySource.Unknown,
                    )
                }
            routed to AudioOutputCapabilitySource.ActiveRoute
        } else {
            val available = runCatching { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList() }
                .getOrElse {
                    return AudioOutputCapabilitySnapshot(
                        devices = emptyList(),
                        platformSdk = sdkInt,
                        source = AudioOutputCapabilitySource.Unknown,
                    )
                }
            available to AudioOutputCapabilitySource.AvailableDevices
        }
        val devices = rawDevices
            .filter { runCatching { it.isSink }.getOrDefault(false) }
            .map(::toCapability)
            .distinctBy { it.id to it.type }
            .sortedWith(compareBy(AudioOutputDeviceCapability::id, AudioOutputDeviceCapability::type))
        return AudioOutputCapabilitySnapshot(devices = devices, platformSdk = sdkInt, source = source)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun readRoutedOutputDevices(
        audioManager: AudioManager,
        attributes: AudioAttributes,
    ): List<AudioDeviceInfo> = audioManager.getAudioDevicesForAttributes(attributes)

    private fun toCapability(device: AudioDeviceInfo): AudioOutputDeviceCapability {
        return AudioOutputDeviceCapability(
            id = device.id,
            type = device.type,
            route = device.type.toOutputRouteKind(),
            sampleRates = device.sampleRates.toList().distinct().sorted(),
            channelCounts = device.channelCounts.toList().distinct().sorted(),
            channelMasks = device.channelMasks.toList().distinct().sorted(),
            encodings = device.encodings.toList().distinct().sorted(),
        )
    }
}

internal data class AudioProcessingRequirements(
    val signalAlteringEffectsActive: Boolean,
    val normalizationActive: Boolean,
    val monoOrChannelMappingActive: Boolean,
    val crossfadeActive: Boolean,
)

internal data class AudioOutputPolicyDecision(
    val signalProcessingRequired: Boolean,
    val offloadAllowed: Boolean,
)

internal object AudioOutputPolicy {
    fun decide(
        capabilities: AudioOutputCapabilitySnapshot,
        requirements: AudioProcessingRequirements,
        directPathActive: Boolean,
    ): AudioOutputPolicyDecision {
        val processingRequired = requirements.signalAlteringEffectsActive ||
            requirements.normalizationActive ||
            requirements.monoOrChannelMappingActive ||
            requirements.crossfadeActive
        return AudioOutputPolicyDecision(
            signalProcessingRequired = processingRequired || !directPathActive,
            // A failed/unknown route query must not be treated as a known safe route. Staying
            // on the regular software path is the conservative choice until the platform gives
            // us an authoritative capability snapshot.
            offloadAllowed = !processingRequired &&
                capabilities.source != AudioOutputCapabilitySource.Unknown &&
                !capabilities.hasActiveUsbOutput,
        )
    }
}

private fun Int.toOutputRouteKind(): AudioOutputRouteKind {
    return when (this) {
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
        AudioDeviceInfo.TYPE_BUILTIN_MIC,
        -> AudioOutputRouteKind.BuiltIn
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_LINE_ANALOG,
        AudioDeviceInfo.TYPE_LINE_DIGITAL,
        -> AudioOutputRouteKind.Wired
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
        -> AudioOutputRouteKind.Usb
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
        AudioDeviceInfo.TYPE_BLE_SPEAKER,
        -> AudioOutputRouteKind.Bluetooth
        AudioDeviceInfo.TYPE_HDMI,
        AudioDeviceInfo.TYPE_HDMI_ARC,
        AudioDeviceInfo.TYPE_HDMI_EARC,
        -> AudioOutputRouteKind.Hdmi
        AudioDeviceInfo.TYPE_UNKNOWN -> AudioOutputRouteKind.Unknown
        else -> AudioOutputRouteKind.Other
    }
}
