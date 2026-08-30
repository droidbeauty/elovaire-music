package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
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
            uri != null -> hasPersistedReadPermission(context)
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
            // Authorities are case-insensitive, but document IDs and provider-specific
            // URI payloads are opaque and must retain their exact identity.
            val uriKey = uri.takeIf(String::isNotBlank)?.let(::normalizedUriIdentity)
            val pathKey = path
                .takeIf { uriKey == null && it.isNotBlank() && !isUriBackedPath(it) }
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
            if (candidate.uri != null) return@filterNot false
            val candidatePath = candidate.path.takeUnless(::isUriBackedPath) ?: return@filterNot false
            distinctSelections.any { possibleParent ->
                possibleParent !== candidate &&
                    possibleParent.uri == null &&
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
        val authority = uri.authority?.lowercase(Locale.ROOT).orEmpty()
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }
            .getOrNull()
        val identity = if (documentId != null) {
            "$authority\u0000$documentId"
        } else {
            normalizedUriIdentity(uri.toString())
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(identity.toByteArray(StandardCharsets.UTF_8))
        return "saf/${digest.toHexString()}"
    }

    fun isUriBackedPath(path: String): Boolean {
        return path.startsWith("content://", ignoreCase = true)
    }

    private fun normalizedUriIdentity(value: String): String {
        return runCatching {
            val parsed = Uri.parse(value)
            buildString {
                parsed.scheme?.lowercase(Locale.ROOT)?.let {
                    append(it)
                    append(':')
                }
                parsed.authority?.lowercase(Locale.ROOT)?.let {
                    append("//")
                    append(it)
                }
                parsed.encodedPath?.let(::append)
                parsed.encodedQuery?.let {
                    append('?')
                    append(it)
                }
                parsed.encodedFragment?.let {
                    append('#')
                    append(it)
                }
            }.ifBlank { value }
        }.getOrDefault(value)
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

private fun ByteArray.toHexString(): String = buildString(size * 2) {
    for (byte in this@toHexString) {
        append(((byte.toInt() ushr 4) and 0x0f).toString(16))
        append((byte.toInt() and 0x0f).toString(16))
    }
}
