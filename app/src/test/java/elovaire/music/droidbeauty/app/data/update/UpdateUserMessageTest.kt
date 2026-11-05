package elovaire.music.droidbeauty.app.data.update

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.localizedUpdateError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun unexpectedErrorsUseTheTranslatableOperationMessage() {
        val fallback = "Update download failed"
        assertEquals(fallback, updateUserMessage(IllegalStateException("Unexpected internal detail"), fallback))
        assertEquals(fallback, updateUserMessage(IllegalStateException(), fallback))
        assertEquals("Downloaded update is incomplete", updateUserMessage(IllegalStateException("Downloaded update is incomplete"), fallback))
    }

    @Test
    fun supportedUpdateFailuresHaveLocalizedUserFacingMessages() {
        val failures = listOf(
            "Update download is too large", "Update download exceeded its byte limit",
            "The staged update is no longer available", "Allow installing updates from this source first",
            "Update download failed", "Downloaded update is incomplete", "Update verification failed",
            "Unable to verify update", "Downloaded update targets another app", "Downloaded update version is invalid",
            "Downloaded update is not newer", "Downloaded update is not a valid app", "Downloaded update is invalid",
            "Unable to check for updates", "GitHub update check failed", "Update source is invalid",
            "Update asset name is invalid", "Too many update redirects", "Update source redirect is invalid",
            "Unable to open install permission settings", "Unable to persist update installer handoff",
            "Downloaded update is signed by an unknown certificate", "The update was not installed",
        )
        failures.forEach { failure ->
            val output = updateUserMessage(IllegalStateException(failure), "Update download failed")
            val english = localizedUpdateError(output, AppLanguage.English)
            assertTrue("English message ends in a period: $failure", !english.endsWith('.'))
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                val translated = localizedUpdateError(output, language)
                assertTrue("Blank update failure for $language: $failure", translated.isNotBlank())
                assertNotEquals("English update failure for $language: $failure", english, translated)
            }
        }
    }
}
