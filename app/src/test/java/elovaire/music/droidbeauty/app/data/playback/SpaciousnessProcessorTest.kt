package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlin.math.abs
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaciousnessProcessorTest {
    @Test
    fun mappedAmountLookup_staysCloseToReferenceCurve() {
        listOf(0f, 0.1f, 0.25f, 0.5f, 0.75f, 1f).forEach { amount ->
            val expected = amount.toDouble().pow(1.18).toFloat()
            val actual = SpaciousnessProcessorModel.mappedAmount(amount)

            assertTrue("amount=$amount actual=$actual expected=$expected", abs(actual - expected) < 0.0002f)
        }
    }

    @Test
    fun headroomLookup_returnsFiniteGainForEveryMode() {
        SpaciousnessMode.entries.forEach { mode ->
            assertTrue(SpaciousnessProcessorModel.headroomGain(mode, 0.5f).isFinite())
        }
    }
}
