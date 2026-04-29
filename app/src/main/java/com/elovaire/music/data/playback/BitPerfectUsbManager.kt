package elovaire.music.app.data.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioMixerAttributes
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import elovaire.music.app.BuildConfig
import java.util.concurrent.Executor
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
    private var applySucceeded = false
    private var verifiedActive = false
    private var lastErrorMessage: String? = null
    private var preferredMixerListenerHandle: Any? = null

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
        val descriptors = audioManager
            ?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            ?.map(::toDescriptor)
            .orEmpty()
        val selectedDescriptor = policy.selectUsbDevice(
            devices = descriptors,
            currentDeviceId = selectedUsbDevice?.id,
        )
        selectedUsbDevice = selectedDescriptor?.let(::findDeviceById)
        if (selectedUsbDevice == null) {
            clearPreferredMixerAttributes()
        }
        applyCurrentConfiguration("usb-device-refresh")
    }

    fun updateTrackFormat(trackFormat: TrackPlaybackFormat?) {
        currentTrackFormat = trackFormat
        requestedFormatData = trackFormat?.let(policy::mapTrackFormat)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE || manager == null || device == null || requested == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && (device == null || requested == null)) {
                clearPreferredMixerAttributes()
            }
            updateStatus(reason)
            return
        }
        val supportedProfiles = Api34.getSupportedMixerProfiles(manager, device)
        val matchedProfile = policy.selectSupportedProfile(requested, supportedProfiles)
        if (matchedProfile == null) {
            clearPreferredMixerAttributes()
            updateStatus(reason)
            logDebug(
                "event=no_supported_profile api=${Build.VERSION.SDK_INT} device=${device.displayName()} " +
                    "sampleRate=${requested.sampleRateHz} encoding=${requested.encoding} channelMask=${requested.channelMask}",
            )
            return
        }
        val applied = Api34.setPreferredMixerAttributes(
            manager = manager,
            audioAttributes = playbackAudioAttributes,
            device = device,
            format = requested,
        )
        applySucceeded = applied
        verifiedActive = false
        lastErrorMessage = null
        updateStatus(reason)
        logDebug(
            "event=apply api=${Build.VERSION.SDK_INT} device=${device.displayName()} " +
                "sampleRate=${requested.sampleRateHz} encoding=${requested.encoding} channelMask=${requested.channelMask} " +
                "bitPerfectRequested=true success=$applied fallback=${status.value.fallbackReason.orEmpty()}",
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
            matchedProfile = matchedProfile(),
            applySucceeded = applySucceeded,
            verifiedActive = verifiedActive,
            errorMessage = lastErrorMessage,
        )
        _status.value = status
        logDebug(
            "event=status reason=$reason state=${status.state.javaClass.simpleName} api=${Build.VERSION.SDK_INT} " +
                "device=${selectedUsbDevice?.displayName().orEmpty()} sampleRate=${status.requestedFormat?.sampleRateHz ?: -1} " +
                "encoding=${status.requestedFormat?.encoding ?: -1} channelMask=${status.requestedFormat?.channelMask ?: -1} " +
                "bitPerfectRequested=${matchedProfile() != null} success=$applySucceeded fallback=${status.fallbackReason.orEmpty()}",
        )
    }

    private fun matchedProfile(): SupportedMixerProfile? {
        val manager = audioManager
        val device = selectedUsbDevice
        val requested = requestedFormatData
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE || manager == null || device == null || requested == null) {
            return null
        }
        return policy.selectSupportedProfile(requested, Api34.getSupportedMixerProfiles(manager, device))
    }

    private fun toDescriptor(device: AudioDeviceInfo): UsbAudioDeviceDescriptor {
        return UsbAudioDeviceDescriptor(
            id = device.id,
            type = device.type,
            isSink = device.isSink,
            productName = device.productName?.toString(),
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
