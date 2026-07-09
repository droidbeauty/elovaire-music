package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import java.util.Locale

data class LibraryFolderSelection(
    val uri: Uri?,
    val path: String,
    val displayName: String,
    val isDefaultMusicFolder: Boolean = false,
) {
    fun hasPersistedReadPermission(context: Context): Boolean {
        val targetUri = uri ?: return false
        return context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == targetUri && permission.isReadPermission
        }
    }

    fun isAvailable(context: Context): Boolean {
        val pathDirectory = path.takeUnless(LibraryFolderSelectionResolver::isUriBackedPath)?.let(::File)
        return when {
            uri != null -> hasPersistedReadPermission(context) || pathDirectory?.let { it.exists() && it.isDirectory } == true
            pathDirectory != null -> pathDirectory.exists() && pathDirectory.isDirectory
            else -> false
        }
    }
}

object LibraryFolderSelectionResolver {
    fun defaultMusicFolder(): LibraryFolderSelection {
        val musicDirectory = MediaFilePathResolver.defaultMusicDirectory()
        return LibraryFolderSelection(
            uri = null,
            path = musicDirectory.absolutePath,
            displayName = Environment.DIRECTORY_MUSIC,
            isDefaultMusicFolder = true,
        )
    }

    fun fromTreeUri(
        context: Context,
        uri: Uri,
    ): LibraryFolderSelection {
        val resolvedPath = resolveTreePath(context, uri).orEmpty()
        return LibraryFolderSelection(
            uri = uri,
            path = resolvedPath.ifBlank { uri.toString() },
            displayName = displayNameFor(uri, resolvedPath),
            isDefaultMusicFolder = false,
        )
    }

    fun normalize(selections: List<LibraryFolderSelection>): List<LibraryFolderSelection> {
        val seenUris = linkedSetOf<String>()
        val seenPaths = linkedSetOf<String>()
        val distinctSelections = selections.mapNotNull { selection ->
            val path = selection.path.trim().replace('\\', '/').trimEnd('/')
            val uri = selection.uri?.toString()?.trim().orEmpty()
            val uriKey = uri.takeIf(String::isNotBlank)?.lowercase(Locale.ROOT)
            val pathKey = path
                .takeIf { it.isNotBlank() && !isUriBackedPath(it) }
                ?.lowercase(Locale.ROOT)
            if (uriKey == null && pathKey == null) return@mapNotNull null
            if (uriKey != null && uriKey in seenUris) return@mapNotNull null
            if (pathKey != null && pathKey in seenPaths) return@mapNotNull null
            uriKey?.let(seenUris::add)
            pathKey?.let(seenPaths::add)
            selection.copy(
                path = path,
                displayName = selection.displayName.trim().ifBlank { path.substringAfterLast('/').ifBlank { "Music" } },
            )
        }
        return distinctSelections.filterNot { candidate ->
            val candidatePath = candidate.path.takeUnless(::isUriBackedPath) ?: return@filterNot false
            distinctSelections.any { possibleParent ->
                possibleParent !== candidate &&
                    (possibleParent.path.takeUnless(::isUriBackedPath)
                        ?.let { parentPath -> isSameOrChildPath(candidatePath, parentPath) } == true)
            }
        }
    }

    fun accessibleFileRoots(selections: List<LibraryFolderSelection>): List<File> {
        return selections.asSequence()
            .mapNotNull { selection -> selection.path.takeIf { it.isNotBlank() && !isUriBackedPath(it) }?.let(::File) }
            .filter { it.exists() && it.isDirectory }
            .distinctBy { it.absolutePath }
            .toList()
    }

    fun relativeRoots(selections: List<LibraryFolderSelection>): Set<String> {
        return selections.mapNotNullTo(linkedSetOf()) { selection ->
            val pathRoot = sharedStorageRelativePath(selection.path)
            val treeRoot = selection.uri?.let(::treeRelativePath)
            pathRoot ?: treeRoot
        }
    }

    fun normalizedPathKey(path: String): String {
        return path.trim().replace('\\', '/').trimEnd('/').lowercase(Locale.ROOT)
    }

    internal fun isSameOrChildPath(
        child: String,
        parent: String,
    ): Boolean {
        val normalizedChild = normalizedPathKey(child)
        val normalizedParent = normalizedPathKey(parent)
        return normalizedChild == normalizedParent || normalizedChild.startsWith("$normalizedParent/")
    }

    fun safSyntheticRoot(uri: Uri): String {
        return "saf/${uri.toString().hashCode().toUInt().toString(16)}"
    }

    fun isUriBackedPath(path: String): Boolean {
        return path.startsWith("content://", ignoreCase = true)
    }

    private fun resolveTreePath(
        context: Context,
        uri: Uri,
    ): String? {
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return null
        val volume = treeId.substringBefore(':', "")
        val relativePath = treeId.substringAfter(':', "").trim('/')
        val base = when {
            volume.equals("primary", ignoreCase = true) -> MediaFilePathResolver.primarySharedStorageRoot()
            volume.isNotBlank() -> context.getExternalFilesDirs(null)
                .orEmpty()
                .mapNotNull { file ->
                    generateSequence(file) { it.parentFile }
                        .firstOrNull { candidate -> candidate.name.equals(volume, ignoreCase = true) }
                }
                .firstOrNull()
            else -> null
        } ?: return null
        return if (relativePath.isBlank()) base.absolutePath else File(base, relativePath).absolutePath
    }

    private fun displayNameFor(
        uri: Uri,
        resolvedPath: String,
    ): String {
        resolvedPath.substringAfterLast('/').takeIf { it.isNotBlank() }?.let { return it }
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull().orEmpty()
        return treeId.substringAfter(':', treeId)
            .substringAfterLast('/')
            .ifBlank { "Library folder" }
    }

    private fun treeRelativePath(uri: Uri): String? {
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return null
        val relativePath = treeId.substringAfter(':', "").trim('/')
        return relativePath.takeIf { it.isNotBlank() }?.lowercase(Locale.ROOT)
    }

    private fun sharedStorageRelativePath(path: String): String? {
        val normalizedPath = path.trim().replace('\\', '/').trimEnd('/')
        return STORAGE_ROOT_REGEX
            .replace("$normalizedPath/", "")
            .trim('/')
            .lowercase(Locale.ROOT)
            .ifBlank { null }
    }

    private val STORAGE_ROOT_REGEX = Regex("^/storage/emulated/[^/]+/|^/storage/[^/]+/|^/mnt/media_rw/[^/]+/")
}
