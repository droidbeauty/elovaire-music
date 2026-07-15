package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFocusStateMachineTest {
    @Test
    fun mapsPlatformCallbacksToStableActions() {
        assertEquals(AudioFocusAction.Gain, AudioFocusStateMachine.actionFor(AudioManager.AUDIOFOCUS_GAIN))
        assertEquals(
            AudioFocusAction.TransientLoss,
            AudioFocusStateMachine.actionFor(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT),
        )
        assertEquals(
            AudioFocusAction.Duck,
            AudioFocusStateMachine.actionFor(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK),
        )
        assertEquals(
            AudioFocusAction.PermanentLoss,
            AudioFocusStateMachine.actionFor(AudioManager.AUDIOFOCUS_LOSS),
        )
        assertEquals(AudioFocusAction.Ignore, AudioFocusStateMachine.actionFor(Int.MIN_VALUE))
    }
}
