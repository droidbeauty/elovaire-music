package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackHistoryWriteBufferTest {
    @Test
    fun batchesCountsAndSchedulesOnlyOneFlush() {
        val buffer = PlaybackHistoryWriteBuffer()

        assertTrue(buffer.addTransition(songId = 1L, albumId = 10L))
        assertFalse(buffer.addTransition(songId = 1L, albumId = 10L))
        assertFalse(buffer.addTransition(songId = 2L, albumId = null))

        assertEquals(
            PlaybackCountBatch(
                songCounts = mapOf(1L to 2, 2L to 1),
                albumCounts = mapOf(10L to 2),
            ),
            buffer.takeTransitions(),
        )
        assertTrue(buffer.addTransition(songId = 3L, albumId = null))
    }

    @Test
    fun conflatesRecentPlaybackToLatestSnapshot() {
        val buffer = PlaybackHistoryWriteBuffer()
        val first = RecentPlaybackWrite(listOf(1L), listOf(10L), PlaybackCollectionKind.Album, 10L)
        val latest = RecentPlaybackWrite(listOf(2L), listOf(20L), PlaybackCollectionKind.Playlist, 20L)

        assertTrue(buffer.setRecent(first))
        assertFalse(buffer.setRecent(latest))
        assertEquals(latest, buffer.takeRecent())
        assertNull(buffer.takeRecent())
    }
}
