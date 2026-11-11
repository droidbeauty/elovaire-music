package elovaire.music.droidbeauty.app.ui.screens.about

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.droidBeautyDescription
import elovaire.music.droidbeauty.app.ui.i18n.localizedChangelogItem
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRepository
import elovaire.music.droidbeauty.app.ui.screens.loadAboutScreenModel
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutTitle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenCopyInstrumentedTest {
    @Test
    fun bundledChangelogEntriesRemainEnglish() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entries = ChangelogRepository(context).loadReleases().flatMap { it.changes }
        assertTrue(entries.isNotEmpty())
        entries.forEach { entry ->
            assertFalse("Unexpected final period: $entry", entry.endsWith('.') && !entry.endsWith("..."))
        }
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            entries.forEach { entry ->
                assertEquals("Changelog content must remain English for $language", entry, localizedChangelogItem(entry, language))
            }
        }
    }

    @Test
    fun englishCreatorSourceMatchesTheTranslatedCopyBaseline() {
        val model = InstrumentationRegistry.getInstrumentation().targetContext.loadAboutScreenModel()
        val creator = model.sections.flatMap { it.entries }.single { it.title == "Droid Beauty" }
        assertEquals(droidBeautyDescription(AppLanguage.English), creator.description)
        val resources = model.sections.single { it.title == "Resources" }.description.orEmpty()
        listOf("Apache-2.0", "Lucide", "ISC/MIT", "Geist", "OFL-1.1", "jaudiotagger", "LGPL").forEach {
            assertTrue("Missing $it", it in resources)
        }
        val resourceTitles = model.sections.single { it.title == "Resources" }.entries.map { it.title }
        listOf("Lucide icons", "Geist font").forEach { title ->
            assertTrue("Missing resource title: $title", title in resourceTitles)
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                val localized = localizedAboutTitle(title, language)
                assertTrue("Missing resource name for $language", localized.contains(title.substringBefore(' ')))
            }
        }
    }

    @Test
    fun englishAboutCopyHasNoTerminalSentencePeriod() {
        val model = InstrumentationRegistry.getInstrumentation().targetContext.loadAboutScreenModel()
        val descriptions = model.sections.flatMap { section ->
            listOfNotNull(section.description) + section.entries.mapNotNull { it.description }
        }
        descriptions.forEach { copy ->
            assertFalse("Unexpected final period: $copy", copy.endsWith('.') && !copy.endsWith("..."))
            val sentenceSeparators = Regex("(?<!\\.)\\.(?!\\.)\\s+(?=[A-Z])").findAll(copy).count()
            assertTrue("Too many sentence separators: $copy", sentenceSeparators <= 1)
        }
    }
}
