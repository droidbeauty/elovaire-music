package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalAudioIntentHandlerTest {
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
