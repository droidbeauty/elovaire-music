package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SearchIndexTest {
    @Test
    fun scoreMatch_keepsScoreWithPrecomputedComposite() {
        val query = NormalizedSearchQuery.from("dream theater awake")
        val normalizedTitle = normalizeSearchText("Awake")
        val normalizedArtist = normalizeSearchText("Dream Theater")
        val normalizedAlbum = normalizeSearchText("Awake")
        val composite = listOf(normalizedTitle, normalizedArtist, normalizedAlbum).joinToString(" ")

        val computedScore = scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbum = normalizedAlbum,
        )
        val precomputedScore = scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbum = normalizedAlbum,
            normalizedComposite = composite,
        )

        assertEquals(computedScore, precomputedScore)
        assertNotNull(precomputedScore)
    }

    @Test
    fun scoreMatch_matchesTokensAcrossFields() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("kind davis blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles Davis"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles Davis"),
        )

        assertNotNull(score)
    }

    @Test
    fun scoreMatch_rejectsQueryWhenAnyTokenIsMissing() {
        val score = scoreMatch(
            query = NormalizedSearchQuery.from("kind coltrane blue"),
            normalizedTitle = normalizeSearchText("Kind of Blue"),
            normalizedArtist = normalizeSearchText("Miles Davis"),
            normalizedAlbum = "",
            normalizedComposite = normalizeSearchText("Kind of Blue Miles Davis"),
        )

        assertNull(score)
    }
}
