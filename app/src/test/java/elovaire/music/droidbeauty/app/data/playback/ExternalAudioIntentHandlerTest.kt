package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.core.AppClock
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalAudioIntentHandlerTest {
    @Test
    fun cachePruningDoesNotCountFilesAlreadyDeletedByAgePass() {
        val directory = Files.createTempDirectory("elovaire-audio-cache").toFile()
        try {
            val expired = File(directory, "expired.mp3").apply { writeBytes(ByteArray(10)) }
            val fresh = File(directory, "fresh.mp3").apply { writeBytes(ByteArray(2)) }
            val nowMs = 30L * 24L * 60L * 60L * 1_000L
            expired.setLastModified(nowMs - 8L * 24L * 60L * 60L * 1_000L)
            fresh.setLastModified(nowMs - 1L * 24L * 60L * 60L * 1_000L)

            ExternalAudioPrivateCopy.prune(
                directory = directory,
                keep = File(directory, "keep.mp3"),
                clock = object : AppClock {
                    override fun wallTimeMs(): Long = nowMs
                    override fun elapsedTimeMs(): Long = 0L
                },
                maxCacheBytes = 10L,
            )

            assertFalse(expired.exists())
            assertTrue(fresh.exists())
        } finally {
            directory.deleteRecursively()
        }
    }

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
