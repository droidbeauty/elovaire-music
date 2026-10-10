package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.lazy.LazyListState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.PriorityQueue
import kotlin.math.roundToInt

internal fun recentlyAddedAlbumsFor(
    libraryState: LibraryUiState,
    limit: Int? = null,
): List<Album> {
    val cutoffSeconds = (System.currentTimeMillis() / 1_000L) - RECENTLY_ADDED_WINDOW_SECONDS
    if (limit == null || limit >= libraryState.albums.size) {
        return libraryState.albums.mapNotNull { album ->
            val timestampSeconds = album.songs.maxOfOrNull(Song::recentLibraryTimestampSeconds)
                ?: return@mapNotNull null
            if (timestampSeconds > 0L && timestampSeconds >= cutoffSeconds) album to timestampSeconds else null
        }
            .sortedByDescending { (_, timestampSeconds) -> timestampSeconds }
            .map { (album, _) -> album }
    }
    if (limit <= 0) return emptyList()

    val bestAlbums = ArrayList<RecentAlbum>(limit.coerceAtMost(libraryState.albums.size))
    libraryState.albums.forEachIndexed { index, album ->
        val timestampSeconds = album.songs.maxOfOrNull(Song::recentLibraryTimestampSeconds) ?: return@forEachIndexed
        if (timestampSeconds <= 0L || timestampSeconds < cutoffSeconds) return@forEachIndexed
        var insertionIndex = 0
        while (insertionIndex < bestAlbums.size) {
            val existing = bestAlbums[insertionIndex]
            if (timestampSeconds > existing.timestampSeconds ||
                (timestampSeconds == existing.timestampSeconds && index < existing.index)
            ) {
                break
            }
            insertionIndex++
        }
        if (insertionIndex < limit) {
            bestAlbums.add(insertionIndex, RecentAlbum(album, timestampSeconds, index))
            if (bestAlbums.size > limit) bestAlbums.removeAt(limit)
        }
    }
    return bestAlbums.map(RecentAlbum::album)
}

private data class RecentAlbum(
    val album: Album,
    val timestampSeconds: Long,
    val index: Int,
)

private fun Song.recentLibraryTimestampSeconds(): Long {
    return dateAddedSeconds.takeIf { it > 0L } ?: dateModifiedSeconds ?: 0L
}

private const val RECENTLY_ADDED_WINDOW_SECONDS = 14L * 24L * 60L * 60L

internal fun recentAlbumsFor(
    albumsById: Map<Long, Album>,
    playbackState: PlaybackUiState,
): List<Album> {
    return playbackState.recentAlbumIds.asSequence()
        .mapNotNull(albumsById::get)
        .take(HOME_RECENT_ALBUM_LIMIT)
        .toList()
}

internal fun recentSongsFor(
    recentSongIds: List<Long>,
    songsById: Map<Long, Song>,
): List<Song> = recentSongIds.asSequence()
    .mapNotNull(songsById::get)
    .filter { it.mediaKind == AudioMediaKind.Music }
    .take(HOME_RECENT_SONG_LIMIT)
    .toList()

internal fun favoriteAlbumsFor(
    libraryState: LibraryUiState,
    songPlayCounts: Map<Long, Int>,
    recentAlbums: List<Album>,
    recentlyAddedAlbums: List<Album>,
): List<Album> {
    val comparator = compareByDescending<RankedFavoriteAlbum> { it.playCount }
        .thenBy { it.artistKey }
        .thenBy { it.titleKey }
        .thenBy { it.index }
    val bestAlbums = PriorityQueue(HOME_FAVORITE_ALBUM_LIMIT, comparator.reversed())
    val bestAlbumsById = HashMap<Long, RankedFavoriteAlbum>(HOME_FAVORITE_ALBUM_LIMIT)
    libraryState.albums.forEachIndexed { index, album ->
        val playCount = album.songs.sumOf { songPlayCounts[it.id] ?: 0 }
        if (playCount <= 0) return@forEachIndexed
        val existing = bestAlbumsById[album.id]
        val worst = if (existing == null && bestAlbums.size == HOME_FAVORITE_ALBUM_LIMIT) {
            requireNotNull(bestAlbums.peek())
        } else {
            null
        }
        if (existing != null && playCount < existing.playCount) return@forEachIndexed
        if (worst != null && playCount < worst.playCount) return@forEachIndexed
        val candidate = RankedFavoriteAlbum(
            album = album,
            playCount = playCount,
            artistKey = album.artist.lowercase(),
            titleKey = album.title.lowercase(),
            index = index,
        )
        if (existing != null) {
            if (comparator.compare(candidate, existing) >= 0) return@forEachIndexed
            bestAlbums.remove(existing)
        } else if (bestAlbums.size == HOME_FAVORITE_ALBUM_LIMIT) {
            val currentWorst = requireNotNull(bestAlbums.peek())
            if (comparator.compare(candidate, currentWorst) >= 0) return@forEachIndexed
            bestAlbums.poll()
            bestAlbumsById.remove(currentWorst.album.id)
        }
        bestAlbumsById[album.id] = candidate
        bestAlbums += candidate
    }

    val selected = ArrayList<Album>(HOME_FAVORITE_ALBUM_LIMIT)
    val selectedIds = HashSet<Long>(HOME_FAVORITE_ALBUM_LIMIT)
    fun addIfMissing(album: Album) {
        if (selected.size < HOME_FAVORITE_ALBUM_LIMIT && selectedIds.add(album.id)) selected += album
    }
    bestAlbums.toList().sortedWith(comparator).forEach { addIfMissing(it.album) }
    recentAlbums.forEach { addIfMissing(it) }
    recentlyAddedAlbums.forEach { addIfMissing(it) }
    return selected
}

private data class RankedFavoriteAlbum(
    val album: Album,
    val playCount: Int,
    val artistKey: String,
    val titleKey: String,
    val index: Int,
)

private const val HOME_FAVORITE_ALBUM_LIMIT = 6
private const val HOME_RECENT_ALBUM_LIMIT = 6
private const val HOME_RECENT_SONG_LIMIT = 5

internal fun distinctMusicArtistCount(songs: List<Song>): Int {
    val artists = HashSet<String>()
    songs.forEach { song -> artists += song.artist.ifBlank { "Unknown Artist" } }
    return artists.size
}

internal fun distinctMusicGenreCount(songs: List<Song>): Int {
    val genres = HashSet<String>()
    songs.forEach { song -> genres += song.genre.ifBlank { "Unknown Genre" } }
    return genres.size
}

internal data class AlbumCollectionSortEntry(
    val album: Album,
    val artistKey: String = album.artist.lowercase(),
    val titleKey: String = album.title.lowercase(),
)

internal fun albumCollectionSortEntries(albums: List<Album>): List<AlbumCollectionSortEntry> =
    albums.map(::AlbumCollectionSortEntry)

internal fun sortAlbumCollection(
    entries: List<AlbumCollectionSortEntry>,
    sortMode: AlbumSortMode,
): List<Album> = when (sortMode) {
    AlbumSortMode.Artist -> entries.sortedWith(
        compareBy<AlbumCollectionSortEntry> { it.artistKey }.thenBy { it.titleKey },
    )
    AlbumSortMode.Album -> entries.sortedWith(
        compareBy<AlbumCollectionSortEntry> { it.titleKey }.thenBy { it.artistKey },
    )
}.map(AlbumCollectionSortEntry::album)

internal data class SongCollectionSortEntry(
    val song: Song,
    val titleKey: String = song.title.lowercase(),
    val artistKey: String = song.artist.lowercase(),
    val albumKey: String = song.album.lowercase(),
)

internal fun songCollectionSortEntries(songs: List<Song>): List<SongCollectionSortEntry> =
    songs.map(::SongCollectionSortEntry)

internal fun sortSongCollection(
    entries: List<SongCollectionSortEntry>,
    sortMode: SongSortMode,
): List<Song> = when (sortMode) {
    SongSortMode.Title -> entries.sortedWith(
        compareBy<SongCollectionSortEntry> { it.titleKey }.thenBy { it.artistKey }.thenBy { it.albumKey },
    )
    SongSortMode.Artist -> entries.sortedWith(
        compareBy<SongCollectionSortEntry> { it.artistKey }.thenBy { it.titleKey }.thenBy { it.albumKey },
    )
    SongSortMode.Album -> entries.sortedWith(
        compareBy<SongCollectionSortEntry> { it.albumKey }.thenBy { it.titleKey }.thenBy { it.artistKey },
    )
}.map(SongCollectionSortEntry::song)

internal fun artistEntriesFor(songs: List<Song>): List<ArtistEntry> {
    data class ArtistCounts(
        val albumIds: MutableSet<Long> = HashSet(),
        var artUri: android.net.Uri? = null,
        var songCount: Int = 0,
    )

    val countsByArtist = linkedMapOf<String, ArtistCounts>()
    songs.forEach { song ->
        val counts = countsByArtist.getOrPut(song.libraryArtistName(), ::ArtistCounts)
        if (counts.artUri == null) counts.artUri = song.artUri
        counts.albumIds += song.albumId
        counts.songCount++
    }
    return countsByArtist.map { (name, counts) ->
        ArtistEntry(
            name = name,
            artUri = counts.artUri,
            albumCount = counts.albumIds.size,
            songCount = counts.songCount,
        )
    }.sortedBy { it.name.lowercase() }
}

internal fun genreEntriesFor(songs: List<Song>): List<GenreEntry> {
    val albumIdsByGenre = linkedMapOf<String, MutableSet<Long>>()
    songs.forEach { song ->
        val genre = song.genre.ifBlank { "Unknown Genre" }
        albumIdsByGenre.getOrPut(genre, ::HashSet).add(song.albumId)
    }
    return albumIdsByGenre.map { (name, albumIds) ->
        GenreEntry(name = name, albumCount = albumIds.size)
    }.sortedBy { it.name.lowercase() }
}

internal fun suggestedAlbumsFor(
    libraryState: LibraryUiState,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toSet()
    val comparator = compareBy<Album> { album -> if ((albumPlayCounts[album.id] ?: 0) > 0) 0 else 1 }
        .thenBy { album -> albumPlayCounts[album.id] ?: 0 }
        .thenBy { album -> if (album.id in recentAlbumIdSet) 1 else 0 }
        .thenBy { it.artist.lowercase() }
        .thenBy { it.title.lowercase() }
    val sortedAlbums = libraryState.albums
        .filter { (albumPlayCounts[it.id] ?: 0) >= 0 }
        .sortedWith(comparator)

    return buildList {
        sortedAlbums.forEach { album ->
            if (none { it.id == album.id }) add(album)
            if (size == 6) return@buildList
        }
    }
}

internal fun lyricsSeekPositionMs(
    lines: List<LyricsLine>,
    index: Int,
    isSynced: Boolean,
): Long? {
    if (lines.isEmpty() || index !in lines.indices) return null

    if (isSynced) {
        return lines[index].startTimeMs
            ?.coerceAtLeast(0L)
    }

    return null
}

internal suspend fun LazyListState.animateLyricJumpToItem(
    index: Int,
    scrollOffset: Int = 0,
) {
    val distance = kotlin.math.abs(firstVisibleItemIndex - index)
    if (distance > 6) {
        val landingIndex = if (index > firstVisibleItemIndex) {
            (index - 2).coerceAtLeast(0)
        } else {
            (index + 2).coerceAtMost(layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
        scrollToItem(landingIndex, scrollOffset)
    }
    animateScrollToItem(index = index, scrollOffset = scrollOffset)
}

internal fun fractionToDurationPosition(
    fraction: Float,
    durationMs: Long,
): Long {
    if (durationMs <= 0L) return 0L
    return (durationMs * fraction.coerceIn(0f, 1f)).roundToInt().toLong().coerceIn(0L, durationMs)
}

internal fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    return formatTimestamp(durationMs)
}

internal fun formatPlaybackPosition(positionMs: Long): String {
    if (positionMs <= 0L) return "00:00"
    return formatTimestamp(positionMs)
}

private fun formatTimestamp(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        val remainingMinutes = (totalSeconds % 3600) / 60
        "%d:%02d:%02d".format(hours, remainingMinutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
