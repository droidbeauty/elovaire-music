package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
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
    val matchingAudiobooks: List<Audiobook> = emptyList(),
)

internal fun buildSearchResults(
    query: NormalizedSearchQuery,
    sortMode: SearchSortMode,
    index: SearchIndex,
    includeAllSongs: Boolean = true,
    cancellationCheck: () -> Unit = {},
): SearchResults {
    if (query.value.isBlank()) return SearchResults()

    val (sortedSongs, totalSongMatchCount) = if (includeAllSongs) {
        val rankedSongs = index.songs.rankMatching(
            query = query,
            normalizedTitle = SearchableSong::normalizedTitle,
            normalizedArtist = SearchableSong::normalizedArtist,
            normalizedAlbum = SearchableSong::normalizedAlbum,
            normalizedComposite = SearchableSong::normalizedComposite,
            normalizedAlbumArtist = SearchableSong::normalizedAlbumArtist,
            cancellationCheck = cancellationCheck,
        )
        cancellationCheck()
        sortRankedSongs(
            ranked = rankedSongs,
            sortMode = sortMode,
        ).also { cancellationCheck() } to rankedSongs.size
    } else {
        topMatchingSongs(
            songs = index.songs,
            query = query,
            sortMode = sortMode,
            limit = 20,
            cancellationCheck = cancellationCheck,
        ).let { it.songs to it.matchCount }
    }

    cancellationCheck()
    val matchingAlbums = topRankedMatches(
        items = index.albums,
        limit = 12,
        rankedComparator = compareByDescending<RankedResult<SearchableAlbum>> { it.score }
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.album.id },
        score = { album ->
            scoreMatch(
                query = query,
                normalizedTitle = album.normalizedTitle,
                normalizedArtist = album.normalizedArtist,
                normalizedComposite = album.normalizedComposite,
            )
        },
        cancellationCheck = cancellationCheck,
    ).map { it.value.album }

    cancellationCheck()
    val matchingArtists = topRankedMatches(
        items = index.artists,
        limit = 6,
        rankedComparator = compareByDescending<RankedResult<SearchableArtist>> { it.score }
            .thenByDescending { it.value.songCount }
            .thenBy { it.value.normalizedName },
        score = { artist ->
            scoreMatch(
                query = query,
                normalizedTitle = artist.normalizedName,
                normalizedArtist = "",
                normalizedComposite = artist.normalizedName,
            )
        },
        cancellationCheck = cancellationCheck,
    )
        .map { rankedArtist ->
            SearchArtistResult(
                name = rankedArtist.value.displayName,
                songCount = rankedArtist.value.songCount,
                artUri = rankedArtist.value.artUri,
            )
        }

    cancellationCheck()
    val matchingAudiobooks = topRankedMatches(
        items = index.audiobooks,
        limit = 6,
        rankedComparator = compareByDescending<RankedResult<SearchableAudiobook>> { it.score }
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedAuthor },
        score = { audiobook ->
            scoreMatch(
                query = query,
                normalizedTitle = audiobook.normalizedTitle,
                normalizedArtist = audiobook.normalizedAuthor,
                normalizedComposite = audiobook.normalizedComposite,
            )
        },
        cancellationCheck = cancellationCheck,
    )
        .map { it.value.audiobook }

    return SearchResults(
        allMatchingSongs = if (includeAllSongs) sortedSongs else emptyList(),
        matchingSongs = if (includeAllSongs) sortedSongs.take(20) else sortedSongs,
        totalSongMatchCount = totalSongMatchCount,
        matchingAlbums = matchingAlbums,
        matchingArtists = matchingArtists,
        matchingAudiobooks = matchingAudiobooks,
    )
}

private data class TopMatchingSongs(
    val songs: List<Song>,
    val matchCount: Int,
)

private fun <T> topRankedMatches(
    items: List<T>,
    limit: Int,
    rankedComparator: Comparator<RankedResult<T>>,
    score: (T) -> Int?,
    cancellationCheck: () -> Unit,
): List<RankedResult<T>> {
    if (limit <= 0 || items.isEmpty()) return emptyList()
    val stableBestFirst = rankedComparator.thenComparator { left, right ->
        left.sourceIndex.compareTo(right.sourceIndex)
    }
    val heap = PriorityQueue<RankedResult<T>>(limit, stableBestFirst.reversed())
    items.forEachIndexed { index, item ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        score(item)?.let { matchScore ->
            val candidate = RankedResult(item, matchScore, sourceIndex = index)
            if (heap.size < limit) {
                heap += candidate
            } else if (stableBestFirst.compare(candidate, requireNotNull(heap.peek())) < 0) {
                heap.poll()
                heap += candidate
            }
        }
    }
    cancellationCheck()
    return heap.sortedWith(stableBestFirst)
}

private fun topMatchingSongs(
    songs: List<SearchableSong>,
    query: NormalizedSearchQuery,
    sortMode: SearchSortMode,
    limit: Int,
    cancellationCheck: () -> Unit = {},
): TopMatchingSongs {
    val bestFirst = rankedSongComparator(sortMode)
    val songComparator = searchableSongComparator(sortMode)
    val worstFirst = bestFirst.reversed()
    val heap = PriorityQueue<RankedResult<SearchableSong>>(limit, worstFirst)
    var matchCount = 0
    songs.forEachIndexed { index, song ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        scoreMatch(
            query = query,
            normalizedTitle = song.normalizedTitle,
            normalizedArtist = song.normalizedArtist,
            normalizedAlbum = song.normalizedAlbum,
            normalizedComposite = song.normalizedComposite,
            normalizedAlbumArtist = song.normalizedAlbumArtist,
        )?.let { score ->
            matchCount++
            if (heap.size < limit) {
                heap += RankedResult(song, score)
            } else {
                val worst = requireNotNull(heap.peek())
                if (score > worst.score || (score == worst.score && songComparator.compare(song, worst.value) < 0)) {
                    heap.poll()
                    heap += RankedResult(song, score)
                }
            }
        }
    }
    cancellationCheck()
    return TopMatchingSongs(
        songs = heap.sortedWith(bestFirst).map { it.value.song },
        matchCount = matchCount,
    )
}
