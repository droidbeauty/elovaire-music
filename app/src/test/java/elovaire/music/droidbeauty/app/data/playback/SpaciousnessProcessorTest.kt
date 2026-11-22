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

    @Test
    fun stereoModesRemainFiniteAcrossRepeatedDelayWraps() {
        SpaciousnessMode.entries.filter { it != SpaciousnessMode.Off }.forEach { mode ->
            val processor = SpaciousnessProcessor()
            processor.configure(sampleRateHz = 8_000, channelCount = 2)
            processor.setConfig(SpaciousnessConfig(enabled = true, mode = mode, amountNormalized = 0.8f))
            val frame = FloatArray(2)
            repeat(10_000) { index ->
                frame[0] = if (index % 2 == 0) 0.7f else -0.7f
                frame[1] = if (index % 3 == 0) -0.5f else 0.5f
                processor.processFrame(frame, 2)
                assertTrue("mode=$mode frame=$index", frame[0].isFinite() && frame[1].isFinite())
            }
        }
    }
}
