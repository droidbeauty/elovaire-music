package elovaire.music.droidbeauty.app.ui.screens

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryFoldersScreenTest {
    @Test
    fun countInFolder_matchesResolvedSafPath() {
        val treeUri = TestUri(
            "content://com.android.externalstorage.documents/tree/primary%3AMusic%2FProbe",
        )
        val folder = LibraryFolderSelection(
            uri = treeUri,
            path = "/storage/emulated/0/Music/Probe",
            displayName = "Probe",
        )

        assertEquals(
            1,
            listOf(song("/storage/emulated/0/Music/Probe/Album/track.m4a"))
                .countInFolder(folder),
        )
    }

    @Test
    fun countInFolder_matchesSyntheticSafPathWhenProviderPathIsUnavailable() {
        val treeUri = TestUri(
            "content://com.android.externalstorage.documents/tree/primary%3AMusic%2FProbe",
        )
        val folder = LibraryFolderSelection(
            uri = treeUri,
            path = treeUri.toString(),
            displayName = "Probe",
        )
        val syntheticRoot = LibraryFolderSelectionResolver.safSyntheticRoot(treeUri)

        assertEquals(
            1,
            listOf(song("$syntheticRoot/Album/track.m4a")).countInFolder(folder),
        )
    }

    private fun song(path: String): Song = Song(
        id = -1L,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "M4A/MP4 Audio",
        audioQuality = null,
        fileName = "track.m4a",
        albumId = -1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 0L,
        uri = TestUri("content://media/external/audio/media/1"),
        artUri = null,
        libraryPath = path,
    )
}
