@file:Suppress("InlinedApi")

package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import elovaire.music.droidbeauty.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class BitPerfectPlaybackState {
    UnsupportedAndroidVersion,
    NoEligibleRoute,
    BluetoothRoute,
    EffectsActive,
    FormatUnknown,
    FormatUnsupported,
    OffloadOrTunneling,
    Eligible,
}

internal enum class BitPerfectPlaybackDirective {
    KeepCurrent,
    PreferRegular,
    PreferDirect,
}

internal data class DirectPlaybackEvaluationKey(
    val routeDeviceId: Int?,
    val routeType: Int?,
    val sampleRate: Int,
    val channelMask: Int,
    val channelCount: Int,
    val encoding: Int,
    val effectsActive: Boolean,
)

internal data class BitPerfectPlaybackStatus(
    val state: BitPerfectPlaybackState = BitPerfectPlaybackState.UnsupportedAndroidVersion,
    val directive: BitPerfectPlaybackDirective = BitPerfectPlaybackDirective.PreferRegular,
    val shouldUseDirectPlayback: Boolean = false,
    val activeRouteDeviceId: Int? = null,
    val activeRouteType: Int? = null,
    val directPlaybackSupport: Int = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
    val evaluationKey: DirectPlaybackEvaluationKey? = null,
) {
    val shouldPreferDirectPlayback: Boolean
        get() = directive == BitPerfectPlaybackDirective.PreferDirect
}

@UnstableApi
internal class BitPerfectUsbManager(
    private val audioManager: AudioManager?,
    private val playbackAudioAttributes: AudioAttributes,
) {
    private val _status = MutableStateFlow(BitPerfectPlaybackStatus())
    val status: StateFlow<BitPerfectPlaybackStatus> = _status.asStateFlow()

    private var effectsActive = false
    private var currentTrackConfig: DirectPlaybackTrackConfig? = null
    private var currentRouteFingerprint: List<RouteFingerprint> = emptyList()
    private var preferredRouteDevice: AudioDeviceInfo? = null
    private var cachedEvaluation: CachedDirectPlaybackEvaluation? = null

    fun refreshConnectedDevices() {
        publishStatus()
    }

    fun updateCurrentAudioTrackConfig(audioTrackConfig: AudioSink.AudioTrackConfig?) {
        val nextConfig = audioTrackConfig?.toDirectPlaybackTrackConfig()
        if (currentTrackConfig == nextConfig) return
        currentTrackConfig = nextConfig
        cachedEvaluation = null
        publishStatus()
    }

    fun updateEffectsActive(active: Boolean) {
        if (effectsActive == active) return
        effectsActive = active
        cachedEvaluation = null
        publishStatus()
    }

    fun clearPlaybackFormat() {
        if (currentTrackConfig == null) return
        currentTrackConfig = null
        cachedEvaluation = null
        publishStatus()
    }

    fun clearForStop() {
        currentTrackConfig = null
        cachedEvaluation = null
        publishStatus()
    }

    fun preferredOutputDevice(): AudioDeviceInfo? = preferredRouteDevice

    private fun publishStatus() {
        if (audioManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            preferredRouteDevice = null
            currentRouteFingerprint = emptyList()
            cachedEvaluation = null
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.UnsupportedAndroidVersion,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                ),
            )
            return
        }

        val routeSnapshot = resolveRouteSnapshot(audioManager, playbackAudioAttributes)
        if (routeSnapshot.fingerprint != currentRouteFingerprint) {
            currentRouteFingerprint = routeSnapshot.fingerprint
            cachedEvaluation = null
        }
        preferredRouteDevice = routeSnapshot.preferredUsbDevice

        val routeDeniedStatus = routeSnapshot.toDeniedStatus()
        if (routeDeniedStatus != null) {
            updateStatus(routeDeniedStatus)
            return
        }

        if (effectsActive) {
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.EffectsActive,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
                    activeRouteType = routeSnapshot.preferredUsbDevice?.type,
                ),
            )
            return
        }

        val trackConfig = currentTrackConfig
        if (trackConfig == null) {
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.FormatUnknown,
                    directive = BitPerfectPlaybackDirective.KeepCurrent,
                    activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
                    activeRouteType = routeSnapshot.preferredUsbDevice?.type,
                ),
            )
            return
        }

        if (trackConfig.offload || trackConfig.tunneling) {
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.OffloadOrTunneling,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
                    activeRouteType = routeSnapshot.preferredUsbDevice?.type,
                ),
            )
            return
        }

        val evaluationKey = trackConfig.toEvaluationKey(
            routeDeviceId = routeSnapshot.preferredUsbDevice?.id,
            routeType = routeSnapshot.preferredUsbDevice?.type,
            effectsActive = effectsActive,
        )
        if (evaluationKey == null) {
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.FormatUnsupported,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
                    activeRouteType = routeSnapshot.preferredUsbDevice?.type,
                ),
            )
            return
        }

        val cached = cachedEvaluation
        if (cached?.key == evaluationKey) {
            updateStatus(cached.status)
            return
        }

        val platformAudioFormat = trackConfig.toPlatformAudioFormat() ?: run {
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.FormatUnsupported,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
                    activeRouteType = routeSnapshot.preferredUsbDevice?.type,
                ),
            )
            return
        }

        val support = AudioManager.getDirectPlaybackSupport(platformAudioFormat, playbackAudioAttributes)
        val supported = support != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
        val evaluatedStatus = BitPerfectPlaybackStatus(
            state = if (supported) {
                BitPerfectPlaybackState.Eligible
            } else {
                BitPerfectPlaybackState.FormatUnsupported
            },
            directive = if (supported) {
                BitPerfectPlaybackDirective.PreferDirect
            } else {
                BitPerfectPlaybackDirective.PreferRegular
            },
            shouldUseDirectPlayback = supported,
            activeRouteDeviceId = routeSnapshot.preferredUsbDevice?.id,
            activeRouteType = routeSnapshot.preferredUsbDevice?.type,
            directPlaybackSupport = support,
            evaluationKey = evaluationKey,
        )
        cachedEvaluation = CachedDirectPlaybackEvaluation(evaluationKey, evaluatedStatus)
        updateStatus(evaluatedStatus)
    }

    private fun updateStatus(nextStatus: BitPerfectPlaybackStatus) {
        if (_status.value == nextStatus) return
        _status.value = nextStatus
        logDebug(
            "status=${nextStatus.state} directive=${nextStatus.directive} route=${nextStatus.activeRouteType}:${nextStatus.activeRouteDeviceId} " +
                "support=${nextStatus.directPlaybackSupport} key=${nextStatus.evaluationKey}",
        )
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "BitPerfectUsb"
    }
}

private data class CachedDirectPlaybackEvaluation(
    val key: DirectPlaybackEvaluationKey,
    val status: BitPerfectPlaybackStatus,
)

private data class RouteFingerprint(
    val id: Int,
    val type: Int,
)

private data class DirectPlaybackRouteSnapshot(
    val fingerprint: List<RouteFingerprint>,
    val preferredUsbDevice: AudioDeviceInfo?,
    val primaryRoutedDevice: AudioDeviceInfo?,
    val isBluetoothRouted: Boolean,
) {
    fun toDeniedStatus(): BitPerfectPlaybackStatus? {
        val reportedDevice = preferredUsbDevice ?: primaryRoutedDevice
        return when {
            isBluetoothRouted -> BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.BluetoothRoute,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                activeRouteDeviceId = reportedDevice?.id,
                activeRouteType = reportedDevice?.type,
            )

            preferredUsbDevice == null -> BitPerfectPlaybackStatus(
                state = BitPerfectPlaybackState.NoEligibleRoute,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                activeRouteDeviceId = reportedDevice?.id,
                activeRouteType = reportedDevice?.type,
            )

            else -> null
        }
    }
}

internal data class DirectPlaybackTrackConfig(
    val encoding: Int,
    val sampleRate: Int,
    val channelMask: Int,
    val channelCount: Int,
    val tunneling: Boolean,
    val offload: Boolean,
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun resolveRouteSnapshot(
    audioManager: AudioManager,
    playbackAudioAttributes: AudioAttributes,
): DirectPlaybackRouteSnapshot {
    val routedDevices = audioManager.getAudioDevicesForAttributes(playbackAudioAttributes)
        .filter(AudioDeviceInfo::isSink)
    val fingerprint = routedDevices
        .map { RouteFingerprint(id = it.id, type = it.type) }
        .sortedWith(compareBy(RouteFingerprint::type, RouteFingerprint::id))
    return DirectPlaybackRouteSnapshot(
        fingerprint = fingerprint,
        preferredUsbDevice = routedDevices.firstOrNull { it.type.isEligibleUsbOutputType() },
        primaryRoutedDevice = routedDevices.firstOrNull(),
        isBluetoothRouted = routedDevices.any { it.type.isBluetoothOutputType() },
    )
}

@UnstableApi
private fun AudioSink.AudioTrackConfig.toDirectPlaybackTrackConfig(): DirectPlaybackTrackConfig? {
    val channelCount = channelConfig.channelCountFromChannelMask()
    if (sampleRate <= 0 || channelConfig == AudioFormat.CHANNEL_INVALID || channelCount <= 0) {
        return null
    }
    return DirectPlaybackTrackConfig(
        encoding = encoding,
        sampleRate = sampleRate,
        channelMask = channelConfig,
        channelCount = channelCount,
        tunneling = tunneling,
        offload = offload,
    )
}

internal fun DirectPlaybackTrackConfig.toEvaluationKey(
    routeDeviceId: Int?,
    routeType: Int?,
    effectsActive: Boolean,
): DirectPlaybackEvaluationKey? {
    val platformEncoding = encoding.toPlatformAudioEncoding() ?: return null
    if (sampleRate <= 0 || channelMask == AudioFormat.CHANNEL_INVALID || channelCount <= 0) {
        return null
    }
    return DirectPlaybackEvaluationKey(
        routeDeviceId = routeDeviceId,
        routeType = routeType,
        sampleRate = sampleRate,
        channelMask = channelMask,
        channelCount = channelCount,
        encoding = platformEncoding,
        effectsActive = effectsActive,
    )
}

private fun DirectPlaybackTrackConfig.toPlatformAudioFormat(): AudioFormat? {
    val platformEncoding = encoding.toPlatformAudioEncoding() ?: return null
    if (sampleRate <= 0 || channelMask == AudioFormat.CHANNEL_INVALID || channelCount <= 0) {
        return null
    }
    return AudioFormat.Builder()
        .setEncoding(platformEncoding)
        .setSampleRate(sampleRate)
        .setChannelMask(channelMask)
        .build()
}

internal fun Int.channelCountFromChannelMask(): Int {
    if (this == AudioFormat.CHANNEL_OUT_MONO) return 1
    if (this == AudioFormat.CHANNEL_OUT_STEREO) return 2
    return Integer.bitCount(this).coerceAtLeast(0)
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

internal fun Int.isEligibleUsbOutputType(): Boolean {
    return this == AudioDeviceInfo.TYPE_USB_DEVICE ||
        this == AudioDeviceInfo.TYPE_USB_HEADSET ||
        this == AudioDeviceInfo.TYPE_USB_ACCESSORY
}

internal fun Int.isBluetoothOutputType(): Boolean {
    return this == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
        this == AudioDeviceInfo.TYPE_BLE_BROADCAST ||
        this == AudioDeviceInfo.TYPE_BLE_HEADSET ||
        this == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
        this == AudioDeviceInfo.TYPE_HEARING_AID
}
