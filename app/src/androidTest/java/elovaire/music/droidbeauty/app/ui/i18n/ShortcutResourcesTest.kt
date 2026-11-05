package elovaire.music.droidbeauty.app.ui.i18n

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.R
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutResourcesTest {
    @Test
    fun supportedDeviceLocalesHaveLocalizedShortcutLabels() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resourceIds = listOf(
            R.string.shortcut_last_played,
            R.string.shortcut_albums,
            R.string.shortcut_playlists,
            R.string.shortcut_search,
        )
        val tags = listOf(
            "sq", "bs", "bg", "ca", "fi", "ro", "sl", "id", "pa", "zh-CN", "hr", "cs",
            "da", "nl", "bn", "et", "fr", "de", "el", "hi", "hu", "it", "ja", "ko",
            "lv", "lt", "ms", "mk", "nb", "pl", "pt", "ru", "sk", "sr", "es", "sv", "th", "uk", "ur",
        )
        tags.forEach { tag ->
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(tag))
            }
            val resources = context.createConfigurationContext(configuration).resources
            resourceIds.forEach { id -> assertFalse("$tag has an empty shortcut", resources.getString(id).isBlank()) }
            assertFalse("$tag falls back to English", resources.getString(R.string.shortcut_last_played) == "Last played")
            assertFalse("$tag has an English playlist shortcut", resources.getString(R.string.shortcut_playlists) == "Playlists")
        }
    }
}
