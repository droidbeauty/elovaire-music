package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.util.Locale

internal object MediaFilePathResolver {
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
        val projection = arrayOf(
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.VOLUME_NAME,
        )
        return contentResolver.queryMediaStoreFilePath(context, mediaUri, projection)
    }

    fun resolveMediaStoreFilePath(
        context: Context,
        relativePath: String?,
        displayName: String?,
        volumeName: String?,
    ): String? {
        return resolveMediaStoreFilePath(
            relativePath = relativePath,
            displayName = displayName,
            roots = sharedStorageRoots(context, volumeName),
        )
    }

    internal fun resolveMediaStoreFilePath(
        relativePath: String?,
        displayName: String?,
        roots: List<File>,
    ): String? {
        val normalizedName = displayName?.trim()?.ifBlank { null } ?: return null
        if (normalizedName == "." || normalizedName == ".." || '/' in normalizedName || '\\' in normalizedName) {
            return null
        }
        val normalizedRelativePath = relativePath
            ?.trim()
            ?.replace('\\', '/')
            ?.trim('/')
            ?.ifBlank { null }
        roots.forEach { root ->
            val childPath = normalizedRelativePath
                ?.let { "$it/$normalizedName" }
                ?: normalizedName
            val candidate = root.resolveContainedChild(childPath) ?: return@forEach
            if (candidate.isFile) return candidate.absolutePath
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
        val primaryRoot = primarySharedStorageRoot()
        val roots = linkedSetOf<File>()
        primaryRoot
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
        return mediaStoreRootsForVolume(roots.toList(), primaryRoot, volumeName)
    }

    internal fun sharedStorageRootsForScan(context: Context, volumeName: String?): List<File> {
        return sharedStorageRoots(context, volumeName)
    }

    private fun android.database.Cursor.optionalString(columnName: String): String? {
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex < 0 || isNull(columnIndex)) return null
        return getString(columnIndex)?.trim()?.ifBlank { null }
    }
}

internal fun mediaStoreRootsForVolume(
    roots: List<File>,
    primaryRoot: File,
    volumeName: String?,
): List<File> {
    val normalizedVolumeName = volumeName?.trim()?.lowercase(Locale.ROOT).orEmpty()
    return when {
        normalizedVolumeName.isBlank() -> roots
        normalizedVolumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY.lowercase(Locale.ROOT) ->
            roots.filter { it.absoluteFile == primaryRoot.absoluteFile }
        else -> roots.filter { it.name.lowercase(Locale.ROOT) == normalizedVolumeName }
    }
}

internal fun File.resolveContainedChild(relativePath: String): File? = try {
    val canonicalRoot = canonicalFile
    val candidate = File(canonicalRoot, relativePath).canonicalFile
    candidate.takeIf { it.toPath().startsWith(canonicalRoot.toPath()) }
} catch (_: IOException) {
    null
} catch (_: SecurityException) {
    null
}

internal fun cachedRootsForVolume(
    cache: MutableMap<String, List<File>>,
    volumeKey: String,
    resolve: () -> List<File>,
): List<File> {
    cache[volumeKey]?.takeIf { it.isNotEmpty() }?.let { return it }
    val roots = resolve()
    if (roots.isNotEmpty() && cache.size < MAX_VOLUME_ROOT_CACHE_ENTRIES) cache[volumeKey] = roots
    return roots
}

private const val MAX_VOLUME_ROOT_CACHE_ENTRIES = 8

internal class MediaStoreScanPathResolver(
    private val context: Context,
) {
    private val rootsByVolume = HashMap<String, List<File>>()

    fun resolve(
        relativePath: String?,
        displayName: String?,
        volumeName: String?,
    ): String? {
        val cacheKey = volumeName?.trim()?.lowercase(Locale.ROOT).orEmpty()
        val roots = cachedRootsForVolume(rootsByVolume, cacheKey) {
            MediaFilePathResolver.sharedStorageRootsForScan(context, volumeName)
        }
        return MediaFilePathResolver.resolveMediaStoreFilePath(
            relativePath = relativePath,
            displayName = displayName,
            roots = roots,
        )
    }
}

internal fun ContentResolver.queryMediaStoreFilePath(
    context: Context,
    mediaUri: Uri,
): String? {
    return MediaFilePathResolver.queryMediaStoreFilePath(this, context, mediaUri)
}
