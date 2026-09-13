package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioDeviceInfo

internal data class AudioOutputRouteSnapshot(
    val generation: Long,
    val routedDevices: List<AudioDeviceInfo>,
    val usbOutput: UsbAudioDeviceDescriptor?,
    val identity: List<AudioRouteDeviceIdentity>,
)

internal data class AudioRouteDeviceIdentity(
    val id: Int,
    val type: Int,
    val isSink: Boolean,
    val address: String,
)
