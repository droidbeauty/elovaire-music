package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerDspModelTest {
    @Test
    fun automaticHeadroom_keepsBassBoostAudible() {
        val config = EqualizerDspConfig()
        val bass = HighQualityBassProcessorModel.curveFor(1f, config.bassConfig)
        val headroom = EqualizerDspModel.automaticHeadroomDb(
            bandGainsDb = FloatArray(EqualizerDspModel.BAND_COUNT),
            bassShelfDb = bass.shelfDb,
            bassBodyDb = bass.bodyDb,
            bassAccentDb = bass.punchDb,
            bassTrimDb = bass.mudTrimDb,
            midrangeToneDb = 0f,
            bassPregainDb = 0f,
            trebleBoostDb = 0f,
            spaciousnessAmount = 0f,
            sampleRateHz = 48_000,
            config = config,
        )

        assertTrue(headroom <= 0f)
        assertTrue(headroom >= -5.5f)
    }
}
