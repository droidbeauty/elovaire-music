package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.data.library.MediaIdentityResolver
import elovaire.music.droidbeauty.app.data.library.LibrarySongDuplicateResolver
import elovaire.music.droidbeauty.app.data.library.TrackMatchConfidence
import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackQueueMetadataRefresher {
    private var lastQueueMetadataSignature: Int? = null

    fun onQueueReplaced(queue: List<Song>) {
        lastQueueMetadataSignature = queue.queueMetadataSignature()
    }

    fun refreshQueueIfNeeded(
        queue: List<Song>,
        librarySongsById: Map<Long, Song>,
        librarySongsByIdentity: Map<String, Song> = emptyMap(),
        librarySongsByPath: Map<String, Song> = emptyMap(),
    ): List<Song>? {
        if (
            queue.isEmpty() ||
            (librarySongsById.isEmpty() && librarySongsByIdentity.isEmpty() && librarySongsByPath.isEmpty())
        ) return null
        var changed = false
        val refreshedQueue = queue.map { queuedSong ->
            val librarySong = librarySongsById[queuedSong.id]
                ?: librarySongsByIdentity[MediaIdentityResolver.stableKey(queuedSong)]
                ?: LibrarySongDuplicateResolver.normalizedRealPath(queuedSong.libraryPath)
                    ?.let(librarySongsByPath::get)
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

    /**
     * Reconciles a library-backed queue against an authoritative library snapshot. Matching is
     * source-first: a reused numeric id must not silently bind the queue to a different file.
     * Metadata matching is only used when it produces one strong candidate.
     */
    fun reconcileQueue(
        queue: List<Song>,
        librarySongs: List<Song>,
    ): PlaybackQueueReconciliation? {
        if (queue.isEmpty()) return null

        val songsByIdentity = librarySongs
            .groupBy(MediaIdentityResolver::stableKey)
            .filterValues { it.size == 1 }
            .mapValues { (_, songs) -> songs.single() }
        val songsByPath = librarySongs
            .mapNotNull { song ->
                LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath)?.let { it to song }
            }
            .groupBy({ it.first }, { it.second })
            .filterValues { it.size == 1 }
            .mapValues { (_, songs) -> songs.single() }

        val retained = ArrayList<Song>(queue.size)
        val retainedOriginalIndices = ArrayList<Int>(queue.size)
        val removedOriginalIndices = ArrayList<Int>()
        val unresolvedOriginalIndices = ArrayList<Int>()
        queue.forEachIndexed { index, queuedSong ->
            val match = songsByIdentity[MediaIdentityResolver.stableKey(queuedSong)]
                ?: LibrarySongDuplicateResolver.normalizedRealPath(queuedSong.libraryPath)
                    ?.let(songsByPath::get)
                ?: MediaIdentityResolver.resolveTrackMatch(
                    MediaIdentityResolver.trackMatchIdentity(queuedSong).copy(sourceStableKey = null),
                    librarySongs,
                ).takeIf { it.confidence == TrackMatchConfidence.Strong }
                    ?.song
            if (match == null) {
                removedOriginalIndices += index
                unresolvedOriginalIndices += index
            } else {
                retained += match
                retainedOriginalIndices += index
            }
        }
        if (removedOriginalIndices.isEmpty() && retained == queue) return null
        return PlaybackQueueReconciliation(
            queue = retained,
            retainedOriginalIndices = retainedOriginalIndices,
            removedOriginalIndices = removedOriginalIndices,
            unresolvedOriginalIndices = unresolvedOriginalIndices,
        )
    }

    fun reset() {
        lastQueueMetadataSignature = null
    }
}

internal data class PlaybackQueueReconciliation(
    val queue: List<Song>,
    val retainedOriginalIndices: List<Int>,
    val removedOriginalIndices: List<Int>,
    val unresolvedOriginalIndices: List<Int>,
)

private fun List<Song>.queueMetadataSignature(): Int {
    return fold(17) { acc, song ->
        31 * acc + song.playbackMetadataSignature()
    }
}
