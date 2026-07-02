package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun AudioManager.routedDevicesForAttributes(attributes: AudioAttributes): List<AudioDeviceInfo> {
    return getAudioDevicesForAttributes(attributes)
}

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return IntentCompat.getParcelableExtra(this, name, T::class.java)
}
