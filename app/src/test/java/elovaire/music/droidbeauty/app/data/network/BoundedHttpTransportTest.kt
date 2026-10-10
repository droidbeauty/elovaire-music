package elovaire.music.droidbeauty.app.data.network

import org.junit.Assert.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

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

    @Test
    fun crossOriginRedirectDropsCredentialsAndHostHeaders() {
        val headers = mapOf(
            "Authorization" to "Bearer secret",
            "Cookie" to "session=secret",
            "Proxy-Authorization" to "Basic secret",
            "Host" to "origin.test",
            "X-Request-Id" to "request-1",
        )

        assertEquals(
            mapOf("X-Request-Id" to "request-1"),
            headersForHttpRedirect(
                headers,
                URL("https://origin.test/resource"),
                URL("https://cdn.test/resource"),
            ),
        )
    }

    @Test
    fun sameOriginRedirectKeepsRequestHeaders() {
        val headers = mapOf("Authorization" to "Bearer token")

        assertEquals(
            headers,
            headersForHttpRedirect(
                headers,
                URL("https://origin.test/resource"),
                URL("https://ORIGIN.test:443/next"),
            ),
        )
    }
}
