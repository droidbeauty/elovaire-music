package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist

private val PlaylistWhitespaceRegex = Regex("\\s+")

internal data class PlaylistCreateResult(
    val playlists: List<Playlist>,
    val createdPlaylist: Playlist,
    val nextPlaylistId: Long,
)

internal fun normalizePlaylistName(name: String): String {
    return name.trim().replace(PlaylistWhitespaceRegex, " ")
}

internal fun normalizePlaylistSongIds(songIds: List<Long>): List<Long> {
    val seen = LinkedHashSet<Long>(songIds.size)
    songIds.forEach { id ->
        if (id != 0L) {
            seen += id
        }
    }
    return seen.toList()
}

internal fun generatePlaylistId(
    existingIds: Set<Long>,
    initialCandidate: Long,
): Long {
    var candidate = initialCandidate.coerceAtLeast(1L)
    while (candidate in existingIds || candidate <= 0L) {
        candidate = if (candidate == Long.MAX_VALUE) 1L else candidate + 1L
    }
    return candidate
}

internal fun createPlaylistEntries(
    playlists: List<Playlist>,
    name: String,
    nextPlaylistId: Long,
): PlaylistCreateResult? {
    val normalizedName = normalizePlaylistName(name)
    if (normalizedName.isBlank()) return null
    val createdPlaylist = Playlist(
        id = generatePlaylistId(playlists.asSequence().mapTo(mutableSetOf()) { it.id }, nextPlaylistId),
        name = normalizedName,
    )
    return PlaylistCreateResult(
        playlists = listOf(createdPlaylist) + playlists,
        createdPlaylist = createdPlaylist,
        nextPlaylistId = if (createdPlaylist.id == Long.MAX_VALUE) 1L else createdPlaylist.id + 1L,
    )
}

internal fun addSongsToPlaylistEntries(
    playlists: List<Playlist>,
    playlistId: Long,
    songIds: List<Long>,
): List<Playlist>? {
    val normalizedSongIds = normalizePlaylistSongIds(songIds)
    if (normalizedSongIds.isEmpty()) return null
    return playlists.replacePlaylist(playlistId) { playlist ->
        if (playlist.isSystem) {
            playlist
        } else {
            val updatedSongIds = normalizePlaylistSongIds(playlist.songIds + normalizedSongIds)
            if (updatedSongIds == playlist.songIds) playlist else playlist.copy(songIds = updatedSongIds)
        }
    }
}

internal fun renamePlaylistEntry(
    playlists: List<Playlist>,
    playlistId: Long,
    name: String,
): List<Playlist>? {
    val normalizedName = normalizePlaylistName(name)
    if (normalizedName.isBlank()) return null
    return playlists.replacePlaylist(playlistId) { playlist ->
        if (playlist.isSystem || playlist.name == normalizedName) {
            playlist
        } else {
            playlist.copy(name = normalizedName)
        }
    }
}

internal fun updatePlaylistSongIdsEntry(
    playlists: List<Playlist>,
    playlistId: Long,
    songIds: List<Long>,
): List<Playlist>? {
    val normalizedIds = normalizePlaylistSongIds(songIds)
    return playlists.replacePlaylist(playlistId) { playlist ->
        if (playlist.isSystem || playlist.songIds == normalizedIds) {
            playlist
        } else {
            playlist.copy(songIds = normalizedIds)
        }
    }
}

internal fun deletePlaylistEntries(
    playlists: List<Playlist>,
    playlistIds: Set<Long>,
): List<Playlist>? {
    if (playlistIds.isEmpty()) return null
    val updated = playlists.filterNot { playlist ->
        playlist.id in playlistIds && !playlist.isSystem
    }
    return updated.takeIf { it != playlists }
}

internal fun removeSongReferencesFromPlaylists(
    playlists: List<Playlist>,
    songId: Long,
): List<Playlist>? {
    if (songId == 0L) return null
    var changed = false
    val updated = playlists.map { playlist ->
        val filteredSongIds = playlist.songIds.filterNot { it == songId }
        if (filteredSongIds == playlist.songIds) {
            playlist
        } else {
            changed = true
            playlist.copy(songIds = filteredSongIds)
        }
    }
    return updated.takeIf { changed }
}

private inline fun List<Playlist>.replacePlaylist(
    playlistId: Long,
    transform: (Playlist) -> Playlist,
): List<Playlist>? {
    var found = false
    var changed = false
    val updated = map { playlist ->
        if (playlist.id != playlistId) {
            playlist
        } else {
            found = true
            transform(playlist).also { changed = changed || it != playlist }
        }
    }
    return updated.takeIf { found && changed }
}
