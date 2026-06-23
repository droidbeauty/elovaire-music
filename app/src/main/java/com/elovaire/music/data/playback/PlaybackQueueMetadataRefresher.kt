package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackQueueMetadataRefresher {
    private var lastQueueMetadataSignature: Int? = null

    fun onQueueReplaced(queue: List<Song>) {
        lastQueueMetadataSignature = queue.queueMetadataSignature()
    }

    fun refreshQueueIfNeeded(
        queue: List<Song>,
        librarySongsById: Map<Long, Song>,
    ): List<Song>? {
        if (queue.isEmpty() || librarySongsById.isEmpty()) return null
        var changed = false
        val refreshedQueue = queue.map { queuedSong ->
            val librarySong = librarySongsById[queuedSong.id]
            if (librarySong != null && librarySong != queuedSong) {
                changed = true
                librarySong
            } else {
                queuedSong
            }
        }
        if (!changed) return null
        val signature = refreshedQueue.queueMetadataSignature()
        if (signature == lastQueueMetadataSignature) return null
        lastQueueMetadataSignature = signature
        return refreshedQueue
    }

    fun reset() {
        lastQueueMetadataSignature = null
    }
}

private fun List<Song>.queueMetadataSignature(): Int {
    return fold(17) { acc, song ->
        31 * acc + song.playbackMetadataSignature()
    }
}
