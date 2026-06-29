package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

internal fun requiredAudioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

internal fun Context.hasAudioReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, requiredAudioPermission()) == PackageManager.PERMISSION_GRANTED
}

internal fun Context.hasNotificationPostingPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return IntentCompat.getParcelableExtra(this, name, T::class.java)
}
