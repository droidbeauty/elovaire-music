package elovaire.music.app.data.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioMixerAttributes
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import elovaire.music.app.BuildConfig
import java.util.concurrent.Executor
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class BitPerfectUsbManager(
    context: Context,
    private val audioManager: AudioManager?,
    private val playbackAudioAttributes: AudioAttributes,
    private val policy: BitPerfectUsbPolicy = BitPerfectUsbPolicy(),
) {
    private val callbackExecutor = Executor { command -> command.run() }
    private val _status = MutableStateFlow(
        BitPerfectUsbStatus(
            policy.deriveState(Build.VERSION.SDK_INT, null, false, null, null, false, false).state,
        ),
    )
    val status: StateFlow<BitPerfectUsbStatus> = _status.asStateFlow()

    private var selectedUsbDevice: AudioDeviceInfo? = null
    private var currentTrackFormat: TrackPlaybackFormat? = null
    private var requestedFormatData: PlatformAudioFormatData? = null
    private var probeResult: UsbFormatProbeResult? = null
    private var applySucceeded = false
    private var verifiedActive = false
    private var lastErrorMessage: String? = null
    private var preferredMixerListenerHandle: Any? = null
    private val formatProbeCache = ConcurrentHashMap<String, UsbFormatProbeResult>()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            preferredMixerListenerHandle = Api34.registerPreferredMixerListener(
                manager = audioManager,
                executor = callbackExecutor,
            ) { deviceId, format, mixerBehavior ->
                if (deviceId != selectedUsbDevice?.id) return@registerPreferredMixerListener
                val requested = requestedFormatData
                val stillMatching = requested != null &&
                    format == requested &&
                    mixerBehavior == BitPerfectUsbPolicy.BIT_PERFECT_MIXER_BEHAVIOR
                if (!stillMatching) {
                    applySucceeded = false
                    verifiedActive = false
                    lastErrorMessage = null
                    updateStatus("preferred-mixer-attributes-changed")
                }
            }
        }
        refreshConnectedDevices()
    }

    fun refreshConnectedDevices() {
        val previousDeviceId = selectedUsbDevice?.id
        val descriptors = audioManager
            ?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            ?.map(::toDescriptor)
            .orEmpty()
        val selectedDescriptor = policy.selectUsbDevice(
            devices = descriptors,
            currentDeviceId = selectedUsbDevice?.id,
        )
        selectedUsbDevice = selectedDescriptor?.let(::findDeviceById)
        if (selectedUsbDevice?.id != previousDeviceId) {
            formatProbeCache.clear()
            probeResult = null
            applySucceeded = false
            verifiedActive = false
        }
        if (selectedUsbDevice == null) {
            probeResult = null
            formatProbeCache.clear()
            clearPreferredMixerAttributes()
        }
        applyCurrentConfiguration("usb-device-refresh")
    }

    fun updateTrackFormat(trackFormat: TrackPlaybackFormat?) {
        currentTrackFormat = trackFormat
        requestedFormatData = trackFormat?.let(policy::mapTrackFormat)
        probeResult = null
        applySucceeded = false
        verifiedActive = false
        lastErrorMessage = null
        applyCurrentConfiguration("track-format-update")
    }

    @androidx.annotation.OptIn(markerClass = [UnstableApi::class])
    fun onAudioTrackInitialized(audioTrackConfig: AudioSink.AudioTrackConfig) {
        val requested = requestedFormatData ?: run {
            verifiedActive = false
            updateStatus("audio-track-init-no-request")
            return
        }
        val matchesRequested = requested.sampleRateHz == audioTrackConfig.sampleRate &&
            requested.channelMask == audioTrackConfig.channelConfig &&
            requested.encoding == audioTrackConfig.encoding
        verifiedActive = applySucceeded && matchesRequested && selectedUsbDevice != null
        if (!matchesRequested && applySucceeded) {
            lastErrorMessage = null
        }
        updateStatus("audio-track-initialized")
    }

    fun onAudioSinkError(error: Exception) {
        verifiedActive = false
        lastErrorMessage = error.message ?: error.javaClass.simpleName
        updateStatus("audio-sink-error")
    }

    fun clearForStop() {
        currentTrackFormat = null
        requestedFormatData = null
        probeResult = null
        applySucceeded = false
        verifiedActive = false
        lastErrorMessage = null
        clearPreferredMixerAttributes()
        updateStatus("playback-stop")
    }

    fun release() {
        clearForStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Api34.unregisterPreferredMixerListener(audioManager, preferredMixerListenerHandle)
        }
    }

    fun preferredAudioDevice(): AudioDeviceInfo? = selectedUsbDevice

    fun shouldBypassProcessing(): Boolean = status.value.shouldBypassProcessing

    fun hasUsbOutputCandidate(): Boolean = selectedUsbDevice != null

    private fun applyCurrentConfiguration(reason: String) {
        val manager = audioManager
        val device = selectedUsbDevice
        val requested = requestedFormatData
        if (!policy.shouldAttemptPreferredDeviceRouting(Build.VERSION.SDK_INT) || manager == null || device == null || requested == null) {
            probeResult = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && (device == null || requested == null)) {
                clearPreferredMixerAttributes()
            }
            updateStatus(reason)
            return
        }

        probeResult = probeUsbFormatSupport(device, requested)
        val mixerMatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val supportedProfiles = Api34.getSupportedMixerProfiles(manager, device)
            policy.selectSupportedProfile(requested, supportedProfiles)
        } else {
            null
        }
        probeResult = probeResult?.copy(
            mixerAttributesSupported = mixerMatch != null,
            routeStrategy = when {
                mixerMatch != null -> UsbBitPerfectRouteStrategy.MixerAttributesBitPerfect
                probeResult?.exactFormatSupported == true && probeResult?.directPlaybackSupported == true -> UsbBitPerfectRouteStrategy.ExactFormatDirect
                probeResult?.exactFormatSupported == true -> UsbBitPerfectRouteStrategy.BestEffortPreferredDevice
                else -> UsbBitPerfectRouteStrategy.None
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && mixerMatch != null) {
            applySucceeded = Api34.setPreferredMixerAttributes(
                manager = manager,
                audioAttributes = playbackAudioAttributes,
                device = device,
                format = requested,
            )
        } else {
            clearPreferredMixerAttributes()
            applySucceeded = probeResult?.exactFormatSupported == true
        }
        verifiedActive = false
        lastErrorMessage = null
        updateStatus(reason)
        logDebug(
            "event=apply api=${Build.VERSION.SDK_INT} device=${device.displayName()} " +
                "sampleRate=${requested.sampleRateHz} encoding=${requested.encoding} channelMask=${requested.channelMask} " +
                "bitPerfectRequested=${mixerMatch != null} success=$applySucceeded strategy=${probeResult?.routeStrategy} " +
                "direct=${probeResult?.directPlaybackSupported} exact=${probeResult?.exactFormatSupported} " +
                "fallback=${status.value.fallbackReason.orEmpty()}",
        )
    }

    private fun clearPreferredMixerAttributes() {
        val manager = audioManager
        val device = selectedUsbDevice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && manager != null && device != null) {
            Api34.clearPreferredMixerAttributes(manager, playbackAudioAttributes, device)
        }
        applySucceeded = false
        verifiedActive = false
    }

    private fun updateStatus(reason: String) {
        val status = policy.deriveState(
            apiLevel = Build.VERSION.SDK_INT,
            selectedDevice = selectedUsbDevice?.let(::toDescriptor),
            trackFormatKnown = currentTrackFormat != null,
            requestedFormat = requestedFormatData,
            probeResult = probeResult,
            applySucceeded = applySucceeded,
            verifiedActive = verifiedActive,
            errorMessage = lastErrorMessage,
        )
        _status.value = status
        logDebug(
            "event=status reason=$reason state=${status.state.javaClass.simpleName} api=${Build.VERSION.SDK_INT} " +
                "device=${selectedUsbDevice?.displayName().orEmpty()} sampleRate=${status.requestedFormat?.sampleRateHz ?: -1} " +
                "encoding=${status.requestedFormat?.encoding ?: -1} channelMask=${status.requestedFormat?.channelMask ?: -1} " +
                "strategy=${status.routeStrategy} bypass=${status.shouldBypassProcessing} success=$applySucceeded " +
                "fallback=${status.fallbackReason.orEmpty()}",
        )
    }

    private fun toDescriptor(device: AudioDeviceInfo): UsbAudioDeviceDescriptor {
        return UsbAudioDeviceDescriptor(
            id = device.id,
            type = device.type,
            isSink = device.isSink,
            productName = device.productName?.toString(),
            sampleRates = device.sampleRates,
            encodings = device.encodings,
        )
    }

    private fun findDeviceById(descriptor: UsbAudioDeviceDescriptor): AudioDeviceInfo? {
        return audioManager
            ?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            ?.firstOrNull { it.id == descriptor.id }
    }

    private fun AudioDeviceInfo.displayName(): String {
        return buildString {
            append(productName?.toString().orEmpty().ifBlank { "USB-${id}" })
            append("#")
            append(id)
        }
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }

    private fun probeUsbFormatSupport(
        device: AudioDeviceInfo,
        requested: PlatformAudioFormatData,
    ): UsbFormatProbeResult {
        val cacheKey = buildProbeCacheKey(device, requested)
        formatProbeCache[cacheKey]?.let { return it }

        val directPlaybackSupported = supportsDirectPlayback(requested)
        val probeSucceeded = tryInitializeExactAudioTrack(device, requested)
        val result = policy.evaluateExactFormatSupport(
            selectedDevice = toDescriptor(device),
            requestedFormat = requested,
            directPlaybackSupported = directPlaybackSupported,
            audioTrackProbeSucceeded = probeSucceeded,
        )
        formatProbeCache[cacheKey] = result
        return result
    }

    @Suppress("DEPRECATION")
    private fun supportsDirectPlayback(requested: PlatformAudioFormatData): Boolean {
        val audioFormat = requested.toAudioFormat()
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && audioManager != null ->
                AudioManager.getDirectPlaybackSupport(audioFormat, playbackAudioAttributes) != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                AudioTrack.isDirectPlaybackSupported(audioFormat, playbackAudioAttributes)
            else -> false
        }
    }

    private fun tryInitializeExactAudioTrack(
        device: AudioDeviceInfo,
        requested: PlatformAudioFormatData,
    ): Boolean {
        val minBufferSize = AudioTrack.getMinBufferSize(
            requested.sampleRateHz,
            requested.channelMask,
            requested.encoding,
        )
        if (minBufferSize <= 0) return false
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(playbackAudioAttributes)
                .setAudioFormat(requested.toAudioFormat())
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
                .build()
        }.getOrNull() ?: return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                runCatching { track.preferredDevice = device }
            }
            track.state == AudioTrack.STATE_INITIALIZED
        } finally {
            runCatching { track.release() }
        }
    }

    private fun buildProbeCacheKey(
        device: AudioDeviceInfo,
        requested: PlatformAudioFormatData,
    ): String {
        return "${device.id}:${requested.sampleRateHz}:${requested.channelMask}:${requested.encoding}"
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private object Api34 {
        fun registerPreferredMixerListener(
            manager: AudioManager?,
            executor: Executor,
            onChange: (deviceId: Int, format: PlatformAudioFormatData?, mixerBehavior: Int) -> Unit,
        ): Any? {
            val listener = AudioManager.OnPreferredMixerAttributesChangedListener { _, device, mixerAttributes ->
                onChange(
                    device.id,
                    mixerAttributes?.let {
                        PlatformAudioFormatData(
                            sampleRateHz = it.format.sampleRate,
                            channelMask = it.format.channelMask,
                            encoding = it.format.encoding,
                            channelCount = it.format.channelCount,
                        )
                    },
                    mixerAttributes?.mixerBehavior ?: -1,
                )
            }
            manager?.addOnPreferredMixerAttributesChangedListener(executor, listener)
            return listener
        }

        fun unregisterPreferredMixerListener(
            manager: AudioManager?,
            listener: Any?,
        ) {
            val typedListener = listener as? AudioManager.OnPreferredMixerAttributesChangedListener ?: return
            manager?.removeOnPreferredMixerAttributesChangedListener(typedListener)
        }

        fun getSupportedMixerProfiles(
            manager: AudioManager,
            device: AudioDeviceInfo,
        ): List<SupportedMixerProfile> {
            return manager.getSupportedMixerAttributes(device).map { mixerAttributes ->
                SupportedMixerProfile(
                    format = PlatformAudioFormatData(
                        sampleRateHz = mixerAttributes.format.sampleRate,
                        channelMask = mixerAttributes.format.channelMask,
                        encoding = mixerAttributes.format.encoding,
                        channelCount = mixerAttributes.format.channelCount,
                    ),
                    mixerBehavior = mixerAttributes.mixerBehavior,
                )
            }
        }

        fun setPreferredMixerAttributes(
            manager: AudioManager,
            audioAttributes: AudioAttributes,
            device: AudioDeviceInfo,
            format: PlatformAudioFormatData,
        ): Boolean {
            val mixerAttributes = android.media.AudioMixerAttributes.Builder(
                AudioFormat.Builder()
                    .setSampleRate(format.sampleRateHz)
                    .setChannelMask(format.channelMask)
                    .setEncoding(format.encoding)
                    .build(),
            )
                .setMixerBehavior(AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT)
                .build()
            return manager.setPreferredMixerAttributes(audioAttributes, device, mixerAttributes)
        }

        fun clearPreferredMixerAttributes(
            manager: AudioManager,
            audioAttributes: AudioAttributes,
            device: AudioDeviceInfo,
        ) {
            runCatching {
                manager.clearPreferredMixerAttributes(audioAttributes, device)
            }
        }
    }

    private companion object {
        const val TAG = "BitPerfectUsb"
    }
}

private fun PlatformAudioFormatData.toAudioFormat(): AudioFormat {
    return AudioFormat.Builder()
        .setSampleRate(sampleRateHz)
        .setChannelMask(channelMask)
        .setEncoding(encoding)
        .build()
}
