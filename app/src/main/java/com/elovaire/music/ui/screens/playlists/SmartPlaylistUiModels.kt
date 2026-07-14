package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistResult
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class SmartPlaylistSummary(
    val playlist: SmartPlaylist,
    val result: SmartPlaylistResult,
)

internal fun buildSmartPlaylistSummaries(
    playlists: List<SmartPlaylist>,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    songPlayCounts: Map<Long, Int>,
): List<SmartPlaylistSummary> {
    return playlists.map { playlist ->
        val result = SmartPlaylistEngine.resolve(
            definition = playlist,
            songs = songs,
            favoriteSongIds = favoriteSongIds,
            playCounts = songPlayCounts,
        )
        SmartPlaylistSummary(
            playlist = playlist,
            result = result,
        )
    }
}
