package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledChangelogCopyTest {
    @Test
    fun everyBundledEntryFollowsEnglishCopyRulesAndHasTranslations() {
        val relativePath = "src/main/res/xml/changelog.xml"
        val source = File(relativePath).takeIf(File::isFile) ?: File("app/$relativePath")
        val parser = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }.newDocumentBuilder()
        val document = parser.parse(source)
        val entries = listOf("item", "change").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it).textContent.trim() }
        }
        assertTrue("Bundled changelog is empty", entries.isNotEmpty())
        entries.forEach { entry ->
            assertFalse("Unexpected final period: $entry", entry.endsWith('.') && !entry.endsWith("..."))
            assertEquals(entry, localizedChangelogItem(entry, AppLanguage.English))
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                assertFalse("Missing $language translation for: $entry", localizedChangelogItem(entry, language) == entry)
            }
        }
    }
}
