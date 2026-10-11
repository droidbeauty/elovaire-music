package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NetworkRangePolicyTest {
    @Test
    fun boundedReadPastEofReportsOnlyAvailableBytes() {
        assertEquals(
            NetworkRangeResolution(50L, 50L),
            resolveNetworkRange(totalLength = 100L, position = 50L, requestedLength = 90L),
        )
    }

    @Test
    fun exactEofIsValidAndHasNoAvailableBytes() {
        assertEquals(
            NetworkRangeResolution(0L, 0L),
            resolveNetworkRange(totalLength = 100L, position = 100L, requestedLength = 25L),
        )
    }

    @Test
    fun positionPastEofIsRejected() {
        assertThrows(NetworkRangeException::class.java) {
            resolveNetworkRange(totalLength = 100L, position = 101L, requestedLength = -1L)
        }
    }
}
