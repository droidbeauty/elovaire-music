package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudiobookProgressPolicyTest {
    @Test
    fun checkpointSelectionPrefersAValidRecordOverAnInvalidNewerRecord() {
        val valid = AudiobookProgress(7L, 500L, false, 10L)
        assertEquals(0, selectNewestValidCheckpoint(valid, null))
        assertEquals(1, selectNewestValidCheckpoint(null, valid))
        assertEquals(1, selectNewestValidCheckpoint(valid, valid.copy(updatedAtMs = 20L)))
        assertEquals(null, selectNewestValidCheckpoint(null, null))
    }

    @Test
    fun normalizedDurationsStayAlignedWhenIdsAreInvalidOrDurationsMissing() {
        val context = AudiobookPlaybackContext(
            "book", listOf(0L, 2L, -1L, 2L, 3L), 100L,
            listOf(50L, -5L, 20L, 80L, 100L),
        )
        assertEquals(listOf(2L, -1L, 3L), context.normalizedSongIds)
        assertEquals(listOf(0L, 20L, 100L), context.normalizedSongDurationsMs)
        assertEquals(0L, context.elapsedBefore(1))
        assertEquals(20L, context.durationAt(1))
        assertEquals(20L, resolveAudiobookBookElapsed(context, emptyList(), -1L, 20L))

        val missing = context.copy(orderedSongDurationsMs = listOf(1L))
        assertEquals(emptyList<Long>(), missing.normalizedSongDurationsMs)
        assertEquals(null, missing.durationAt(0))
        assertEquals(null, missing.elapsedBefore(0))
    }
    @Test
    fun shortBookIsNotCompleteBeforeMostOfItsDurationHasPlayed() {
        assertFalse(isAudiobookComplete(5_000L, 0L))
        assertTrue(isAudiobookComplete(5_000L, 4_500L))
        assertFalse(isAudiobookComplete(0L, 0L))
    }

    @Test
    fun duplicateSongUsesDurationOfItsFirstOccurrence() {
        val context = AudiobookPlaybackContext(
            bookKey = "book",
            orderedSongIds = listOf(1L, 1L, 2L),
            bookDurationMs = 350L,
            orderedSongDurationsMs = listOf(100L, 200L, 50L),
        )

        assertEquals(listOf(1L, 2L), context.normalizedSongIds)
        assertEquals(listOf(100L, 50L), context.normalizedSongDurationsMs)
    }

    @Test
    fun finishingAnEarlierPhysicalPartDoesNotCompleteTheBook() {
        val book = book(partCount = 3)

        val resolved = resolveAudiobookProgress(
            book = book,
            savedProgress = AudiobookProgress(songId = 1L, positionMs = 100_000L, completed = true, updatedAtMs = 1L),
            currentSongId = null,
            currentPositionMs = 0L,
        )

        assertFalse(resolved.completed)
        assertEquals(100_000L, resolved.bookElapsedMs)
    }

    @Test
    fun finishingTheFinalPhysicalPartCompletesTheBook() {
        val book = book(partCount = 3)

        val resolved = resolveAudiobookProgress(
            book = book,
            savedProgress = AudiobookProgress(songId = 3L, positionMs = 95_000L, completed = false, updatedAtMs = 1L),
            currentSongId = null,
            currentPositionMs = 0L,
        )

        assertTrue(resolved.completed)
        assertEquals(295_000L, resolved.bookElapsedMs)
    }

    @Test
    fun embeddedChapterProgressIsNotWholeBookCompletion() {
        val song = song(1L, 1_000_000L)
        val book = Audiobook(
            stableKey = "book",
            title = "Book",
            author = "Author",
            artUri = null,
            durationMs = song.durationMs,
            parts = listOf(
                AudiobookPart(song, 1, "Chapter 1", startMs = 0L, endMs = 300_000L),
                AudiobookPart(song, 2, "Chapter 2", startMs = 300_000L, endMs = 1_000_000L),
            ),
        )

        val resolved = resolveAudiobookProgress(
            book = book,
            savedProgress = AudiobookProgress(songId = 1L, positionMs = 300_000L, completed = false, updatedAtMs = 1L),
            currentSongId = null,
            currentPositionMs = 0L,
        )

        assertFalse(resolved.completed)
        assertEquals(300_000L, resolved.bookElapsedMs)
        assertEquals(1, resolved.partIndex)
    }

    @Test
    fun positionBeforeFirstChapterAndInChapterGapsUsesNearestPreviousPart() {
        val song = song(-1L, 1_000L)
        val parts = listOf(
            AudiobookPart(song, 1, "First", startMs = 100L, endMs = 300L),
            AudiobookPart(song, 2, "Second", startMs = 500L, endMs = 800L),
        )
        val book = Audiobook("book", "Book", "Author", null, 500L, parts)
        assertEquals(0, resolveAudiobookProgress(book, null, -1L, 50L).partIndex)
        assertEquals(0, resolveAudiobookProgress(book, null, -1L, 400L).partIndex)
        assertEquals(1, resolveAudiobookProgress(book, null, -1L, 900L).partIndex)
    }

    @Test
    fun playbackContextUsesUniquePhysicalSongOrder() {
        val songs = listOf(song(1L, 100L), song(2L, 200L))
        val context = AudiobookPlaybackContext("book", listOf(1L, 1L, 2L), 300L)

        assertEquals(150L, resolveAudiobookBookElapsed(context, songs, 2L, 50L))
    }

    @Test
    fun playbackContextSaturatesElapsedTimeInsteadOfWrapping() {
        val songs = listOf(song(1L, Long.MAX_VALUE), song(2L, 10L))
        val context = AudiobookPlaybackContext("book", listOf(1L, 2L), Long.MAX_VALUE)

        assertEquals(Long.MAX_VALUE, resolveAudiobookBookElapsed(context, songs, 2L, 10L))
    }

    private fun book(partCount: Int): Audiobook {
        val parts = (1..partCount).map { id -> AudiobookPart(song(id.toLong(), 100_000L), id) }
        return Audiobook("book", "Book", "Author", null, parts.sumOf { it.durationMs }, parts)
    }

    private fun song(id: Long, durationMs: Long) = Song(
        id = id,
        title = "Part $id",
        isExplicit = false,
        artist = "Author",
        album = "Book",
        releaseYear = null,
        genre = "",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = "part$id.m4b",
        albumId = 1L,
        durationMs = durationMs,
        trackNumber = id.toInt(),
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("file:///part$id.m4b"),
        artUri = null,
        mediaKind = AudioMediaKind.Audiobook,
    )
}
