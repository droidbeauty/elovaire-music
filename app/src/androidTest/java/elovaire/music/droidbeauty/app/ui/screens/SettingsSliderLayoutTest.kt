package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.playback.CrossfadeSilencePolicy
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.upscalerCopy
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import elovaire.music.droidbeauty.app.ui.theme.ElovaireTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsSliderLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun crossfadeSlidersRemainAlignedAndAdjustable() {
        var duration by mutableLongStateOf(3_000L)
        var threshold by mutableFloatStateOf(-80f)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    CrossfadeScreen(duration, threshold, 0.dp, {}, { duration = it }, { threshold = it })
                }
            }
        }
        val durationSlider = rule.onNodeWithTag("crossfade_duration_slider")
        val silenceSlider = rule.onNodeWithTag("crossfade_silence_slider")
        durationSlider.assertIsDisplayed()
        silenceSlider.assertIsDisplayed()
        assertEquals(20f, durationSlider.getUnclippedBoundsInRoot().left.value, 0.5f)
        assertEquals(durationSlider.getUnclippedBoundsInRoot().left.value, silenceSlider.getUnclippedBoundsInRoot().left.value, 0.5f)
        durationSlider.performTouchInput { click(Offset(center.x * 1.9f, center.y)) }
        silenceSlider.performTouchInput { click(Offset(center.x * 0.1f, center.y)) }
        rule.runOnIdle {
            assertEquals(5_000L, duration)
            assertEquals(CrossfadeSilencePolicy.MIN_LEVEL_DB, threshold, 0.01f)
        }
    }

    @Test
    fun upscalerSliderRemainsAdjustable() {
        var settings by mutableStateOf(UpscalerSettings())
        val copy = upscalerCopy(AppLanguage.English)
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    UpscalerScreen(settings, 0.dp, {}, { settings = it })
                }
            }
        }
        rule.onNodeWithContentDescription(copy.multiplier)
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
        rule.runOnIdle { assertEquals(8, settings.multiplier) }
    }
    @Test
    fun upscalerDefaultButtonRestoresEverySetting() {
        var settings by mutableStateOf(UpscalerSettings(enabled = true, multiplier = 8, highPrecision = true, headroomDb = 6))
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.English) {
                ElovaireTheme(ThemeMode.Dark, TextSizePreset.Default) {
                    UpscalerScreen(settings, 0.dp, {}, { settings = it })
                }
            }
        }
        rule.onNodeWithText("Set to default").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(UpscalerSettings(), settings) }
    }

}
