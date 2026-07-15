package elovaire.music.droidbeauty.app.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class HttpTransportTest {
    @Test
    fun rejectsNonHttpsBeforeOpeningConnection() {
        try {
            HttpTransport().getText(HttpRequest("http://example.com", "text/plain"), 16)
            fail("Expected non-HTTPS request to fail")
        } catch (failure: HttpTransportException) {
            assertEquals(HttpFailureKind.InvalidUrl, failure.kind)
        }
    }

    @Test
    fun rejectsMalformedUrlDeterministically() {
        try {
            HttpTransport().getText(HttpRequest("not a url", "text/plain"), 16)
            fail("Expected malformed request to fail")
        } catch (failure: HttpTransportException) {
            assertEquals(HttpFailureKind.InvalidUrl, failure.kind)
        }
    }

    @Test
    fun rejectsInvalidTimeoutAndResponseBoundsBeforeOpeningConnection() {
        assertFailureKind(HttpFailureKind.InvalidUrl) {
            validateHttpRequest(HttpRequest("https://example.com", "text/plain", connectTimeoutMs = 0), 16)
        }
        assertFailureKind(HttpFailureKind.ResponseTooLarge) {
            validateHttpRequest(HttpRequest("https://example.com", "text/plain"), -1)
        }
    }

    @Test
    fun acceptsBoundedHttpsRequest() {
        validateHttpRequest(HttpRequest("https://example.com/path", "application/json"), 1024)
    }

    private fun assertFailureKind(expected: HttpFailureKind, block: () -> Unit) {
        try {
            block()
            fail("Expected request validation to fail")
        } catch (failure: HttpTransportException) {
            assertEquals(expected, failure.kind)
        }
    }
}
