package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class AudiobookDescriptionPreviewTest {
    @Test
    fun previewMatchesFullSentenceSplitting() {
        val samples = listOf(
            null, "", "   ", "One sentence", "First. Second! Third? Fourth. Fifth. Sixth.",
            "\nFirst.\t\nSecond!   Third?  ", "a".repeat(2_000),
            (1..10_000).joinToString(" ") { "Sentence $it." },
        )
        samples.forEach { description ->
            val normalized = description.orEmpty().trim()
            val expected = if (normalized.isBlank()) {
                AudiobookDescriptionPreview("", false)
            } else {
                val preview = normalized.split(Regex("(?<=[.!?])\\s+"))
                    .filter(String::isNotBlank).take(5).joinToString(" ")
                    .ifBlank { normalized }.take(900).trim()
                AudiobookDescriptionPreview(preview, preview.length < normalized.length)
            }
            assertEquals(expected, audiobookDescriptionPreview(description))
        }
    }
}
