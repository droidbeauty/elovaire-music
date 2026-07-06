package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElovaireAdaptiveInfoTest {
    @Test
    fun compactWidthPreservesPhoneChrome() {
        val info = elovaireAdaptiveInfo(width = 599.dp)

        assertEquals(ElovaireWindowMode.CompactPhone, info.windowMode)
        assertFalse(info.useNavigationRail)
        assertFalse(info.useListDetailPanes)
        assertFalse(info.useSupportingPane)
    }

    @Test
    fun mediumWidthUsesRailWithoutPanes() {
        val info = elovaireAdaptiveInfo(width = 600.dp)

        assertEquals(ElovaireWindowMode.Medium, info.windowMode)
        assertTrue(info.useNavigationRail)
        assertFalse(info.useListDetailPanes)
        assertFalse(info.useSupportingPane)
    }

    @Test
    fun expandedWidthEnablesAdaptivePanes() {
        val info = elovaireAdaptiveInfo(width = 840.dp)

        assertEquals(ElovaireWindowMode.Expanded, info.windowMode)
        assertTrue(info.useNavigationRail)
        assertTrue(info.useListDetailPanes)
        assertTrue(info.useSupportingPane)
    }

    @Test
    fun bookPostureCanUseListDetailBeforeExpandedWidth() {
        val info = elovaireAdaptiveInfo(
            width = 700.dp,
            postureMode = ElovairePostureMode.Book,
        )

        assertEquals(ElovaireWindowMode.Medium, info.windowMode)
        assertTrue(info.useNavigationRail)
        assertTrue(info.useListDetailPanes)
        assertTrue(info.useSupportingPane)
    }
}
