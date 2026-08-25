package elovaire.music.droidbeauty.app.ui.interaction

import org.junit.Assert.assertEquals
import org.junit.Test

class ElovaireInteractionTest {
    @Test
    fun pressTakesPriorityOverConfirmation() {
        assertEquals(
            0.96f,
            pillActionTargetScale(
                pressed = true,
                confirmation = true,
                pressedScale = 0.96f,
                confirmationScale = 1.035f,
            ),
            0f,
        )
    }

    @Test
    fun confirmationReturnsToRestAfterStateSettles() {
        assertEquals(
            1.035f,
            pillActionTargetScale(
                pressed = false,
                confirmation = true,
                pressedScale = 0.96f,
                confirmationScale = 1.035f,
            ),
            0f,
        )
        assertEquals(
            1f,
            pillActionTargetScale(
                pressed = false,
                confirmation = false,
                pressedScale = 0.96f,
                confirmationScale = 1.035f,
            ),
            0f,
        )
    }
}
