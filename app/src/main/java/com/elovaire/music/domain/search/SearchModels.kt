package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.text.Normalizer
import java.util.Locale
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

internal enum class SearchSortMode {
    Title,
    Artist,
}

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
            val normalized = normalizeSearchText(rawQuery)
            return NormalizedSearchQuery(
                value = normalized,
                tokens = if (normalized.isEmpty()) emptyList() else normalized.split(' '),
            )
        }
    }
}

internal data class SearchLibrarySnapshot(
    val songs: List<Song>,
    val albums: List<Album>,
) {
    fun signature(): Long {
        var result = 17L
        result = 31L * result + songs.size
        result = 31L * result + albums.size
        songs.forEach { song ->
            result = 31L * result + song.id
            result = 31L * result + song.title.hashCode()
            result = 31L * result + song.artist.hashCode()
            result = 31L * result + song.album.hashCode()
            result = 31L * result + song.albumArtist.orEmpty().hashCode()
        }
        albums.forEach { album ->
            result = 31L * result + album.id
            result = 31L * result + album.title.hashCode()
            result = 31L * result + album.artist.hashCode()
        }
        return result
    }
}

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
        .lowercase(Locale.ROOT)
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
    val titleAcronym = acronymOf(normalizedTitle)
    val artistAcronym = acronymOf(normalizedArtist)
    val albumAcronym = acronymOf(normalizedAlbum)
    if (!tokens.all { token ->
            tokenMatchesAnyField(
                token = token,
                normalizedComposite = composite,
                titleAcronym = titleAcronym,
                artistAcronym = artistAcronym,
                albumAcronym = albumAcronym,
            )
        }
    ) return null

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
        if (wordStartsWith(normalizedTitle, token)) score += 10
        if (wordStartsWith(normalizedArtist, token)) score += 7
        if (wordStartsWith(normalizedAlbum, token)) score += 5
        if (artistAcronym.startsWith(token)) score += 12
        if (albumAcronym.startsWith(token)) score += 10
        if (token.length >= 4 && fuzzyTokenMatches(normalizedTitle, token)) score += 4
    }

    score -= min(composite.length / 80, 10)
    return score
}

internal fun sortRankedSongs(
    ranked: List<RankedResult<SearchableSong>>,
    sortMode: SearchSortMode,
): List<Song> {
    val baseComparator = compareByDescending<RankedResult<SearchableSong>> { it.score }
    val comparator = when (sortMode) {
        SearchSortMode.Title -> baseComparator
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }

        SearchSortMode.Artist -> baseComparator
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }
    }
    return ranked.sortedWith(comparator).map { it.value.song }
}

internal fun rankedSongComparator(
    sortMode: SearchSortMode,
): Comparator<RankedResult<SearchableSong>> {
    val baseComparator = compareByDescending<RankedResult<SearchableSong>> { it.score }
    return when (sortMode) {
        SearchSortMode.Title -> baseComparator
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }

        SearchSortMode.Artist -> baseComparator
            .thenBy { it.value.normalizedArtist }
            .thenBy { it.value.normalizedTitle }
            .thenBy { it.value.normalizedAlbum }
            .thenBy { it.value.song.id }
    }
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
    return buildString {
        parts.forEach { part ->
            if (part.isBlank()) return@forEach
            if (isNotEmpty()) append(' ')
            append(part)
        }
    }
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
        .filter { it.libraryArtistName().isNotBlank() }
        .groupBy { normalizeSearchText(it.libraryArtistName()) }
        .mapNotNull { (normalizedName, artistSongs) ->
            if (normalizedName.isBlank()) return@mapNotNull null
            val displayName = artistSongs
                .map { it.libraryArtistName().trim() }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
                ?.key
                ?: artistSongs.first().libraryArtistName().trim()
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

private fun Song.libraryArtistName(): String {
    return albumArtist?.takeIf { it.isNotBlank() } ?: artist
}

private fun tokenMatchesAnyField(
    token: String,
    normalizedComposite: String,
    titleAcronym: String,
    artistAcronym: String,
    albumAcronym: String,
): Boolean {
    return normalizedComposite.contains(token) ||
        titleAcronym.startsWith(token) ||
        artistAcronym.startsWith(token) ||
        albumAcronym.startsWith(token) ||
        (token.length >= 4 && fuzzyTokenMatches(normalizedComposite, token))
}

private fun wordStartsWith(value: String, token: String): Boolean {
    if (token.isEmpty()) return true
    var wordStart = 0
    while (wordStart <= value.length - token.length) {
        if (value.regionMatches(wordStart, token, 0, token.length)) return true
        val separator = value.indexOf(' ', wordStart)
        if (separator < 0) return false
        wordStart = separator + 1
    }
    return false
}

private fun acronymOf(value: String): String {
    return buildString {
        var atWordStart = true
        value.forEach { character ->
            if (character == ' ') {
                atWordStart = true
            } else if (atWordStart) {
                append(character)
                atWordStart = false
            }
        }
    }
}

private fun fuzzyTokenMatches(value: String, token: String): Boolean {
    return value.split(' ').any { word ->
        word.length >= 4 && editDistanceAtMostOne(word, token)
    }
}

private fun editDistanceAtMostOne(left: String, right: String): Boolean {
    if (left == right) return true
    if (kotlin.math.abs(left.length - right.length) > 1) return false

    var differences = 0
    var leftIndex = 0
    var rightIndex = 0
    while (leftIndex < left.length && rightIndex < right.length) {
        if (left[leftIndex] == right[rightIndex]) {
            leftIndex++
            rightIndex++
        } else {
            differences++
            if (differences > 1) return false
            when {
                left.length > right.length -> leftIndex++
                right.length > left.length -> rightIndex++
                else -> {
                    leftIndex++
                    rightIndex++
                }
            }
        }
    }
    return differences + (left.length - leftIndex) + (right.length - rightIndex) <= 1
}

private val SEARCH_DIACRITICS_REGEX = Regex("\\p{Mn}+")
private val SEARCH_APOSTROPHE_REGEX = Regex("[\\'’`´]")
private val SEARCH_PUNCTUATION_REGEX = Regex("[\\[\\]{}()_,.;:!?\\-_/\\\\|]+")
private val SEARCH_NOISE_REGEX = Regex(
    "\\b(feat|ft|featuring|prod|remaster|remastered|explicit|clean|official audio|official video)\\b",
)
private val SEARCH_WHITESPACE_REGEX = Regex("\\s+")
