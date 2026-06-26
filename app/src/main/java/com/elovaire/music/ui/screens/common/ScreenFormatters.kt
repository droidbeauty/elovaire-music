package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.lazy.LazyListState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlin.math.roundToInt

internal fun recentlyAddedAlbumsFor(
    libraryState: LibraryUiState,
): List<Album> {
    return libraryState.albums
        .sortedByDescending { album ->
            album.songs.maxOfOrNull(Song::dateAddedSeconds) ?: 0L
        }
        .take(4)
}

internal fun recentAlbumsFor(
    libraryState: LibraryUiState,
    playbackState: PlaybackUiState,
): List<Album> {
    val albumsById = libraryState.albums.associateBy { it.id }
    val played = playbackState.recentAlbumIds.mapNotNull(albumsById::get)
    return played.take(6)
}

internal fun favoriteAlbumsFor(
    libraryState: LibraryUiState,
    songPlayCounts: Map<Long, Int>,
    recentAlbums: List<Album>,
    recentlyAddedAlbums: List<Album>,
): List<Album> {
    val rankedByFrequency = libraryState.albums
        .mapNotNull { album ->
            val playCount = album.songs.sumOf { songPlayCounts[it.id] ?: 0 }
            if (playCount > 0) album to playCount else null
        }
        .sortedWith(
            compareByDescending<Pair<Album, Int>> { it.second }
                .thenBy { it.first.artist.lowercase() }
                .thenBy { it.first.title.lowercase() },
        )
        .map { it.first }

    return buildList {
        (rankedByFrequency + recentAlbums + recentlyAddedAlbums).forEach { album ->
            if (none { it.id == album.id }) add(album)
            if (size == 6) return@buildList
        }
    }
}

internal fun suggestedAlbumsFor(
    libraryState: LibraryUiState,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toSet()
    val rarePlayedAlbums = libraryState.albums
        .mapNotNull { album ->
            val playCount = albumPlayCounts[album.id] ?: 0
            if (playCount > 0) album to playCount else null
        }
        .sortedWith(
            compareBy<Pair<Album, Int>> { it.second }
                .thenBy { album -> if (album.first.id in recentAlbumIdSet) 1 else 0 }
                .thenBy { it.first.artist.lowercase() }
                .thenBy { it.first.title.lowercase() },
        )
        .map { it.first }

    val neverPlayedAlbums = libraryState.albums
        .filter { (albumPlayCounts[it.id] ?: 0) == 0 }
        .sortedWith(
            compareBy<Album> { if (it.id in recentAlbumIdSet) 1 else 0 }
                .thenBy { it.artist.lowercase() }
                .thenBy { it.title.lowercase() },
        )

    return buildList {
        (rarePlayedAlbums + neverPlayedAlbums).forEach { album ->
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
