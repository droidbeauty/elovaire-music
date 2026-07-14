package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class SuggestedAlbumCandidate(
    val searchableAlbum: SearchableAlbum,
    val playCount: Int,
    val isRecent: Boolean,
)

internal fun buildSuggestedAlbums(
    albums: List<SearchableAlbum>,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toHashSet()
    val candidates = albums.map { searchableAlbum ->
        val album = searchableAlbum.album
        SuggestedAlbumCandidate(
            searchableAlbum = searchableAlbum,
            playCount = albumPlayCounts[album.id] ?: 0,
            isRecent = album.id in recentAlbumIdSet,
        )
    }

    val seen = HashSet<Long>(6)
    val output = ArrayList<Album>(6)

    fun addIfNeeded(album: Album): Boolean {
        if (!seen.add(album.id)) return false
        output += album
        return output.size == 6
    }

    candidates
        .asSequence()
        .filter { it.playCount > 0 }
        .sortedWith(
            compareBy<SuggestedAlbumCandidate> { it.playCount }
                .thenBy { if (it.isRecent) 1 else 0 }
                .thenBy { it.searchableAlbum.normalizedArtist }
                .thenBy { it.searchableAlbum.normalizedTitle },
        )
        .map { it.searchableAlbum.album }
        .forEach { album ->
            if (addIfNeeded(album)) return output
        }

    candidates
        .asSequence()
        .filter { it.playCount == 0 }
        .sortedWith(
            compareBy<SuggestedAlbumCandidate> { if (it.isRecent) 1 else 0 }
                .thenBy { it.searchableAlbum.normalizedArtist }
                .thenBy { it.searchableAlbum.normalizedTitle },
        )
        .map { it.searchableAlbum.album }
        .forEach { album ->
            if (addIfNeeded(album)) return output
        }

    return output
}

internal fun List<Song>.playbackSourceLabel(fallbackAlbum: String): String {
    var albumName: String? = null
    forEach { song ->
        val candidate = song.album.takeIf(String::isNotBlank) ?: return@forEach
        if (albumName != null && albumName != candidate) return "Search"
        albumName = candidate
    }
    return albumName ?: fallbackAlbum
}
