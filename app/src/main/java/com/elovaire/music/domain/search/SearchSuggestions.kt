package elovaire.music.droidbeauty.app.domain.search

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class SuggestedAlbumCandidate(
    val album: Album,
    val playCount: Int,
    val isRecent: Boolean,
    val normalizedArtist: String,
    val normalizedTitle: String,
)

internal fun buildSuggestedAlbums(
    albums: List<Album>,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toHashSet()
    val candidates = albums.map { album ->
        SuggestedAlbumCandidate(
            album = album,
            playCount = albumPlayCounts[album.id] ?: 0,
            isRecent = album.id in recentAlbumIdSet,
            normalizedArtist = normalizeSearchText(album.artist),
            normalizedTitle = normalizeSearchText(album.title),
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
                .thenBy(SuggestedAlbumCandidate::normalizedArtist)
                .thenBy(SuggestedAlbumCandidate::normalizedTitle),
        )
        .map(SuggestedAlbumCandidate::album)
        .forEach { album ->
            if (addIfNeeded(album)) return output
        }

    candidates
        .asSequence()
        .filter { it.playCount == 0 }
        .sortedWith(
            compareBy<SuggestedAlbumCandidate> { if (it.isRecent) 1 else 0 }
                .thenBy(SuggestedAlbumCandidate::normalizedArtist)
                .thenBy(SuggestedAlbumCandidate::normalizedTitle),
        )
        .map(SuggestedAlbumCandidate::album)
        .forEach { album ->
            if (addIfNeeded(album)) return output
        }

    return output
}

internal fun List<Song>.playbackSourceLabel(fallbackAlbum: String): String {
    val distinctAlbums = asSequence().map { it.album }.filter { it.isNotBlank() }.distinct().toList()
    return when {
        distinctAlbums.size == 1 -> distinctAlbums.first()
        distinctAlbums.isNotEmpty() -> "Search"
        else -> fallbackAlbum
    }
}
