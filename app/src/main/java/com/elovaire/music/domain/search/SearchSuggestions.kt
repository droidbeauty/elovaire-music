package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.HashMap
import java.util.PriorityQueue

private data class IndexedSuggestedAlbumCandidate(
    val searchableAlbum: SearchableAlbum,
    val playCount: Int,
    val isRecent: Boolean,
    val index: Int,
)

internal fun buildSuggestedAlbums(
    albums: List<SearchableAlbum>,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toHashSet()
    val seen = HashSet<Long>(6)
    val output = ArrayList<Album>(6)

    if (collectSuggestedAlbums(
            albums = albums,
            albumPlayCounts = albumPlayCounts,
            recentAlbumIdSet = recentAlbumIdSet,
            matches = { it > 0 },
            comparePlayCount = true,
            comparator = compareBy<IndexedSuggestedAlbumCandidate> { it.playCount }
                .thenBy { if (it.isRecent) 1 else 0 }
                .thenBy { it.searchableAlbum.normalizedArtist }
                .thenBy { it.searchableAlbum.normalizedTitle }
                .thenBy { it.index },
            seen = seen,
            output = output,
        )
    ) return output

    collectSuggestedAlbums(
        albums = albums,
        albumPlayCounts = albumPlayCounts,
        recentAlbumIdSet = recentAlbumIdSet,
        matches = { it == 0 },
        comparePlayCount = false,
        comparator = compareBy<IndexedSuggestedAlbumCandidate> { if (it.isRecent) 1 else 0 }
            .thenBy { it.searchableAlbum.normalizedArtist }
            .thenBy { it.searchableAlbum.normalizedTitle }
            .thenBy { it.index },
        seen = seen,
        output = output,
    )

    return output
}

private fun collectSuggestedAlbums(
    albums: List<SearchableAlbum>,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIdSet: Set<Long>,
    matches: (Int) -> Boolean,
    comparePlayCount: Boolean,
    comparator: Comparator<IndexedSuggestedAlbumCandidate>,
    seen: MutableSet<Long>,
    output: MutableList<Album>,
): Boolean {
    val remaining = 6 - output.size
    if (remaining <= 0) return true
    val heap = PriorityQueue<IndexedSuggestedAlbumCandidate>(remaining, comparator.reversed())
    val heapByAlbumId = HashMap<Long, IndexedSuggestedAlbumCandidate>(remaining)
    albums.forEachIndexed { index, searchableAlbum ->
        val album = searchableAlbum.album
        val playCount = albumPlayCounts[album.id] ?: 0
        if (!matches(playCount)) return@forEachIndexed
        val isRecent = album.id in recentAlbumIdSet
        val existing = heapByAlbumId[album.id]
        val worst = if (existing == null && heap.size == remaining) requireNotNull(heap.peek()) else null
        val threshold = existing ?: worst
        if (threshold != null && compareSuggestedAlbum(
                searchableAlbum, playCount, isRecent, index, threshold, comparePlayCount,
            ) >= 0
        ) return@forEachIndexed
        val candidate = IndexedSuggestedAlbumCandidate(searchableAlbum, playCount, isRecent, index)
        if (existing != null) {
            heap.remove(existing)
        } else if (worst != null) {
            heap.poll()
            heapByAlbumId.remove(worst.searchableAlbum.album.id)
        }
        heapByAlbumId[album.id] = candidate
        heap += candidate
    }
    heap.sortedWith(comparator).forEach { candidate ->
        if (seen.add(candidate.searchableAlbum.album.id)) {
            output += candidate.searchableAlbum.album
        }
    }
    return output.size == 6
}

private fun compareSuggestedAlbum(
    album: SearchableAlbum,
    playCount: Int,
    isRecent: Boolean,
    index: Int,
    other: IndexedSuggestedAlbumCandidate,
    comparePlayCount: Boolean,
): Int {
    if (comparePlayCount) {
        val byPlayCount = playCount.compareTo(other.playCount)
        if (byPlayCount != 0) return byPlayCount
    }
    val byRecent = isRecent.compareTo(other.isRecent)
    if (byRecent != 0) return byRecent
    val byArtist = album.normalizedArtist.compareTo(other.searchableAlbum.normalizedArtist)
    if (byArtist != 0) return byArtist
    val byTitle = album.normalizedTitle.compareTo(other.searchableAlbum.normalizedTitle)
    return if (byTitle != 0) byTitle else index.compareTo(other.index)
}

internal fun List<Song>.playbackSourceLabel(fallbackAlbum: String): String {
    var albumName: String? = null
    forEach { song ->
        val candidate = song.album.takeIf(String::isNotBlank) ?: return@forEach
        if (albumName != null && albumName != candidate) return elovaire.music.droidbeauty.app.domain.model.SEARCH_PLAYBACK_SOURCE
        albumName = candidate
    }
    return albumName ?: fallbackAlbum
}
