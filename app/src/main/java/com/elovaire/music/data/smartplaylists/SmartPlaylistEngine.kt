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
        val preparedDefinition = PreparedSmartPlaylist(definition)
        val normalized = songs.map { NormalizedSong(it) }
        val matched = normalized
            .asSequence()
            .filter { it.matches(preparedDefinition, context) }
            .toList()
        val sorted = sort(definition, matched, context)
        val limited = definition.limit?.takeIf { it > 0 }?.let(sorted::take) ?: sorted
        return SmartPlaylistResult(
            playlist = definition,
            songs = limited.map(NormalizedSong::song),
            totalMatchedBeforeLimit = matched.size,
        )
    }

    private fun NormalizedSong.matches(
        definition: PreparedSmartPlaylist,
        context: ResolutionContext,
    ): Boolean {
        if (definition.source.builtInType == BuiltInSmartPlaylistType.RecentlyAdded) {
            val addedMs = song.dateAddedSeconds * 1000L
            if (addedMs > 0L && context.nowMs - addedMs > RecentlyAddedWindowMs) return false
        }
        if (definition.rules.isEmpty()) return true
        return when (definition.source.matchMode) {
            SmartPlaylistMatchMode.All -> definition.rules.all { rule -> matchesRule(rule, context) }
            SmartPlaylistMatchMode.Any -> definition.rules.any { rule -> matchesRule(rule, context) }
        }
    }

    private fun NormalizedSong.matchesRule(
        preparedRule: PreparedSmartPlaylistRule,
        context: ResolutionContext,
    ): Boolean {
        return when (val rule = preparedRule.source) {
            is SmartPlaylistRule.TitleContains -> normalizedTitle.containsNormalized(preparedRule.normalizedText, rule.negate)
            is SmartPlaylistRule.ArtistContains -> normalizedArtist.containsNormalized(preparedRule.normalizedText, rule.negate)
            is SmartPlaylistRule.AlbumContains -> normalizedAlbum.containsNormalized(preparedRule.normalizedText, rule.negate)
            is SmartPlaylistRule.GenreMatches -> normalizedGenre.matchesTextRule(preparedRule.normalizedText, rule.mode)
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
                val expected = preparedRule.normalizedText
                expected.isNotBlank() && (
                    song.audioFormat.lowercase(Locale.ROOT) == expected ||
                        song.fileName.substringAfterLast('.', "").lowercase(Locale.ROOT) == expected
                )
            }
            is SmartPlaylistRule.FolderContains -> song.libraryPath.orEmpty().normalizeSmartText()
                .contains(preparedRule.normalizedText)
        }
    }

    private fun sort(
        definition: SmartPlaylist,
        songs: List<NormalizedSong>,
        context: ResolutionContext,
    ): List<NormalizedSong> {
        if (definition.sort.field == SmartPlaylistSortField.Random) {
            return songs.sortedBy { stableRandomKey(definition.id, it.song.id) }
        }
        val comparator = when (definition.sort.field) {
            SmartPlaylistSortField.Title -> compareBy<NormalizedSong> { it.normalizedTitle }
            SmartPlaylistSortField.Artist -> compareBy { it.normalizedArtist }
            SmartPlaylistSortField.Album -> compareBy { it.normalizedAlbum }
            SmartPlaylistSortField.Genre -> compareBy { it.normalizedGenre }
            SmartPlaylistSortField.Duration -> compareBy { it.song.durationMs }
            SmartPlaylistSortField.DateAdded -> compareBy { it.song.dateAddedSeconds }
            SmartPlaylistSortField.PlayCount -> compareBy { context.playCounts[it.song.id] ?: 0 }
            SmartPlaylistSortField.Random -> compareBy { it.song.id }
        }.thenBy { it.normalizedTitle }.thenBy { it.song.id }
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

private data class PreparedSmartPlaylist(
    val source: SmartPlaylist,
    val rules: List<PreparedSmartPlaylistRule> = source.rules.map(::PreparedSmartPlaylistRule),
)

private data class PreparedSmartPlaylistRule(
    val source: SmartPlaylistRule,
) {
    val normalizedText: String = when (val rule = source) {
        is SmartPlaylistRule.TitleContains -> rule.query.normalizeSmartText()
        is SmartPlaylistRule.ArtistContains -> rule.query.normalizeSmartText()
        is SmartPlaylistRule.AlbumContains -> rule.query.normalizeSmartText()
        is SmartPlaylistRule.GenreMatches -> rule.query.normalizeSmartText()
        is SmartPlaylistRule.FileFormatIs -> rule.extension.trim().trimStart('.').lowercase(Locale.ROOT)
        is SmartPlaylistRule.FolderContains -> rule.query.normalizeSmartText()
        is SmartPlaylistRule.FavoriteIs,
        is SmartPlaylistRule.DurationBetween,
        is SmartPlaylistRule.PlayCount,
        -> ""
    }
}

private fun String.containsNormalized(
    normalizedQuery: String,
    negate: Boolean,
): Boolean {
    val matched = normalizedQuery.isBlank() || contains(normalizedQuery)
    return if (negate) !matched else matched
}

private fun String.matchesTextRule(
    normalizedQuery: String,
    mode: TextRuleMode,
): Boolean {
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
