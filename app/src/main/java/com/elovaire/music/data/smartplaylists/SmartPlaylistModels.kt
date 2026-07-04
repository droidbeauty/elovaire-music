package elovaire.music.droidbeauty.app.data.smartplaylists

data class SmartPlaylist(
    val id: Long,
    val name: String,
    val builtInType: BuiltInSmartPlaylistType? = null,
    val matchMode: SmartPlaylistMatchMode = SmartPlaylistMatchMode.All,
    val rules: List<SmartPlaylistRule> = emptyList(),
    val sort: SmartPlaylistSort = SmartPlaylistSort(SmartPlaylistSortField.Title, SortDirection.Ascending),
    val limit: Int? = null,
    val createdAtMs: Long,
    val updatedAtMs: Long,
) {
    val isBuiltIn: Boolean get() = builtInType != null
}

enum class BuiltInSmartPlaylistType {
    RecentlyAdded,
    MostPlayed,
    Favorites,
}

enum class SmartPlaylistMatchMode {
    All,
    Any,
}

data class SmartPlaylistSort(
    val field: SmartPlaylistSortField,
    val direction: SortDirection,
)

enum class SmartPlaylistSortField {
    Title,
    Artist,
    Album,
    Genre,
    Duration,
    DateAdded,
    PlayCount,
    Random,
}

enum class SortDirection {
    Ascending,
    Descending,
}

enum class TextRuleMode {
    Is,
    IsNot,
    Contains,
    DoesNotContain,
}

enum class NumericOperator {
    GreaterThan,
    EqualTo,
    LessThan,
}

sealed interface SmartPlaylistRule {
    data class TitleContains(val query: String, val negate: Boolean = false) : SmartPlaylistRule
    data class ArtistContains(val query: String, val negate: Boolean = false) : SmartPlaylistRule
    data class AlbumContains(val query: String, val negate: Boolean = false) : SmartPlaylistRule
    data class GenreMatches(val query: String, val mode: TextRuleMode) : SmartPlaylistRule
    data class FavoriteIs(val favorite: Boolean) : SmartPlaylistRule
    data class DurationBetween(val minMs: Long? = null, val maxMs: Long? = null) : SmartPlaylistRule
    data class PlayCount(val operator: NumericOperator, val value: Int) : SmartPlaylistRule
    data class FileFormatIs(val extension: String) : SmartPlaylistRule
    data class FolderContains(val query: String) : SmartPlaylistRule
}
