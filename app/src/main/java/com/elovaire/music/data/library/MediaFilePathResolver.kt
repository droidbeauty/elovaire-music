package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

internal object MediaFilePathResolver {
    @Suppress("DEPRECATION")
    val dataColumn: String = MediaStore.MediaColumns.DATA

    @Suppress("DEPRECATION")
    fun defaultMusicDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    }

    @Suppress("DEPRECATION")
    fun primarySharedStorageRoot(): File {
        return Environment.getExternalStorageDirectory()
    }

    fun queryMediaStoreFilePath(
        contentResolver: ContentResolver,
        context: Context,
        mediaUri: Uri,
    ): String? {
        if (mediaUri.scheme == ContentResolver.SCHEME_FILE) {
            return mediaUri.path?.takeIf { File(it).exists() }
        }
        val preferredProjection = arrayOf(
            dataColumn,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.VOLUME_NAME,
        )
        val fallbackProjection = arrayOf(
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.VOLUME_NAME,
        )
        return contentResolver.queryMediaStoreFilePath(context, mediaUri, preferredProjection)
            ?: contentResolver.queryMediaStoreFilePath(context, mediaUri, fallbackProjection)
    }

    fun resolveMediaStoreFilePath(
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
                    rawDataPath = cursor.optionalString(dataColumn),
                    relativePath = cursor.optionalString(MediaStore.MediaColumns.RELATIVE_PATH),
                    displayName = cursor.optionalString(MediaStore.MediaColumns.DISPLAY_NAME),
                    volumeName = cursor.optionalString(MediaStore.MediaColumns.VOLUME_NAME),
                )
            }
        }.getOrNull()
    }

    private fun sharedStorageRoots(
        context: Context,
        volumeName: String?,
    ): List<File> {
        val roots = linkedSetOf<File>()
        primarySharedStorageRoot()
            .takeIf { it.exists() && it.isDirectory }
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
}

internal fun ContentResolver.queryMediaStoreFilePath(
    context: Context,
    mediaUri: Uri,
): String? {
    return MediaFilePathResolver.queryMediaStoreFilePath(this, context, mediaUri)
}
