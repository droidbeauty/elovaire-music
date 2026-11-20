package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreScanPreflightTest {
    private val filter = LibraryAudioFileFilter(
        selectedRelativeRoots = setOf("Music"),
        libraryRootPaths = emptySet(),
    )

    @Test
    fun rejectsExcludedMusicBeforeContainerDetection() {
        val rejection = MediaStoreScanPreflight.evaluateBeforeContainerDetection(
            candidate = candidate(
                relativePath = "Music/WhatsApp Audio",
                extension = "mp3",
            ),
            filter = filter,
        )?.decision as? AudioFileFilterDecision.Exclude

        assertEquals("Excluded path", rejection?.reason)
    }

    @Test
    fun validatesContainersBeforeApplyingPreflightRejection() {
        val rejection = MediaStoreScanPreflight.evaluateBeforeContainerDetection(
            candidate = candidate(
                relativePath = "Music/WhatsApp Audio",
                extension = "mp4",
            ),
            filter = filter,
        )

        assertNull(rejection)
    }

    @Test
    fun extensionlessRowsReachContainerDetection() {
        val rejection = MediaStoreScanPreflight.evaluateBeforeContainerDetection(
            candidate = candidate(
                relativePath = "Music",
                extension = "",
            ),
            filter = filter,
        )

        assertNull(rejection)
    }

    @Test
    fun acceptedPreflightRetainsTheExactCandidateAndMustNotHideChangedDetection() {
        val original = candidate(relativePath = "Music/Artist", extension = "mp3")
        val preflight = checkNotNull(MediaStoreScanPreflight.evaluateBeforeContainerDetection(original, filter))
        assertTrue(preflight.decision is AudioFileFilterDecision.Include)
        assertEquals(
            original.copy(detectedFormat = AudioScanCandidateMapper.fastDetectedFormat("mp3", original.mimeType)),
            preflight.candidate,
        )
        val changed = preflight.candidate.copy(
            detectedFormat = checkNotNull(preflight.candidate.detectedFormat).copy(detectionSucceeded = true, hasVideoTrack = true),
        )
        assertTrue(filter.evaluate(changed) is AudioFileFilterDecision.Exclude)
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
