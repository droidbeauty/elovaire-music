package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreGenreLookupTest {
    @Test
    fun genreVolumes_prioritizeTheRowVolumeWithoutRepeatingFallbacks() {
        val volumes = mediaStoreGenreVolumes("removable")

        assertEquals("removable", volumes.first())
        assertEquals(volumes.distinct(), volumes)
    }

    @Test
    fun genreLookup_acceptsOnlyMediaStoreIdsRepresentableByThePlatformApi() {
        assertTrue(canQueryMediaStoreGenre(1L))
        assertTrue(canQueryMediaStoreGenre(Int.MAX_VALUE.toLong()))
        assertFalse(canQueryMediaStoreGenre(0L))
        assertFalse(canQueryMediaStoreGenre(Int.MAX_VALUE.toLong() + 1L))
    }

    @Test
    fun genreFallbackIsNotQueriedWhenMetadataAlreadyResolved() {
        var fallbackCalls = 0

        val genre = resolveMediaStoreGenre("Rock") {
            fallbackCalls += 1
            "Fallback"
        }

        assertEquals("Rock", genre)
        assertEquals(0, fallbackCalls)
    }
}
