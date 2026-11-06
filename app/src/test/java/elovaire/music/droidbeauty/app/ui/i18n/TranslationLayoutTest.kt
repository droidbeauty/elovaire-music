package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.io.File
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationLayoutTest {
    @Test
    fun everySupportedLanguageHasItsOwnStringValuesFile() {
        val root = File("src/main/java/com/elovaire/music/ui/i18n/translations")
        val expected = AppLanguage.entries.map { it.name.lowercase(Locale.ROOT) }.toSet()
        val actual = root.listFiles()?.filter(File::isDirectory)?.map(File::getName)?.toSet()
        assertEquals(expected, actual)
        AppLanguage.entries.forEach { language ->
            val values = root.resolve(language.name.lowercase(Locale.ROOT)).resolve("${language.name}Strings.kt")
            assertTrue("Missing translation values for $language", values.isFile && values.length() > 0)
        }
    }
}
