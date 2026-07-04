package elovaire.music.droidbeauty.app.data.smartplaylists

internal data class SmartPlaylistCreateResult(
    val playlists: List<SmartPlaylist>,
    val createdPlaylist: SmartPlaylist,
    val nextSmartPlaylistId: Long,
)

internal fun createSmartPlaylistEntry(
    playlists: List<SmartPlaylist>,
    name: String,
    nextSmartPlaylistId: Long,
    nowMs: Long,
): SmartPlaylistCreateResult? {
    val normalizedName = name.trim().replace(Regex("\\s+"), " ")
    if (normalizedName.isBlank()) return null
    val existingIds = playlists.mapTo(mutableSetOf()) { it.id }
    var candidate = nextSmartPlaylistId.coerceAtLeast(1L)
    while (candidate in existingIds) {
        candidate = if (candidate == Long.MAX_VALUE) 1L else candidate + 1L
    }
    val created = SmartPlaylistDefaults.newUserPlaylist(candidate, nowMs).copy(name = normalizedName)
    return SmartPlaylistCreateResult(
        playlists = listOf(created) + playlists.filterNot(SmartPlaylist::isBuiltIn),
        createdPlaylist = created,
        nextSmartPlaylistId = if (candidate == Long.MAX_VALUE) 1L else candidate + 1L,
    )
}

internal fun updateSmartPlaylistEntry(
    playlists: List<SmartPlaylist>,
    playlist: SmartPlaylist,
    nowMs: Long,
): List<SmartPlaylist>? {
    if (playlist.id <= 0L || playlist.name.trim().isBlank()) return null
    var changed = false
    val updated = playlists.filterNot(SmartPlaylist::isBuiltIn).map { current ->
        if (current.id == playlist.id) {
            changed = true
            playlist.copy(
                name = playlist.name.trim().replace(Regex("\\s+"), " "),
                builtInType = null,
                updatedAtMs = nowMs,
            )
        } else {
            current
        }
    }
    return updated.takeIf { changed }
}

internal fun deleteSmartPlaylistEntries(
    playlists: List<SmartPlaylist>,
    ids: Set<Long>,
): List<SmartPlaylist>? {
    if (ids.isEmpty()) return null
    val userPlaylists = playlists.filterNot(SmartPlaylist::isBuiltIn)
    val updated = userPlaylists.filterNot { it.id in ids }
    return updated.takeIf { it != userPlaylists }
}

