package elovaire.music.droidbeauty.app.data.smartplaylists

import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlin.math.absoluteValue

internal data class SmartPlaylistResult(
    val playlist: SmartPlaylist,
    val songs: List<Song>,
    val totalMatchedBeforeLimit: Int,
)

internal object SmartPlaylistEngine {
    fun resolve(
        definition: SmartPlaylist,
        songs: List<Song>,
        favoriteSongIds: Set<Long>,
        playCounts: Map<Long, Int>,
        nowMs: Long = System.currentTimeMillis(),
    ): SmartPlaylistResult {
        val context = ResolutionContext(
            favoriteSongIds = favoriteSongIds,
            playCounts = playCounts,
            nowMs = nowMs,
        )
        val normalized = songs.map { NormalizedSong(it) }
        val matched = normalized
            .asSequence()
            .filter { it.matches(definition, context) }
            .map(NormalizedSong::song)
            .toList()
        val sorted = sort(definition, matched, context)
        val limited = definition.limit?.takeIf { it > 0 }?.let(sorted::take) ?: sorted
        return SmartPlaylistResult(
            playlist = definition,
            songs = limited,
            totalMatchedBeforeLimit = matched.size,
        )
    }

    private fun NormalizedSong.matches(
        definition: SmartPlaylist,
        context: ResolutionContext,
    ): Boolean {
        if (definition.builtInType == BuiltInSmartPlaylistType.RecentlyAdded) {
            val addedMs = song.dateAddedSeconds * 1000L
            if (addedMs > 0L && context.nowMs - addedMs > RecentlyAddedWindowMs) return false
        }
        if (definition.rules.isEmpty()) return true
        return when (definition.matchMode) {
            SmartPlaylistMatchMode.All -> definition.rules.all { rule -> matchesRule(rule, context) }
            SmartPlaylistMatchMode.Any -> definition.rules.any { rule -> matchesRule(rule, context) }
        }
    }

    private fun NormalizedSong.matchesRule(
        rule: SmartPlaylistRule,
        context: ResolutionContext,
    ): Boolean {
        return when (rule) {
            is SmartPlaylistRule.TitleContains -> normalizedTitle.containsNormalized(rule.query, rule.negate)
            is SmartPlaylistRule.ArtistContains -> normalizedArtist.containsNormalized(rule.query, rule.negate)
            is SmartPlaylistRule.AlbumContains -> normalizedAlbum.containsNormalized(rule.query, rule.negate)
            is SmartPlaylistRule.GenreMatches -> normalizedGenre.matchesTextRule(rule.query, rule.mode)
            is SmartPlaylistRule.FavoriteIs -> (song.id in context.favoriteSongIds) == rule.favorite
            is SmartPlaylistRule.DurationBetween -> {
                val min = rule.minMs
                val max = rule.maxMs
                (min == null || song.durationMs > min) && (max == null || song.durationMs < max)
            }
            is SmartPlaylistRule.PlayCount -> {
                val count = context.playCounts[song.id] ?: 0
                when (rule.operator) {
                    NumericOperator.GreaterThan -> count > rule.value
                    NumericOperator.EqualTo -> count == rule.value
                    NumericOperator.LessThan -> count < rule.value
                }
            }
            is SmartPlaylistRule.FileFormatIs -> {
                val expected = rule.extension.trim().trimStart('.').lowercase(Locale.ROOT)
                expected.isNotBlank() && (
                    song.audioFormat.lowercase(Locale.ROOT) == expected ||
                        song.fileName.substringAfterLast('.', "").lowercase(Locale.ROOT) == expected
                    )
            }
            is SmartPlaylistRule.FolderContains -> song.libraryPath.orEmpty().normalizeSmartText()
                .contains(rule.query.normalizeSmartText())
        }
    }

    private fun sort(
        definition: SmartPlaylist,
        songs: List<Song>,
        context: ResolutionContext,
    ): List<Song> {
        if (definition.sort.field == SmartPlaylistSortField.Random) {
            return songs.sortedBy { stableRandomKey(definition.id, it.id) }
        }
        val comparator = when (definition.sort.field) {
            SmartPlaylistSortField.Title -> compareBy<Song> { it.title.normalizeSmartText() }
            SmartPlaylistSortField.Artist -> compareBy { (it.albumArtist ?: it.artist).normalizeSmartText() }
            SmartPlaylistSortField.Album -> compareBy { it.album.normalizeSmartText() }
            SmartPlaylistSortField.Genre -> compareBy { it.genre.normalizeSmartText() }
            SmartPlaylistSortField.Duration -> compareBy { it.durationMs }
            SmartPlaylistSortField.DateAdded -> compareBy { it.dateAddedSeconds }
            SmartPlaylistSortField.PlayCount -> compareBy { context.playCounts[it.id] ?: 0 }
            SmartPlaylistSortField.Random -> compareBy { it.id }
        }.thenBy { it.title.normalizeSmartText() }.thenBy { it.id }
        return if (definition.sort.direction == SortDirection.Descending) {
            songs.sortedWith(comparator.reversed())
        } else {
            songs.sortedWith(comparator)
        }
    }
}

private data class ResolutionContext(
    val favoriteSongIds: Set<Long>,
    val playCounts: Map<Long, Int>,
    val nowMs: Long,
)

private data class NormalizedSong(
    val song: Song,
) {
    val normalizedTitle = song.title.normalizeSmartText()
    val normalizedArtist = (song.albumArtist ?: song.artist).normalizeSmartText()
    val normalizedAlbum = song.album.normalizeSmartText()
    val normalizedGenre = song.genre.normalizeSmartText()
}

private fun String.containsNormalized(
    query: String,
    negate: Boolean,
): Boolean {
    val normalizedQuery = query.normalizeSmartText()
    val matched = normalizedQuery.isBlank() || contains(normalizedQuery)
    return if (negate) !matched else matched
}

private fun String.matchesTextRule(
    query: String,
    mode: TextRuleMode,
): Boolean {
    val normalizedQuery = query.normalizeSmartText()
    if (normalizedQuery.isBlank()) return true
    return when (mode) {
        TextRuleMode.Is -> this == normalizedQuery
        TextRuleMode.IsNot -> this != normalizedQuery
        TextRuleMode.Contains -> contains(normalizedQuery)
        TextRuleMode.DoesNotContain -> !contains(normalizedQuery)
    }
}

internal fun String.normalizeSmartText(): String {
    return trim().lowercase(Locale.ROOT).replace(SmartWhitespaceRegex, " ")
}

private val SmartWhitespaceRegex = Regex("\\s+")

private fun stableRandomKey(
    playlistId: Long,
    songId: Long,
): Long {
    var value = playlistId xor (songId * -7046029254386353131L)
    value = value xor (value ushr 33)
    value *= -4417276706812531889L
    value = value xor (value ushr 29)
    return value.absoluteValue
}
