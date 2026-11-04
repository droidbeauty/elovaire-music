package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutDescription
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutTitle
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class BundledAboutCopyTest {
    @Test
    fun packagedDescriptionsFollowCopyRulesAndHaveTranslations() {
        val relativePath = "src/main/res/xml/info_screen.xml"
        val source = File(relativePath).takeIf(File::isFile) ?: File("app/$relativePath")
        val parser = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }.newDocumentBuilder()
        val document = parser.parse(source)
        val descriptions = listOf("entry", "section").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it) as Element }
                .filter { it.hasAttribute("description") }
                .map { it.getAttribute("title") to it.getAttribute("description") }
        }
        assertTrue(descriptions.isNotEmpty())
        descriptions.forEach { (title, description) ->
            assertFalse("Unexpected final period: $description", description.endsWith('.') && !description.endsWith("..."))
            assertTrue("Too many sentence separators: $description", Regex("(?<!\\.)\\.(?!\\.)\\s+(?=[A-Z])").findAll(description).count() <= 1)
            assertEquals(description, localizedAboutDescription(title, description, AppLanguage.English))
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                assertFalse("Missing $language translation for $title", localizedAboutDescription(title, description, language) == description)
            }
        }
        assertEquals(descriptions.single { it.first == "Droid Beauty" }.second, droidBeautyDescription(AppLanguage.English))
        AppLanguage.entries.forEach { language ->
            assertTrue(localizedAboutTitle("Lucide icons", language).contains("Lucide"))
            assertTrue(localizedAboutTitle("Geist font", language).contains("Geist"))
        }
    }
}
