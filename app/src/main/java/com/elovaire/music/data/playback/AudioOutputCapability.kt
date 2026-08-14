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
) {
    val hasUsbOutput: Boolean
        get() = devices.any { it.route == AudioOutputRouteKind.Usb }

    val routeSignature: Int
        get() = devices.fold(platformSdk) { result, device ->
            var value = 31 * result + device.id
            value = 31 * value + device.type
            value = 31 * value + device.route.ordinal
            value = 31 * value + device.sampleRates.hashCode()
            value = 31 * value + device.channelCounts.hashCode()
            value = 31 * value + device.channelMasks.hashCode()
            31 * value + device.encodings.hashCode()
        }

    companion object {
        val Unknown = AudioOutputCapabilitySnapshot(emptyList(), Build.VERSION.SDK_INT)
    }
}

internal object AudioOutputCapabilityReader {
    @SuppressLint("NewApi")
    fun read(
        audioManager: AudioManager?,
        attributes: AudioAttributes,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): AudioOutputCapabilitySnapshot {
        if (audioManager == null) return AudioOutputCapabilitySnapshot(emptyList(), sdkInt)
        val devices = runCatching {
            val routed = if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                readRoutedOutputDevices(audioManager, attributes)
            } else emptyList()
            (routed.ifEmpty { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList() })
                .filter { it.isSink }
                .map(::toCapability)
                .distinctBy { it.id to it.type }
        }.getOrDefault(emptyList())
        return AudioOutputCapabilitySnapshot(devices = devices, platformSdk = sdkInt)
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
            sampleRates = device.sampleRates.toList(),
            channelCounts = device.channelCounts.toList(),
            channelMasks = device.channelMasks.toList(),
            encodings = device.encodings.toList(),
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
            offloadAllowed = !processingRequired && !capabilities.hasUsbOutput,
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
