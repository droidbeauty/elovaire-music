package elovaire.music.droidbeauty.app.data.audio

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
}

