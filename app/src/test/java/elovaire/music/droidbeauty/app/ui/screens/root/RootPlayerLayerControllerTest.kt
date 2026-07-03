package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class RootPlayerLayerControllerTest {
    @Test
    fun playerLayerStateAfterHide_returnsToCompactOnlyWhenSongStillExists() {
        assertEquals(
            PlayerLayerState.ReturningToCompact,
            playerLayerStateAfterHide(returnToCompact = true, currentSongPresent = true),
        )
        assertEquals(
            PlayerLayerState.Compact,
            playerLayerStateAfterHide(returnToCompact = true, currentSongPresent = false),
        )
        assertEquals(
            PlayerLayerState.Compact,
            playerLayerStateAfterHide(returnToCompact = false, currentSongPresent = true),
        )
    }
}
