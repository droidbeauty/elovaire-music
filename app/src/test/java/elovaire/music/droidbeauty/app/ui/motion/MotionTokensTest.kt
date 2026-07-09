package elovaire.music.droidbeauty.app.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class MotionTokensTest {
    @Test
    fun durationTokensRemainStable() {
        assertEquals(80, MotionDuration.Micro)
        assertEquals(90, MotionDuration.Quick)
        assertEquals(120, MotionDuration.Fast)
        assertEquals(180, MotionDuration.Standard)
        assertEquals(260, MotionDuration.Medium)
        assertEquals(340, MotionDuration.Spacious)
        assertEquals(360, MotionDuration.Screen)
        assertEquals(240, MotionDuration.ScreenFade)
        assertEquals(420, MotionDuration.ScreenSlide)
        assertEquals(440, MotionDuration.ScreenExpand)
        assertEquals(540, MotionDuration.Player)
        assertEquals(320, MotionDuration.ListReveal)
    }

    @Test
    fun elovaireMotionFacadeUsesCanonicalTokensAndEasings() {
        assertEquals(MotionDuration.Quick, ElovaireMotion.Quick)
        assertEquals(MotionDuration.Fast, ElovaireMotion.Fast)
        assertEquals(MotionDuration.Standard, ElovaireMotion.Standard)
        assertEquals(MotionDuration.Player, ElovaireMotion.PlayerScreen)
        assertSame(MotionEasing.SoftOut, ElovaireMotion.SoftOut)
        assertSame(MotionEasing.RefinedDecelerate, ElovaireMotion.RefinedDecelerate)
        assertSame(MotionEasing.RefinedAccelerate, ElovaireMotion.RefinedAccelerate)
    }
}
