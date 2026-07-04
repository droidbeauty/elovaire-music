package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Build
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

internal fun requiredAudioPermission(): String {
    return requiredAudioPermission(Build.VERSION.SDK_INT)
}

internal fun requiredAudioPermission(sdkInt: Int): String {
    return if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

internal fun Context.hasAudioReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, requiredAudioPermission()) == PackageManager.PERMISSION_GRANTED
}

internal fun Context.hasNotificationPostingPermission(): Boolean {
    return !requiresNotificationPostingPermission(Build.VERSION.SDK_INT) ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

internal fun requiresNotificationPostingPermission(sdkInt: Int): Boolean {
    return sdkInt >= Build.VERSION_CODES.TIRAMISU
}

internal fun supportsVerifiedDirectPlaybackRouting(sdkInt: Int): Boolean {
    return sdkInt >= Build.VERSION_CODES.TIRAMISU
}

internal fun AudioManager.safeOutputDevices(): List<AudioDeviceInfo> {
    return runCatching {
        getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }.getOrDefault(emptyList())
        .filter { device -> runCatching { device.isSink }.getOrDefault(false) }
}

internal fun AudioManager.safeRoutedOutputDevicesForAttributes(attributes: AudioAttributes): List<AudioDeviceInfo> {
    val routedDevices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runCatching { getAudioDevicesForAttributes(attributes) }.getOrDefault(emptyList())
    } else {
        emptyList()
    }
    return (routedDevices.ifEmpty { safeOutputDevices() })
        .filter { device -> runCatching { device.isSink }.getOrDefault(false) }
}

internal fun AudioManager.safeDirectPlaybackSupport(
    format: AudioFormat,
    attributes: AudioAttributes,
): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
    }
    return runCatching {
        AudioManager.getDirectPlaybackSupport(format, attributes)
    }.getOrDefault(AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED)
}

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return IntentCompat.getParcelableExtra(this, name, T::class.java)
}
