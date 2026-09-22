package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope

/** Owns mutable output-route, gain, USB-DAC, and direct-playback policy state. */
@OptIn(UnstableApi::class)
internal class PlaybackAudioOutputController(
    context: Context,
    private val audioManager: AudioManager?,
    usbManager: UsbManager?,
    scope: CoroutineScope,
    playbackAudioAttributes: AudioAttributes,
) {
    val usbDacHardwareVolumeManager = UsbDacHardwareVolumeManager(
        context = context.applicationContext,
        audioManager = audioManager,
        usbManager = usbManager,
        scope = scope,
    )
    val bitPerfectUsbManager = BitPerfectUsbManager(
        audioManager = audioManager,
        playbackAudioAttributes = playbackAudioAttributes,
    )

    var userVolume: Float = currentSystemVolumeFraction()
    var volumeFineGain: Float = 1f
    var ignoreObservedSystemVolumeStep: Int? = null
    var isDirectPlaybackActive: Boolean = false
    var lastAppliedPreferredDeviceKey: PreferredAudioDeviceKey? = null
    var lastAppliedAudioPathDecisionKey: AudioPathDecisionKey? = null
    var outputCapabilities: AudioOutputCapabilitySnapshot = AudioOutputCapabilitySnapshot.Unknown
    var audioRouteGeneration: Long = 0L
    var audioRouteIdentity: List<AudioRouteDeviceIdentity> = emptyList()
    var currentAudioRouteSnapshot = AudioOutputRouteSnapshot(0L, emptyList(), null, emptyList())

    fun release() {
        usbDacHardwareVolumeManager.release()
    }

    fun usesFixedVolumeOutput(): Boolean {
        return runCatching { audioManager?.isVolumeFixed == true }.getOrDefault(false)
    }

    fun currentSystemVolumeStep(): Int {
        val manager = audioManager ?: return 0
        return runCatching {
            manager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }.getOrDefault(0).coerceAtLeast(0)
    }

    fun currentSystemVolumeFraction(): Float {
        val manager = audioManager ?: return 1f
        val maxStep = runCatching {
            manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }.getOrDefault(1).coerceAtLeast(1)
        val currentStep = currentSystemVolumeStep()
        return currentStep.toFloat() / maxStep.toFloat()
    }
}
