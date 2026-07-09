package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import java.io.File

internal class ScannerMetadataCache {
    private val metadata = mutableMapOf<String, CachedSongMetadata>()

    operator fun get(mediaUri: String): CachedSongMetadata? = metadata[mediaUri]

    fun replaceWith(refreshed: Map<String, CachedSongMetadata>) {
        metadata.clear()
        metadata.putAll(refreshed)
    }

    fun prime(songs: List<Song>) {
        metadata.clear()
        songs.forEach { song ->
            val hasMeaningfulGenre = song.genre.isNotBlank() && song.genre != "Unknown Genre"
            val cachedMetadata = SongMetadata(
                title = song.title,
                artist = song.artist,
                albumArtist = song.albumArtist,
                album = song.album,
                releaseYear = song.releaseYear,
                genre = song.genre.takeIf { hasMeaningfulGenre },
                format = song.audioFormat,
                quality = song.audioQuality,
                trackNumber = song.trackNumber.takeIf { it > 0 },
                discNumber = song.discNumber.takeIf { it > 0 },
                volumeNormalization = song.volumeNormalization,
            )
            metadata[song.uri.toString()] = CachedSongMetadata(
                songId = song.id,
                fileName = song.fileName,
                filePath = song.libraryPath,
                dateAddedSeconds = song.dateAddedSeconds,
                dateModifiedSeconds = song.dateModifiedSeconds,
                isEnriched = song.metadataResolved,
                metadata = cachedMetadata,
            )
        }
    }

    fun clear() {
        metadata.clear()
    }

    fun invalidatePaths(paths: Collection<String>) {
        val normalizedPaths = paths.asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet()
        if (normalizedPaths.isEmpty()) return
        val fileNames = normalizedPaths.asSequence()
            .map(::File)
            .map(File::getName)
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet()
        metadata.entries.removeAll { (_, cached) ->
            cached.filePath in normalizedPaths || cached.fileName in fileNames
        }
    }

    fun invalidateSongIds(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        val ids = songIds.toSet()
        metadata.entries.removeAll { (_, cached) -> cached.songId in ids }
    }
}

internal data class CachedSongMetadata(
    val songId: Long,
    val fileName: String,
    val filePath: String?,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long?,
    val isEnriched: Boolean,
    val metadata: SongMetadata,
)

internal data class SongMetadata(
    val title: String?,
    val artist: String?,
    val albumArtist: String?,
    val album: String?,
    val releaseYear: Int?,
    val genre: String?,
    val format: String,
    val quality: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val volumeNormalization: VolumeNormalizationMetadata?,
)
