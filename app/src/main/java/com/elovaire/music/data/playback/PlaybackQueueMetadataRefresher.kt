package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.data.library.MediaIdentityResolver
import elovaire.music.droidbeauty.app.data.library.LibrarySongDuplicateResolver
import elovaire.music.droidbeauty.app.data.library.TrackMatchConfidence
import elovaire.music.droidbeauty.app.domain.model.Song

internal class PlaybackQueueMetadataRefresher {
    private var lastQueueMetadataSignature: List<PlaybackMetadataSignature>? = null

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
            val songById = librarySongsById[queuedSong.id]
            if (songById === queuedSong) {
                refreshedQueue?.add(queuedSong)
                return@forEachIndexed
            }
            val identity = MediaIdentityResolver.stableKey(queuedSong)
            val librarySong = songById
                ?.takeIf { MediaIdentityResolver.stableKey(it) == identity }
                ?: librarySongsByIdentity[identity]
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
        checkCancellation: () -> Unit = {},
    ): PlaybackQueueReconciliation? {
        if (queue.isEmpty()) return null

        val filterToQueue = queue.size < librarySongs.size / 2
        val queuedIdentities = if (filterToQueue) queue.mapTo(HashSet(queue.size), MediaIdentityResolver::stableKey) else null
        val queuedPaths = if (filterToQueue) queue.mapNotNullTo(HashSet()) {
            LibrarySongDuplicateResolver.normalizedRealPath(it.libraryPath)
        } else null
        val songsByIdentity = HashMap<String, Song>(minOf(queue.size, librarySongs.size))
        val ambiguousIdentities = HashSet<String>()
        val songsByPath = HashMap<String, Song>(queuedPaths?.size ?: librarySongs.size)
        val ambiguousPaths = HashSet<String>()
        librarySongs.forEachIndexed { index, song ->
            if (index and 127 == 0) checkCancellation()
            val identity = MediaIdentityResolver.stableKey(song)
            if (queuedIdentities == null || identity in queuedIdentities) {
                songsByIdentity.putUnique(identity, song, ambiguousIdentities)
            }
            if (queuedPaths == null || queuedPaths.isNotEmpty()) {
                LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath)?.let { path ->
                    if (queuedPaths == null || path in queuedPaths) songsByPath.putUnique(path, song, ambiguousPaths)
                }
            }
        }
        val trackMatcher by lazy { MediaIdentityResolver.prepareTrackMatcher(librarySongs, checkCancellation) }

        val retained = ArrayList<Song>(queue.size)
        val retainedOriginalIndices = ArrayList<Int>(queue.size)
        val removedOriginalIndices = ArrayList<Int>()
        val unresolvedOriginalIndices = ArrayList<Int>()
        queue.forEachIndexed { index, queuedSong ->
            if (index and 127 == 0) checkCancellation()
            val match = songsByIdentity[MediaIdentityResolver.stableKey(queuedSong)]
                ?: LibrarySongDuplicateResolver.normalizedRealPath(queuedSong.libraryPath)
                    ?.let(songsByPath::get)
                ?: trackMatcher.resolve(
                    MediaIdentityResolver.trackMatchIdentity(queuedSong, includeSourceStableKey = false),
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

private fun List<Song>.queueMetadataSignature(): List<PlaybackMetadataSignature> =
    map(Song::playbackMetadataSignature)

private fun <K> MutableMap<K, Song>.putUnique(key: K, song: Song, ambiguous: MutableSet<K>) {
    if (key !in ambiguous && putIfAbsent(key, song) != null) {
        remove(key)
        ambiguous.add(key)
    }
}
