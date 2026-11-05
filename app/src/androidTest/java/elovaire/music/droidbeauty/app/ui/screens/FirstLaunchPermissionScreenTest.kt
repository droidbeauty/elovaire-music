package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstLaunchPermissionScreenTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun permissionScreenShowsBrandAndPreservesPermissionAction() {
        var requested = false
        val copy = rootUiCopy(AppLanguage.English)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    FirstLaunchPermissionLoadingScreen(false) { requested = true }
                }
            }
        }
        val brand = rule.onNodeWithText("ELOVAIRE").assertIsDisplayed()
        val message = rule.onNodeWithText(copy.firstLaunchPermissionMessage).assertIsDisplayed()
        assertTrue(brand.getUnclippedBoundsInRoot().top.value >= 40f)
        assertTrue(brand.getUnclippedBoundsInRoot().bottom < message.getUnclippedBoundsInRoot().top)
        rule.onNodeWithText(copy.firstLaunchPermissionButton).performClick()
        rule.runOnIdle { assertTrue(requested) }
    }

    @Test
    fun loadingScreenKeepsBrandWithoutPermissionButton() {
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                FirstLaunchPermissionLoadingScreen(true) {}
            }
        }
        rule.onNodeWithText("ELOVAIRE").assertIsDisplayed()
        rule.onNodeWithText(rootUiCopy(AppLanguage.English).firstLaunchPermissionButton).assertDoesNotExist()
    }
}
