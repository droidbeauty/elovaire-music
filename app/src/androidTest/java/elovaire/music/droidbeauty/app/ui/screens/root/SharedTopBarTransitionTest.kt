package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.navigationAccessibilityCopy
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedTopBarTransitionTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun backButtonRemainsSingleAndUsesCurrentCallbackAcrossHeaderChanges() {
        var backCalls = 0
        var spec: SharedTopBarSpec by mutableStateOf(
            SharedTopBarSpec.Back(title = "Albums", onBack = { backCalls = 1 }),
        )
        val backLabel = navigationAccessibilityCopy(AppLanguage.English).back
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    SharedTopBarOverlay(spec)
                }
            }
        }
        val originalBounds = rule.onNodeWithContentDescription(backLabel).getUnclippedBoundsInRoot()
        rule.mainClock.autoAdvance = false
        val targets = listOf(
            SharedTopBarSpec.Detail(title = "Album", subtitle = "Artist", onBack = { backCalls = 2 }),
            SharedTopBarSpec.Detail(title = "Another album", subtitle = "Artist", onBack = { backCalls = 3 }, actions = listOf(
                TopBarActionSpec(R.drawable.ic_lucide_plus, "Add", {}),
            )),
            SharedTopBarSpec.Detail(title = "Album", subtitle = null, onBack = { backCalls = 4 }, actions = listOf(
                TopBarActionSpec(R.drawable.ic_lucide_plus, "Add", {}),
            )),
            SharedTopBarSpec.Back(title = "Settings", onBack = { backCalls = 5 }),
            SharedTopBarSpec.Back(title = "Library", onBack = { backCalls = 6 }, actions = listOf(
                TopBarActionSpec(R.drawable.ic_lucide_plus, "Add", {}),
            )),
        )
        targets.forEachIndexed { index, target ->
            rule.runOnIdle { spec = target }
            rule.mainClock.advanceTimeBy(50)
            rule.onAllNodesWithContentDescription(backLabel).assertCountEquals(1)
            assertEquals(originalBounds, rule.onNodeWithContentDescription(backLabel).getUnclippedBoundsInRoot())
            rule.onNodeWithContentDescription(backLabel).performClick()
            rule.runOnIdle { assertEquals(index + 2, backCalls) }
            rule.mainClock.advanceTimeBy(1000)
        }
    }
    @Test
    fun menuRemainsSingleWhenSupplementalActionChanges() {
        var menuCalls = 0
        var spec by mutableStateOf(SharedTopBarSpec.Unified("Playlists", true, onOpenMenu = { menuCalls++ }))
        val menuLabel = navigationAccessibilityCopy(AppLanguage.English).menu
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    SharedTopBarOverlay(spec)
                }
            }
        }
        rule.mainClock.autoAdvance = false
        rule.runOnIdle {
            spec = spec.copy(
                supplementalActionIconResId = R.drawable.ic_lucide_plus,
                supplementalActionContentDescription = "Add",
                onSupplementalAction = {},
            )
        }
        rule.mainClock.advanceTimeBy(50)
        rule.onAllNodesWithContentDescription(menuLabel).assertCountEquals(1)
        rule.onNodeWithContentDescription(menuLabel).performClick()
        rule.runOnIdle { assertEquals(1, menuCalls) }
    }

    @Test
    fun tabTitleSlidesHorizontallyWhenMenuVisibilityChanges() {
        var spec by mutableStateOf(SharedTopBarSpec.Unified("Library", true, onOpenMenu = {}))
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    SharedTopBarOverlay(spec)
                }
            }
        }
        val top = rule.onNodeWithText("Library").getUnclippedBoundsInRoot().top
        rule.mainClock.autoAdvance = false
        listOf("Playlists" to true, "Search" to false, "Home" to true).forEach { (title, showMenu) ->
            rule.runOnIdle { spec = spec.copy(title = title, showSettings = showMenu) }
            rule.mainClock.advanceTimeBy(50)
            val moving = rule.onNodeWithText(title).getUnclippedBoundsInRoot()
            rule.mainClock.advanceTimeBy(1000)
            val settled = rule.onNodeWithText(title).getUnclippedBoundsInRoot()
            assertEquals(top.value, moving.top.value, 0.5f)
            assertEquals(top.value, settled.top.value, 0.5f)
            assertTrue("Title should enter horizontally", moving.left > settled.left)
        }
    }

}
