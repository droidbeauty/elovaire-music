package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistResult
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class SmartPlaylistSummary(
    val playlist: SmartPlaylist,
    val result: SmartPlaylistResult,
    val subtitle: String,
)

internal fun buildSmartPlaylistSummaries(
    playlists: List<SmartPlaylist>,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    songPlayCounts: Map<Long, Int>,
    recentSongIds: List<Long>,
): List<SmartPlaylistSummary> {
    val engine = SmartPlaylistEngine()
    return playlists.map { playlist ->
        val result = engine.resolve(
            definition = playlist,
            songs = songs,
            favoriteSongIds = favoriteSongIds,
            playCounts = songPlayCounts,
            recentSongIds = recentSongIds,
        )
        SmartPlaylistSummary(
            playlist = playlist,
            result = result,
            subtitle = "Auto-updating • ${playlist.sort.field.summaryLabel()}",
        )
    }
}

private fun SmartPlaylistSortField.summaryLabel(): String {
    return when (this) {
        SmartPlaylistSortField.Title -> "Sorted by title"
        SmartPlaylistSortField.Artist -> "Sorted by artist"
        SmartPlaylistSortField.Album -> "Sorted by album"
        SmartPlaylistSortField.Genre -> "Sorted by genre"
        SmartPlaylistSortField.Duration -> "Sorted by duration"
        SmartPlaylistSortField.DateAdded -> "Sorted by date added"
        SmartPlaylistSortField.PlayCount -> "Sorted by play count"
        SmartPlaylistSortField.Random -> "Random order"
    }
}
