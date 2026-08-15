package elovaire.music.droidbeauty.app.data.audio

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CanonicalMetadataTest {
    @Test
    fun embeddedValuesWinOverPlatformAndIndexedValues() {
        val result = CanonicalMetadataResolver.resolve(
            embedded = MetadataSourceValues(
                title = "  Embedded title ",
                artist = "Embedded artist",
                trackNumber = "03/12",
            ),
            platform = MetadataSourceValues(
                title = "Platform title",
                artist = "Platform artist",
                trackNumber = "4",
            ),
            indexed = MetadataSourceValues(title = "Indexed title"),
        )

        assertEquals("Embedded title", result.title)
        assertEquals("Embedded artist", result.artist)
        assertEquals(3, result.trackNumber)
    }

    @Test
    fun blankAndInvalidValuesFallThroughWithoutInventingMetadata() {
        val result = CanonicalMetadataResolver.resolve(
            embedded = MetadataSourceValues(title = "  ", releaseYear = 100_000, trackNumber = "-2"),
            platform = MetadataSourceValues(title = "Platform", releaseYear = 2024, discNumber = "02/03"),
        )

        assertEquals("Platform", result.title)
        assertEquals(2024, result.releaseYear)
        assertEquals(2, result.discNumber)
        assertNull(result.trackNumber)
    }

    @Test
    fun replayGainFieldsResolveIndependentlyAcrossSources() {
        val result = CanonicalMetadataResolver.resolve(
            embedded = MetadataSourceValues(
                volumeNormalization = VolumeNormalizationMetadata(trackPeak = 0.98f),
            ),
            platform = MetadataSourceValues(
                volumeNormalization = VolumeNormalizationMetadata(
                    trackGainDb = -7.5f,
                    albumGainDb = -6.0f,
                    albumPeak = 0.91f,
                ),
            ),
        )

        assertEquals(
            VolumeNormalizationMetadata(
                trackGainDb = -7.5f,
                albumGainDb = -6.0f,
                trackPeak = 0.98f,
                albumPeak = 0.91f,
            ),
            result.volumeNormalization,
        )
    }

    @Test
    fun canonicalTextTrimsUnicodeAndFallsBackForWhitespaceOnlyValues() {
        val result = CanonicalMetadataResolver.resolve(
            embedded = MetadataSourceValues(title = " \u0301 "),
            platform = MetadataSourceValues(title = "  東京 🎵  "),
        )

        assertEquals("東京 🎵", result.title)
    }
}
