package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LibraryScanRootsTest {
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
