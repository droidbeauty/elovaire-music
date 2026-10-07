package elovaire.music.droidbeauty.app.data.mutation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMutationStoragePolicyTest {
    @Test
    fun requiredSpaceUsesAvailableStorageAndSaturatesSafely() {
        assertTrue(hasSufficientMutationStorage(3_000L, 1_000L))
        assertFalse(hasSufficientMutationStorage(2_999L, 1_000L))
        assertTrue(requiredMutationStagingBytes(Long.MAX_VALUE) > 0L)
        assertTrue(requiredMutationStagingBytes(Long.MAX_VALUE) >= Long.MAX_VALUE - 2L)
    }

    @Test
    fun unknownOrEmptySourceDoesNotInventAStagingRequirement() {
        assertTrue(hasSufficientMutationStorage(0L, 0L))
        assertTrue(hasSufficientMutationStorage(0L, -1L))
    }

    @Test
    fun temporaryFileExtensionCannotAddPathComponentsOrGrowWithoutBound() {
        assertEquals("mp3", mediaMutationTemporaryExtension("track.mp3"))
        assertEquals("tmp", mediaMutationTemporaryExtension("track"))
        assertEquals("mp3", mediaMutationTemporaryExtension("track/../../file.mp3"))
        assertEquals(16, mediaMutationTemporaryExtension("track.${"a".repeat(40)}").length)
    }
}
