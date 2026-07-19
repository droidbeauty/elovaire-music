package elovaire.music.droidbeauty.app.data.tags.matching

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AlbumArtworkResultEqualityTest {
    @Test
    fun usesArtworkByteContentForEquality() {
        val first = AlbumArtworkResult(byteArrayOf(1, 2, 3), 800, 800, ArtworkSource.Embedded)
        val equal = AlbumArtworkResult(byteArrayOf(1, 2, 3), 800, 800, ArtworkSource.Embedded)
        val different = AlbumArtworkResult(byteArrayOf(1, 2, 4), 800, 800, ArtworkSource.Embedded)

        assertEquals(first, equal)
        assertEquals(first.hashCode(), equal.hashCode())
        assertNotEquals(first, different)
    }

    @Test
    fun rejectsArtworkWithUnsafeDecodedDimensions() {
        val oversized = AlbumArtworkResult(byteArrayOf(1), 8_193, 8_193, ArtworkSource.Embedded)

        assertFalse(oversized.isAcceptableForEmbedding())
    }
}
