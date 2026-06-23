package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.Song

internal fun albumSearchHistoryEntry(album: Album): SearchHistoryEntry {
    return SearchHistoryEntry(
        key = "album:${album.id}",
        kind = SearchHistoryKind.Album,
        title = album.title,
        subtitle = album.artist,
        artUri = album.artUri,
        albumId = album.id,
    )
}

internal fun artistSearchHistoryEntry(song: Song): SearchHistoryEntry {
    val normalizedArtist = normalizeSearchText(song.artist)
    return SearchHistoryEntry(
        key = "artist:$normalizedArtist",
        kind = SearchHistoryKind.Artist,
        title = song.artist.trim(),
        subtitle = song.album,
        artUri = song.artUri,
        query = song.artist.trim(),
    )
}

internal fun sanitizeSearchHistory(
    history: List<SearchHistoryEntry>,
    index: SearchIndex,
): List<SearchHistoryEntry> {
    val sanitized = ArrayList<SearchHistoryEntry>(history.size)
    val seenKeys = HashSet<String>(history.size)
    history.forEach { entry ->
        val sanitizedEntry = when (entry.kind) {
            SearchHistoryKind.Album -> {
                entry.albumId
                    ?.let(index.albumsById::get)
                    ?.let(::albumSearchHistoryEntry)
            }

            SearchHistoryKind.Artist -> {
                val normalizedArtist = normalizeSearchText(entry.query ?: entry.title)
                val artist = index.artistsByNormalizedName[normalizedArtist] ?: return@forEach
                entry.copy(
                    key = "artist:${artist.normalizedName}",
                    title = artist.displayName,
                    artUri = artist.artUri,
                    query = artist.displayName,
                )
            }
        }
        if (sanitizedEntry != null && seenKeys.add(sanitizedEntry.key)) {
            sanitized += sanitizedEntry
        }
    }
    return sanitized
}
