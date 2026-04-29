package elovaire.music.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BassBoostProcessorModelTest {
    @Test
    fun normalizedAmountMapping_isClampedMonotonicAndSmooth() {
        val config = BassBoostConfig(maxBoostDb = 7.5f)

        val zero = BassBoostProcessorModel.normalizedAmountToBoostDb(0f, config)
        val quarter = BassBoostProcessorModel.normalizedAmountToBoostDb(0.25f, config)
        val half = BassBoostProcessorModel.normalizedAmountToBoostDb(0.5f, config)
        val full = BassBoostProcessorModel.normalizedAmountToBoostDb(1f, config)
        val clamped = BassBoostProcessorModel.normalizedAmountToBoostDb(3f, config)

        assertEquals(0f, zero, 0.0001f)
        assertTrue(quarter in 0f..half)
        assertTrue(half in quarter..full)
        assertEquals(config.maxBoostDb, full, 0.0001f)
        assertEquals(full, clamped, 0.0001f)
    }

    @Test
    fun responseBypassesCompletelyAtZeroAmount() {
        val response = BassBoostProcessorModel.responseAt(
            frequencyHz = 80f,
            sampleRateHz = 48_000,
            config = BassBoostConfig(amountNormalized = 0f),
        )

        assertEquals(0f, response.totalDb, 0.0001f)
        assertEquals(0f, response.pregainDb, 0.0001f)
        assertEquals(0f, response.shelfDb, 0.0001f)
        assertEquals(0f, response.mudTrimDb, 0.0001f)
    }

    @Test
    fun responseBoostsBassMoreThanMidrangeAcrossSampleRates() {
        val config = BassBoostConfig(amountNormalized = 1f)

        listOf(44_100, 48_000, 96_000, 192_000).forEach { sampleRate ->
            val low40 = BassBoostProcessorModel.responseAt(40f, sampleRate, config).totalDb
            val low80 = BassBoostProcessorModel.responseAt(80f, sampleRate, config).totalDb
            val low100 = BassBoostProcessorModel.responseAt(100f, sampleRate, config).totalDb
            val low250 = BassBoostProcessorModel.responseAt(250f, sampleRate, config).totalDb
            val mid1k = BassBoostProcessorModel.responseAt(1_000f, sampleRate, config).totalDb

            assertTrue(low40 > mid1k)
            assertTrue(low80 > mid1k)
            assertTrue(low100 > low250)
            assertTrue(mid1k <= 0.1f)
        }
    }

    @Test
    fun pregainCompensationIncreasesWithBoost() {
        val light = BassBoostProcessorModel.pregainDbForBoost(2f, BassBoostConfig())
        val strong = BassBoostProcessorModel.pregainDbForBoost(7f, BassBoostConfig())

        assertTrue(light < 0f)
        assertTrue(strong < light)
    }

    @Test
    fun coefficientsAndResponsesStayFinite() {
        val config = BassBoostConfig(amountNormalized = 1f, maxBoostDb = 8.5f)
        val shelf = LowShelfBiquad.design(
            sampleRateHz = 96_000f,
            cornerFrequencyHz = 90f,
            gainDb = 6.5f,
            slope = 0.8f,
        )

        listOf(shelf.b0, shelf.b1, shelf.b2, shelf.a1, shelf.a2).forEach { coefficient ->
            assertFalse(coefficient.isNaN())
            assertFalse(coefficient.isInfinite())
        }

        listOf(40f, 80f, 100f, 250f, 1_000f, 16_000f).forEach { frequency ->
            val response = BassBoostProcessorModel.responseAt(frequency, 96_000, config)
            assertFalse(response.totalDb.isNaN())
            assertFalse(response.totalDb.isInfinite())
        }
    }

    @Test
    fun rampingIsMonotonicAndReachesTarget() {
        val ramp = BassBoostProcessorModel.buildRamp(
            from = 0f,
            to = 1f,
            smoothingTimeMs = 128,
        )

        assertTrue(ramp.isNotEmpty())
        assertTrue(ramp.zipWithNext().all { (left, right) -> right >= left })
        assertEquals(1f, ramp.last(), 0.0001f)
    }

    @Test
    fun invalidConfigValuesAreSafelyClamped() {
        val sanitized = BassBoostConfig(
            amountNormalized = 3f,
            maxBoostDb = 99f,
            shelfFrequencyHz = 2f,
            shelfSlope = 9f,
            mudTrimCenterHz = 5f,
            mudTrimOctaves = 99f,
            maxMudTrimDb = 12f,
            pregainRatio = 9f,
            smoothingTimeMs = 2,
        ).sanitized()

        assertEquals(1f, sanitized.amountNormalized, 0.0001f)
        assertTrue(sanitized.maxBoostDb <= 9f)
        assertTrue(sanitized.shelfFrequencyHz >= 40f)
        assertTrue(sanitized.shelfSlope <= 1.2f)
        assertTrue(sanitized.mudTrimCenterHz >= 140f)
        assertTrue(sanitized.mudTrimOctaves <= 2.5f)
        assertTrue(sanitized.maxMudTrimDb <= 3f)
        assertTrue(sanitized.pregainRatio <= 0.8f)
        assertTrue(sanitized.smoothingTimeMs >= 24)
    }
}
