package elovaire.music.droidbeauty.app.data.library.db

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale

internal object LibraryDatabaseMapper {
    fun songEntity(song: Song, generationId: Long): SongEntity {
        return SongEntity(
            songId = song.id,
            mediaStoreId = song.id.takeIf { it > 0L },
            uri = song.uri.toString(),
            filePath = song.libraryPath,
            fileName = song.fileName,
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumArtist = song.albumArtist,
            albumId = song.albumId,
            durationMs = song.durationMs,
            trackNumber = song.trackNumber,
            discNumber = song.discNumber,
            dateAddedSeconds = song.dateAddedSeconds,
            dateModifiedSeconds = song.dateModifiedSeconds ?: 0L,
            releaseYear = song.releaseYear,
            genre = song.genre,
            audioFormat = song.audioFormat,
            audioQuality = song.audioQuality,
            metadataResolved = song.metadataResolved,
            artUri = song.artUri?.toString(),
            volumeNormalization = song.volumeNormalization?.trackGainDb,
            lastSeenGenerationId = generationId,
            removedAtMs = null,
        )
    }

    fun albumEntity(album: Album, generationId: Long): AlbumEntity {
        return AlbumEntity(
            albumId = album.id,
            title = album.title,
            artist = album.artist,
            songCount = album.songCount,
            durationMs = album.durationMs,
            releaseYear = album.songs.firstNotNullOfOrNull { it.releaseYear },
            genre = album.songs.firstOrNull { it.genre.isNotBlank() }?.genre,
            artUri = album.artUri?.toString(),
            lastSeenGenerationId = generationId,
            removedAtMs = null,
        )
    }

    fun mediaFileEntity(song: Song, generationId: Long, scannedAtMs: Long): MediaFileEntity {
        val extension = song.fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val stableFileKey = song.libraryPath?.trim()?.takeIf { it.isNotBlank() } ?: song.uri.toString()
        return MediaFileEntity(
            stableFileKey = stableFileKey,
            mediaStoreId = song.id.takeIf { it > 0L },
            uri = song.uri.toString(),
            filePath = song.libraryPath,
            displayName = song.fileName,
            extension = extension,
            mimeType = null,
            container = song.audioFormat,
            codecMimeType = null,
            decoderAvailable = null,
            hasAudioTrack = true,
            hasVideoTrack = false,
            fileSizeBytes = null,
            dateModifiedSeconds = song.dateModifiedSeconds ?: 0L,
            lastScannedAtMs = scannedAtMs,
            lastSeenGenerationId = generationId,
        )
    }

    fun indexedSnapshot(snapshot: LibrarySnapshot, generationId: Long, scannedAtMs: Long): IndexedLibrarySnapshot {
        return IndexedLibrarySnapshot(
            songs = snapshot.songs.map { songEntity(it, generationId) },
            albums = snapshot.albums.map { albumEntity(it, generationId) },
            mediaFiles = snapshot.songs.map { mediaFileEntity(it, generationId, scannedAtMs) },
        )
    }
}

internal data class IndexedLibrarySnapshot(
    val songs: List<SongEntity>,
    val albums: List<AlbumEntity>,
    val mediaFiles: List<MediaFileEntity>,
)
