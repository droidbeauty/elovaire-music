package elovaire.music.droidbeauty.app.core

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File

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

@Suppress("DEPRECATION")
internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name) as? T
    }
}

internal fun ContentResolver.queryMediaStoreFilePath(
    context: Context,
    mediaUri: Uri,
): String? {
    if (mediaUri.scheme == "file") {
        return mediaUri.path?.takeIf { File(it).exists() }
    }
    val preferredProjection = arrayOf(
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.VOLUME_NAME,
    )
    val fallbackProjection = arrayOf(
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.VOLUME_NAME,
    )
    return queryMediaStoreFilePath(context, mediaUri, preferredProjection)
        ?: queryMediaStoreFilePath(context, mediaUri, fallbackProjection)
}

private fun ContentResolver.queryMediaStoreFilePath(
    context: Context,
    mediaUri: Uri,
    projection: Array<String>,
): String? {
    return runCatching {
        query(mediaUri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            resolveMediaStoreFilePath(
                context = context,
                rawDataPath = cursor.optionalString(MediaStore.MediaColumns.DATA),
                relativePath = cursor.optionalString(MediaStore.MediaColumns.RELATIVE_PATH),
                displayName = cursor.optionalString(MediaStore.MediaColumns.DISPLAY_NAME),
                volumeName = cursor.optionalString(MediaStore.MediaColumns.VOLUME_NAME),
            )
        }
    }.getOrNull()
}

internal fun resolveMediaStoreFilePath(
    context: Context,
    rawDataPath: String?,
    relativePath: String?,
    displayName: String?,
    volumeName: String?,
): String? {
    val directPath = rawDataPath
        ?.trim()
        ?.ifBlank { null }
        ?.let(::File)
        ?.takeIf(File::exists)
        ?.absolutePath
    if (directPath != null) return directPath

    val normalizedName = displayName?.trim()?.ifBlank { null } ?: return null
    val normalizedRelativePath = relativePath
        ?.trim()
        ?.replace('\\', '/')
        ?.trim('/')
        ?.ifBlank { null }
    sharedStorageRoots(context, volumeName).forEach { root ->
        val candidate = normalizedRelativePath
            ?.let { File(File(root, it), normalizedName) }
            ?: File(root, normalizedName)
        if (candidate.exists()) {
            return candidate.absolutePath
        }
    }
    return null
}

@Suppress("DEPRECATION")
private fun sharedStorageRoots(
    context: Context,
    volumeName: String?,
): List<File> {
    val roots = linkedSetOf<File>()
    Environment.getExternalStorageDirectory()
        ?.takeIf { it.exists() && it.isDirectory }
        ?.let(roots::add)
    context.getExternalFilesDirs(null)
        .orEmpty()
        .mapNotNull { directory ->
            directory
                ?.parentFile
                ?.parentFile
                ?.parentFile
                ?.parentFile
                ?.takeIf { it.exists() && it.isDirectory }
        }
        .forEach(roots::add)
    if (roots.isEmpty()) return emptyList()

    val normalizedVolumeName = volumeName?.trim()?.lowercase().orEmpty()
    if (normalizedVolumeName.isBlank() || normalizedVolumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY.lowercase()) {
        return roots.toList()
    }
    return roots.sortedByDescending { root ->
        root.absolutePath.lowercase().contains(normalizedVolumeName)
    }
}

private fun android.database.Cursor.optionalString(columnName: String): String? {
    val columnIndex = getColumnIndex(columnName)
    if (columnIndex < 0 || isNull(columnIndex)) return null
    return getString(columnIndex)?.trim()?.ifBlank { null }
}
