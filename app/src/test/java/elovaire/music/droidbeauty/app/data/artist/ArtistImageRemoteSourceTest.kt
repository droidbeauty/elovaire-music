package elovaire.music.droidbeauty.app.data.artist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistImageRemoteSourceTest {
    @Test
    fun exactPrimaryNameWinsOverUnrelatedSearchResults() {
        val match = selectReliableArtistMatch(
            requestedName = "  Björk  ",
            candidates = listOf(
                artist(id = "1", name = "Björk", image = "https://r2.theaudiodb.com/images/bjork.jpg"),
                artist(id = "2", name = "Björk Tribute", image = "https://r2.theaudiodb.com/images/tribute.jpg"),
            ),
            stableProviderId = null,
        )

        assertEquals("1", match?.providerArtistId)
        assertTrue(match?.imageUrl?.endsWith("/small") == true)
    }

    @Test
    fun exactAlternateNameIsAcceptedWhenUnambiguous() {
        val match = selectReliableArtistMatch(
            requestedName = "The Beatles",
            candidates = listOf(
                artist(
                    id = "1",
                    name = "Beatles",
                    alternate = "The Beatles",
                    image = "https://r2.theaudiodb.com/images/beatles.jpg",
                ),
            ),
            stableProviderId = null,
        )

        assertEquals("1", match?.providerArtistId)
    }

    @Test
    fun ambiguousExactNamesFailClosed() {
        val match = selectReliableArtistMatch(
            requestedName = "The Sound",
            candidates = listOf(
                artist(id = "1", name = "The Sound", image = "https://r2.theaudiodb.com/images/one.jpg"),
                artist(id = "2", name = "The Sound", image = "https://r2.theaudiodb.com/images/two.jpg"),
            ),
            stableProviderId = null,
        )

        assertNull(match)
    }

    @Test
    fun missingArtworkAndPseudoArtistsAreRejected() {
        assertNull(
            selectReliableArtistMatch(
                requestedName = "Unknown Artist",
                candidates = listOf(artist(id = "1", name = "Unknown Artist", image = "https://r2.theaudiodb.com/images/unknown.jpg")),
                stableProviderId = null,
            ),
        )
        assertNull(
            selectReliableArtistMatch(
                requestedName = "Real Artist",
                candidates = listOf(artist(id = "1", name = "Real Artist", image = "")),
                stableProviderId = null,
            ),
        )
    }

    @Test
    fun artistCacheKeyIsLocaleIndependentAndHashed() {
        assertEquals("hello world", normalizeArtistIdentity("  HELLO\u00a0WORLD "))
        assertEquals(64, stableArtistCacheFileHash("Hello World")?.length)
        assertNull(stableArtistCacheKey("Various Artists"))
    }

    private fun artist(
        id: String,
        name: String,
        image: String,
        alternate: String? = null,
    ): ArtistImageCandidate = ArtistImageCandidate(
        providerId = id,
        providerName = name,
        alternateNames = alternate?.let(::setOf).orEmpty(),
        imageUrl = image,
    )
}
