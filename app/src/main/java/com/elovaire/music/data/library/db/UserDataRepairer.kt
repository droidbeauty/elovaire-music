package elovaire.music.droidbeauty.app.data.library.db

internal data class UserDataRepairPlan(
    val normalizePlaylistPositions: Boolean = false,
    val normalizeFavoritePositions: Boolean = false,
    val normalizeRecentPositions: Boolean = false,
    val normalizeSongPlayCounts: Boolean = false,
    val normalizeAlbumPlayCounts: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !normalizePlaylistPositions &&
            !normalizeFavoritePositions &&
            !normalizeRecentPositions &&
            !normalizeSongPlayCounts &&
            !normalizeAlbumPlayCounts
}

internal object UserDataRepairer {
    fun plan(
        invalidPlaylistEntries: Boolean,
        invalidFavorites: Boolean,
        invalidRecentPlayback: Boolean,
        invalidSongPlayCounts: Boolean,
        invalidAlbumPlayCounts: Boolean,
    ): UserDataRepairPlan {
        return UserDataRepairPlan(
            normalizePlaylistPositions = invalidPlaylistEntries,
            normalizeFavoritePositions = invalidFavorites,
            normalizeRecentPositions = invalidRecentPlayback,
            normalizeSongPlayCounts = invalidSongPlayCounts,
            normalizeAlbumPlayCounts = invalidAlbumPlayCounts,
        )
    }
}
