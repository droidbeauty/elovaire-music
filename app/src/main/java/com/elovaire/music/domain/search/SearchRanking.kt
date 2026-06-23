package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode

internal data class SearchArtistResult(
    val name: String,
    val songCount: Int,
    val artUri: Uri?,
)

internal data class SearchResults(
    val allMatchingSongs: List<Song> = emptyList(),
    val matchingSongs: List<Song> = emptyList(),
    val matchingAlbums: List<Album> = emptyList(),
    val matchingArtists: List<SearchArtistResult> = emptyList(),
)

internal fun buildSearchResults(
    query: NormalizedSearchQuery,
    sortMode: SearchSongSortMode,
    index: SearchIndex,
): SearchResults {
    if (query.value.isBlank()) return SearchResults()

    val rankedSongs = index.songs.rankMatching(
        query = query,
        normalizedTitle = SearchableSong::normalizedTitle,
        normalizedArtist = SearchableSong::normalizedArtist,
        normalizedAlbum = SearchableSong::normalizedAlbum,
        normalizedComposite = SearchableSong::normalizedComposite,
    )
    val allMatchingSongs = sortRankedSongs(
        ranked = rankedSongs,
        sortMode = sortMode,
    )

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
        allMatchingSongs = allMatchingSongs,
        matchingSongs = allMatchingSongs.take(20),
        matchingAlbums = matchingAlbums,
        matchingArtists = matchingArtists,
    )
}
