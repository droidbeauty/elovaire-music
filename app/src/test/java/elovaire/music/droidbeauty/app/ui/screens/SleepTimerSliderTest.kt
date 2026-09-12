package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class SleepTimerSliderTest {
    @Test
    fun thirtyMinutes_isTheCenterStep() {
        assertEquals(0.5f, sleepTimerFractionForMinutes(30f), 0.0001f)
        assertEquals(30f, sleepTimerMinutesForFraction(0.5f), 0.0001f)
    }

    @Test
    fun fractionMapping_clampsAndSnapsToFiveMinuteSteps() {
        assertEquals(10f, sleepTimerMinutesForFraction(-1f), 0.0001f)
        assertEquals(60f, sleepTimerMinutesForFraction(2f), 0.0001f)
        assertEquals(25f, sleepTimerMinutesForFraction(0.375f), 0.0001f)
        assertEquals(45f, sleepTimerMinutesForFraction(0.75f), 0.0001f)
    }
}
