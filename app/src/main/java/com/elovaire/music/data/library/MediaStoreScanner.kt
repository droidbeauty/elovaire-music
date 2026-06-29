package elovaire.music.droidbeauty.app.data.library

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioQualityFormatter
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale

class MediaStoreScanner(
    private val context: Context,
) {
    private val metadataCache = ScannerMetadataCache()
    private val audioFormatDetector = AudioFormatDetector(context)
    private val embeddedTagMetadataReader = EmbeddedTagMetadataReader()
    private val scanRoots = LibraryScanRoots()
    private val mediaStoreIndexer = MediaStoreIndexer(
        context = context,
        scanRoots = scanRoots::accessibleFileRoots,
    )

    fun setLibraryFolders(selections: List<LibraryFolderSelection>): Boolean {
        return scanRoots.setSelections(selections)
    }

    fun currentFilterFingerprint(): String {
        return scanRoots.filterFingerprint(FILTER_FINGERPRINT_VERSION)
    }

    internal fun currentSyncState(): LibraryMediaStoreSyncState? {
        return runCatching {
            val volumes = MediaStore.getExternalVolumeNames(context)
                .sorted()
                .mapNotNull { volumeName ->
                    val version = MediaStore.getVersion(context, volumeName)
                    LibraryMediaStoreVolumeSyncState(
                        volumeName = volumeName,
                        version = version,
                        generation = MediaStore.getGeneration(context, volumeName),
                    )
                }
            if (volumes.isEmpty()) return@runCatching null
            LibraryMediaStoreSyncState(
                filterFingerprint = currentFilterFingerprint(),
                volumes = volumes,
            )
        }.getOrNull()
    }

    fun primeMetadataCache(
        songs: List<Song>,
    ) {
        metadataCache.prime(songs)
    }

    fun clearMetadataCache() {
        metadataCache.clear()
    }

    fun scanRoots(): List<File> = scanRoots.accessibleFileRoots()

    fun invalidateMetadataCacheForPaths(paths: Collection<String>) {
        metadataCache.invalidatePaths(paths)
    }

    fun invalidateMetadataCacheForSongIds(songIds: Collection<Long>) {
        metadataCache.invalidateSongIds(songIds)
    }

    fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        if (refreshMediaIndex) {
            refreshMediaIndex()
        } else if (refreshMediaPaths.isNotEmpty()) {
            refreshMediaIndex(refreshMediaPaths)
        }

        var totalRows = 0
        val songs = mutableListOf<Song>()
        val refreshedMetadataCache = mutableMapOf<Long, CachedSongMetadata>()
        val genreCache = mutableMapOf<Long, String?>()
        val progressEmitter = ScannerProgressEmitter(onProgress)
        val audioFileFilter = buildAudioFileFilter()

        context.contentResolver.query(
            MediaStoreAudioQuery.collectionUri,
            MediaStoreAudioQuery.projection,
            MediaStoreAudioQuery.selection,
            null,
            MediaStoreAudioQuery.orderBy,
        )?.use { cursor ->
            totalRows = cursor.count.coerceAtLeast(0)
            progressEmitter.emit(0, totalRows)
            val rowMapper = MediaStoreAudioRowMapper(context, cursor)

            var processedRows = 0
            while (cursor.moveToNext()) {
                processedRows += 1
                val row = rowMapper.row(cursor)
                val detectedFormat = if (AudioFormatPolicy.shouldDetectContainer(row.extension, enrichMetadata)) {
                    audioFormatDetector.detect(row.uri, row.fileName, row.mimeType)
                } else {
                    AudioScanCandidateMapper.fastDetectedFormat(
                        extension = row.extension,
                        mimeType = row.mimeType,
                    )
                }
                val candidate = AudioScanCandidateMapper.toCandidate(row, detectedFormat)
                when (val decision = audioFileFilter.evaluate(candidate)) {
                    AudioFileFilterDecision.Include -> ScannerDebugLogger.logPlatformDependentCandidate(candidate)
                    is AudioFileFilterDecision.Exclude -> {
                        ScannerDebugLogger.logFilteredOutCandidate(candidate, decision.reason)
                        if (processedRows == totalRows || processedRows % 24 == 0) {
                            progressEmitter.emit(processedRows, totalRows)
                        }
                        continue
                    }
                }
                val cachedMetadata = metadataCache[row.id]
                    ?.takeIf {
                        it.fileName == row.fileName &&
                            (it.filePath == null || row.filePath == null || it.filePath == row.filePath) &&
                            it.dateAddedSeconds == row.dateAddedSeconds &&
                            it.dateModifiedSeconds == row.dateModifiedSeconds &&
                            (!enrichMetadata || it.isEnriched)
                    }
                val songMetadata = cachedMetadata
                    ?.metadata
                    ?: if (enrichMetadata) {
                        readSongMetadata(
                            songId = row.id,
                            songUri = row.uri,
                            filePath = row.filePath,
                            mediaStoreYear = row.mediaStoreYear,
                            fileSizeBytes = row.fileSizeBytes,
                            durationMs = row.durationMs,
                            detectedFormat = detectedFormat,
                            genreCache = genreCache,
                        )
                    } else {
                        SongMetadata(
                            title = row.title,
                            artist = row.artist,
                            albumArtist = null,
                            album = row.album,
                            releaseYear = row.mediaStoreYear,
                            genre = null,
                            format = detectedFormat.displayName,
                            quality = null,
                            trackNumber = null,
                            discNumber = null,
                        )
                    }
                val resolvedTitle = songMetadata.title ?: row.title
                val resolvedArtist = songMetadata.artist ?: row.artist
                val resolvedAlbum = songMetadata.album ?: row.album
                val isExplicit = detectExplicit(resolvedTitle, row.fileName)
                val title = sanitizeDisplayTitle(resolvedTitle, isExplicit)
                refreshedMetadataCache[row.id] = CachedSongMetadata(
                    fileName = row.fileName,
                    filePath = row.filePath,
                    dateAddedSeconds = row.dateAddedSeconds,
                    dateModifiedSeconds = row.dateModifiedSeconds,
                    isEnriched = enrichMetadata || cachedMetadata?.isEnriched == true,
                    metadata = songMetadata,
                )
                val rawTrack = row.track
                songs += Song(
                    id = row.id,
                    title = title,
                    isExplicit = isExplicit,
                    artist = resolvedArtist,
                    album = resolvedAlbum,
                    releaseYear = songMetadata.releaseYear,
                    genre = songMetadata.genre.orUnknown("Unknown Genre"),
                    audioFormat = songMetadata.format,
                    audioQuality = songMetadata.quality,
                    fileName = row.fileName,
                    albumId = row.albumId,
                    durationMs = row.durationMs,
                    trackNumber = songMetadata.trackNumber ?: normalizeTrackNumber(rawTrack),
                    discNumber = songMetadata.discNumber ?: normalizeDiscNumber(rawTrack),
                    dateAddedSeconds = row.dateAddedSeconds,
                    dateModifiedSeconds = row.dateModifiedSeconds,
                    libraryPath = row.filePath,
                    uri = row.uri,
                    artUri = albumArtworkUri(row.albumId),
                    metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                    albumArtist = songMetadata.albumArtist,
                )
                if (processedRows == totalRows || processedRows % 24 == 0) {
                    progressEmitter.emit(processedRows, totalRows)
                }
            }
        }

        if (totalRows == 0) {
            progressEmitter.emit(1, 1)
        } else {
            progressEmitter.emit(totalRows, totalRows)
        }

        metadataCache.replaceWith(refreshedMetadataCache)

        return LibrarySnapshot(
            songs = songs.sortedByDescending { it.dateAddedSeconds },
            albums = buildAlbumsFromSongs(songs),
        )
    }

    fun findExistingSongIds(songIds: Set<Long>): Set<Long> {
        if (songIds.isEmpty()) return emptySet()
        return songIds.chunked(MEDIASTORE_ID_QUERY_CHUNK_SIZE).flatMapTo(linkedSetOf()) { chunk ->
            val placeholders = List(chunk.size) { "?" }.joinToString(",")
            context.contentResolver.query(
                MediaStoreAudioQuery.collectionUri,
                arrayOf(MediaStore.Audio.Media._ID),
                "${MediaStore.Audio.Media._ID} IN ($placeholders)",
                chunk.map(Long::toString).toTypedArray(),
                null,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                buildList {
                    while (cursor.moveToNext()) add(cursor.getLong(idIndex))
                }
            }.orEmpty()
        }
    }

    fun musicDirectory(): File {
        @Suppress("DEPRECATION")
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    }

    fun refreshMediaIndex() {
        mediaStoreIndexer.refreshAll()
    }

    fun refreshMediaIndex(paths: List<String>) {
        mediaStoreIndexer.refreshPaths(paths)
    }

    private fun buildAudioFileFilter(): LibraryAudioFileFilter {
        return LibraryAudioFileFilter(
            selectedRelativeRoots = scanRoots.relativeRoots(),
            libraryRootPaths = scanRoots.normalizedFileRootPaths(),
        )
    }

    private fun albumArtworkUri(albumId: Long): Uri? {
        return if (albumId <= 0L) {
            null
        } else {
            ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
        }
    }

    private fun String?.orUnknown(fallback: String): String {
        val value = this?.trim().orEmpty()
        return if (value.isBlank() || value == "<unknown>") fallback else value
    }

    private fun normalizeTrackNumber(rawTrack: Int): Int {
        if (rawTrack <= 0) return 0
        return rawTrack % 1000
    }

    private fun normalizeDiscNumber(rawTrack: Int): Int {
        if (rawTrack <= 0) return 1
        val parsedDiscNumber = rawTrack / 1000
        return parsedDiscNumber.coerceAtLeast(1)
    }

    private fun detectExplicit(
        title: String,
        fileName: String,
    ): Boolean {
        val normalizedTitle = title.lowercase()
        val normalizedFileName = fileName.lowercase()
        return EXPLICIT_MARKERS.any { marker ->
            normalizedTitle.contains(marker) || normalizedFileName.contains(marker)
        } || EXPLICIT_ADVISORY_SUFFIX.containsMatchIn(title)
    }

    private fun sanitizeDisplayTitle(
        title: String,
        isExplicit: Boolean,
    ): String {
        if (!isExplicit) return title
        return title
            .replace(EXPLICIT_ADVISORY_SUFFIX, "")
            .replace(Regex("""\s*[\uFFFD?]{3,}\s*$"""), "")
            .trim()
            .ifBlank { title }
    }

    private fun readSongMetadata(
        songId: Long,
        songUri: Uri,
        filePath: String?,
        mediaStoreYear: Int?,
        fileSizeBytes: Long?,
        durationMs: Long,
        detectedFormat: DetectedAudioFormat,
        genreCache: MutableMap<Long, String?>,
    ): SongMetadata {
        val embeddedMetadata = embeddedTagMetadataReader.read(filePath)
        val retrieverMetadata = readRetrieverMetadata(songUri)
        val resolvedFormat = detectedFormat.displayName
        val year = embeddedMetadata?.releaseYear ?: retrieverMetadata.year ?: mediaStoreYear
        val sampleRate = retrieverMetadata.sampleRate ?: detectedFormat.sampleRate
        val bitDepth = retrieverMetadata.bitDepth
        val bitrate = retrieverMetadata.bitrate
            ?: detectedFormat.bitrate
            ?: estimateBitrateBitsPerSecond(
                fileSizeBytes = fileSizeBytes,
                durationMs = durationMs,
                resolvedFormat = resolvedFormat,
            )
        val genre = embeddedMetadata?.genre
            ?: retrieverMetadata.genre
            ?: genreCache.getOrPut(songId) { queryGenre(songId) }
        return SongMetadata(
            title = embeddedMetadata?.title ?: retrieverMetadata.title,
            artist = embeddedMetadata?.artist ?: retrieverMetadata.artist,
            albumArtist = embeddedMetadata?.albumArtist ?: retrieverMetadata.albumArtist,
            album = embeddedMetadata?.album ?: retrieverMetadata.album,
            releaseYear = year,
            genre = genre,
            format = resolvedFormat,
            quality = AudioQualityFormatter.format(
                container = detectedFormat.container,
                bitDepth = bitDepth,
                sampleRate = sampleRate,
                bitrate = bitrate,
                codecMimeType = detectedFormat.codecMimeType,
            ),
            trackNumber = embeddedMetadata?.trackNumber ?: retrieverMetadata.trackNumber,
            discNumber = embeddedMetadata?.discNumber ?: retrieverMetadata.discNumber,
        )
    }

    private fun readRetrieverMetadata(songUri: Uri): RetrieverMetadata {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, songUri)
                val platformMetadata = RetrieverMetadata(
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                        ?.take(4)
                        ?.toIntOrNull()
                        ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                            ?.let(::parseYearFromDateTag),
                    sampleRate = extractRetrieverSampleRate(retriever),
                    bitDepth = extractRetrieverBitDepth(retriever),
                    bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull(),
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                        ?.substringBefore(';')
                        ?.substringBefore('/')
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.let(::parseTrackNumberTag),
                    discNumber = extractRetrieverDiscNumber(retriever),
                )
                platformMetadata
            } finally {
                runCatching { retriever.release() }
            }
        }.getOrDefault(RetrieverMetadata())
    }

    private fun queryGenre(songId: Long): String? {
        val volumeNames = buildList {
            add("external")
            add(MediaStore.VOLUME_EXTERNAL)
            add(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }.distinct()

        return volumeNames.firstNotNullOfOrNull { volumeName ->
            val genreUri = MediaStore.Audio.Genres.getContentUriForAudioId(volumeName, songId.toInt())
            runCatching {
                context.contentResolver.query(
                    genreUri,
                    arrayOf(MediaStore.Audio.Genres.NAME),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                    generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }
                        .map { it.trim() }
                        .firstOrNull { it.isNotBlank() }
                }
            }.getOrNull()
        }
    }

    private fun parseYearFromDateTag(value: String): Int? {
        return YEAR_REGEX.find(value)?.value?.toIntOrNull()
    }

    private fun parseTrackNumberTag(value: String): Int? {
        return value
            .substringBefore('/')
            .trim()
            .toIntOrNull()
            ?.takeIf { it > 0 }
    }

    private fun extractRetrieverDiscNumber(retriever: MediaMetadataRetriever): Int? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
            ?.substringBefore('/')
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
    }

    private fun estimateBitrateBitsPerSecond(
        fileSizeBytes: Long?,
        durationMs: Long,
        resolvedFormat: String,
    ): Int? {
        if (fileSizeBytes == null || fileSizeBytes <= 0L || durationMs <= 0L) return null
        if (resolvedFormat.uppercase() in setOf("WAV", "FLAC")) return null
        val seconds = durationMs / 1000.0
        if (seconds <= 0.0) return null
        return ((fileSizeBytes * 8.0) / seconds).toInt().takeIf { it > 0 }
    }

    internal companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
        const val MEDIASTORE_ID_QUERY_CHUNK_SIZE = 400
        val YEAR_REGEX = Regex("""\b(19|20)\d{2}\b""")
        val EXPLICIT_MARKERS = listOf(
            "(explicit)",
            "[explicit]",
            " - explicit",
            " explicit version",
        )
        val EXPLICIT_ADVISORY_SUFFIX = Regex(
            pattern = """(?:\s|^)(?:[\[(]\s*explicit\s*[\])]|🅴|[\uFFFD?]{3,})\s*$""",
            option = RegexOption.IGNORE_CASE,
        )
    }

    @Suppress("InlinedApi")
    private fun extractRetrieverSampleRate(retriever: MediaMetadataRetriever): Int? {
        return runCatching {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
        }.getOrNull()
    }

    @Suppress("InlinedApi")
    private fun extractRetrieverBitDepth(retriever: MediaMetadataRetriever): Int? {
        return runCatching {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.toIntOrNull()
        }.getOrNull()
    }

}

internal fun sortAlbumSongs(albumSongs: List<Song>): List<Song> {
    val hasTrackTags = albumSongs.any { it.trackNumber > 0 }
    return if (hasTrackTags) {
        albumSongs.sortedWith(
            compareBy<Song>(
                { it.discNumber },
                { if (it.trackNumber > 0) 0 else 1 },
                { if (it.trackNumber > 0) it.trackNumber else Int.MAX_VALUE },
                { it.fileName.lowercase(Locale.ROOT) },
            ),
        )
    } else {
        albumSongs.sortedBy { it.fileName.lowercase(Locale.ROOT) }
    }
}

private data class RetrieverMetadata(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val year: Int? = null,
    val sampleRate: Int? = null,
    val bitDepth: Int? = null,
    val bitrate: Int? = null,
    val genre: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
)

internal fun Song.qualityNeedsEnrichment(): Boolean {
    if (audioQuality.isNullOrBlank()) return true
    val normalizedFormat = audioFormat.uppercase()
    return when {
        isLossyFormat(normalizedFormat) -> !audioQuality.contains("/")
        isLosslessFormat(normalizedFormat) -> !LOSSLESS_QUALITY_REGEX.matches(audioQuality)
        else -> false
    }
}

private fun isLossyFormat(format: String): Boolean {
    return format in LOSSY_AUDIO_FORMATS
}

private fun isLosslessFormat(format: String): Boolean {
    return format in LOSSLESS_AUDIO_FORMATS
}

internal fun isSupportedAudioExtension(extension: String): Boolean {
    return extension.lowercase() in AudioFormatPolicy.scannerExtensions
}

internal fun isSupportedAudioFileName(fileName: String): Boolean {
    return fileName.substringAfterLast('.', "").let(::isSupportedAudioExtension)
}

internal fun isSupportedLibrarySong(song: Song): Boolean {
    return isSupportedAudioFileName(song.fileName)
}

private const val FILTER_FINGERPRINT_VERSION = 2
private val LOSSY_AUDIO_FORMATS = setOf(
    "MP3",
    "AAC",
    "OGG",
    "OGG/OPUS",
    "OPUS",
    "AMR",
    "3GP",
    "3GP AUDIO",
    "MP4",
    "MP4 AUDIO",
    "M4A",
    "MKA",
)
private val LOSSLESS_AUDIO_FORMATS = setOf("FLAC", "WAV")
private val LOSSLESS_QUALITY_REGEX = Regex("""\d{1,2}/\d{1,3}(?:\.\d)?kHz""")
