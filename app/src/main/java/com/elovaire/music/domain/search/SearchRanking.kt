package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.PriorityQueue

internal data class SearchArtistResult(
    val name: String,
    val songCount: Int,
    val artUri: Uri?,
)

internal data class SearchResults(
    val allMatchingSongs: List<Song> = emptyList(),
    val matchingSongs: List<Song> = emptyList(),
    val totalSongMatchCount: Int = 0,
    val matchingAlbums: List<Album> = emptyList(),
    val matchingArtists: List<SearchArtistResult> = emptyList(),
)

internal fun buildSearchResults(
    query: NormalizedSearchQuery,
    sortMode: SearchSortMode,
    index: SearchIndex,
    includeAllSongs: Boolean = true,
): SearchResults {
    if (query.value.isBlank()) return SearchResults()

    val rankedSongs = index.songs.rankMatching(
        query = query,
        normalizedTitle = SearchableSong::normalizedTitle,
        normalizedArtist = SearchableSong::normalizedArtist,
        normalizedAlbum = SearchableSong::normalizedAlbum,
        normalizedComposite = SearchableSong::normalizedComposite,
    )
    val sortedSongs = if (includeAllSongs) {
        sortRankedSongs(
            ranked = rankedSongs,
            sortMode = sortMode,
        )
    } else {
        topRankedSongs(
            ranked = rankedSongs,
            sortMode = sortMode,
            limit = 20,
        )
    }

    val matchingAlbums = index.albums
        .rankMatching(
            query = query,
            normalizedTitle = SearchableAlbum::normalizedTitle,
            normalizedArtist = SearchableAlbum::normalizedArtist,
            normalizedComposite = SearchableAlbum::normalizedComposite,
        )
        .let(::sortRankedAlbums)
        .take(12)

    val matchingArtists = index.artists
        .rankMatching(
            query = query,
            normalizedTitle = SearchableArtist::normalizedName,
            normalizedArtist = { "" },
            normalizedComposite = SearchableArtist::normalizedName,
        )
        .sortedWith(
            compareByDescending<RankedResult<SearchableArtist>> { it.score }
                .thenByDescending { it.value.songCount }
                .thenBy { it.value.normalizedName },
        )
        .map { rankedArtist ->
            SearchArtistResult(
                name = rankedArtist.value.displayName,
                songCount = rankedArtist.value.songCount,
                artUri = rankedArtist.value.artUri,
            )
        }
        .take(6)

    return SearchResults(
        allMatchingSongs = if (includeAllSongs) sortedSongs else emptyList(),
        matchingSongs = if (includeAllSongs) sortedSongs.take(20) else sortedSongs,
        totalSongMatchCount = rankedSongs.size,
        matchingAlbums = matchingAlbums,
        matchingArtists = matchingArtists,
    )
}

private fun topRankedSongs(
    ranked: List<RankedResult<SearchableSong>>,
    sortMode: SearchSortMode,
    limit: Int,
): List<Song> {
    if (ranked.size <= limit) {
        return sortRankedSongs(
            ranked = ranked,
            sortMode = sortMode,
        )
    }

    val bestFirst = rankedSongComparator(sortMode)
    val worstFirst = bestFirst.reversed()
    val heap = PriorityQueue<RankedResult<SearchableSong>>(limit, worstFirst)
    ranked.forEach { candidate ->
        if (heap.size < limit) {
            heap += candidate
        } else if (bestFirst.compare(candidate, heap.peek()) < 0) {
            heap.poll()
            heap += candidate
        }
    }
    return heap.sortedWith(bestFirst).map { it.value.song }
}
