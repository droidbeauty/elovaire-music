package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import java.util.Base64

private const val PlaylistSchemaV2Prefix = "v2:"
private const val PlaylistRecordSeparator = "\u001E"
private const val PlaylistFieldSeparator = "\u001F"
private const val LegacyPlaylistRecordSeparator = PlaylistRecordSeparator
private const val LegacyPlaylistFieldSeparator = PlaylistFieldSeparator

internal fun deserializePlaylists(value: String?): List<Playlist> {
    val rawValue = value?.takeIf { it.isNotBlank() } ?: return emptyList()
    if (rawValue.length > MAX_PLAYLIST_SERIALIZED_CHARS) return emptyList()
    return when {
        rawValue.startsWith(PlaylistSchemaV2Prefix) -> deserializePlaylistsV2(rawValue.removePrefix(PlaylistSchemaV2Prefix))
        VERSIONED_PREFIX_MARKER.containsMatchIn(rawValue) -> emptyList()
        else -> deserializePlaylistsLegacy(rawValue)
    }
}

internal fun serializePlaylists(playlists: List<Playlist>): String {
    return PlaylistSchemaV2Prefix + playlists
        .filterNot(Playlist::isSystem)
        .joinToString(PlaylistRecordSeparator) { playlist ->
            listOf(
                playlist.id.toString(),
                playlist.name.encodePlaylistField(),
                normalizePlaylistSongIds(playlist.songIds).joinToString(","),
                false.toString(),
            ).joinToString(PlaylistFieldSeparator)
        }
}

private fun deserializePlaylistsV2(value: String): List<Playlist> {
    return value.split(PlaylistRecordSeparator, limit = MAX_PLAYLIST_COUNT + 1)
        .takeIf { it.size <= MAX_PLAYLIST_COUNT }
        ?.mapNotNull { entry ->
            val parts = entry.split(PlaylistFieldSeparator)
            if (parts.size < 4) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val normalizedName = parts[1].decodePlaylistField()?.let(::normalizePlaylistName).orEmpty()
            if (
                id <= 0L ||
                normalizedName.isBlank() ||
                normalizedName.length > MAX_PLAYLIST_NAME_CHARS
            ) return@mapNotNull null
            val encodedSongIds = parts[2]
            val songIds = if (encodedSongIds.isBlank()) {
                emptyList()
            } else {
                encodedSongIds.split(",", limit = MAX_PLAYLIST_SONG_COUNT + 1)
                    .takeIf { it.size <= MAX_PLAYLIST_SONG_COUNT }
                    ?.mapNotNull(String::toLongOrNull)
                    ?: return@mapNotNull null
            }
            Playlist(
                id = id,
                name = normalizedName,
                songIds = normalizePlaylistSongIds(songIds),
                isSystem = parts[3].toBooleanStrictOrNull() ?: false,
            )
        }
        ?.deduplicatePlaylistIds()
        .orEmpty()
}

private fun deserializePlaylistsLegacy(value: String): List<Playlist> {
    return value.split(LegacyPlaylistRecordSeparator, limit = MAX_PLAYLIST_COUNT + 1)
        .takeIf { it.size <= MAX_PLAYLIST_COUNT }
        ?.mapNotNull { entry -> entry.toLegacyPlaylistOrNull() }
        ?.deduplicatePlaylistIds()
        .orEmpty()
}

private fun List<Playlist>.deduplicatePlaylistIds(): List<Playlist> {
    val seen = HashSet<Long>(size)
    return filter { seen.add(it.id) }
}

private fun String.toLegacyPlaylistOrNull(): Playlist? {
    val parts = split(LegacyPlaylistFieldSeparator)
    if (parts.size < 3) return null
    val id = parts[0].toLongOrNull() ?: return null
    val normalizedName = normalizePlaylistName(parts[1])
    if (id <= 0L || normalizedName.isBlank() || normalizedName.length > MAX_PLAYLIST_NAME_CHARS) return null
    val encodedSongIds = parts[2]
    val songIds = if (encodedSongIds.isBlank()) {
        emptyList()
    } else {
        encodedSongIds.split(",", limit = MAX_PLAYLIST_SONG_COUNT + 1)
            .takeIf { it.size <= MAX_PLAYLIST_SONG_COUNT }
            ?.mapNotNull(String::toLongOrNull)
            ?: return null
    }
    return Playlist(
        id = id,
        name = normalizedName,
        songIds = normalizePlaylistSongIds(songIds),
        isSystem = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
    )
}

private fun String.decodePlaylistField(): String? {
    if (length > MAX_PLAYLIST_ENCODED_NAME_CHARS) return null
    return runCatching {
        String(Base64.getUrlDecoder().decode(this), Charsets.UTF_8)
            .takeIf { it.length <= MAX_PLAYLIST_NAME_CHARS }
    }.getOrNull()
}

private fun String.encodePlaylistField(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray(Charsets.UTF_8))
}

private const val MAX_PLAYLIST_SERIALIZED_CHARS = 4 * 1024 * 1024
private const val MAX_PLAYLIST_COUNT = 2_048
private const val MAX_PLAYLIST_SONG_COUNT = 100_000
private const val MAX_PLAYLIST_NAME_CHARS = 4_096
private const val MAX_PLAYLIST_ENCODED_NAME_CHARS = 16 * 1024
private val VERSIONED_PREFIX_MARKER = Regex("(?i)^v\\d+:")
