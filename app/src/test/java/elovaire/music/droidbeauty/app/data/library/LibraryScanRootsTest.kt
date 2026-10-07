package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryScanRootsTest {
    @Test
    fun libraryPathNormalizationIsSharedAcrossScanCode() {
        assertEquals("/Storage/Music", normalizeLibraryAbsolutePath(" \\Storage\\Music\\ "))
        assertEquals("Music/Live", normalizeLibraryRelativePath(" /Music/Live/ "))
        assertEquals(null, normalizeLibraryRelativePath(" / / "))
        assertEquals(null, normalizeLibraryAbsolutePath("/"))
    }

    @Test
    fun normalize_keepsCaseDistinctFileRootsSeparate() {
        val selections = LibraryFolderSelectionResolver.normalize(
            listOf(
                LibraryFolderSelection(null, "/storage/emulated/0/Music/FLAC", "Upper"),
                LibraryFolderSelection(null, "/storage/emulated/0/Music/flac", "Lower"),
            ),
        )

        assertEquals(2, selections.size)
    }

    @Test
    fun unscopedMediaStoreRowsAreAllowedOnlyForTheDefaultMusicRoot() {
        val defaultMusic = LibraryFolderSelection(
            uri = null,
            path = "/storage/emulated/0/Music",
            displayName = "Music",
            isDefaultMusicFolder = true,
        )

        assertTrue(LibraryScanRoots(listOf(defaultMusic)).canIncludeUnscopedMediaStoreRows())
        assertFalse(
            LibraryScanRoots(listOf(defaultMusic.copy(isDefaultMusicFolder = false)))
                .canIncludeUnscopedMediaStoreRows(),
        )
        assertFalse(LibraryScanRoots(emptyList()).canIncludeUnscopedMediaStoreRows())
    }

    @Test
    fun normalize_keepsParentPathAndDropsChildPath() {
        val selections = LibraryFolderSelectionResolver.normalize(
            listOf(
                LibraryFolderSelection(null, "/storage/emulated/0/Music/Album", "Album"),
                LibraryFolderSelection(null, "/storage/emulated/0/Music", "Music"),
            ),
        )

        assertEquals(listOf("/storage/emulated/0/Music"), selections.map(LibraryFolderSelection::path))
    }

    @Test
    fun normalize_keepsUnresolvedContentRootsSeparate() {
        val selections = LibraryFolderSelectionResolver.normalize(
            listOf(
                LibraryFolderSelection(TestUri("content://tree/one"), "content://tree/one", "One"),
                LibraryFolderSelection(TestUri("content://tree/two"), "content://tree/two", "Two"),
            ),
        )

        assertEquals(2, selections.size)
        assertTrue(selections.all { it.path.startsWith("content://") })
    }

    @Test
    fun musicSelection_exposesMusicRelativeRoot() {
        val roots = LibraryScanRoots(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                    isDefaultMusicFolder = true,
                ),
            ),
        )

        assertEquals(setOf("Music"), roots.relativeRoots())
    }

    @Test
    fun directFileRoots_excludesResolvedSafPaths() {
        val roots = LibraryScanRoots(
            listOf(
                LibraryFolderSelection(
                    uri = TestUri("content://tree/music"),
                    path = File(".").absolutePath,
                    displayName = "Music",
                ),
            ),
        )

        assertTrue(roots.directFileRoots().isEmpty())
    }

    @Test
    fun requiresMediaIndexRepair_onlyForCustomPathRoots() {
        val roots = LibraryScanRoots(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                    isDefaultMusicFolder = true,
                ),
                LibraryFolderSelection(
                    uri = TestUri("content://tree/music"),
                    path = "content://tree/music",
                    displayName = "Music",
                ),
            ),
        )

        assertFalse(roots.requiresMediaIndexRepair())

        roots.setSelections(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Recordings",
                    displayName = "Recordings",
                ),
            ),
        )

        assertTrue(roots.requiresMediaIndexRepair())
    }

    @Test
    fun setSelections_deduplicatesEquivalentUris() {
        val roots = LibraryScanRoots(emptyList())
        roots.setSelections(
            listOf(
                LibraryFolderSelection(
                    uri = TestUri("content://tree/music"),
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                ),
                LibraryFolderSelection(
                    uri = TestUri("content://tree/music"),
                    path = "/storage/emulated/0/Music/",
                    displayName = "Music duplicate",
                ),
            ),
        )

        assertEquals(1, roots.selections().size)
    }

    @Test
    fun setSelections_keepsTreeUriWithEquivalentPathRoot() {
        val roots = LibraryScanRoots(emptyList())
        roots.setSelections(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                ),
                LibraryFolderSelection(
                    uri = TestUri("content://tree/primary%3AMusic"),
                    path = "/storage/emulated/0/Music/",
                    displayName = "Music duplicate",
                ),
            ),
        )

        assertEquals(2, roots.selections().size)
    }

    @Test
    fun setSelections_keepsSafChildWhenDefaultMusicPathIsSelected() {
        val selections = LibraryFolderSelectionResolver.normalize(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                ),
                LibraryFolderSelection(
                    uri = TestUri("content://tree/primary%3AMusic%2FSubfolder"),
                    path = "/storage/emulated/0/Music/Subfolder",
                    displayName = "Subfolder",
                ),
            ),
        )

        assertEquals(2, selections.size)
        assertTrue(selections.any { it.uri == null })
        assertTrue(selections.any { it.uri != null })
    }

    @Test
    fun isLastUriSelection_doesNotReleaseAUriStillReferencedByAnotherEntry() {
        val uri = TestUri("content://tree/music")
        val first = LibraryFolderSelection(uri, "/storage/emulated/0/Music", "Music")
        val second = first.copy(displayName = "Music mirror")

        assertFalse(LibraryFolderSelectionResolver.isLastUriSelection(first, listOf(first, second)))
        assertTrue(LibraryFolderSelectionResolver.isLastUriSelection(first, listOf(first)))
        assertFalse(LibraryFolderSelectionResolver.isLastUriSelection(
            LibraryFolderSelection(null, "/storage/emulated/0/Music", "Music"),
            listOf(first),
        ))
    }

    @Test
    fun safSyntheticRoot_isStableForTheSameTreeUri() {
        val tree = TestUri("content://com.android.externalstorage.documents/tree/primary%3AMusic")

        assertEquals(
            LibraryFolderSelectionResolver.safSyntheticRoot(tree),
            LibraryFolderSelectionResolver.safSyntheticRoot(tree),
        )
    }

    @Test
    fun safSyntheticRoot_separatesProvidersAndOpaqueTreeIds() {
        val provider = TestUri("content://com.android.externalstorage.documents/tree/primary%3AMusic")
        val otherProvider = TestUri("content://com.example.documents/tree/primary%3AMusic")
        val otherTree = TestUri("content://com.android.externalstorage.documents/tree/primary%3AMusic%2FSubfolder")

        assertNotEquals(
            LibraryFolderSelectionResolver.safSyntheticRoot(provider),
            LibraryFolderSelectionResolver.safSyntheticRoot(otherProvider),
        )
        assertNotEquals(
            LibraryFolderSelectionResolver.safSyntheticRoot(provider),
            LibraryFolderSelectionResolver.safSyntheticRoot(otherTree),
        )
    }

    @Test
    fun setSelections_keepsUnresolvedTreeUriRoot() {
        val roots = LibraryScanRoots(emptyList())
        roots.setSelections(
            listOf(
                LibraryFolderSelection(
                    uri = TestUri("content://tree/removable%3AMusic"),
                    path = "content://tree/removable%3AMusic",
                    displayName = "Music",
                ),
            ),
        )

        assertTrue(roots.filterFingerprint(version = 10).isNotBlank())
    }

    @Test
    fun filterFingerprintFramesFolderBoundariesContainingSeparators() {
        val singleRoot = LibraryScanRoots(emptyList()).apply {
            setSelections(
                listOf(
                    LibraryFolderSelection(
                        uri = null,
                        path = "/music|@/jazz",
                        displayName = "Jazz",
                    ),
                ),
            )
        }
        val splitRoots = LibraryScanRoots(emptyList()).apply {
            setSelections(
                listOf(
                    LibraryFolderSelection(uri = null, path = "/music", displayName = "Music"),
                    LibraryFolderSelection(uri = null, path = "/jazz", displayName = "Jazz"),
                ),
            )
        }

        assertTrue(singleRoot.filterFingerprint(version = 10) != splitRoots.filterFingerprint(version = 10))
    }

    @Test
    fun filterFingerprint_reflectsSelectedFolders() {
        val roots = LibraryScanRoots(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Music",
                    displayName = "Music",
                ),
            ),
        )
        val initial = roots.filterFingerprint(version = 10)

        roots.setSelections(
            listOf(
                LibraryFolderSelection(
                    uri = null,
                    path = "/storage/emulated/0/Albums",
                    displayName = "Albums",
                ),
            ),
        )

        assertNotEquals(initial, roots.filterFingerprint(version = 10))
    }
}
