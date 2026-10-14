package elovaire.music.droidbeauty.app.data.library

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MediaFilePathResolverTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun mediaStorePathRequiresARegularFileContainedByStorageRoot() {
        val root = temp.newFolder("storage")
        val music = File(root, "Music").apply { mkdirs() }
        File(music, "track.mp3").writeBytes(byteArrayOf(1))

        assertEquals(
            File(music, "track.mp3").canonicalPath,
            MediaFilePathResolver.resolveMediaStoreFilePath("Music/", "track.mp3", listOf(root)),
        )
        assertNull(
            MediaFilePathResolver.resolveMediaStoreFilePath("../", "outside.mp3", listOf(root)),
        )
        assertNull(
            MediaFilePathResolver.resolveMediaStoreFilePath("Music", "nested/track.mp3", listOf(root)),
        )
    }

    @Test
    fun containedPathRejectsSymlinkEscape() {
        val root = temp.newFolder("storage")
        val outside = temp.newFolder("outside")
        val outsideFile = File(outside, "track.mp3").apply { writeBytes(byteArrayOf(1)) }
        Files.createSymbolicLink(File(root, "linked").toPath(), outside.toPath())

        assertNull(root.resolveContainedChild("linked/track.mp3"))
        assertEquals(outsideFile.canonicalPath, outside.resolveContainedChild("track.mp3")?.canonicalPath)
    }

    @Test
    fun volumeScopedPathResolutionNeverFallsBackToAnotherVolume() {
        val primaryRoot = temp.newFolder("primary")
        val removableRoot = temp.newFolder("ABCD-1234")
        val otherRoot = temp.newFolder("EFGH-5678")

        assertEquals(
            listOf(primaryRoot),
            mediaStoreRootsForVolume(listOf(primaryRoot, removableRoot, otherRoot), primaryRoot, "external_primary"),
        )
        assertEquals(
            listOf(removableRoot),
            mediaStoreRootsForVolume(listOf(primaryRoot, removableRoot, otherRoot), primaryRoot, "ABCD-1234"),
        )
        assertEquals(
            emptyList<File>(),
            mediaStoreRootsForVolume(listOf(primaryRoot), primaryRoot, "ABCD-1234"),
        )
    }

    @Test
    fun unavailableVolumeRootsAreRetriedAfterTheVolumeBecomesAvailable() {
        val cache = mutableMapOf<String, List<File>>()
        val mountedRoot = temp.newFolder("ABCD-1234")
        var available = false

        assertEquals(emptyList<File>(), cachedRootsForVolume(cache, "abcd-1234") {
            if (available) listOf(mountedRoot) else emptyList()
        })
        available = true

        assertEquals(listOf(mountedRoot), cachedRootsForVolume(cache, "abcd-1234") {
            if (available) listOf(mountedRoot) else emptyList()
        })
    }
}
