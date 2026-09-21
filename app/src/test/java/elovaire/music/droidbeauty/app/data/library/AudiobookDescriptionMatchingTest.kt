package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import elovaire.music.droidbeauty.app.domain.model.Audiobook

class AudiobookDescriptionMatchingTest {
    @Test
    fun normalizationIsStableAcrossDiacriticsAndPunctuation() {
        assertEquals(
            "the little prince",
            normalizeAudiobookMetadataText("  Thé-Little Prince! "),
        )
    }

    @Test
    fun exactTitleAndAuthorBeatsUnrelatedDescribedResult() {
        val metadata = AudiobookLookupMetadata("The Hobbit", "J. R. R. Tolkien", 1937)
        val result = AudiobookDescriptionMatcher.select(
            metadata,
            listOf(
                AudiobookDescriptionCandidate(
                    title = "The Hobbit Study Guide",
                    authors = listOf("J. R. R. Tolkien"),
                    description = "Wrong description",
                ),
                AudiobookDescriptionCandidate(
                    title = "The Hobbit",
                    authors = listOf("J.R.R. Tolkien"),
                    releaseYear = 1937,
                    description = "Correct description",
                ),
            ),
        )

        assertEquals("Correct description", result?.description)
    }

    @Test
    fun mismatchedAuthorIsRejectedEvenWhenTitleIsExact() {
        val result = AudiobookDescriptionMatcher.select(
            AudiobookLookupMetadata("Dune", "Frank Herbert", 1965),
            listOf(
                AudiobookDescriptionCandidate(
                    title = "Dune",
                    authors = listOf("Brian Herbert"),
                    releaseYear = 1965,
                    description = "Wrong author",
                ),
            ),
        )

        assertNull(result)
    }

    @Test
    fun subtitlePreservesTheBaseTitleMatchWithoutUsingContains() {
        val result = AudiobookDescriptionMatcher.select(
            AudiobookLookupMetadata("The Hobbit", "J. R. R. Tolkien", null),
            listOf(
                AudiobookDescriptionCandidate(
                    title = "The Hobbit: There and Back Again",
                    authors = listOf("J. R. R. Tolkien"),
                    description = "Usable description",
                ),
            ),
        )

        assertEquals("Usable description", result?.description)
    }

    @Test
    fun missingAuthorRequiresAnUnambiguousExactTitle() {
        val metadata = AudiobookLookupMetadata("The Martian", null, null)
        assertTrue(
            AudiobookDescriptionMatcher.select(
                metadata,
                listOf(
                    AudiobookDescriptionCandidate(
                        title = "The Martian",
                        description = "Usable description",
                    ),
                ),
            ) != null,
        )
        assertNull(
            AudiobookDescriptionMatcher.select(
                metadata,
                listOf(
                    AudiobookDescriptionCandidate(
                        title = "The Martian",
                        authors = listOf("Andy Weir"),
                        description = "Ambiguous description",
                    ),
                ),
            ),
        )
    }

    @Test
    fun providerFallbackIsCachedButEmbeddedDescriptionSkipsProviders() = runBlocking {
        val google = FakeProvider(AudiobookDescriptionProviderResult.NoMatch)
        val openLibrary = FakeProvider(AudiobookDescriptionProviderResult.Found("Open Library synopsis"))
        val reader = GoogleBooksAudiobookDescriptionReader(google, openLibrary)
        val book = Audiobook("book", "The Hobbit", "J. R. R. Tolkien", null, 1L, emptyList())

        assertEquals("Open Library synopsis", reader.description(book))
        assertEquals("Open Library synopsis", reader.description(book))
        assertEquals(1, google.calls)
        assertEquals(1, openLibrary.calls)

        val embedded = book.copy(description = "Local synopsis")
        assertEquals("Local synopsis", reader.description(embedded))
        assertEquals(1, google.calls)
        assertEquals(1, openLibrary.calls)
    }

    @Test
    fun temporaryProviderFailureIsNotCachedAsNoMatch() = runBlocking {
        val google = FakeProvider(AudiobookDescriptionProviderResult.TemporaryFailure)
        val openLibrary = FakeProvider(AudiobookDescriptionProviderResult.NoMatch)
        val reader = GoogleBooksAudiobookDescriptionReader(google, openLibrary)
        val book = Audiobook("book", "The Hobbit", "J. R. R. Tolkien", null, 1L, emptyList())

        assertNull(reader.description(book))
        assertNull(reader.description(book))
        assertEquals(2, google.calls)
        assertEquals(2, openLibrary.calls)
    }

    private class FakeProvider(
        private val result: AudiobookDescriptionProviderResult,
    ) : AudiobookDescriptionProvider {
        var calls: Int = 0
            private set

        override suspend fun lookup(metadata: AudiobookLookupMetadata): AudiobookDescriptionProviderResult {
            calls += 1
            return result
        }
    }

}
