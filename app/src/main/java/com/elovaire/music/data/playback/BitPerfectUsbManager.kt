@file:Suppress("InlinedApi")

package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.safeDirectPlaybackSupport
import elovaire.music.droidbeauty.app.core.safeOutputDevices
import elovaire.music.droidbeauty.app.core.safeRoutedOutputDevicesForAttributes
import elovaire.music.droidbeauty.app.core.supportsVerifiedDirectPlaybackRouting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class BitPerfectPlaybackState {
    UnsupportedAndroidVersion,
    LegacyUnverifiedUsbRoute,
    NoEligibleRoute,
    BluetoothRoute,
    EffectsActive,
    FormatUnknown,
    FormatUnsupported,
    OffloadOrTunneling,
    UsbRouteDetectedButDirectSupportUnavailable,
    OffloadOnlyDirectPlaybackSupported,
    GaplessOffloadDirectPlaybackSupported,
    BitstreamDirectPlaybackSupported,
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
    val routeFingerprintHash: Int?,
    val sampleRate: Int,
    val channelMask: Int,
    val channelCount: Int,
    val encoding: Int,
    val effectsActive: Boolean,
)

internal data class BitPerfectPlaybackStatus(
    val state: BitPerfectPlaybackState = BitPerfectPlaybackState.UnsupportedAndroidVersion,
    val directive: BitPerfectPlaybackDirective = BitPerfectPlaybackDirective.PreferRegular,
    val mode: DirectPlaybackMode = DirectPlaybackMode.RegularPlaybackRequired,
    val shouldUseDirectPlayback: Boolean = false,
    val activeRouteDeviceId: Int? = null,
    val activeRouteType: Int? = null,
    val directPlaybackSupport: Int = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
    val supportClassification: DirectPlaybackSupportClassification = DirectPlaybackSupportClassification.notSupported,
    val evaluationKey: DirectPlaybackEvaluationKey? = null,
    val confidence: BitPerfectConfidence = BitPerfectConfidence.Unsupported,
    val capability: DirectPlaybackCapability = DirectPlaybackCapability(
        platformSupported = false,
        routeEligible = false,
        formatSupported = false,
        directSupportFlags = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
        supportKinds = emptySet(),
    ),
    val configuration: DirectPlaybackConfiguration = DirectPlaybackConfiguration(
        directModeRequested = false,
        preferredDeviceApplied = false,
        signalProcessingDisabled = false,
        offloadActive = false,
        tunnelingActive = false,
        currentTrackConfigKnown = false,
    ),
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
        runCatching {
            publishStatusUnsafe()
        }.onFailure {
            preferredRouteDevice = null
            cachedEvaluation = null
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.NoEligibleRoute,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    mode = DirectPlaybackMode.RegularPlaybackRequired,
                ),
            )
        }
    }

    private fun publishStatusUnsafe() {
        if (audioManager == null) {
            preferredRouteDevice = null
            currentRouteFingerprint = emptyList()
            cachedEvaluation = null
            updateStatus(
                BitPerfectPlaybackStatus(
                    state = BitPerfectPlaybackState.UnsupportedAndroidVersion,
                    directive = BitPerfectPlaybackDirective.PreferRegular,
                    mode = DirectPlaybackMode.RegularPlaybackRequired,
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

        val trackConfig = currentTrackConfig
        val routeContext = routeSnapshot.toRouteContext()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            !supportsVerifiedDirectPlaybackRouting(Build.VERSION.SDK_INT)
        ) {
            updateStatus(
                BitPerfectEligibilityPolicy.evaluate(
                    sdkInt = Build.VERSION.SDK_INT,
                    routeContext = routeContext,
                    effectsActive = effectsActive,
                    trackConfig = trackConfig,
                    evaluationKey = null,
                ),
            )
            return
        }

        val evaluationKey = trackConfig?.toEvaluationKey(
            routeFingerprintHash = routeSnapshot.preferredUsbFingerprint?.hashCode(),
            routeDeviceId = routeSnapshot.preferredUsbDevice?.safeId(),
            routeType = routeSnapshot.preferredUsbDevice?.safeType(),
            effectsActive = effectsActive,
        )

        val earlyDecision = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION.SDK_INT,
            routeContext = routeContext,
            effectsActive = effectsActive,
            trackConfig = trackConfig,
            evaluationKey = evaluationKey,
            supportClassification = QUERY_ALLOWED_SUPPORT,
            directPlaybackSupport = QUERY_ALLOWED_SUPPORT.rawFlags,
        )
        if (earlyDecision.directive != BitPerfectPlaybackDirective.PreferDirect) {
            updateStatus(earlyDecision)
            return
        }
        val directEvaluationKey = evaluationKey ?: run {
            updateStatus(earlyDecision.copy(state = BitPerfectPlaybackState.FormatUnsupported))
            return
        }

        val cached = cachedEvaluation
        if (cached?.key == directEvaluationKey) {
            updateStatus(cached.status)
            return
        }

        val platformAudioFormat = trackConfig.toPlatformAudioFormat() ?: run {
            updateStatus(earlyDecision.copy(state = BitPerfectPlaybackState.FormatUnsupported))
            return
        }

        val support = audioManager.safeDirectPlaybackSupport(platformAudioFormat, playbackAudioAttributes)
        val supportClassification = DirectPlaybackSupportClassifier.classify(support)
        val evaluatedStatus = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION.SDK_INT,
            routeContext = routeContext,
            effectsActive = effectsActive,
            trackConfig = trackConfig,
            evaluationKey = evaluationKey,
            supportClassification = supportClassification,
            directPlaybackSupport = support,
        )
        cachedEvaluation = CachedDirectPlaybackEvaluation(directEvaluationKey, evaluatedStatus)
        updateStatus(evaluatedStatus)
    }

    private fun updateStatus(nextStatus: BitPerfectPlaybackStatus) {
        if (_status.value == nextStatus) return
        _status.value = nextStatus
        logDebug(
            "status=${nextStatus.state} mode=${nextStatus.mode} directive=${nextStatus.directive} " +
                "route=${nextStatus.activeRouteType}:${nextStatus.activeRouteDeviceId} " +
                "support=${nextStatus.directPlaybackSupport} class=${nextStatus.supportClassification.summary} " +
                "confidence=${nextStatus.confidence} key=${nextStatus.evaluationKey}",
        )
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "BitPerfectUsb"
        val QUERY_ALLOWED_SUPPORT = DirectPlaybackSupportClassification(
            rawFlags = AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED,
            kinds = setOf(DirectPlaybackSupportKind.BitstreamPassThrough),
        )
    }
}

private data class CachedDirectPlaybackEvaluation(
    val key: DirectPlaybackEvaluationKey,
    val status: BitPerfectPlaybackStatus,
)

private data class DirectPlaybackRouteSnapshot(
    val fingerprint: List<RouteFingerprint>,
    val preferredUsbDevice: AudioDeviceInfo?,
    val preferredUsbFingerprint: RouteFingerprint?,
    val primaryRoutedDevice: AudioDeviceInfo?,
    val primaryRouteFingerprint: RouteFingerprint?,
    val hasBluetoothRoute: Boolean,
    val isRouteVerified: Boolean,
) {
    fun toRouteContext(): DirectPlaybackRouteContext {
        val reportedDevice = preferredUsbDevice ?: primaryRoutedDevice
        val reportedFingerprint = preferredUsbFingerprint ?: primaryRouteFingerprint
        return DirectPlaybackRouteContext(
            hasEligibleUsbRoute = preferredUsbDevice != null,
            hasVerifiedRoutedUsbRoute = isRouteVerified && preferredUsbDevice != null,
            hasVerifiedBluetoothRoute = isRouteVerified && hasBluetoothRoute,
            activeRouteDeviceId = reportedDevice?.safeId(),
            activeRouteType = reportedDevice?.safeType(),
            activeRouteSignature = reportedFingerprint?.hashCode(),
            platformRoutingSupported = isRouteVerified,
        )
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

private fun resolveRouteSnapshot(
    audioManager: AudioManager,
    playbackAudioAttributes: AudioAttributes,
): DirectPlaybackRouteSnapshot {
    val routedDevices = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        supportsVerifiedDirectPlaybackRouting(Build.VERSION.SDK_INT)
    ) {
        audioManager.safeRoutedOutputDevicesForAttributes(playbackAudioAttributes)
    } else {
        audioManager.safeOutputDevices()
    }
    val fingerprint = routedDevices
        .mapNotNull { device -> runCatching { device.toRouteFingerprint() }.getOrNull() }
        .sortedWith(compareBy(RouteFingerprint::type, RouteFingerprint::id))
    val preferredUsbDevice = routedDevices.firstOrNull { device ->
        runCatching { device.type.isEligibleUsbOutputType() }.getOrDefault(false)
    }
    val primaryRoutedDevice = routedDevices.firstOrNull()
    return DirectPlaybackRouteSnapshot(
        fingerprint = fingerprint,
        preferredUsbDevice = preferredUsbDevice,
        preferredUsbFingerprint = preferredUsbDevice?.let { runCatching { it.toRouteFingerprint() }.getOrNull() },
        primaryRoutedDevice = primaryRoutedDevice,
        primaryRouteFingerprint = primaryRoutedDevice?.let { runCatching { it.toRouteFingerprint() }.getOrNull() },
        hasBluetoothRoute = routedDevices.any { device ->
            runCatching { device.type.isBluetoothOutputType() }.getOrDefault(false)
        },
        isRouteVerified = supportsVerifiedDirectPlaybackRouting(Build.VERSION.SDK_INT),
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
    routeFingerprintHash: Int?,
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
        routeFingerprintHash = routeFingerprintHash,
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
    return runCatching {
        AudioFormat.Builder()
            .setEncoding(platformEncoding)
            .setSampleRate(sampleRate)
            .setChannelMask(channelMask)
            .build()
    }.getOrNull()
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

private data class RouteFingerprint(
    val id: Int,
    val type: Int,
    val address: String,
    val productName: String?,
    val sampleRates: List<Int>,
    val encodings: List<Int>,
    val channelCounts: List<Int>,
    val channelMasks: List<Int>,
)

private fun AudioDeviceInfo.toRouteFingerprint(): RouteFingerprint {
    return RouteFingerprint(
        id = safeId() ?: -1,
        type = safeType() ?: -1,
        address = runCatching { address.orEmpty() }.getOrDefault(""),
        productName = runCatching { productName?.toString()?.trim() }.getOrNull(),
        sampleRates = runCatching { sampleRates.toList().sorted() }.getOrDefault(emptyList()),
        encodings = runCatching { encodings.toList().sorted() }.getOrDefault(emptyList()),
        channelCounts = runCatching { channelCounts.toList().sorted() }.getOrDefault(emptyList()),
        channelMasks = runCatching { channelMasks.toList().sorted() }.getOrDefault(emptyList()),
    )
}

private fun AudioDeviceInfo.safeId(): Int? = runCatching { id }.getOrNull()

private fun AudioDeviceInfo.safeType(): Int? = runCatching { type }.getOrNull()

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
