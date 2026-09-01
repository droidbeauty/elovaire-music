package elovaire.music.droidbeauty.app.data.smartplaylists

import java.util.Base64

private const val Prefix = "v1:"
private const val RecordSeparator = "\u001E"
private const val FieldSeparator = "\u001F"
private const val ListSeparator = "\u001D"

internal fun serializeSmartPlaylists(playlists: List<SmartPlaylist>): String {
    return Prefix + playlists.filterNot(SmartPlaylist::isBuiltIn).joinToString(RecordSeparator) { playlist ->
        listOf(
            playlist.id.toString(),
            playlist.name.smartEncode(),
            playlist.matchMode.name,
            playlist.rules.joinToString(ListSeparator) { it.serializeRule() }.smartEncode(),
            playlist.sort.field.name,
            playlist.sort.direction.name,
            playlist.limit?.toString().orEmpty(),
            playlist.createdAtMs.toString(),
            playlist.updatedAtMs.toString(),
        ).joinToString(FieldSeparator)
    }
}

internal fun deserializeSmartPlaylists(value: String?): List<SmartPlaylist> {
    val raw = value?.takeIf { it.isNotBlank() } ?: return emptyList()
    if (raw.length > MAX_SMART_PLAYLIST_SERIALIZED_CHARS) return emptyList()
    val versioned = raw.startsWith(Prefix)
    if (!versioned && VERSIONED_PREFIX_MARKER.containsMatchIn(raw)) return emptyList()
    val body = if (versioned) raw.removePrefix(Prefix) else raw
    return body.takeIf { it.isNotBlank() }
        ?.split(RecordSeparator, limit = MAX_SMART_PLAYLIST_COUNT + 1)
        ?.takeIf { it.size <= MAX_SMART_PLAYLIST_COUNT }
        ?.mapNotNull { deserializeSmartPlaylist(it, allowLegacySort = !versioned) }
        .orEmpty()
}

private fun deserializeSmartPlaylist(entry: String, allowLegacySort: Boolean): SmartPlaylist? {
    val parts = entry.split(FieldSeparator)
    if (parts.size != 9) return null
    val id = parts[0].toLongOrNull()?.takeIf { it > 0L } ?: return null
    val name = parts[1].smartDecode()?.trim()?.takeIf {
        it.isNotBlank() && it.length <= MAX_SMART_PLAYLIST_NAME_CHARS
    } ?: return null
    val matchMode = parts[2].enumValueOrNull<SmartPlaylistMatchMode>() ?: return null
    val encodedRules = parts[3].smartDecode() ?: return null
    val rules = if (encodedRules.isBlank()) {
        emptyList()
    } else {
        encodedRules.split(ListSeparator, limit = MAX_SMART_PLAYLIST_RULE_COUNT + 1)
            .takeIf { it.size <= MAX_SMART_PLAYLIST_RULE_COUNT }
            ?.map { deserializeRule(it) ?: return null }
            ?: return null
    }
    val sortField = (
        parts[4].enumValueOrNull<SmartPlaylistSortField>()
            ?: if (allowLegacySort) legacySortField(parts[4]) else null
        ) ?: return null
    val sortDirection = parts[5].enumValueOrNull<SortDirection>() ?: return null
    val limitText = parts[6]
    val limit = when {
        limitText.isBlank() -> null
        else -> limitText.toIntOrNull()?.takeIf { it in 1..MAX_SMART_PLAYLIST_RESULT_LIMIT }
            ?: return null
    }
    return SmartPlaylist(
        id = id,
        name = name,
        matchMode = matchMode,
        rules = rules,
        sort = SmartPlaylistSort(sortField, sortDirection),
        limit = limit,
        createdAtMs = parts[7].toLongOrNull()?.takeIf { it >= 0L } ?: return null,
        updatedAtMs = parts[8].toLongOrNull()?.takeIf { it >= 0L } ?: return null,
    )
}

private fun SmartPlaylistRule.serializeRule(): String {
    return when (this) {
        is SmartPlaylistRule.TitleContains -> listOf("title", query.smartEncode(), negate.toString())
        is SmartPlaylistRule.ArtistContains -> listOf("artist", query.smartEncode(), negate.toString())
        is SmartPlaylistRule.AlbumContains -> listOf("album", query.smartEncode(), negate.toString())
        is SmartPlaylistRule.GenreMatches -> listOf("genre", query.smartEncode(), mode.name)
        is SmartPlaylistRule.FavoriteIs -> listOf("favorite", favorite.toString())
        is SmartPlaylistRule.DurationBetween -> listOf("duration", minMs?.toString().orEmpty(), maxMs?.toString().orEmpty())
        is SmartPlaylistRule.PlayCount -> listOf("play_count", operator.name, value.toString())
        is SmartPlaylistRule.FileFormatIs -> listOf("format", extension.smartEncode())
        is SmartPlaylistRule.FolderContains -> listOf("folder", query.smartEncode())
    }.joinToString(":")
}

private fun deserializeRule(value: String): SmartPlaylistRule? {
    val parts = value.split(":")
    return when (parts.firstOrNull()) {
        "title" -> deserializeContainsRule(parts) { query, negate ->
            SmartPlaylistRule.TitleContains(query, negate)
        }
        "artist" -> deserializeContainsRule(parts) { query, negate ->
            SmartPlaylistRule.ArtistContains(query, negate)
        }
        "album" -> deserializeContainsRule(parts) { query, negate ->
            SmartPlaylistRule.AlbumContains(query, negate)
        }
        "genre" -> deserializeGenreRule(parts)
        "favorite" -> deserializeFavoriteRule(parts)
        "duration" -> deserializeDurationRule(parts)
        "play_count" -> deserializePlayCountRule(parts)
        "format" -> deserializeSingleTextRule(parts) { value ->
            SmartPlaylistRule.FileFormatIs(value)
        }
        "folder" -> deserializeSingleTextRule(parts) { value ->
            SmartPlaylistRule.FolderContains(value)
        }
        else -> null
    }
}

private fun deserializeContainsRule(
    parts: List<String>,
    create: (query: String, negate: Boolean) -> SmartPlaylistRule,
): SmartPlaylistRule? {
    if (parts.size != 3) return null
    val query = parts[1].smartDecode()?.takeIf(String::isNotBlank) ?: return null
    val negate = parts[2].toBooleanStrictOrNull() ?: return null
    return create(query, negate)
}

private fun deserializeGenreRule(parts: List<String>): SmartPlaylistRule? {
    if (parts.size != 3) return null
    val query = parts[1].smartDecode()?.takeIf(String::isNotBlank) ?: return null
    val mode = parts[2].enumValueOrNull<TextRuleMode>() ?: return null
    return SmartPlaylistRule.GenreMatches(query, mode)
}

private fun deserializeFavoriteRule(parts: List<String>): SmartPlaylistRule? {
    if (parts.size != 2) return null
    val favorite = parts[1].toBooleanStrictOrNull() ?: return null
    return SmartPlaylistRule.FavoriteIs(favorite)
}

private fun deserializeDurationRule(parts: List<String>): SmartPlaylistRule? {
    if (parts.size != 3) return null
    val minText = parts[1]
    val maxText = parts[2]
    val min = minText.toNonNegativeLongOrNull() ?: if (minText.isBlank()) null else return null
    val max = maxText.toNonNegativeLongOrNull() ?: if (maxText.isBlank()) null else return null
    if (min != null && max != null && min > max) return null
    return SmartPlaylistRule.DurationBetween(min, max)
}

private fun deserializePlayCountRule(parts: List<String>): SmartPlaylistRule? {
    if (parts.size != 3) return null
    val operator = parts[1].enumValueOrNull<NumericOperator>() ?: return null
    val value = parts[2].toIntOrNull()?.takeIf { it in 0..MAX_SMART_PLAYLIST_PLAY_COUNT } ?: return null
    return SmartPlaylistRule.PlayCount(operator, value)
}

private fun deserializeSingleTextRule(
    parts: List<String>,
    create: (value: String) -> SmartPlaylistRule,
): SmartPlaylistRule? {
    if (parts.size != 2) return null
    val value = parts[1].smartDecode()?.trim()?.takeIf(String::isNotBlank) ?: return null
    return create(value)
}

private fun String.smartEncode(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray(Charsets.UTF_8))
}

private fun String.smartDecode(): String? {
    if (length > MAX_SMART_PLAYLIST_ENCODED_FIELD_CHARS) return null
    return runCatching {
        String(Base64.getUrlDecoder().decode(this), Charsets.UTF_8)
            .takeIf { it.length <= MAX_SMART_PLAYLIST_TEXT_CHARS }
    }.getOrNull()
}

private fun String.toNonNegativeLongOrNull(): Long? = toLongOrNull()?.takeIf { it >= 0L }

private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? {
    return this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
}

private fun legacySortField(value: String?): SmartPlaylistSortField? {
    return when (value) {
        "RecentlyPlayed" -> SmartPlaylistSortField.PlayCount
        else -> null
    }
}

private const val MAX_SMART_PLAYLIST_SERIALIZED_CHARS = 4 * 1024 * 1024
private const val MAX_SMART_PLAYLIST_COUNT = 2_048
private const val MAX_SMART_PLAYLIST_NAME_CHARS = 512
private const val MAX_SMART_PLAYLIST_RULE_COUNT = 64
private const val MAX_SMART_PLAYLIST_TEXT_CHARS = 4_096
private const val MAX_SMART_PLAYLIST_ENCODED_FIELD_CHARS = 16 * 1024
private const val MAX_SMART_PLAYLIST_RESULT_LIMIT = 100_000
private const val MAX_SMART_PLAYLIST_PLAY_COUNT = 1_000_000_000
private val VERSIONED_PREFIX_MARKER = Regex("(?i)^v\\d+:")
