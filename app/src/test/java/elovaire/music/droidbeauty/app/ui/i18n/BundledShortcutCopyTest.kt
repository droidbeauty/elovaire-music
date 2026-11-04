package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class BundledShortcutCopyTest {
    @Test
    fun eachSupportedLanguagePackagesEveryShortcutLabel() {
        val relativePath = "src/main/res"
        val resources = File(relativePath).takeIf(File::isDirectory) ?: File("app/$relativePath")
        val parser = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }.newDocumentBuilder()
        val keys = listOf("shortcut_last_played", "shortcut_albums", "shortcut_playlists", "shortcut_search")
        AppLanguage.entries.forEach { language ->
            val qualifier = when (language) {
                AppLanguage.English -> ""
                AppLanguage.Albanian -> "-sq"; AppLanguage.Bengali -> "-bn"; AppLanguage.Bosnian -> "-bs"
                AppLanguage.Bulgarian -> "-bg"; AppLanguage.Catalan -> "-ca"; AppLanguage.ChineseSimplified -> "-zh-rCN"
                AppLanguage.Croatian -> "-hr"; AppLanguage.Czech -> "-cs"; AppLanguage.Danish -> "-da"; AppLanguage.Dutch -> "-nl"
                AppLanguage.Estonian -> "-et"; AppLanguage.Finnish -> "-fi"; AppLanguage.French -> "-fr"; AppLanguage.German -> "-de"
                AppLanguage.Greek -> "-el"; AppLanguage.Hindi -> "-hi"; AppLanguage.Hungarian -> "-hu"; AppLanguage.Indonesian -> "-in"
                AppLanguage.Italian -> "-it"; AppLanguage.Japanese -> "-ja"; AppLanguage.Korean -> "-ko"; AppLanguage.Latin -> "-la"
                AppLanguage.Latvian -> "-lv"; AppLanguage.Lithuanian -> "-lt"; AppLanguage.Macedonian -> "-mk"; AppLanguage.Malay -> "-ms"
                AppLanguage.Norwegian -> "-nb"; AppLanguage.Polish -> "-pl"; AppLanguage.Portuguese -> "-pt"; AppLanguage.Punjabi -> "-pa"
                AppLanguage.Romanian -> "-ro"; AppLanguage.Russian -> "-ru"; AppLanguage.Serbian -> "-sr"; AppLanguage.Slovak -> "-sk"
                AppLanguage.Slovenian -> "-sl"; AppLanguage.Spanish -> "-es"; AppLanguage.Swedish -> "-sv"; AppLanguage.Thai -> "-th"
                AppLanguage.Ukrainian -> "-uk"; AppLanguage.Urdu -> "-ur"
            }
            val source = File(resources, "values$qualifier/strings.xml")
            assertTrue("Missing shortcut resources for $language", source.isFile)
            val nodes = parser.parse(source).getElementsByTagName("string")
            val labels = (0 until nodes.length).map { nodes.item(it) as Element }
                .associate { it.getAttribute("name") to it.textContent.trim() }
            keys.forEach { key ->
                val value = labels[key]
                assertTrue("Missing $key for $language", !value.isNullOrBlank())
                if (language == AppLanguage.English) {
                    assertFalse("Unexpected final period in $key", value!!.endsWith('.') && !value.endsWith("..."))
                }
            }
            if (language != AppLanguage.English) {
                assertFalse("English fallback for $language last played", labels["shortcut_last_played"] == "Last played")
                assertFalse("English fallback for $language playlists", labels["shortcut_playlists"] == "Playlists")
            }
        }
    }
}
