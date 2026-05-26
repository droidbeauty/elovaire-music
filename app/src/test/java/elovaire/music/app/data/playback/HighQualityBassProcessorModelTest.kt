package elovaire.music.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HighQualityBassProcessorModelTest {
    @Test
    fun amountMappingIsClampedMonotonicAndMusical() {
        val config = HighQualityBassConfig(maxShelfBoostDb = 8f)

        val zero = HighQualityBassProcessorModel.curveFor(0f, config)
        val quarter = HighQualityBassProcessorModel.curveFor(0.25f, config)
        val half = HighQualityBassProcessorModel.curveFor(0.5f, config)
        val full = HighQualityBassProcessorModel.curveFor(1f, config)
        val clamped = HighQualityBassProcessorModel.curveFor(3f, config)

        assertEquals(0f, zero.shelfDb, 0.0001f)
        assertTrue(quarter.shelfDb in 0f..half.shelfDb)
        assertTrue(half.shelfDb in quarter.shelfDb..full.shelfDb)
        assertEquals(config.maxShelfBoostDb, full.shelfDb, 0.0001f)
        assertEquals(full.shelfDb, clamped.shelfDb, 0.0001f)
    }

    @Test
    fun responseBypassesCompletelyAtZeroAmount() {
        val response = HighQualityBassProcessorModel.responseAt(
            frequencyHz = 80f,
            sampleRateHz = 48_000,
            config = HighQualityBassConfig(amountNormalized = 0f),
        )

        assertEquals(0f, response.totalDb, 0.0001f)
        assertEquals(0f, response.automaticHeadroomDb, 0.0001f)
        assertEquals(0f, response.shelfDb, 0.0001f)
        assertEquals(0f, response.mudTrimDb, 0.0001f)
    }

    @Test
    fun responseLiftsSubBassAndAvoidsLowMidBoomAcrossSampleRates() {
        val config = HighQualityBassConfig(amountNormalized = 1f)

        listOf(44_100, 48_000, 96_000, 192_000).forEach { sampleRate ->
            val low55 = HighQualityBassProcessorModel.responseAt(55f, sampleRate, config).totalDb
            val low85 = HighQualityBassProcessorModel.responseAt(85f, sampleRate, config).totalDb
            val low250 = HighQualityBassProcessorModel.responseAt(250f, sampleRate, config).totalDb
            val mid1k = HighQualityBassProcessorModel.responseAt(1_000f, sampleRate, config).totalDb

            assertTrue(low55 > low250)
            assertTrue(low85 > low250)
            assertTrue(low250 <= low85 - 1.5f)
            assertTrue(mid1k <= 0.1f)
        }
    }

    @Test
    fun automaticHeadroomGetsMoreConservativeWithBassAndLowEqBoost() {
        val light = HighQualityBassProcessorModel.curveFor(
            amountNormalized = 0.35f,
            lowBandEqBoostSafetyDb = 0f,
        ).automaticHeadroomDb
        val strong = HighQualityBassProcessorModel.curveFor(
            amountNormalized = 1f,
            lowBandEqBoostSafetyDb = 7f,
            trebleBoostDb = 3f,
            spaciousnessAmount = 0.8f,
        ).automaticHeadroomDb

        assertTrue(light < 0f)
        assertTrue(strong < light)
    }

    @Test
    fun dynamicReductionRespondsOnlyToHeavyBassEnergy() {
        val idle = HighQualityBassProcessorModel.dynamicReductionDb(
            lowBandEnvelope = 0.05f,
            amountNormalized = 1f,
        )
        val heavy = HighQualityBassProcessorModel.dynamicReductionDb(
            lowBandEnvelope = 0.72f,
            amountNormalized = 1f,
        )

        assertEquals(0f, idle, 0.0001f)
        assertTrue(heavy < 0f)
    }

    @Test
    fun responsesStayFiniteAtEdgeSampleRates() {
        val config = HighQualityBassConfig(amountNormalized = 1f, maxShelfBoostDb = 8.8f)

        listOf(8_000, 44_100, 96_000, 192_000).forEach { sampleRate ->
            listOf(20f, 40f, 80f, 250f, 1_000f, 16_000f).forEach { frequency ->
                val response = HighQualityBassProcessorModel.responseAt(frequency, sampleRate, config)
                assertFalse(response.totalDb.isNaN())
                assertFalse(response.totalDb.isInfinite())
            }
        }
    }

    @Test
    fun rampingIsMonotonicAndReachesTarget() {
        val ramp = HighQualityBassProcessorModel.buildRamp(
            from = 0f,
            to = 1f,
            smoothingTimeMs = 96,
        )

        assertTrue(ramp.isNotEmpty())
        assertTrue(ramp.zipWithNext().all { (left, right) -> right >= left })
        assertEquals(1f, ramp.last(), 0.0001f)
    }

    @Test
    fun invalidConfigValuesAreSafelyClamped() {
        val sanitized = HighQualityBassConfig(
            amountNormalized = 3f,
            highPassFrequencyHz = 2f,
            shelfFrequencyHz = 2f,
            shelfSlope = 9f,
            maxShelfBoostDb = 99f,
            punchCenterHz = 4f,
            punchQ = 99f,
            maxPunchDb = 99f,
            mudTrimCenterHz = 5f,
            mudTrimQ = 99f,
            maxMudTrimDb = 12f,
            smoothingTimeMs = 2,
        ).sanitized()

        assertEquals(1f, sanitized.amountNormalized, 0.0001f)
        assertTrue(sanitized.highPassFrequencyHz >= 18f)
        assertTrue(sanitized.shelfFrequencyHz >= 65f)
        assertTrue(sanitized.shelfSlope <= 1.05f)
        assertTrue(sanitized.maxShelfBoostDb <= 9f)
        assertTrue(sanitized.punchCenterHz >= 45f)
        assertTrue(sanitized.punchQ <= 1.25f)
        assertTrue(sanitized.maxPunchDb <= 2.7f)
        assertTrue(sanitized.mudTrimCenterHz >= 170f)
        assertTrue(sanitized.mudTrimQ <= 1.25f)
        assertTrue(sanitized.maxMudTrimDb <= 1.8f)
        assertTrue(sanitized.smoothingTimeMs >= 45)
    }
}
