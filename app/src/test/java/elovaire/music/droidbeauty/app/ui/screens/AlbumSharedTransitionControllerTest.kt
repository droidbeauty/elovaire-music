package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AlbumSharedTransitionControllerTest {
    @Test
    fun selection_replacesOnlyThePreviousSource() {
        val controller = AlbumSharedTransitionController()

        controller.select(albumId = 7L, sourceToken = 11L)
        assertEquals(
            AlbumSharedTransitionSelection(albumId = 7L, sourceToken = 11L),
            controller.selection,
        )

        controller.select(albumId = 7L, sourceToken = 12L)
        assertEquals(
            AlbumSharedTransitionSelection(albumId = 7L, sourceToken = 12L),
            controller.selection,
        )
    }

    @Test
    fun sourceTokens_areUniqueAcrossVisibleRepresentations() {
        assertNotEquals(AlbumSharedTransitionToken.next(), AlbumSharedTransitionToken.next())
    }
}
