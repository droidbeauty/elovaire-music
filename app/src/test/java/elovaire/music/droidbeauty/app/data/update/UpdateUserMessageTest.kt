package elovaire.music.droidbeauty.app.data.update

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.json.JSONException

class UpdateUserMessageTest {
    @Test
    fun networkFailuresUseTheAppropriateTranslatableOperationMessage() {
        assertEquals("Unable to check for updates", updateUserMessage(UnknownHostException("example.invalid"), "Unable to check for updates"))
        assertEquals("Update download failed", updateUserMessage(SocketTimeoutException("timeout"), "Update download failed"))
        assertEquals("Unable to verify update", updateUserMessage(SocketTimeoutException("timeout"), "Unable to verify update"))
        assertEquals("Unable to check for updates", updateUserMessage(JSONException("invalid JSON"), "Unable to check for updates"))
        assertEquals("The update was not installed", updateUserMessage(SecurityException("permission denied"), "The update was not installed"))
        assertEquals("Update verification failed", updateUserMessage(IllegalStateException("Update verification failed"), "Update download failed"))
    }

    @Test
    fun transportErrorsMapToExistingTranslatableUpdateMessages() {
        val fallback = "Unable to verify update"
        assertEquals("Update source is invalid", updateUserMessage(IllegalArgumentException("HTTP request URL is not allowed"), fallback))
        assertEquals("Too many update redirects", updateUserMessage(IllegalStateException("Too many HTTPS redirects"), fallback))
        assertEquals("Update source redirect is invalid", updateUserMessage(IllegalStateException("Redirect has no location"), fallback))
        listOf("HTTP response is too large", "Unsupported HTTP connection", "Unable to resolve HTTPS request").forEach {
            assertEquals(fallback, updateUserMessage(IllegalStateException(it), fallback))
        }
    }
}
