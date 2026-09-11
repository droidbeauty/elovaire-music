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
        var refreshedQueue: ArrayList<Song>? = null
        queue.forEachIndexed { index, queuedSong ->
            val librarySong = librarySongsById[queuedSong.id]
                ?: librarySongsByIdentity[MediaIdentityResolver.stableKey(queuedSong)]
                ?: LibrarySongDuplicateResolver.normalizedRealPath(queuedSong.libraryPath)
                    ?.let(librarySongsByPath::get)
            if (librarySong != null && librarySong != queuedSong) {
                val target = refreshedQueue ?: ArrayList<Song>(queue.size).also {
                    it.addAll(queue.subList(0, index))
                    refreshedQueue = it
                }
                target.add(librarySong)
            } else {
                refreshedQueue?.add(queuedSong)
            }
        }
        val updatedQueue = refreshedQueue ?: return null
        val signature = updatedQueue.queueMetadataSignature()
        if (signature == lastQueueMetadataSignature) return null
        lastQueueMetadataSignature = signature
        return updatedQueue
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
        val trackMatcher by lazy { MediaIdentityResolver.prepareTrackMatcher(librarySongs) }

        val retained = ArrayList<Song>(queue.size)
        val retainedOriginalIndices = ArrayList<Int>(queue.size)
        val removedOriginalIndices = ArrayList<Int>()
        val unresolvedOriginalIndices = ArrayList<Int>()
        queue.forEachIndexed { index, queuedSong ->
            val match = songsByIdentity[MediaIdentityResolver.stableKey(queuedSong)]
                ?: LibrarySongDuplicateResolver.normalizedRealPath(queuedSong.libraryPath)
                    ?.let(songsByPath::get)
                ?: trackMatcher.resolve(
                    MediaIdentityResolver.trackMatchIdentity(queuedSong).copy(sourceStableKey = null),
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
