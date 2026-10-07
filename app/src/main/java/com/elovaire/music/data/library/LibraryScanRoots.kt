package elovaire.music.droidbeauty.app.data.library

import java.io.File

internal class LibraryScanRoots(
    selections: List<LibraryFolderSelection> = listOf(LibraryFolderSelectionResolver.defaultMusicFolder()),
) {
    private var selectedFolders: List<LibraryFolderSelection> = LibraryFolderSelectionResolver.normalize(selections)
    private var cachedFilterFingerprintVersion: Int? = null
    private var cachedFilterFingerprint: String? = null

    fun setSelections(selections: List<LibraryFolderSelection>): Boolean {
        val normalized = LibraryFolderSelectionResolver.normalize(selections)
        if (selectedFolders == normalized) return false
        selectedFolders = normalized
        cachedFilterFingerprintVersion = null
        cachedFilterFingerprint = null
        return true
    }

    fun selections(): List<LibraryFolderSelection> = selectedFolders

    fun filterFingerprint(version: Int): String {
        if (cachedFilterFingerprintVersion == version) {
            return requireNotNull(cachedFilterFingerprint)
        }
        val fields = buildList {
            add(version.toString())
            selectedFolders.forEach { selection ->
                val path = selection.path.takeUnless(LibraryFolderSelectionResolver::isUriBackedPath).orEmpty()
                add(selection.uri?.toString().orEmpty())
                add(normalizeAbsolutePath(path))
            }
        }
        return fields.joinToString(separator = "") { "${it.length}:$it" }.also {
            cachedFilterFingerprintVersion = version
            cachedFilterFingerprint = it
        }
    }

    fun accessibleFileRoots(): List<File> {
        return LibraryFolderSelectionResolver.accessibleFileRoots(selectedFolders)
    }

    fun directFileRoots(): List<File> {
        return LibraryFolderSelectionResolver.accessibleFileRoots(
            selectedFolders.filter { it.uri == null },
        )
    }

    /**
     * MediaScanner repair is only useful for path-backed custom roots. The default Music source
     * is already represented by the authoritative MediaStore query, and SAF trees have their own
     * provider traversal; neither should trigger a recursive filesystem walk when folders change.
     */
    fun requiresMediaIndexRepair(): Boolean {
        return selectedFolders.any { it.uri == null && !it.isDefaultMusicFolder }
    }

    fun relativeRoots(): Set<String> {
        return LibraryFolderSelectionResolver.relativeRoots(selectedFolders)
    }

    fun normalizedFileRootPaths(): Set<String> {
        return accessibleFileRoots()
            .map { normalizeAbsolutePath(it.absolutePath) }
            .toSet()
    }

    fun explicitCustomFileRootPaths(): Set<String> {
        return selectedFolders
            .filterNot(LibraryFolderSelection::isDefaultMusicFolder)
            .mapNotNull { selection ->
                selection.path
                    .takeIf { it.isNotBlank() && !LibraryFolderSelectionResolver.isUriBackedPath(it) }
                    ?.let(::normalizeAbsolutePath)
            }
            .toSet()
    }

    fun explicitCustomRelativeRoots(): Set<String> {
        return LibraryFolderSelectionResolver.relativeRoots(
            selectedFolders.filterNot(LibraryFolderSelection::isDefaultMusicFolder),
        )
    }

    fun safTreeSelections(): List<LibraryFolderSelection> {
        return selectedFolders.filter { it.uri != null }
    }

    fun hasSafSelections(): Boolean = selectedFolders.any { it.uri != null }

    fun canIncludeUnscopedMediaStoreRows(): Boolean =
        selectedFolders.size == 1 && selectedFolders.single().let {
            it.uri == null && it.isDefaultMusicFolder
        }

    private fun normalizeAbsolutePath(path: String): String {
        return normalizeLibraryAbsolutePath(path).orEmpty()
    }
}

internal fun normalizeLibraryAbsolutePath(path: String): String? = path
    .trim()
    .replace('\\', '/')
    .trimEnd('/')
    .takeIf(String::isNotBlank)

internal fun normalizeLibraryRelativePath(path: String?): String? = path
    ?.trim()
    ?.replace('\\', '/')
    ?.trim('/')
    ?.takeIf(String::isNotBlank)
