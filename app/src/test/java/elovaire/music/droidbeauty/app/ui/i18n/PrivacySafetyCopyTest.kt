package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacySafetyCopyTest {
    @Test
    fun everyLanguageExposesExactlyOnePrivacyPolicyLink() {
        AppLanguage.entries.forEach { language ->
            assertEquals(
                language.name,
                1,
                privacySafetyCopy(language).sections.count { it.showsPrivacyPolicyLink },
            )
        }
    }
}
