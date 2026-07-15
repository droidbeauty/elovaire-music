package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class SearchablePlaylist(
    val playlist: Playlist,
    val normalizedName: String,
    val normalizedComposite: String,
)

private data class SearchablePickerArtist<T>(
    val value: T,
    val normalizedName: String,
    val normalizedComposite: String,
    val songCount: Int,
)

internal fun searchSongsForPicker(
    songs: List<Song>,
    rawQuery: String,
    preserveInputOrderWhenBlank: Boolean = true,
): List<Song> {
    return searchSongsForPicker(
        songs = songs,
        query = NormalizedSearchQuery.from(rawQuery),
        preserveInputOrderWhenBlank = preserveInputOrderWhenBlank,
    )
}

internal fun searchSongsForPicker(
    songs: List<Song>,
    query: NormalizedSearchQuery,
    preserveInputOrderWhenBlank: Boolean = true,
): List<Song> {
    if (query.value.isBlank()) {
        return if (preserveInputOrderWhenBlank) songs else sortRankedSongs(
            ranked = songs.map(Song::toSearchableSong).map { RankedResult(it, 0) },
            sortMode = SearchSortMode.Title,
        )
    }

    return searchIndexedSongsForPicker(songs.map(Song::toSearchableSong), query)
}

internal fun searchIndexedSongsForPicker(
    songs: List<SearchableSong>,
    query: NormalizedSearchQuery,
): List<Song> {
    return sortRankedSongs(
        ranked = songs.rankMatching(
            query = query,
            normalizedTitle = SearchableSong::normalizedTitle,
            normalizedArtist = SearchableSong::normalizedArtist,
            normalizedAlbum = SearchableSong::normalizedAlbum,
            normalizedComposite = SearchableSong::normalizedComposite,
        ),
        sortMode = SearchSortMode.Title,
    )
}

internal fun searchAlbumsForPicker(
    albums: List<Album>,
    rawQuery: String,
): List<Album> {
    return searchAlbumsForPicker(
        albums = albums,
        query = NormalizedSearchQuery.from(rawQuery),
    )
}

internal fun searchAlbumsForPicker(
    albums: List<Album>,
    query: NormalizedSearchQuery,
): List<Album> {
    if (query.value.isBlank()) return albums

    return sortRankedAlbums(
        albums.map(Album::toSearchableAlbum).rankMatching(
            query = query,
            normalizedTitle = SearchableAlbum::normalizedTitle,
            normalizedArtist = SearchableAlbum::normalizedArtist,
            normalizedComposite = SearchableAlbum::normalizedComposite,
        ),
    )
}

internal fun searchPlaylists(
    playlists: List<Playlist>,
    rawQuery: String,
): List<Playlist> {
    return searchPlaylists(
        playlists = playlists,
        query = NormalizedSearchQuery.from(rawQuery),
    )
}

internal fun searchPlaylists(
    playlists: List<Playlist>,
    query: NormalizedSearchQuery,
): List<Playlist> {
    if (query.value.isBlank()) return playlists

    return playlists
        .map { playlist ->
            val normalizedName = normalizeSearchText(playlist.name)
            SearchablePlaylist(
                playlist = playlist,
                normalizedName = normalizedName,
                normalizedComposite = normalizedName,
            )
        }
        .rankMatching(
            query = query,
            normalizedTitle = SearchablePlaylist::normalizedName,
            normalizedArtist = { "" },
            normalizedComposite = SearchablePlaylist::normalizedComposite,
        )
        .sortedWith(
            compareByDescending<RankedResult<SearchablePlaylist>> { it.score }
                .thenBy { it.value.normalizedName }
                .thenBy { it.value.playlist.id },
        )
        .map { it.value.playlist }
}

internal fun <T> searchArtistsForPicker(
    artists: List<T>,
    query: NormalizedSearchQuery,
    name: (T) -> String,
    songs: (T) -> List<Song>,
    songCount: (T) -> Int,
): List<T> {
    if (query.value.isBlank()) return artists

    return artists
        .map { artist ->
            val artistSongs = songs(artist)
            val normalizedName = normalizeSearchText(name(artist))
            val normalizedComposite = buildNormalizedComposite(
                normalizedName,
                artistSongs.firstOrNull()?.album.orEmpty().let(::normalizeSearchText),
            )
            SearchablePickerArtist(
                value = artist,
                normalizedName = normalizedName,
                normalizedComposite = normalizedComposite,
                songCount = songCount(artist),
            )
        }
        .rankMatching(
            query = query,
            normalizedTitle = { it.normalizedName },
            normalizedArtist = { "" },
            normalizedComposite = { it.normalizedComposite },
        )
        .sortedWith(
            compareByDescending<RankedResult<SearchablePickerArtist<T>>> { it.score }
                .thenBy { it.value.normalizedName }
                .thenByDescending { it.value.songCount }
                .thenBy { name(it.value.value) },
        )
        .map { it.value.value }
}
