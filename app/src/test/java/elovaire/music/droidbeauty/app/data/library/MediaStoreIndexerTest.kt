package elovaire.music.droidbeauty.app.data.library

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MediaStoreIndexerTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun audioFilesForPaths_includesDirectAudioFile() {
        val file = temp.newFile("track.mp3")

        assertEquals(listOf(file.absolutePath), audioFilesForPaths(listOf(file.absolutePath)).map(File::getAbsolutePath))
    }

    @Test
    fun audioFilesForPaths_expandsNestedDirectories() {
        val root = temp.newFolder("Music")
        val nested = File(root, "Nested").apply { mkdirs() }
        val track = File(nested, "track.flac").apply { writeText("x") }
        File(nested, "cover.jpg").writeText("x")

        assertEquals(listOf(track.absolutePath), audioFilesForPaths(listOf(root.absolutePath)).map(File::getAbsolutePath))
    }

    @Test
    fun audioFilesForPaths_ignoresMissingAndUnsupportedFiles() {
        val root = temp.newFolder("Music")
        File(root, "notes.txt").writeText("x")

        assertEquals(emptyList<File>(), audioFilesForPaths(listOf(root.absolutePath, File(root, "missing.mp3").absolutePath)))
    }
}
