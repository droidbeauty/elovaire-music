package elovaire.music.droidbeauty.app.data.library

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFailureRegistryTest {
    @Test
    fun firstFailureDoesNotSuppressAndRepeatedFailureBacksOff() {
        var now = 1_000L
        val registry = MediaFailureRegistry(nowMs = { now })
        val key = MediaFailureKey("song", "revision-1", MediaFailureDomain.Metadata)

        assertFalse(registry.shouldSuppress(key))
        registry.recordFailure(key, MediaFailureCategory.TransientIo)
        assertFalse(registry.shouldSuppress(key))
        registry.recordFailure(key, MediaFailureCategory.TransientIo)
        assertTrue(registry.shouldSuppress(key))

        now += 30_000L
        assertFalse(registry.shouldSuppress(key))
    }

    @Test
    fun revisionAndSuccessClearSuppression() {
        val registry = MediaFailureRegistry(nowMs = { 10_000L })
        val first = MediaFailureKey("song", "revision-1", MediaFailureDomain.FormatProbe)
        val second = first.copy(revision = "revision-2")
        registry.recordFailure(first, MediaFailureCategory.Malformed)
        registry.recordFailure(first, MediaFailureCategory.Malformed)

        assertTrue(registry.shouldSuppress(first))
        assertFalse(registry.shouldSuppress(second))
        registry.recordSuccess(first)
        assertEquals(0, registry.size())
    }

    @Test
    fun explicitRetryBypassesBackoffWithoutDroppingRecord() {
        val registry = MediaFailureRegistry(nowMs = { 10_000L })
        val key = MediaFailureKey("song", "revision", MediaFailureDomain.EmbeddedTags)
        registry.recordFailure(key, MediaFailureCategory.Unknown)
        registry.recordFailure(key, MediaFailureCategory.Unknown)

        assertFalse(registry.shouldSuppress(key, force = true))
        assertEquals(MediaFailureCategory.TransientIo, mediaFailureCategory(IOException()))
    }

    @Test
    fun retryPolicyKeepsTransientFailuresPromptButSuppressesStableFailuresLonger() {
        assertTrue(
            mediaFailureRetryDelayMs(MediaFailureCategory.TransientIo, 2) <
                mediaFailureRetryDelayMs(MediaFailureCategory.Malformed, 2),
        )
        assertTrue(
            mediaFailureRetryDelayMs(MediaFailureCategory.Resource, 2) <
                mediaFailureRetryDelayMs(MediaFailureCategory.Missing, 2),
        )
    }

    @Test
    fun changingFailureCategoryStartsASeparateBackoffSequence() {
        var now = 1_000L
        val registry = MediaFailureRegistry(nowMs = { now })
        val key = MediaFailureKey("song", "revision", MediaFailureDomain.FormatProbe)

        registry.recordFailure(key, MediaFailureCategory.Malformed)
        registry.recordFailure(key, MediaFailureCategory.Malformed)
        registry.recordFailure(key, MediaFailureCategory.TransientIo)

        now += 30_000L
        assertFalse(registry.shouldSuppress(key))
    }

    @Test
    fun backoffDeadlineDoesNotWrapAtElapsedTimeLimit() {
        var now = Long.MAX_VALUE - 1L
        val registry = MediaFailureRegistry(nowMs = { now })
        val key = MediaFailureKey("song", "revision", MediaFailureDomain.FormatProbe)

        registry.recordFailure(key, MediaFailureCategory.TransientIo)
        registry.recordFailure(key, MediaFailureCategory.TransientIo)

        assertTrue(registry.shouldSuppress(key))
        now = Long.MAX_VALUE
        assertFalse(registry.shouldSuppress(key))
    }
}
