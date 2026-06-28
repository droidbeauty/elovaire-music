package elovaire.music.droidbeauty.app.data.library

import java.io.File
import java.util.Locale

internal class LibraryScanRoots(
    selections: List<LibraryFolderSelection> = listOf(LibraryFolderSelectionResolver.defaultMusicFolder()),
) {
    private var selectedFolders: List<LibraryFolderSelection> = LibraryFolderSelectionResolver.normalize(selections)

    fun setSelections(selections: List<LibraryFolderSelection>): Boolean {
        val normalized = LibraryFolderSelectionResolver.normalize(selections)
        if (selectedFolders == normalized) return false
        selectedFolders = normalized
        return true
    }

    fun filterFingerprint(version: Int): String {
        return listOf(
            version.toString(),
            selectedFolders.joinToString("|") { selection ->
                listOf(selection.uri?.toString().orEmpty(), normalizeAbsolutePath(selection.path)).joinToString("@")
            },
        ).joinToString("::")
    }

    fun accessibleFileRoots(): List<File> {
        return LibraryFolderSelectionResolver.accessibleFileRoots(selectedFolders)
    }

    fun relativeRoots(): Set<String> {
        return LibraryFolderSelectionResolver.relativeRoots(selectedFolders)
    }

    fun normalizedFileRootPaths(): Set<String> {
        return accessibleFileRoots()
            .map { normalizeAbsolutePath(it.absolutePath) }
            .toSet()
    }

    private fun normalizeAbsolutePath(path: String): String {
        return path
            .trim()
            .replace('\\', '/')
            .trimEnd('/')
            .lowercase(Locale.ROOT)
    }
}
