package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExternalAudioMetadataPolicyTest {
    @Test
    fun sanitizeDisplayName_removesControlsAndPathSeparators() {
        assertEquals(
            "folder song.flac",
            ExternalAudioMetadataPolicy.sanitizeDisplayName("folder/song\u0000.flac"),
        )
    }

    @Test
    fun boundedDurationMs_rejectsMalformedNegativeAndImpossibleValues() {
        assertEquals(0L, ExternalAudioMetadataPolicy.boundedDurationMs("not-a-number"))
        assertEquals(0L, ExternalAudioMetadataPolicy.boundedDurationMs("-10"))
        assertEquals(0L, ExternalAudioMetadataPolicy.boundedDurationMs("999999999999"))
    }

    @Test
    fun resolveCapability_usesExtensionBeforeMimeType() {
        val capability = ExternalAudioMetadataPolicy.resolveCapability(
            displayName = "track.flac",
            pathSegment = null,
            mimeType = "application/octet-stream",
        )

        assertNotNull(capability)
    }

    @Test
    fun resolveCapability_acceptsMimeTypeParameters() {
        val capability = ExternalAudioMetadataPolicy.resolveCapability(
            displayName = "audio",
            pathSegment = null,
            mimeType = "audio/mpeg; charset=binary",
        )

        assertNotNull(capability)
    }

    @Test
    fun resolveCapability_rejectsUnsupportedExtensionAndMimeType() {
        assertNull(
            ExternalAudioMetadataPolicy.resolveCapability(
                displayName = "document.txt",
                pathSegment = null,
                mimeType = "text/plain",
            ),
        )
    }
}
