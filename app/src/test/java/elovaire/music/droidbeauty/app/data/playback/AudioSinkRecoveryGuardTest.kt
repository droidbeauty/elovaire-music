package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioSinkRecoveryGuardTest {
    @Test
    fun sameFailureGenerationIsRecoveredAtMostOnce() {
        val guard = AudioSinkRecoveryGuard()
        val first = AudioSinkRecoveryKey(1L, 2L, "sink")

        assertTrue(guard.claim(first))
        assertFalse(guard.claim(first))
        assertTrue(guard.claim(first.copy(routeGeneration = 3L)))
        guard.reset()
        assertTrue(guard.claim(first))
    }
}
