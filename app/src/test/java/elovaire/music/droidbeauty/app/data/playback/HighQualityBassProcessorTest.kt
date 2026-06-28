package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HighQualityBassProcessorTest {
    @Test
    fun bassCurve_fullAmountBoostsBassWithoutLargeMudCut() {
        val config = HighQualityBassConfig(amountNormalized = 1f).sanitized()
        val r60 = HighQualityBassProcessorModel.responseAt(60f, 48_000, config)
        val r100 = HighQualityBassProcessorModel.responseAt(100f, 48_000, config)
        val r125 = HighQualityBassProcessorModel.responseAt(125f, 48_000, config)
        val r250 = HighQualityBassProcessorModel.responseAt(250f, 48_000, config)
        val r500 = HighQualityBassProcessorModel.responseAt(500f, 48_000, config)
        val r1000 = HighQualityBassProcessorModel.responseAt(1_000f, 48_000, config)

        assertTrue(r60.totalDb > 1.5f)
        assertTrue(r100.totalDb > 1.0f)
        assertTrue(r125.totalDb > 0.6f)
        assertTrue(r250.totalDb > -1.2f)
        assertTrue(r500.totalDb > -1.0f)
        assertTrue(r1000.totalDb > -1.0f)
    }

    @Test
    fun automaticHeadroom_isProtectiveButNotOverlyDestructiveForBassOnly() {
        val curve = HighQualityBassProcessorModel.curveFor(amountNormalized = 1f)

        assertTrue(curve.automaticHeadroomDb <= 0f)
        assertTrue(curve.automaticHeadroomDb >= -4.5f)
    }

    @Test
    fun bassCurve_zeroAmountBypasses() {
        val curve = HighQualityBassProcessorModel.curveFor(amountNormalized = 0f)

        assertTrue(curve.isBypassed)
    }

    @Test
    fun bassDynamicReduction_ignoresLowEnvelopeBelowThreshold() {
        val config = HighQualityBassConfig().sanitized()

        assertEquals(
            0f,
            HighQualityBassProcessorModel.dynamicReductionDb(
                lowBandEnvelope = config.dynamicControlThreshold * 0.5f,
                amountNormalized = 1f,
                config = config,
            ),
            0.0001f,
        )
    }
}
