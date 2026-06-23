package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import java.text.Normalizer
import kotlin.math.min

internal data class SearchableSong(
    val song: Song,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val normalizedComposite: String,
)

internal data class SearchableAlbum(
    val album: Album,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedComposite: String,
)

internal data class SearchableArtist(
    val displayName: String,
    val normalizedName: String,
    val songCount: Int,
    val artUri: Uri?,
)

internal data class SearchIndex(
    val songs: List<SearchableSong> = emptyList(),
    val albums: List<SearchableAlbum> = emptyList(),
    val artists: List<SearchableArtist> = emptyList(),
    val albumsById: Map<Long, Album> = emptyMap(),
    val artistsByNormalizedName: Map<String, SearchableArtist> = emptyMap(),
)

internal data class RankedResult<T>(
    val value: T,
    val score: Int,
)

internal data class NormalizedSearchQuery(
    val value: String,
    val tokens: List<String>,
) {
    companion object {
        fun from(rawQuery: String): NormalizedSearchQuery {
            val normalized = normalizeSearchText(rawQuery.trim())
            return NormalizedSearchQuery(
                value = normalized,
                tokens = normalized.split(' ').filter { it.isNotBlank() },
            )
        }
    }
}

internal data class SearchLibrarySnapshot(
    val songs: List<Song>,
    val albums: List<Album>,
)

internal fun SearchLibrarySnapshot.toSearchIndex(): SearchIndex {
    return buildSearchIndex(
        songs = songs,
        albums = albums,
    )
}

internal fun buildSearchIndex(
    songs: List<Song>,
    albums: List<Album>,
): SearchIndex {
    val searchableSongs = songs.map(Song::toSearchableSong)
    val searchableAlbums = albums.map(Album::toSearchableAlbum)
    val searchableArtists = buildSearchableArtists(songs)

    return SearchIndex(
        songs = searchableSongs,
        albums = searchableAlbums,
        artists = searchableArtists,
        albumsById = searchableAlbums.associate { it.album.id to it.album },
        artistsByNormalizedName = searchableArtists.associateBy(SearchableArtist::normalizedName),
    )
}

internal inline fun <T> Iterable<T>.rankMatching(
    query: NormalizedSearchQuery,
    crossinline normalizedTitle: (T) -> String,
    crossinline normalizedArtist: (T) -> String,
    crossinline normalizedAlbum: (T) -> String = { "" },
    crossinline normalizedComposite: (T) -> String,
): List<RankedResult<T>> {
    return mapNotNull { item ->
        scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle(item),
            normalizedArtist = normalizedArtist(item),
            normalizedAlbum = normalizedAlbum(item),
            normalizedComposite = normalizedComposite(item),
        )?.let { score ->
            RankedResult(
                value = item,
                score = score,
            )
        }
    }
}

internal fun normalizeSearchText(value: String): String {
    val withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(SEARCH_DIACRITICS_REGEX, "")

    return withoutDiacritics
        .lowercase()
        .replace('&', ' ')
        .replace(SEARCH_APOSTROPHE_REGEX, "")
        .replace(SEARCH_PUNCTUATION_REGEX, " ")
        .replace(SEARCH_NOISE_REGEX, " ")
        .replace(SEARCH_WHITESPACE_REGEX, " ")
        .trim()
}

internal fun scoreMatch(
    query: NormalizedSearchQuery,
    normalizedTitle: String,
    normalizedArtist: String,
    normalizedAlbum: String = "",
    normalizedComposite: String? = null,
): Int? {
    val normalizedQuery = query.value
    if (normalizedQuery.isBlank()) return null

    val tokens = query.tokens
    if (tokens.isEmpty()) return null

    val composite = normalizedComposite ?: buildNormalizedComposite(
        normalizedTitle,
        normalizedArtist,
        normalizedAlbum,
    )
    if (!tokens.all(composite::contains)) return null

    var score = 0

    if (normalizedTitle == normalizedQuery) score += 120
    if (normalizedArtist == normalizedQuery) score += 90
    if (normalizedAlbum == normalizedQuery) score += 75

    if (normalizedTitle.startsWith(normalizedQuery)) score += 70
    if (normalizedArtist.startsWith(normalizedQuery)) score += 55
    if (normalizedAlbum.startsWith(normalizedQuery)) score += 45

    if (normalizedTitle.contains(normalizedQuery)) score += 35
    if (normalizedArtist.contains(normalizedQuery)) score += 25
    if (normalizedAlbum.contains(normalizedQuery)) score += 20

    tokens.forEach { token ->
        if (normalizedTitle.startsWith(token)) score += 12
        if (normalizedArtist.startsWith(token)) score += 8
        if (normalizedAlbum.startsWith(token)) score += 6
    }

    score -= min(composite.length / 80, 10)
    return score
}

internal fun sortRankedSongs(
    ranked: List<RankedResult<SearchableSong>>,
    sortMode: SearchSongSortMode,
): List<Song> {
    val baseComparator = compareByDescending<RankedResult<SearchableSong>> { it.score }
    val comparator = when (sortMode) {
        SearchSongSortMode.Title -> baseComparator
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }

        SearchSongSortMode.Artist -> baseComparator
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }
    }
    return ranked.sortedWith(comparator).map { it.value.song }
}

internal fun sortRankedAlbums(ranked: List<RankedResult<SearchableAlbum>>): List<Album> {
    return ranked
        .sortedWith(
            compareByDescending<RankedResult<SearchableAlbum>> { it.score }
                .thenBy { it.value.normalizedArtist }
                .thenBy { it.value.normalizedTitle }
                .thenBy { it.value.album.id },
        )
        .map { it.value.album }
}

internal fun buildNormalizedComposite(vararg parts: String): String {
    return parts.filter { it.isNotBlank() }.joinToString(" ")
}

internal fun Song.toSearchableSong(): SearchableSong {
    val normalizedTitle = normalizeSearchText(title)
    val normalizedArtist = normalizeSearchText(artist)
    val normalizedAlbum = normalizeSearchText(album)
    return SearchableSong(
        song = this,
        normalizedTitle = normalizedTitle,
        normalizedArtist = normalizedArtist,
        normalizedAlbum = normalizedAlbum,
        normalizedComposite = buildNormalizedComposite(
            normalizedTitle,
            normalizedArtist,
            normalizedAlbum,
        ),
    )
}

internal fun Album.toSearchableAlbum(): SearchableAlbum {
    val normalizedTitle = normalizeSearchText(title)
    val normalizedArtist = normalizeSearchText(artist)
    return SearchableAlbum(
        album = this,
        normalizedTitle = normalizedTitle,
        normalizedArtist = normalizedArtist,
        normalizedComposite = buildNormalizedComposite(
            normalizedTitle,
            normalizedArtist,
        ),
    )
}

private fun buildSearchableArtists(songs: List<Song>): List<SearchableArtist> {
    return songs
        .filter { it.artist.isNotBlank() }
        .groupBy { normalizeSearchText(it.artist) }
        .mapNotNull { (normalizedName, artistSongs) ->
            if (normalizedName.isBlank()) return@mapNotNull null
            val displayName = artistSongs
                .map { it.artist.trim() }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
                ?.key
                ?: artistSongs.first().artist.trim()
            SearchableArtist(
                displayName = displayName,
                normalizedName = normalizedName,
                songCount = artistSongs.size,
                artUri = artistSongs.firstOrNull { it.artUri != null }?.artUri,
            )
        }
        .sortedWith(
            compareBy<SearchableArtist> { it.normalizedName }
                .thenByDescending { it.songCount }
                .thenBy { it.displayName },
        )
}

private val SEARCH_DIACRITICS_REGEX = Regex("\\p{Mn}+")
private val SEARCH_APOSTROPHE_REGEX = Regex("[\\'’`´]")
private val SEARCH_PUNCTUATION_REGEX = Regex("[\\[\\]{}()_,.;:!?\\-_/\\\\|]+")
private val SEARCH_NOISE_REGEX = Regex(
    "\\b(feat|ft|featuring|prod|remaster|remastered|explicit|clean|official audio|official video)\\b",
)
private val SEARCH_WHITESPACE_REGEX = Regex("\\s+")
