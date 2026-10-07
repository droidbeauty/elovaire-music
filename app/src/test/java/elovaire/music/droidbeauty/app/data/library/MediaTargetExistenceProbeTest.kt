package elovaire.music.droidbeauty.app.data.library

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTargetExistenceProbeTest {
    @Test
    fun providerFailureRemainsPossiblyPresent() = runBlocking {
        assertTrue(queryTargetMayExist { throw IllegalStateException("provider failed") })
    }

    @Test
    fun queryCancellationPropagatesInsteadOfConfirmingDeletion() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                queryTargetMayExist { throw CancellationException("provider query cancelled") }
            }
        }
    }

    @Test
    fun successfulQueryRetainsItsResult() = runBlocking {
        assertTrue(queryTargetMayExist { true })
        assertFalse(queryTargetMayExist { false })
    }

    @Test
    fun failedFallbackQueryRetainsRequestedSongIds() = runBlocking {
        val requested = setOf(11L, 12L)

        assertEquals(requested, queryExistingSongIdsOrRetain(requested) { null })
        assertEquals(requested, queryExistingSongIdsOrRetain(requested) { error("provider failed") })
        assertTrue(queryExistingSongIdsOrRetain(requested) { emptySet() }.isEmpty())
    }
}
