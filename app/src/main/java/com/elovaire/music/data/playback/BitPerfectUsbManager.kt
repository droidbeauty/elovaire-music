package elovaire.music.app.data.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import androidx.media3.exoplayer.audio.AudioSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class BitPerfectPlaybackState {
    UnsupportedAndroidVersion,
    NoEligibleRoute,
    EffectsActive,
    FormatUnknown,
    FormatUnsupported,
    Eligible,
}

internal data class BitPerfectPlaybackStatus(
    val state: BitPerfectPlaybackState = BitPerfectPlaybackState.UnsupportedAndroidVersion,
    val shouldUseDirectPlayback: Boolean = false,
    val activeRouteDeviceId: Int? = null,
    val activeRouteType: Int? = null,
    val directPlaybackSupport: Int = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
)

internal class BitPerfectUsbManager(
    context: Context,
    private val audioManager: AudioManager?,
    private val playbackAudioAttributes: AudioAttributes,
) {
    @Suppress("unused")
    private val appContext = context.applicationContext
    private val _status = MutableStateFlow(BitPerfectPlaybackStatus())
    val status: StateFlow<BitPerfectPlaybackStatus> = _status.asStateFlow()

    private var effectsActive = false
    private var currentAudioTrackConfig: AudioSink.AudioTrackConfig? = null
    private var preferredRouteDevice: AudioDeviceInfo? = null

    fun refreshConnectedDevices() {
        publishStatus()
    }

    fun updateCurrentAudioTrackConfig(audioTrackConfig: AudioSink.AudioTrackConfig?) {
        currentAudioTrackConfig = audioTrackConfig
        publishStatus()
    }

    fun updateEffectsActive(active: Boolean) {
        effectsActive = active
        publishStatus()
    }

    fun clearForStop() {
        currentAudioTrackConfig = null
        publishStatus()
    }

    fun preferredOutputDevice(): AudioDeviceInfo? = preferredRouteDevice

    fun release() = Unit

    private fun publishStatus() {
        if (audioManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            preferredRouteDevice = null
            _status.value = BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.UnsupportedAndroidVersion,
            )
            return
        }

        val activeRoute = resolveActiveUsbRouteDevice(audioManager, playbackAudioAttributes)
        preferredRouteDevice = activeRoute
        if (activeRoute == null) {
            _status.value = BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.NoEligibleRoute,
            )
            return
        }
        if (effectsActive) {
            _status.value = BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.EffectsActive,
                activeRouteDeviceId = activeRoute.id,
                activeRouteType = activeRoute.type,
            )
            return
        }

        val trackConfig = currentAudioTrackConfig
        if (trackConfig == null) {
            _status.value = BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.FormatUnknown,
                activeRouteDeviceId = activeRoute.id,
                activeRouteType = activeRoute.type,
            )
            return
        }

        val platformAudioFormat = trackConfig.toPlatformAudioFormat()
        if (platformAudioFormat == null) {
            _status.value = BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.FormatUnsupported,
                activeRouteDeviceId = activeRoute.id,
                activeRouteType = activeRoute.type,
            )
            return
        }

        val support = AudioManager.getDirectPlaybackSupport(platformAudioFormat, playbackAudioAttributes)
        val supported = support != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
        _status.value = BitPerfectPlaybackStatus(
            state = if (supported) {
                BitPerfectPlaybackState.Eligible
            } else {
                BitPerfectPlaybackState.FormatUnsupported
            },
            shouldUseDirectPlayback = supported,
            activeRouteDeviceId = activeRoute.id,
            activeRouteType = activeRoute.type,
            directPlaybackSupport = support,
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resolveActiveUsbRouteDevice(
        audioManager: AudioManager,
        playbackAudioAttributes: AudioAttributes,
    ): AudioDeviceInfo? {
        val routedDevices = audioManager.getAudioDevicesForAttributes(playbackAudioAttributes)
            .filter(AudioDeviceInfo::isSink)
        if (routedDevices.any { it.type.isBluetoothOutputType() }) {
            return null
        }
        return routedDevices.firstOrNull { it.type.isEligibleUsbOutputType() }
    }
}

private fun AudioSink.AudioTrackConfig.toPlatformAudioFormat(): AudioFormat? {
    val platformEncoding = encoding.toPlatformAudioEncoding() ?: return null
    if (sampleRate <= 0 || channelConfig == AudioFormat.CHANNEL_INVALID || channelConfig == 0) {
        return null
    }
    return AudioFormat.Builder()
        .setEncoding(platformEncoding)
        .setSampleRate(sampleRate)
        .setChannelMask(channelConfig)
        .build()
}

private fun Int.toPlatformAudioEncoding(): Int? {
    return when (this) {
        C.ENCODING_PCM_8BIT -> AudioFormat.ENCODING_PCM_8BIT
        C.ENCODING_PCM_16BIT -> AudioFormat.ENCODING_PCM_16BIT
        C.ENCODING_PCM_24BIT -> AudioFormat.ENCODING_PCM_24BIT_PACKED
        C.ENCODING_PCM_32BIT -> AudioFormat.ENCODING_PCM_32BIT
        C.ENCODING_PCM_FLOAT -> AudioFormat.ENCODING_PCM_FLOAT
        else -> null
    }
}

private fun Int.isEligibleUsbOutputType(): Boolean {
    return this == AudioDeviceInfo.TYPE_USB_DEVICE ||
        this == AudioDeviceInfo.TYPE_USB_HEADSET ||
        this == AudioDeviceInfo.TYPE_USB_ACCESSORY
}

private fun Int.isBluetoothOutputType(): Boolean {
    return this == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
        this == AudioDeviceInfo.TYPE_BLE_BROADCAST ||
        this == AudioDeviceInfo.TYPE_BLE_HEADSET ||
        this == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
        this == AudioDeviceInfo.TYPE_HEARING_AID
}
