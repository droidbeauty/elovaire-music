package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalAudioIntentHandlerTest {
    @Test
    fun externalInputPolicyAcceptsOnlyBoundedAudioInputs() {
        assertTrue(ExternalAudioMetadataPolicy.acceptsUri("content", 120))
        assertTrue(ExternalAudioMetadataPolicy.acceptsUri("file", 120))
        assertFalse(ExternalAudioMetadataPolicy.acceptsUri("https", 120))
        assertFalse(ExternalAudioMetadataPolicy.acceptsUri("content", 4_097))
        assertTrue(ExternalAudioMetadataPolicy.acceptsDeclaredMimeType("audio/flac"))
        assertTrue(ExternalAudioMetadataPolicy.acceptsDeclaredMimeType("audio/vendor-specific"))
        assertTrue(ExternalAudioMetadataPolicy.acceptsDeclaredMimeType("application/octet-stream"))
        assertFalse(ExternalAudioMetadataPolicy.acceptsDeclaredMimeType("text/plain"))
    }

    @Test
    fun stableExternalId_isDeterministicAndStaysWithinItsReservedRange() {
        val base = -8_000_000_000_000L
        val first = stableExternalId("content://provider/audio/one", base)

        assertEquals(first, stableExternalId("content://provider/audio/one", base))
        assertTrue(first in base until (base + 1_000_000_000_000L))
    }

    @Test
    fun stableExternalId_usesDifferentValuesForDifferentUris() {
        val base = -8_000_000_000_000L

        assertNotEquals(
            stableExternalId("content://provider/audio/one", base),
            stableExternalId("content://provider/audio/two", base),
        )
    }
}
