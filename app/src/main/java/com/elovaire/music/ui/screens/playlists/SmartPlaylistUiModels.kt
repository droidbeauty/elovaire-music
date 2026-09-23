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
    return SmartPlaylistEngine.resolveAll(
        definitions = playlists,
        songs = songs,
        favoriteSongIds = favoriteSongIds,
        playCounts = songPlayCounts,
    ).map { result ->
        SmartPlaylistSummary(
            playlist = result.playlist,
            result = result,
        )
    }
}
