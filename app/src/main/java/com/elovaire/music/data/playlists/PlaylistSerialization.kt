package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import org.json.JSONArray
import org.json.JSONObject

private const val LegacyPlaylistRecordSeparator = "\u001E"
private const val LegacyPlaylistFieldSeparator = "\u001F"

internal fun serializePlaylists(playlists: List<Playlist>): String {
    val array = JSONArray()
    playlists.forEach { playlist ->
        val songIds = JSONArray()
        playlist.songIds.forEach(songIds::put)
        array.put(
            JSONObject()
                .put("id", playlist.id)
                .put("name", playlist.name)
                .put("songIds", songIds)
                .put("isSystem", playlist.isSystem),
        )
    }
    return array.toString()
}

internal fun deserializePlaylists(value: String?): List<Playlist> {
    val rawValue = value?.takeIf { it.isNotBlank() } ?: return emptyList()
    return runCatching { deserializePlaylistsJson(rawValue) }
        .getOrElse { deserializePlaylistsLegacy(rawValue) }
}

private fun deserializePlaylistsJson(value: String): List<Playlist> {
    val array = JSONArray(value)
    return buildList(array.length()) {
        for (index in 0 until array.length()) {
            val playlist = array.optJSONObject(index)?.toPlaylistOrNull() ?: continue
            add(playlist)
        }
    }
}

private fun deserializePlaylistsLegacy(value: String): List<Playlist> {
    return value.split(LegacyPlaylistRecordSeparator)
        .mapNotNull { entry -> entry.toLegacyPlaylistOrNull() }
}

private fun JSONObject.toPlaylistOrNull(): Playlist? {
    val id = optLong("id", -1L)
    val normalizedName = normalizePlaylistName(optString("name"))
    if (id <= 0L || normalizedName.isBlank()) return null
    val rawSongIds = optJSONArray("songIds")
        ?.let { array ->
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    add(array.optLong(index, -1L))
                }
            }
        }
        .orEmpty()
    return Playlist(
        id = id,
        name = normalizedName,
        songIds = normalizePlaylistSongIds(rawSongIds),
        isSystem = optBoolean("isSystem", false),
    )
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
                ?.mapNotNull { it.toLongOrNull() }
                .orEmpty(),
        ),
        isSystem = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
    )
}
