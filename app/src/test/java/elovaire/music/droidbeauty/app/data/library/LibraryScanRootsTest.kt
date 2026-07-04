package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LibraryScanRootsTest {
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

        assertEquals(setOf("music"), roots.relativeRoots())
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

        assertEquals(
            "10::content://tree/music@/storage/emulated/0/music",
            roots.filterFingerprint(version = 10),
        )
    }

    @Test
    fun setSelections_deduplicatesTreeUriWithEquivalentPathRoot() {
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

        assertEquals(
            "10::@/storage/emulated/0/music",
            roots.filterFingerprint(version = 10),
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

        assertEquals(
            "10::content://tree/removable%3AMusic@",
            roots.filterFingerprint(version = 10),
        )
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
