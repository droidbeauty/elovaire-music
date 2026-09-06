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
    val matchingAlbums = index.albums
        .rankMatching(
            query = query,
            normalizedTitle = SearchableAlbum::normalizedTitle,
            normalizedArtist = SearchableAlbum::normalizedArtist,
            normalizedComposite = SearchableAlbum::normalizedComposite,
            cancellationCheck = cancellationCheck,
        )
        .let(::sortRankedAlbums)
        .take(12)

    cancellationCheck()
    val matchingArtists = index.artists
        .rankMatching(
            query = query,
            normalizedTitle = SearchableArtist::normalizedName,
            normalizedArtist = { "" },
            normalizedComposite = SearchableArtist::normalizedName,
            cancellationCheck = cancellationCheck,
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

    cancellationCheck()
    val matchingAudiobooks = index.audiobooks
        .rankMatching(
            query = query,
            normalizedTitle = SearchableAudiobook::normalizedTitle,
            normalizedArtist = SearchableAudiobook::normalizedAuthor,
            normalizedComposite = SearchableAudiobook::normalizedComposite,
            cancellationCheck = cancellationCheck,
        )
        .sortedWith(
            compareByDescending<RankedResult<SearchableAudiobook>> { it.score }
                .thenBy { it.value.normalizedTitle }
                .thenBy { it.value.normalizedAuthor },
        )
        .map { it.value.audiobook }
        .take(6)

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
