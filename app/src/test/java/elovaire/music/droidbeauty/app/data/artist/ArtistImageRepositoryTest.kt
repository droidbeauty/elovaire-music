package elovaire.music.droidbeauty.app.data.artist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistImageRepositoryTest {
    @Test
    fun normalizeArtistIdentity_foldsCaseWhitespaceAndDiacritics() {
        assertEquals("beyonce", normalizeArtistIdentity("  Beyoncé  "))
        assertEquals("the artist", normalizeArtistIdentity("The   Artist"))
    }

    @Test
    fun shouldSkipArtistRemoteLookup_blocksSyntheticArtistNames() {
        assertTrue(shouldSkipArtistRemoteLookup(normalizeArtistIdentity("Unknown Artist")))
        assertTrue(shouldSkipArtistRemoteLookup(normalizeArtistIdentity("Various Artists")))
        assertFalse(shouldSkipArtistRemoteLookup(normalizeArtistIdentity("Björk")))
    }
}
