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
    val body = raw.removePrefix(Prefix)
    return body.takeIf { it.isNotBlank() }
        ?.split(RecordSeparator)
        ?.mapNotNull(::deserializeSmartPlaylist)
        .orEmpty()
}

private fun deserializeSmartPlaylist(entry: String): SmartPlaylist? {
    val parts = entry.split(FieldSeparator)
    if (parts.size < 9) return null
    val id = parts[0].toLongOrNull()?.takeIf { it > 0L } ?: return null
    val name = parts[1].smartDecode()?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val matchMode = parts[2].enumValueOrNull<SmartPlaylistMatchMode>() ?: SmartPlaylistMatchMode.All
    val rules = parts[3].smartDecode()
        ?.takeIf { it.isNotBlank() }
        ?.split(ListSeparator)
        ?.mapNotNull(::deserializeRule)
        .orEmpty()
    val sortField = parts[4].enumValueOrNull<SmartPlaylistSortField>() ?: SmartPlaylistSortField.Title
    val sortDirection = parts[5].enumValueOrNull<SortDirection>() ?: SortDirection.Ascending
    return SmartPlaylist(
        id = id,
        name = name,
        matchMode = matchMode,
        rules = rules,
        sort = SmartPlaylistSort(sortField, sortDirection),
        limit = parts[6].toIntOrNull()?.takeIf { it > 0 },
        createdAtMs = parts[7].toLongOrNull() ?: 0L,
        updatedAtMs = parts[8].toLongOrNull() ?: 0L,
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
        "title" -> SmartPlaylistRule.TitleContains(
            query = parts.getOrNull(1)?.smartDecode().orEmpty(),
            negate = parts.getOrNull(2).toBooleanLenient(),
        )
        "artist" -> SmartPlaylistRule.ArtistContains(
            query = parts.getOrNull(1)?.smartDecode().orEmpty(),
            negate = parts.getOrNull(2).toBooleanLenient(),
        )
        "album" -> SmartPlaylistRule.AlbumContains(
            query = parts.getOrNull(1)?.smartDecode().orEmpty(),
            negate = parts.getOrNull(2).toBooleanLenient(),
        )
        "genre" -> SmartPlaylistRule.GenreMatches(
            query = parts.getOrNull(1)?.smartDecode().orEmpty(),
            mode = parts.getOrNull(2).enumValueOrNull<TextRuleMode>() ?: TextRuleMode.Contains,
        )
        "favorite" -> SmartPlaylistRule.FavoriteIs(parts.getOrNull(1).toBooleanLenient())
        "duration" -> SmartPlaylistRule.DurationBetween(
            minMs = parts.getOrNull(1)?.toLongOrNull(),
            maxMs = parts.getOrNull(2)?.toLongOrNull(),
        )
        "play_count" -> SmartPlaylistRule.PlayCount(
            operator = parts.getOrNull(1).enumValueOrNull<NumericOperator>() ?: NumericOperator.GreaterThan,
            value = parts.getOrNull(2)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
        )
        "format" -> SmartPlaylistRule.FileFormatIs(parts.getOrNull(1)?.smartDecode().orEmpty())
        "folder" -> SmartPlaylistRule.FolderContains(parts.getOrNull(1)?.smartDecode().orEmpty())
        else -> null
    }
}

private fun String.smartEncode(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray(Charsets.UTF_8))
}

private fun String.smartDecode(): String? {
    return runCatching { String(Base64.getUrlDecoder().decode(this), Charsets.UTF_8) }.getOrNull()
}

private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? {
    return this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
}

private fun String?.toBooleanLenient(): Boolean {
    return this == "true"
}

