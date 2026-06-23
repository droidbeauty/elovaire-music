package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import java.util.Base64

private const val PlaylistSchemaV2Prefix = "v2:"
private const val PlaylistRecordSeparator = "\u001E"
private const val PlaylistFieldSeparator = "\u001F"
private const val LegacyPlaylistRecordSeparator = PlaylistRecordSeparator
private const val LegacyPlaylistFieldSeparator = PlaylistFieldSeparator

internal fun serializePlaylists(playlists: List<Playlist>): String {
    return buildString {
        append(PlaylistSchemaV2Prefix)
        playlists.forEachIndexed { index, playlist ->
            if (index > 0) append(PlaylistRecordSeparator)
            append(playlist.id)
            append(PlaylistFieldSeparator)
            append(normalizePlaylistName(playlist.name).encodePlaylistField())
            append(PlaylistFieldSeparator)
            append(normalizePlaylistSongIds(playlist.songIds).joinToString(","))
            append(PlaylistFieldSeparator)
            append(playlist.isSystem)
        }
    }
}

internal fun deserializePlaylists(value: String?): List<Playlist> {
    val rawValue = value?.takeIf { it.isNotBlank() } ?: return emptyList()
    return if (rawValue.startsWith(PlaylistSchemaV2Prefix)) {
        deserializePlaylistsV2(rawValue.removePrefix(PlaylistSchemaV2Prefix))
    } else {
        deserializePlaylistsLegacy(rawValue)
    }
}

private fun deserializePlaylistsV2(value: String): List<Playlist> {
    return value.split(PlaylistRecordSeparator)
        .mapNotNull { entry ->
            val parts = entry.split(PlaylistFieldSeparator)
            if (parts.size < 4) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val normalizedName = parts[1].decodePlaylistField()?.let(::normalizePlaylistName).orEmpty()
            if (id <= 0L || normalizedName.isBlank()) return@mapNotNull null
            Playlist(
                id = id,
                name = normalizedName,
                songIds = normalizePlaylistSongIds(
                    parts[2]
                        .takeIf { it.isNotBlank() }
                        ?.split(",")
                        ?.mapNotNull(String::toLongOrNull)
                        .orEmpty(),
                ),
                isSystem = parts[3].toBooleanStrictOrNull() ?: false,
            )
        }
}

private fun deserializePlaylistsLegacy(value: String): List<Playlist> {
    return value.split(LegacyPlaylistRecordSeparator)
        .mapNotNull { entry -> entry.toLegacyPlaylistOrNull() }
}

private fun String.toLegacyPlaylistOrNull(): Playlist? {
    val parts = split(LegacyPlaylistFieldSeparator)
    if (parts.size < 3) return null
    val id = parts[0].toLongOrNull() ?: return null
    val normalizedName = normalizePlaylistName(parts[1])
    if (normalizedName.isBlank()) return null
    return Playlist(
        id = id,
        name = normalizedName,
        songIds = normalizePlaylistSongIds(
            parts[2]
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull(String::toLongOrNull)
                .orEmpty(),
        ),
        isSystem = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
    )
}

private fun String.encodePlaylistField(): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(toByteArray(Charsets.UTF_8))
}

private fun String.decodePlaylistField(): String? {
    return runCatching {
        String(Base64.getUrlDecoder().decode(this), Charsets.UTF_8)
    }.getOrNull()
}
