package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryDirectPathTombstoneTest {
    @Test
    fun deletedDirectoryTombstoneHidesDescendantsButNotSiblingPaths() {
        val tombstones = setOf("/music/books")

        assertTrue(isDirectPathTombstoned("/music/books/book/chapter.mp3", tombstones))
        assertFalse(isDirectPathTombstoned("/music/books-old/track.mp3", tombstones))
    }

    @Test
    fun directoryTombstoneRemainsUntilItsStaleDescendantsDisappear() {
        val tombstones = setOf("/music/books", "/music/gone.mp3")

        assertEquals(
            setOf("/music/books"),
            directPathTombstonesStillPresent(
                tombstones = tombstones,
                presentPaths = setOf("/music/books/book/chapter.mp3"),
            ),
        )
    }

    @Test
    fun recreatedDirectoryClearsItsOwnAndNestedTombstones() {
        val tombstones = mutableSetOf(
            "/music/books",
            "/music/books/book/chapter.mp3",
            "/music/other.mp3",
        )

        clearDirectPathTombstonesAtOrBelow(tombstones, "/music/books")

        assertEquals(setOf("/music/other.mp3"), tombstones)
    }
}
