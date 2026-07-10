package elovaire.music.droidbeauty.app.data.library

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SafTreeLibraryScannerTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun resolveSafLibraryPath_returnsExistingFileWithinRoot() {
        val root = temp.newFolder("Music").canonicalFile
        val album = File(root, "Album").apply { mkdirs() }
        val song = File(album, "Track.flac").apply { writeBytes(byteArrayOf(1)) }

        val result = resolveSafLibraryPath(root, "saf/root", "Album/Track.flac")

        assertEquals(song.canonicalPath, result)
    }

    @Test
    fun resolveSafLibraryPath_rejectsMissingAndEscapingFiles() {
        val root = temp.newFolder("Music").canonicalFile
        val outside = temp.newFile("outside.flac")

        assertEquals(
            "saf/root/Missing.flac",
            resolveSafLibraryPath(root, "saf/root", "Missing.flac"),
        )
        assertEquals(
            "saf/root/../${outside.name}",
            resolveSafLibraryPath(root, "saf/root", "../${outside.name}"),
        )
    }

    @Test
    fun resolveSafLibraryPath_keepsSyntheticPathWithoutLocalRoot() {
        assertEquals(
            "saf/root/Album/Track.opus",
            resolveSafLibraryPath(null, "saf/root", "Album/Track.opus"),
        )
    }
}
