package elovaire.music.droidbeauty.app.data.network

import org.junit.Assert.assertThrows
import org.junit.Test

class BoundedHttpTransportTest {
    @Test
    fun rejectsUnboundedTransportConfiguration() {
        assertThrows(IllegalArgumentException::class.java) {
            BoundedHttpTransport(connectTimeoutMs = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoundedHttpTransport(readTimeoutMs = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoundedHttpTransport(maxRedirects = 9)
        }
    }
}
