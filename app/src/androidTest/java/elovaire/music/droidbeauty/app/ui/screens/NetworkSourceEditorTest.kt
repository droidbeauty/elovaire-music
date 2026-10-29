package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.library.network.NetworkAvailability
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryProtocol
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkSourceEditorTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun availableSourceCanBeEditedAndClosesOnlyAfterSavingSuccessfully() {
        val source = NetworkLibrarySource("fixture", "My NAS", NetworkLibraryProtocol.Smb, "nas.example", "Music", "user", "key")
        var result by mutableStateOf(NetworkProbeResult(NetworkAvailability.Available))
        var saves = 0
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                LibraryFoldersScreen(
                    appLanguage = AppLanguage.English,
                    folders = emptyList(), networkSources = listOf(source),
                    networkProbeResults = mapOf(source.id to result), songs = emptyList(), bottomPadding = 0.dp,
                    onBack = {}, onAddFolder = {}, onAddNetworkSource = { _, _ -> saves++ },
                    onRemoveFolder = {}, onRemoveNetworkSource = {}, onRefresh = {},
                )
            }
        }
        rule.onNodeWithText("My NAS").performClick()
        rule.onNodeWithText("Network source").assertIsDisplayed()
        rule.onNodeWithContentDescription("Save network source").performClick()
        rule.runOnIdle { assertEquals(1, saves); result = NetworkProbeResult(NetworkAvailability.Checking) }
        rule.onNodeWithText("Network source").assertIsDisplayed()
        rule.runOnIdle { result = NetworkProbeResult(NetworkAvailability.Available) }
        rule.onNodeWithText("My NAS").assertIsDisplayed()
    }

    @Test
    fun webDavServerRootCanBeSavedWithoutAnAdditionalPath() {
        val source = NetworkLibrarySource("dav", "My WebDAV", NetworkLibraryProtocol.WebDav, "https://nas.example/music", "", "user", "key")
        var saves = 0
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                LibraryFoldersScreen(
                    appLanguage = AppLanguage.English,
                    folders = emptyList(), networkSources = listOf(source), networkProbeResults = emptyMap(),
                    songs = emptyList(), bottomPadding = 0.dp, onBack = {}, onAddFolder = {},
                    onAddNetworkSource = { saved, _ -> assertEquals("", saved.shareOrPath); saves++ },
                    onRemoveFolder = {}, onRemoveNetworkSource = {}, onRefresh = {},
                )
            }
        }
        rule.onNodeWithText("My WebDAV").performClick()
        rule.onNodeWithContentDescription("Save network source").performClick()
        rule.runOnIdle { assertEquals(1, saves) }
    }
}
