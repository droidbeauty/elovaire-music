package elovaire.music.droidbeauty.app.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackSessionStoreTest {
    @Test
    fun normalizationUsesCurrentSongIdentityAndDropsInvalidIds() {
        val normalized = normalizePersistedPlaybackSession(session(queueSongIds = listOf(-1L, 2L, 3L, 2L), currentSongId = 3L))

        assertEquals(listOf(2L, 3L, 2L), normalized.queueSongIds)
        assertEquals(1, normalized.currentIndex)
        assertEquals(3L, normalized.currentSongId)
    }

    @Test
    fun emptyQueueHasNoCurrentSong() {
        val normalized = normalizePersistedPlaybackSession(session(queueSongIds = emptyList(), currentSongId = 3L))

        assertEquals(-1, normalized.currentIndex)
        assertNull(normalized.currentSongId)
        assertEquals(0L, normalized.positionMs)
    }

    @Test
    fun persistedFormatRejectsUnknownFutureVersions() {
        assertEquals(true, isSupportedPlaybackSessionVersion(LEGACY_FORMAT_VERSION))
        assertEquals(true, isSupportedPlaybackSessionVersion(CURRENT_FORMAT_VERSION))
        assertEquals(false, isSupportedPlaybackSessionVersion(CURRENT_FORMAT_VERSION + 1))
        assertEquals(false, isSupportedPlaybackSessionVersion(-1))
    }

    @Test
    fun positionOnlySaveDoesNotRewriteQueueStructure() {
        val previous = session(queueSongIds = (1L..10_000L).toList(), currentSongId = 1L)
        val next = previous.copy(positionMs = 5_000L)

        assertEquals(PlaybackSessionSavePlan.Recovery, playbackSessionSavePlan(previous, next))
    }

    @Test
    fun queueChangePersistsStructureAndRecoveryTogether() {
        val previous = session(queueSongIds = listOf(1L, 2L), currentSongId = 1L)
        val next = previous.copy(queueSongIds = listOf(1L, 2L, 3L))

        assertEquals(PlaybackSessionSavePlan.StructureAndRecovery, playbackSessionSavePlan(previous, next))
    }

    @Test
    fun equalSessionDoesNotWriteAgain() {
        val saved = session(queueSongIds = listOf(1L), currentSongId = 1L)

        assertEquals(PlaybackSessionSavePlan.None, playbackSessionSavePlan(saved, saved))
    }

    private fun session(
        queueSongIds: List<Long>,
        currentSongId: Long?,
    ) = PersistedPlaybackSession(
        queueSongIds = queueSongIds,
        currentSongId = currentSongId,
        currentIndex = 99,
        positionMs = -3L,
        repeatMode = PlaybackRepeatMode.Off,
        shuffleEnabled = false,
        sourcePlaylistId = null,
        wasPlaying = true,
        savedAtWallTimeMs = 1L,
    )
}
