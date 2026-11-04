package elovaire.music.droidbeauty.app.ui.screens.tags

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.platform.ContentIoException
import elovaire.music.droidbeauty.app.platform.ContentIoFailureKind
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagSaveStatus
import elovaire.music.droidbeauty.app.ui.screens.safWriteErrorMessage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafWriteErrorMessageTest {
    @Test
    fun preflightFailuresHaveTranslatableMessagesWithoutExposingProviderErrors() {
        val failures = ContentIoFailureKind.entries.map { ContentIoException(it, "provider detail") } +
            listOf(SecurityException("provider detail"), IllegalStateException("provider detail"))
        failures.forEach { failure ->
            val message = safWriteErrorMessage(failure)
            assertFalse(message.contains("provider detail"))
            val english = localizedTagSaveStatus(message, 0, AppLanguage.English)
            assertFalse(english.endsWith('.'))
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                val translated = localizedTagSaveStatus(message, 0, language)
                assertTrue("Missing message for $language", translated.isNotBlank())
                assertFalse("English message for $language", translated == english)
            }
        }
    }
}
