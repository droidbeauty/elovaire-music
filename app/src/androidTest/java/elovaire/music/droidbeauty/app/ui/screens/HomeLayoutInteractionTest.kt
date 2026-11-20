package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
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
    fun longPressDragPersistsAndLastPlayedRemainsFixed() {
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
        rule.onNodeWithContentDescription("Reorder Recently added").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(0f, 200f), delayMillis = 250)
            up()
        }
        rule.waitUntil(5_000) { runBlocking { store.data.first()[key] != null } }
        val persisted = runBlocking { parseHomeLayout(store.data.first()[key]) }
        assertEquals(HomeModule.LastPlayed, persisted.first())
        assertEquals(HomeModule.RecentlyPlayed, persisted[1])
        rule.runOnIdle { mounted = false }
        rule.runOnIdle { mounted = true }
        rule.waitUntil(5_000) {
            val first = rule.onNodeWithText("Recently played").getUnclippedBoundsInRoot().top
            val last = rule.onNodeWithText("Recently added").getUnclippedBoundsInRoot().top
            first < last
        }
        assertTrue(rule.onNodeWithText("Recently played").getUnclippedBoundsInRoot().top < rule.onNodeWithText("Recently added").getUnclippedBoundsInRoot().top)
        rule.onNodeWithText("Fixed position").assertIsDisplayed()
    }

    @Test
    fun audiobookSpeedAndResumeRewindControlsChangeTheirValues() {
        var speed by mutableFloatStateOf(1f)
        var settings by mutableStateOf(AudiobookSettings())
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    androidx.compose.material3.Surface(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    ) {
                        AudiobookSettingsScreen(
                            settings, speed, 0.dp, {},
                            { settings = settings.copy(rewindSeconds = it) },
                            { settings = settings.copy(forwardSeconds = it) },
                            {}, { speed = it }, { settings = settings.copy(rewindOnResume = it) },
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("1.0").assertIsDisplayed()
        val rewindIncrease = rule.onAllNodesWithText("+")[0]
        val rewindBounds = rewindIncrease.getUnclippedBoundsInRoot()
        assertTrue(rewindBounds.right - rewindBounds.left >= 48.dp)
        rewindIncrease.performClick()
        rule.runOnIdle { assertEquals(20, settings.rewindSeconds) }
        rule.onNodeWithContentDescription("Playback speed").performSemanticsAction(SemanticsActions.SetProgress) { it(1.25f) }
        rule.runOnIdle { assertEquals(1.25f, speed, 0.001f) }
        rule.onNodeWithContentDescription("Playback speed").performTouchInput { click(Offset(center.x * 1.9f, center.y)) }
        rule.runOnIdle { assertTrue(speed > 1.75f) }
        rule.onNodeWithContentDescription("Playback speed").performTouchInput { click(Offset(center.x * 0.1f, center.y)) }
        rule.runOnIdle { assertTrue(speed < 0.7f) }
        rule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off)).performClick()
        rule.runOnIdle { assertTrue(settings.rewindOnResume) }
    }

    @Test
    fun sliderMarksMatchTheTrackWithoutOverlappingTransparency() {
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                Box(Modifier.width(200.dp).background(Color.Black).testTag("slider_capture")) {
                    SteppedSlider(selectedIndex = 0, stepCount = 5, onSelectedIndexChanged = {})
                }
            }
        }
        val pixels = rule.onNodeWithTag("slider_capture").captureToImage().toPixelMap()
        val density = rule.density.density
        val middle = pixels.height / 2
        val track = pixels[(pixels.width * 0.375f).toInt(), middle]
        val mark = pixels[pixels.width / 4, middle - (3f * density).toInt()]
        val intersection = pixels[pixels.width / 4, middle]
        assertTrue("Track should be visible", track.red > 0.1f)
        assertTrue("Track and marks should have the same color", kotlin.math.abs(track.red - mark.red) < 0.04f)
        assertTrue("Track and marks should not darken at intersections", kotlin.math.abs(track.red - intersection.red) < 0.04f)
    }

    @Test
    fun equalizerSliderMarksMatchTheTrackWithoutOverlappingTransparency() {
        rule.setContent {
            ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                Box(Modifier.width(200.dp).background(Color.Black).testTag("slider_capture")) {
                    ThinContinuousSlider(value = 0f, onValueChange = {}, valueRange = 0f..4f, visualStepSize = 1f)
                }
            }
        }
        val pixels = rule.onNodeWithTag("slider_capture").captureToImage().toPixelMap()
        val middle = pixels.height / 2
        val track = pixels[(pixels.width * 0.39f).toInt(), middle]
        val markX = (pixels.width * 0.275f).toInt()
        val mark = pixels[markX, middle - (3f * rule.density.density).toInt()]
        val intersection = pixels[markX, middle]
        assertTrue("Track should be visible", track.red > 0.1f)
        assertTrue("Track and marks should have the same color", kotlin.math.abs(track.red - mark.red) < 0.04f)
        assertTrue("Track and marks should not darken at intersections", kotlin.math.abs(track.red - intersection.red) < 0.04f)
    }

    @Test
    fun coldStartResetsScrollWhenHomeContentAppearsAfterLoading() {
        val album = Album(1L, "Test album", "Test artist", null, 0, 0L, emptyList())
        var loading by mutableStateOf(true)
        rule.runOnUiThread { lazyListPositionCache["home_screen"] = 2 to 80 }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                    ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                        HomeScreen(
                            lastPlayedAlbum = null, lastPlayedPlaylist = null, songsById = emptyMap(),
                            recentlyAddedAlbums = emptyList(), recentSongs = emptyList(), favoriteAlbums = emptyList(),
                            playbackState = PlaybackUiState(), isLibraryLoading = loading, isLibraryAuthoritative = false,
                            libraryScanProgress = 0f, favoriteSongIds = emptySet(), topPadding = 0.dp, bottomPadding = 0.dp,
                            scrollToTopRequestVersion = 0, resetScrollOnColdStart = true, playInitialReveal = false,
                            onInitialRevealFinished = {}, onAlbumSelected = { _, _ -> }, onPlaylistSelected = {},
                            onPlayAlbum = {}, onPlayPlaylist = { _, _ -> }, onShufflePlaylist = { _, _ -> },
                            onOpenRecentlyAdded = {}, onSongSelected = {}, onToggleFavorite = {},
                            albumOfDay = album, homeLayout = listOf(HomeModule.AlbumOfDay, HomeModule.RecentlyAdded,
                                HomeModule.RecentlyPlayed, HomeModule.FavoriteAlbums),
                        )
                    }
                }
            }
            rule.runOnIdle { loading = false }
            rule.onNodeWithText("Album of the day").assertIsDisplayed()
        } finally {
            rule.runOnUiThread { lazyListPositionCache.removeIf { it == "home_screen" } }
        }
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
