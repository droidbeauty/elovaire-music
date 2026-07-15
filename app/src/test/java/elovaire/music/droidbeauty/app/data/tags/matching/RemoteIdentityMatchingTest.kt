package elovaire.music.droidbeauty.app.data.tags.matching

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteIdentityMatchingTest {
    @Test
    fun normalizationIsLocaleIndependentAndPreservesUnicodeLetters() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("istanbul łódź", normalizeRemoteIdentity("  ISTANBUL / Łódź  "))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun qualifiersCanBeIgnoredWithoutChangingDisplayValues() {
        assertEquals("song", normalizeRemoteIdentity("Song (Live) [Remaster]", stripQualifiers = true))
        assertEquals(1f, remoteIdentitySimilarity("Song (Live)", "Song"))
    }

    @Test
    fun tokenSimilarityIsDeterministicForNearMatches() {
        val forward = remoteIdentitySimilarity("The Example Artist", "Example Artist")
        val reverse = remoteIdentitySimilarity("Example Artist", "The Example Artist")
        assertEquals(forward, reverse)
        assertTrue(forward >= 0.85f)
    }
}
