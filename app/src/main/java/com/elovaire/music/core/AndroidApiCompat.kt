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
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

internal object AndroidCapabilities {
    const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

    fun supportsGroupedMediaWrite(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.R

    fun supportsImageDecoder(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.P

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU, parameter = 0)
    fun supportsDirectPlaybackQuery(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.TIRAMISU

    fun requiresMediaPlaybackForegroundServiceType(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.Q

    fun requiresLocalNetworkPermission(sdkInt: Int): Boolean = sdkInt >= 37
}

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

internal fun shouldLockPhoneOrientation(smallestScreenWidthDp: Int): Boolean = smallestScreenWidthDp < 600

internal fun Context.hasAudioReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, requiredAudioPermission()) == PackageManager.PERMISSION_GRANTED
}

internal fun Context.hasLocalNetworkPermission(): Boolean {
    if (!AndroidCapabilities.requiresLocalNetworkPermission(Build.VERSION.SDK_INT)) return true
    return ContextCompat.checkSelfPermission(
        this,
        AndroidCapabilities.LOCAL_NETWORK_PERMISSION,
    ) == PackageManager.PERMISSION_GRANTED
}

internal fun AudioManager.safeOutputDevices(): List<AudioDeviceInfo> {
    return runCatching {
        getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }.getOrDefault(emptyList())
        .filter { device -> runCatching { device.isSink }.getOrDefault(false) }
}

internal fun AudioManager.safeActiveRoutedOutputDevicesForAttributes(
    attributes: AudioAttributes,
): List<AudioDeviceInfo> {
    if (!AndroidCapabilities.supportsDirectPlaybackQuery(Build.VERSION.SDK_INT)) return emptyList()
    return runCatching { getAudioDevicesForAttributes(attributes) }
        .getOrDefault(emptyList())
        .filter { device -> runCatching { device.isSink }.getOrDefault(false) }
}

internal fun AudioManager.safeDirectPlaybackSupport(
    format: AudioFormat,
    attributes: AudioAttributes,
): Int {
    if (!AndroidCapabilities.supportsDirectPlaybackQuery(Build.VERSION.SDK_INT)) {
        return AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
    }
    return runCatching {
        AudioManager.getDirectPlaybackSupport(format, attributes)
    }.getOrDefault(AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED)
}

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return IntentCompat.getParcelableExtra(this, name, T::class.java)
}
