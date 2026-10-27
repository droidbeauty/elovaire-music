package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollingResistanceTest {
    @get:Rule val rule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test fun verticalDragsHaveGentleResistanceWithoutAffectingPreciseScrolls() = verify(horizontal = false)
    @Test fun horizontalDragsHaveGentleResistanceWithoutStretchingTheItems() = verify(horizontal = true)
    @Test fun horizontalResistanceAlsoWorksInRightToLeftLayouts() = verify(horizontal = true, rtl = true)

    @Test
    fun horizontalScrollStripsUseTheSameResistanceAsLazyGalleries() {
        var resistance by mutableStateOf(false)
        lateinit var state: ScrollState
        lateinit var scope: CoroutineScope
        rule.setContent {
            state = rememberScrollState()
            scope = rememberCoroutineScope()
            Row(
                Modifier.fillMaxSize().testTag("scroll_fixture")
                    .then(if (resistance) Modifier.ensureHorizontalRubberBand(state) else Modifier)
                    .horizontalScroll(state, overscrollEffect = null),
            ) {
                repeat(40) { Box(Modifier.size(80.dp)) { Text("$it") } }
            }
        }
        drag(horizontal = true)
        val normalOffset = rule.runOnIdle { state.value }
        rule.runOnIdle {
            resistance = true
            scope.launch { state.scrollTo(0) }
        }
        rule.waitForIdle()
        drag(horizontal = true)
        val resistedOffset = rule.runOnIdle { state.value }
        assertTrue("Normal=$normalOffset resisted=$resistedOffset", resistedOffset in 1 until normalOffset)
        assertTrue(resistedOffset.toFloat() / normalOffset > 0.88f)
    }

    private fun verify(horizontal: Boolean, rtl: Boolean = false) {
        var resistance by mutableStateOf(false)
        lateinit var state: LazyListState
        lateinit var scope: CoroutineScope
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                state = rememberLazyListState()
                scope = rememberCoroutineScope()
                val modifier = Modifier.fillMaxSize().testTag("scroll_fixture").then(
                    if (!resistance) Modifier else if (horizontal) {
                        Modifier.ensureHorizontalRubberBand(state)
                    } else {
                        Modifier.ensureSingleItemRubberBand(state)
                    },
                )
                if (horizontal) {
                    LazyRow(state = state, overscrollEffect = null, modifier = modifier) {
                        items(100) { index -> Box(Modifier.size(80.dp)) { Text("$index") } }
                    }
                } else {
                    LazyColumn(state = state, overscrollEffect = null, modifier = modifier) {
                        items(100) { index -> Box(Modifier.size(80.dp)) { Text("$index") } }
                    }
                }
            }
        }
        drag(horizontal, rtl)
        val normalOffset = rule.runOnIdle { state.firstVisibleItemScrollOffset }
        rule.runOnIdle {
            resistance = true
            scope.launch { state.scrollToItem(0) }
        }
        rule.waitForIdle()
        drag(horizontal, rtl)
        val resistedOffset = rule.runOnIdle { state.firstVisibleItemScrollOffset }
        assertTrue("Normal=$normalOffset resisted=$resistedOffset", resistedOffset in 1 until normalOffset)
        assertTrue(resistedOffset.toFloat() / normalOffset > 0.88f)

        rule.runOnIdle { scope.launch { state.scrollToItem(10, 7) } }
        rule.waitForIdle()
        rule.runOnIdle {
            assertEquals(10, state.firstVisibleItemIndex)
            assertEquals(7, state.firstVisibleItemScrollOffset)
        }
    }

    private fun drag(horizontal: Boolean, rtl: Boolean = false) {
        rule.onNodeWithTag("scroll_fixture").performTouchInput {
            down(center)
            repeat(12) { moveBy(if (horizontal) Offset(if (rtl) 12f else -12f, 0f) else Offset(0f, -12f), delayMillis = 16) }
            advanceEventTime(200)
            up()
        }
        rule.waitForIdle()
    }
}
