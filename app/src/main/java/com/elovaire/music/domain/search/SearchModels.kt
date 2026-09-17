package elovaire.music.droidbeauty.app.domain.search

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.min

internal data class SearchableSong(
    val song: Song,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbumArtist: String,
    val normalizedAlbum: String,
    val normalizedComposite: String,
)

internal data class SearchableAlbum(
    val album: Album,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedComposite: String,
)

internal data class SearchableAudiobook(
    val audiobook: Audiobook,
    val normalizedTitle: String,
    val normalizedAuthor: String,
    val normalizedComposite: String,
)

internal data class SearchableArtist(
    val displayName: String,
    val normalizedName: String,
    val songCount: Int,
    val artUri: Uri?,
)

internal data class SearchIndex(
    val revision: String = "",
    val songs: List<SearchableSong> = emptyList(),
    val albums: List<SearchableAlbum> = emptyList(),
    val audiobooks: List<SearchableAudiobook> = emptyList(),
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
    val sourceIndex: Int = 0,
)

internal data class NormalizedSearchQuery(
    val value: String,
    val tokens: List<String>,
) {
    companion object {
        fun from(rawQuery: String): NormalizedSearchQuery {
            val normalized = normalizeSearchQueryText(rawQuery)
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
    val audiobooks: List<Audiobook> = emptyList(),
    val revision: String = "",
) {
    fun signature(): String {
        if (revision.isNotBlank()) return revision
        val digest = MessageDigest.getInstance("SHA-256")
        digest.appendSearchRevisionValue(songs.size)
        digest.appendSearchRevisionValue(albums.size)
        songs.forEach { song ->
            digest.appendSearchRevisionValue(song.id)
            digest.appendSearchRevisionValue(song.title)
            digest.appendSearchRevisionValue(song.artist)
            digest.appendSearchRevisionValue(song.album)
            digest.appendSearchRevisionValue(song.albumArtist.orEmpty())
        }
        audiobooks.forEach { audiobook ->
            digest.appendSearchRevisionValue(audiobook.stableKey)
            digest.appendSearchRevisionValue(audiobook.title)
            digest.appendSearchRevisionValue(audiobook.author)
            audiobook.parts.forEach { part -> digest.appendSearchRevisionValue(part.song.id) }
        }
        albums.forEach { album ->
            digest.appendSearchRevisionValue(album.id)
            digest.appendSearchRevisionValue(album.title)
            digest.appendSearchRevisionValue(album.artist)
        }
        return digest.digest().toSearchRevisionHex()
    }
}

private fun MessageDigest.appendSearchRevisionValue(value: Any?) {
    if (value == null) {
        update(0xFF.toByte())
        return
    }
    val bytes = value.toString().toByteArray(StandardCharsets.UTF_8)
    update(0)
    update((bytes.size ushr 24).toByte())
    update((bytes.size ushr 16).toByte())
    update((bytes.size ushr 8).toByte())
    update(bytes.size.toByte())
    update(bytes)
}

private fun ByteArray.toSearchRevisionHex(): String {
    val digits = "0123456789abcdef"
    return buildString(size * 2) {
        this@toSearchRevisionHex.forEach { byte ->
            val value = byte.toInt() and 0xFF
            append(digits[value ushr 4])
            append(digits[value and 0x0F])
        }
    }
}

internal fun SearchLibrarySnapshot.toSearchIndex(
    revision: String = signature(),
    cancellationCheck: () -> Unit = {},
): SearchIndex {
    return buildSearchIndex(
        songs = songs,
        albums = albums,
        audiobooks = audiobooks,
        cancellationCheck = cancellationCheck,
    ).copy(revision = revision)
}

internal fun buildSearchIndex(
    songs: List<Song>,
    albums: List<Album>,
    audiobooks: List<Audiobook> = emptyList(),
    cancellationCheck: () -> Unit = {},
): SearchIndex {
    val searchableSongs = ArrayList<SearchableSong>(songs.size)
    songs.forEachIndexed { index, song ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        searchableSongs += song.toSearchableSong()
    }
    cancellationCheck()
    val searchableAlbums = ArrayList<SearchableAlbum>(albums.size)
    albums.forEachIndexed { index, album ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        searchableAlbums += album.toSearchableAlbum()
    }
    cancellationCheck()
    val searchableAudiobooks = ArrayList<SearchableAudiobook>(audiobooks.size)
    audiobooks.forEachIndexed { index, audiobook ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        searchableAudiobooks += audiobook.toSearchableAudiobook()
    }
    cancellationCheck()
    val searchableArtists = buildSearchableArtists(songs, cancellationCheck)

    return SearchIndex(
        songs = searchableSongs,
        albums = searchableAlbums,
        audiobooks = searchableAudiobooks,
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
    crossinline normalizedAlbumArtist: (T) -> String = { "" },
    crossinline cancellationCheck: () -> Unit = {},
): List<RankedResult<T>> {
    val ranked = ArrayList<RankedResult<T>>()
    for ((index, item) in this.withIndex()) {
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        scoreMatch(
            query = query,
            normalizedTitle = normalizedTitle(item),
            normalizedArtist = normalizedArtist(item),
            normalizedAlbum = normalizedAlbum(item),
            normalizedComposite = normalizedComposite(item),
            normalizedAlbumArtist = normalizedAlbumArtist(item),
        )?.let { score ->
            ranked += RankedResult(
                value = item,
                score = score,
                sourceIndex = index,
            )
        }
    }
    return ranked
}

internal fun normalizeSearchText(value: String): String {
    return normalizeSearchText(value, removeMetadataNoise = true)
}

internal fun normalizeSearchQueryText(value: String): String {
    return normalizeSearchText(limitSearchQueryInput(value), removeMetadataNoise = false)
}

internal fun limitSearchQueryInput(value: String): String {
    if (value.codePointCount(0, value.length) <= MAX_SEARCH_QUERY_CODE_POINTS) return value
    return value.substring(0, value.offsetByCodePoints(0, MAX_SEARCH_QUERY_CODE_POINTS))
}

private fun normalizeSearchText(
    value: String,
    removeMetadataNoise: Boolean,
): String {
    val withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(SEARCH_DIACRITICS_REGEX, "")

    val normalized = withoutDiacritics
        .lowercase(Locale.ROOT)
        .replace('&', ' ')
        .replace(SEARCH_APOSTROPHE_REGEX, "")
        .replace(SEARCH_PUNCTUATION_REGEX, " ")
    return (if (removeMetadataNoise) normalized.replace(SEARCH_NOISE_REGEX, " ") else normalized)
        .replace(SEARCH_WHITESPACE_REGEX, " ")
        .trim()
}

internal fun scoreMatch(
    query: NormalizedSearchQuery,
    normalizedTitle: String,
    normalizedArtist: String,
    normalizedAlbum: String = "",
    normalizedComposite: String? = null,
    normalizedAlbumArtist: String = "",
): Int? {
    val normalizedQuery = query.value
    if (normalizedQuery.isBlank()) return null

    val tokens = query.tokens
    if (tokens.isEmpty()) return null

    val composite = normalizedComposite ?: buildNormalizedComposite(
        normalizedTitle,
        normalizedArtist,
        normalizedAlbumArtist,
        normalizedAlbum,
    )
    var score = 0

    if (normalizedTitle == normalizedQuery) score += 120
    if (normalizedArtist == normalizedQuery) score += 90
    if (normalizedAlbumArtist == normalizedQuery) score += 65
    if (normalizedAlbum == normalizedQuery) score += 75

    if (normalizedTitle.startsWith(normalizedQuery)) score += 70
    if (normalizedArtist.startsWith(normalizedQuery)) score += 55
    if (normalizedAlbumArtist.startsWith(normalizedQuery)) score += 40
    if (normalizedAlbum.startsWith(normalizedQuery)) score += 45

    if (normalizedTitle.contains(normalizedQuery)) score += 35
    if (normalizedArtist.contains(normalizedQuery)) score += 25
    if (normalizedAlbumArtist.contains(normalizedQuery)) score += 18
    if (normalizedAlbum.contains(normalizedQuery)) score += 20

    for (token in tokens) {
        val features = tokenMatchFeatures(
            token = token,
            normalizedComposite = composite,
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbumArtist = normalizedAlbumArtist,
            normalizedAlbum = normalizedAlbum,
        )
        if (features and TOKEN_MATCHED == 0) return null

        if (features and TOKEN_TITLE_START != 0) score += 12
        if (features and TOKEN_ARTIST_START != 0) score += 8
        if (features and TOKEN_ALBUM_ARTIST_START != 0) score += 7
        if (features and TOKEN_ALBUM_START != 0) score += 6
        if (features and TOKEN_TITLE_WORD_START != 0) score += 10
        if (features and TOKEN_ARTIST_WORD_START != 0) score += 7
        if (features and TOKEN_ALBUM_ARTIST_WORD_START != 0) score += 6
        if (features and TOKEN_ALBUM_WORD_START != 0) score += 5
        if (features and TOKEN_ARTIST_ACRONYM != 0) score += 12
        if (features and TOKEN_ALBUM_ARTIST_ACRONYM != 0) score += 6
        if (features and TOKEN_ALBUM_ACRONYM != 0) score += 10
        if (features and TOKEN_TITLE_FUZZY != 0) score += 4
    }

    score -= min(composite.length / 80, 10)
    return score
}

private fun tokenMatchFeatures(
    token: String,
    normalizedComposite: String,
    normalizedTitle: String,
    normalizedArtist: String,
    normalizedAlbumArtist: String,
    normalizedAlbum: String,
): Int {
    val titleStartsWith = normalizedTitle.startsWith(token)
    val artistStartsWith = normalizedArtist.startsWith(token)
    val albumArtistStartsWith = normalizedAlbumArtist.startsWith(token)
    val albumStartsWith = normalizedAlbum.startsWith(token)
    val titleWordStartsWith = wordStartsWith(normalizedTitle, token)
    val artistWordStartsWith = wordStartsWith(normalizedArtist, token)
    val albumArtistWordStartsWith = wordStartsWith(normalizedAlbumArtist, token)
    val albumWordStartsWith = wordStartsWith(normalizedAlbum, token)
    val titleAcronymStartsWith = acronymStartsWith(normalizedTitle, token)
    val artistAcronymStartsWith = acronymStartsWith(normalizedArtist, token)
    val albumArtistAcronymStartsWith = acronymStartsWith(normalizedAlbumArtist, token)
    val albumAcronymStartsWith = acronymStartsWith(normalizedAlbum, token)
    val compositeContains = normalizedComposite.contains(token)
    val fuzzyCompositeMatch = token.length >= 4 && fuzzyTokenMatches(normalizedComposite, token)
    val fuzzyTitleMatch = token.length >= 4 && fuzzyTokenMatches(normalizedTitle, token)
    val hasAcronymMatch = titleAcronymStartsWith ||
        artistAcronymStartsWith ||
        albumArtistAcronymStartsWith ||
        albumAcronymStartsWith
    if (!compositeContains && !hasAcronymMatch && !fuzzyCompositeMatch) return 0

    var features = TOKEN_MATCHED
    if (titleStartsWith) features = features or TOKEN_TITLE_START
    if (artistStartsWith) features = features or TOKEN_ARTIST_START
    if (albumArtistStartsWith) features = features or TOKEN_ALBUM_ARTIST_START
    if (albumStartsWith) features = features or TOKEN_ALBUM_START
    if (titleWordStartsWith) features = features or TOKEN_TITLE_WORD_START
    if (artistWordStartsWith) features = features or TOKEN_ARTIST_WORD_START
    if (albumArtistWordStartsWith) features = features or TOKEN_ALBUM_ARTIST_WORD_START
    if (albumWordStartsWith) features = features or TOKEN_ALBUM_WORD_START
    if (artistAcronymStartsWith) features = features or TOKEN_ARTIST_ACRONYM
    if (albumArtistAcronymStartsWith) features = features or TOKEN_ALBUM_ARTIST_ACRONYM
    if (albumAcronymStartsWith) features = features or TOKEN_ALBUM_ACRONYM
    if (fuzzyTitleMatch) features = features or TOKEN_TITLE_FUZZY
    return features
}

internal fun sortRankedSongs(
    ranked: List<RankedResult<SearchableSong>>,
    sortMode: SearchSortMode,
): List<Song> {
    return ranked.sortedWith(rankedSongComparator(sortMode)).map { it.value.song }
}

internal fun rankedSongComparator(
    sortMode: SearchSortMode,
): Comparator<RankedResult<SearchableSong>> {
    val songComparator = searchableSongComparator(sortMode)
    return compareByDescending<RankedResult<SearchableSong>> { it.score }
        .thenComparator { left, right -> songComparator.compare(left.value, right.value) }
}

internal fun searchableSongComparator(sortMode: SearchSortMode): Comparator<SearchableSong> {
    return when (sortMode) {
        SearchSortMode.Title -> compareBy<SearchableSong> { it.normalizedTitle }
            .thenBy { it.normalizedArtist }
            .thenBy { it.normalizedAlbum }
            .thenBy { it.song.id }

        SearchSortMode.Artist -> compareBy<SearchableSong> { it.normalizedArtist }
            .thenBy { it.normalizedTitle }
            .thenBy { it.normalizedAlbum }
            .thenBy { it.song.id }
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
    val normalizedAlbumArtist = normalizeSearchText(albumArtist.orEmpty())
    val normalizedAlbum = normalizeSearchText(album)
    return SearchableSong(
        song = this,
        normalizedTitle = normalizedTitle,
        normalizedArtist = normalizedArtist,
        normalizedAlbumArtist = normalizedAlbumArtist,
        normalizedAlbum = normalizedAlbum,
        normalizedComposite = buildNormalizedComposite(
            normalizedTitle,
            normalizedArtist,
            normalizedAlbumArtist,
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

internal fun Audiobook.toSearchableAudiobook(): SearchableAudiobook {
    val normalizedTitle = normalizeSearchText(title)
    val normalizedAuthor = normalizeSearchText(author)
    return SearchableAudiobook(
        audiobook = this,
        normalizedTitle = normalizedTitle,
        normalizedAuthor = normalizedAuthor,
        normalizedComposite = buildNormalizedComposite(normalizedTitle, normalizedAuthor),
    )
}

private fun buildSearchableArtists(
    songs: List<Song>,
    cancellationCheck: () -> Unit,
): List<SearchableArtist> {
    val groups = LinkedHashMap<String, MutableList<Song>>()
    songs.forEachIndexed { index, song ->
        if (index and SEARCH_CANCELLATION_CHECK_MASK == 0) cancellationCheck()
        val artistName = song.libraryArtistName()
        if (artistName.isNotBlank()) {
            val normalizedName = normalizeSearchText(artistName)
            if (normalizedName.isNotBlank()) {
                groups.getOrPut(normalizedName, ::mutableListOf).add(song)
            }
        }
    }
    return groups
        .mapNotNull { (normalizedName, artistSongs) ->
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

private fun acronymStartsWith(value: String, token: String): Boolean {
    if (token.isEmpty()) return true
    var tokenIndex = 0
    var atWordStart = true
    value.forEach { character ->
        if (character == ' ') {
            atWordStart = true
        } else if (atWordStart) {
            if (tokenIndex >= token.length || character != token[tokenIndex]) return false
            tokenIndex++
            if (tokenIndex == token.length) return true
            atWordStart = false
        }
    }
    return false
}

private fun fuzzyTokenMatches(value: String, token: String): Boolean {
    var wordStart = 0
    while (wordStart < value.length) {
        val wordEnd = value.indexOf(' ', wordStart).let { separator ->
            if (separator < 0) value.length else separator
        }
        if (wordEnd - wordStart >= 4 && editDistanceAtMostOne(value, wordStart, wordEnd, token)) {
            return true
        }
        if (wordEnd == value.length) break
        wordStart = wordEnd + 1
    }
    return false
}

private fun editDistanceAtMostOne(value: String, start: Int, end: Int, right: String): Boolean {
    val leftLength = end - start
    if (leftLength == right.length && value.regionMatches(start, right, 0, right.length)) return true
    if (kotlin.math.abs(leftLength - right.length) > 1) return false

    var differences = 0
    var leftIndex = start
    var rightIndex = 0
    while (leftIndex < end && rightIndex < right.length) {
        if (value[leftIndex] == right[rightIndex]) {
            leftIndex++
            rightIndex++
        } else {
            differences++
            if (differences > 1) return false
            when {
                leftLength > right.length -> leftIndex++
                right.length > leftLength -> rightIndex++
                else -> {
                    leftIndex++
                    rightIndex++
                }
            }
        }
    }
    return differences + (end - leftIndex) + (right.length - rightIndex) <= 1
}

private val SEARCH_DIACRITICS_REGEX = Regex("\\p{Mn}+")
private val SEARCH_APOSTROPHE_REGEX = Regex("[\\'’`´]")
private val SEARCH_PUNCTUATION_REGEX = Regex("[\\[\\]{}()_,.;:!?\\-_/\\\\|]+")
private val SEARCH_NOISE_REGEX = Regex(
    "\\b(feat|ft|featuring|prod|remaster|remastered|explicit|clean|official audio|official video)\\b",
)
private val SEARCH_WHITESPACE_REGEX = Regex("\\s+")

internal const val MAX_SEARCH_QUERY_CODE_POINTS = 256
internal const val SEARCH_CANCELLATION_CHECK_MASK = 255

private const val TOKEN_MATCHED = 1
private const val TOKEN_TITLE_START = 1 shl 1
private const val TOKEN_ARTIST_START = 1 shl 2
private const val TOKEN_ALBUM_ARTIST_START = 1 shl 3
private const val TOKEN_ALBUM_START = 1 shl 4
private const val TOKEN_TITLE_WORD_START = 1 shl 5
private const val TOKEN_ARTIST_WORD_START = 1 shl 6
private const val TOKEN_ALBUM_ARTIST_WORD_START = 1 shl 7
private const val TOKEN_ALBUM_WORD_START = 1 shl 8
private const val TOKEN_ARTIST_ACRONYM = 1 shl 9
private const val TOKEN_ALBUM_ARTIST_ACRONYM = 1 shl 10
private const val TOKEN_ALBUM_ACRONYM = 1 shl 11
private const val TOKEN_TITLE_FUZZY = 1 shl 12
