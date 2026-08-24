package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionListTest {
    @Test
    fun revealRegistryStopsRetainingKeysAfterBound() {
        val registry = MotionRevealRegistry()

        repeat(128) { index ->
            registry.markRevealed("song-$index")
        }

        assertTrue(registry.isRevealed("new-song"))
        assertFalse(MotionRevealRegistry().isRevealed("new-song"))
    }
}
