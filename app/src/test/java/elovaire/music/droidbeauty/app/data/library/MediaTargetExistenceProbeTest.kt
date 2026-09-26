package elovaire.music.droidbeauty.app.data.library

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTargetExistenceProbeTest {
    @Test
    fun providerFailureRemainsAnUnknownOrMissingTarget() = runBlocking {
        assertFalse(queryTargetExists { throw IllegalStateException("provider failed") })
    }

    @Test
    fun queryCancellationPropagatesInsteadOfConfirmingDeletion() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                queryTargetExists { throw CancellationException("provider query cancelled") }
            }
        }
    }

    @Test
    fun successfulQueryRetainsItsResult() = runBlocking {
        assertTrue(queryTargetExists { true })
    }
}
