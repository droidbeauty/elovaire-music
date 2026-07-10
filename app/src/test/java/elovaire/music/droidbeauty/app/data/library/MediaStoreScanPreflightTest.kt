package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaStoreScanPreflightTest {
    private val filter = LibraryAudioFileFilter(
        selectedRelativeRoots = setOf("music"),
        libraryRootPaths = emptySet(),
    )

    @Test
    fun rejectsExcludedMusicBeforeContainerDetection() {
        val rejection = MediaStoreScanPreflight.rejectionBeforeContainerDetection(
            candidate = candidate(
                relativePath = "Music/WhatsApp Audio",
                extension = "mp3",
            ),
            filter = filter,
        )

        assertEquals("Excluded path", rejection?.reason)
    }

    @Test
    fun validatesContainersBeforeApplyingPreflightRejection() {
        val rejection = MediaStoreScanPreflight.rejectionBeforeContainerDetection(
            candidate = candidate(
                relativePath = "Music/WhatsApp Audio",
                extension = "mp4",
            ),
            filter = filter,
        )

        assertNull(rejection)
    }

    private fun candidate(
        relativePath: String,
        extension: String,
    ) = AudioScanCandidate(
        id = 1L,
        uri = TestUri("content://media/external/audio/media/1"),
        displayName = "track.$extension",
        title = "Track",
        artist = "Artist",
        album = "Album",
        durationMs = 180_000L,
        mimeType = null,
        relativePath = relativePath,
        absolutePath = null,
        extension = extension,
        isMusic = true,
    )
}
