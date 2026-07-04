package elovaire.music.droidbeauty.app.data.smartplaylists

private const val DayMs = 24L * 60L * 60L * 1000L

internal object SmartPlaylistDefaults {
    fun builtIns(nowMs: Long = System.currentTimeMillis()): List<SmartPlaylist> {
        return listOf(
            SmartPlaylist(
                id = -1L,
                name = "Recently Added",
                builtInType = BuiltInSmartPlaylistType.RecentlyAdded,
                rules = listOf(SmartPlaylistRule.DurationBetween(minMs = 1L)),
                sort = SmartPlaylistSort(SmartPlaylistSortField.DateAdded, SortDirection.Descending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
            SmartPlaylist(
                id = -2L,
                name = "Recently Played",
                builtInType = BuiltInSmartPlaylistType.RecentlyPlayed,
                sort = SmartPlaylistSort(SmartPlaylistSortField.RecentlyPlayed, SortDirection.Ascending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
            SmartPlaylist(
                id = -3L,
                name = "Most Played",
                builtInType = BuiltInSmartPlaylistType.MostPlayed,
                rules = listOf(SmartPlaylistRule.PlayCount(NumericOperator.GreaterThan, 0)),
                sort = SmartPlaylistSort(SmartPlaylistSortField.PlayCount, SortDirection.Descending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
            SmartPlaylist(
                id = -4L,
                name = "Never Played",
                builtInType = BuiltInSmartPlaylistType.NeverPlayed,
                rules = listOf(SmartPlaylistRule.PlayCount(NumericOperator.EqualTo, 0)),
                sort = SmartPlaylistSort(SmartPlaylistSortField.Title, SortDirection.Ascending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
            SmartPlaylist(
                id = -5L,
                name = "Favorites",
                builtInType = BuiltInSmartPlaylistType.Favorites,
                rules = listOf(SmartPlaylistRule.FavoriteIs(true)),
                sort = SmartPlaylistSort(SmartPlaylistSortField.Title, SortDirection.Ascending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
            SmartPlaylist(
                id = -6L,
                name = "Long Songs",
                builtInType = BuiltInSmartPlaylistType.LongSongs,
                rules = listOf(SmartPlaylistRule.DurationBetween(minMs = 7L * 60L * 1000L)),
                sort = SmartPlaylistSort(SmartPlaylistSortField.Duration, SortDirection.Descending),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            ),
        )
    }

    fun newUserPlaylist(
        id: Long,
        nowMs: Long,
    ): SmartPlaylist {
        return SmartPlaylist(
            id = id,
            name = "Smart playlist",
            matchMode = SmartPlaylistMatchMode.All,
            rules = emptyList(),
            sort = SmartPlaylistSort(SmartPlaylistSortField.Title, SortDirection.Ascending),
            limit = null,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        )
    }
}

internal val RecentlyAddedWindowMs: Long = 30L * DayMs

