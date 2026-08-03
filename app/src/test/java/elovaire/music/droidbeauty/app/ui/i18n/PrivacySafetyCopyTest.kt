package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacySafetyCopyTest {
    @Test
    fun everyLanguageIncludesLocalPrivacyDisclosure() {
        AppLanguage.entries.forEach { language ->
            val privacySection = privacySafetyCopy(language).sections.last()
            assertEquals(language.name, "Privacy policy", privacySection.title)
            assertEquals(language.name, false, privacySection.body.isBlank())
        }
    }
}
