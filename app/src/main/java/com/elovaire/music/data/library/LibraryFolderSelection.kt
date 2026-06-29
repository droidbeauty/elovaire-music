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
)

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
        val seen = linkedSetOf<String>()
        return selections.mapNotNull { selection ->
            val path = selection.path.trim().replace('\\', '/').trimEnd('/')
            val uri = selection.uri?.toString()?.trim().orEmpty()
            val key = when {
                uri.isNotBlank() -> "uri:${uri.lowercase(Locale.ROOT)}"
                path.isNotBlank() -> "path:${path.lowercase(Locale.ROOT)}"
                else -> return@mapNotNull null
            }
            if (!seen.add(key)) return@mapNotNull null
            selection.copy(
                path = path,
                displayName = selection.displayName.trim().ifBlank { path.substringAfterLast('/').ifBlank { "Music" } },
            )
        }
    }

    fun accessibleFileRoots(selections: List<LibraryFolderSelection>): List<File> {
        return selections.asSequence()
            .mapNotNull { selection -> selection.path.takeIf { it.isNotBlank() }?.let(::File) }
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

    private val STORAGE_ROOT_REGEX = Regex("^/storage/[^/]+/|^/mnt/media_rw/[^/]+/")
}
