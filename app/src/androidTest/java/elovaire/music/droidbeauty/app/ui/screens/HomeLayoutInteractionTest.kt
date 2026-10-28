package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.state.ToggleableState
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.settings.elovaireSettingsDataStore
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeLayoutInteractionTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()
    private val key = stringPreferencesKey("home_module_order")
    private var saved: String? = null
    private var restore = false

    @After
    fun restorePreference() {
        if (!restore) return
        runBlocking {
            rule.activity.applicationContext.elovaireSettingsDataStore().edit { preferences ->
                saved?.let { preferences[key] = it } ?: preferences.remove(key)
            }
        }
    }

    @Test
    fun longPressDragPersistsAndRecentlyAddedRemainsFixed() {
        val store = rule.activity.applicationContext.elovaireSettingsDataStore()
        runBlocking {
            saved = store.data.first()[key]
            restore = true
            store.edit { it.remove(key) }
        }
        var mounted by mutableStateOf(true)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    if (mounted) HomeLayoutScreen(bottomPadding = 0.dp, onBack = {})
                }
            }
        }
        rule.onNodeWithText("Last played").assertIsDisplayed()
        rule.onNodeWithContentDescription("Reorder Last played").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(0f, 200f), delayMillis = 250)
            up()
        }
        rule.waitUntil(5_000) { runBlocking { store.data.first()[key] != null } }
        val persisted = runBlocking { parseHomeLayout(store.data.first()[key]) }
        assertEquals(HomeModule.RecentlyAdded, persisted.first())
        assertEquals(HomeModule.RecentlyPlayed, persisted[1])
        rule.runOnIdle { mounted = false }
        rule.runOnIdle { mounted = true }
        rule.waitUntil(5_000) {
            val first = rule.onNodeWithText("Recently played").getUnclippedBoundsInRoot().top
            val last = rule.onNodeWithText("Last played").getUnclippedBoundsInRoot().top
            first < last
        }
        assertTrue(rule.onNodeWithText("Recently played").getUnclippedBoundsInRoot().top < rule.onNodeWithText("Last played").getUnclippedBoundsInRoot().top)
        rule.onNodeWithText("Fixed position").assertIsDisplayed()
    }

    @Test
    fun audiobookSpeedAndResumeRewindControlsChangeTheirValues() {
        var speed by mutableFloatStateOf(1f)
        var settings by mutableStateOf(AudiobookSettings())
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    AudiobookSettingsScreen(settings, speed, 0.dp, {}, {}, {}, {}, { speed = it }, { settings = settings.copy(rewindOnResume = it) })
                }
            }
        }
        rule.onNodeWithText("1.0×").assertIsDisplayed()
        rule.onNodeWithContentDescription("Playback speed").performSemanticsAction(SemanticsActions.SetProgress) { it(1.1f) }
        rule.runOnIdle { assertEquals(1.1f, speed, 0.001f) }
        rule.onNodeWithContentDescription("Playback speed").performTouchInput { click(Offset(center.x * 1.9f, center.y)) }
        rule.runOnIdle { assertTrue(speed > 2.2f) }
        rule.onNodeWithContentDescription("Playback speed").performTouchInput { click(Offset(center.x * 0.1f, center.y)) }
        rule.runOnIdle { assertTrue(speed < 0.7f) }
        rule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off)).performClick()
        rule.runOnIdle { assertTrue(settings.rewindOnResume) }
    }

    @Test
    fun homeRendersModulesInConfiguredOrder() {
        val album = Album(1L, "Test album", "Test artist", null, 0, 0L, emptyList())
        val order = listOf(HomeModule.AlbumOfDay, HomeModule.RecentlyAdded, HomeModule.RecentlyPlayed, HomeModule.LastPlayed, HomeModule.FavoriteAlbums)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    HomeScreen(
                        lastPlayedAlbum = null, lastPlayedPlaylist = null, songsById = emptyMap(),
                        recentlyAddedAlbums = emptyList(), recentSongs = emptyList(), favoriteAlbums = emptyList(),
                        playbackState = PlaybackUiState(), isLibraryLoading = false, isLibraryAuthoritative = false,
                        libraryScanProgress = 0f, favoriteSongIds = emptySet(), topPadding = 0.dp, bottomPadding = 0.dp,
                        scrollToTopRequestVersion = 0, resetScrollOnColdStart = false, playInitialReveal = false,
                        onInitialRevealFinished = {}, onAlbumSelected = { _, _ -> }, onPlaylistSelected = {},
                        onPlayAlbum = {}, onPlayPlaylist = { _, _ -> }, onShufflePlaylist = { _, _ -> },
                        onOpenRecentlyAdded = {}, onSongSelected = {}, onToggleFavorite = {},
                        albumOfDay = album, homeLayout = order,
                    )
                }
            }
        }
        val daily = rule.onNodeWithText("Album of the day").getUnclippedBoundsInRoot().top
        val recentlyPlayed = rule.onNodeWithText("Recently played").getUnclippedBoundsInRoot().top
        assertTrue(daily < recentlyPlayed)
    }
}
