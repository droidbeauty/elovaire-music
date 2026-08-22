package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioQualityFormatter
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.MetadataSourceValues
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@Suppress("TooManyFunctions")
class MediaStoreScanner(
    private val context: Context,
) {
    private val metadataCache = ScannerMetadataCache()
    private val audioFormatDetector = AudioFormatDetector(context)
    private val localMetadataReader = LocalAudioMetadataReader(context)
    private val scanRoots = LibraryScanRoots()
    private val mediaStoreIndexer = MediaStoreIndexer(
        context = context,
        scanRoots = scanRoots::accessibleFileRoots,
    )
    private val safTreeScanner = SafTreeLibraryScanner(context)
    internal val targetExistenceProbe = MediaTargetExistenceProbe(context)

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

    internal fun onMemoryPressure(pressure: MemoryPressure) {
        metadataCache.onMemoryPressure(pressure)
    }

    fun scanRoots(): List<File> = scanRoots.accessibleFileRoots()

    internal fun hasSafSelections(): Boolean = scanRoots.hasSafSelections()

    fun invalidateMetadataCacheForPaths(paths: Collection<String>) {
        metadataCache.invalidatePaths(paths)
    }

    fun invalidateMetadataCacheForSongIds(songIds: Collection<Long>) {
        metadataCache.invalidateSongIds(songIds)
    }

    suspend fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        if (refreshMediaIndex) {
            ElovaireTrace.section("library_media_index_refresh") {
                refreshMediaIndex()
            }
        } else if (refreshMediaPaths.isNotEmpty()) {
            ElovaireTrace.section("library_media_index_refresh_paths") {
                refreshMediaIndex(refreshMediaPaths)
            }
        }

        var totalRows = 0
        val songs = mutableListOf<Song>()
        val scannedMetadataUris = mutableSetOf<String>()
        val genreCache = mutableMapOf<MediaStoreGenreKey, String?>()
        val progressEmitter = ScannerProgressEmitter(onProgress)
        val audioFileFilter = buildAudioFileFilter()
        val decisionMap = ScannerDebugLogger.newDecisionMap()

        ElovaireTrace.section("library_mediastore_scan") {
            val cursor = ElovaireTrace.section("library_mediastore_query") {
                context.contentResolver.query(
                    MediaStoreAudioQuery.collectionUri,
                    MediaStoreAudioQuery.projection,
                    MediaStoreAudioQuery.selection,
                    null,
                    MediaStoreAudioQuery.orderBy,
                )
            } ?: throw MediaStoreQueryUnavailableException()
            cursor.use {
                totalRows = cursor.count.coerceAtLeast(0)
                progressEmitter.emit(0, totalRows)
                val rowMapper = MediaStoreAudioRowMapper(context, cursor)

                var processedRows = 0
                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    processedRows += 1
                    val row = rowMapper.row(cursor)
                    val preflightCandidate = AudioScanCandidateMapper.toCandidate(row, detectedFormat = null)
                    val preflightRejection = MediaStoreScanPreflight
                        .rejectionBeforeContainerDetection(preflightCandidate, audioFileFilter)
                    if (preflightRejection != null) {
                        decisionMap.recordMediaStoreRow(preflightCandidate)
                        decisionMap.recordMediaStoreExclude(preflightRejection.reason)
                        if (processedRows == totalRows || processedRows % 24 == 0) {
                            progressEmitter.emit(processedRows, totalRows)
                        }
                        continue
                    }
                    val uriKey = MediaIdentityResolver.mediaStore(row.volumeName, row.id)
                        ?.stableKey
                        ?: row.uri.toString()
                    val cachedMetadata = metadataCache[uriKey]
                        ?.takeIf { cached ->
                            cached.matches(
                                fileName = row.fileName,
                                filePath = row.filePath,
                                dateAddedSeconds = row.dateAddedSeconds,
                                dateModifiedSeconds = row.dateModifiedSeconds,
                                fileSizeBytes = row.fileSizeBytes,
                                durationMs = row.durationMs,
                                requireEnriched = enrichMetadata,
                            )
                        }
                    val detectedFormat = if (AudioFormatPolicy.shouldDetectContainer(row.extension, enrichMetadata)) {
                        audioFormatDetector.detect(
                            uri = row.uri,
                            fileName = row.fileName,
                            mediaStoreMimeType = row.mimeType,
                            revisionKey = if (row.dateModifiedSeconds != null || row.fileSizeBytes != null) {
                                MediaIdentityResolver.sourceRevisionKey(
                                    modifiedAtMs = row.dateModifiedSeconds?.times(1_000L),
                                    sizeBytes = row.fileSizeBytes,
                                )
                            } else null,
                            identityKey = MediaIdentityResolver.mediaStore(row.volumeName, row.id)?.stableKey,
                        )
                    } else {
                        AudioScanCandidateMapper.fastDetectedFormat(
                            extension = row.extension,
                            mimeType = row.mimeType,
                        )
                    }
                    val candidate = AudioScanCandidateMapper.toCandidate(row, detectedFormat)
                    decisionMap.recordMediaStoreRow(candidate)
                    when (val decision = audioFileFilter.evaluate(candidate)) {
                        AudioFileFilterDecision.Include -> {
                            decisionMap.recordMediaStoreInclude()
                            ScannerDebugLogger.logPlatformDependentCandidate(candidate)
                        }
                        is AudioFileFilterDecision.Exclude -> {
                            decisionMap.recordMediaStoreExclude(decision.reason)
                            if (processedRows == totalRows || processedRows % 24 == 0) {
                                progressEmitter.emit(processedRows, totalRows)
                            }
                            continue
                        }
                    }
                    scannedMetadataUris += uriKey
                    val songMetadata = cachedMetadata
                        ?.metadata
                        ?: if (enrichMetadata) {
                            readSongMetadata(
                                songId = row.id,
                                songUri = row.uri,
                                fileName = row.fileName,
                                indexedTitle = row.title,
                                indexedArtist = row.artist,
                                indexedAlbum = row.album,
                                filePath = row.filePath,
                                volumeName = row.volumeName,
                                mediaStoreYear = row.mediaStoreYear,
                                fileSizeBytes = row.fileSizeBytes,
                                durationMs = row.durationMs,
                                detectedFormat = detectedFormat,
                                genreCache = genreCache,
                                identityKey = uriKey,
                                revisionKey = if (row.dateModifiedSeconds != null || row.fileSizeBytes != null) {
                                    MediaIdentityResolver.sourceRevisionKey(
                                        modifiedAtMs = row.dateModifiedSeconds?.times(1_000L),
                                        sizeBytes = row.fileSizeBytes,
                                    )
                                } else null,
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
                                volumeNormalization = null,
                            )
                        }
                    val resolvedTitle = songMetadata.title ?: row.title
                    val resolvedArtist = songMetadata.artist ?: row.artist
                    val resolvedAlbum = songMetadata.album ?: row.album
                    val isExplicit = detectExplicit(resolvedTitle, row.fileName)
                    val title = sanitizeDisplayTitle(resolvedTitle, isExplicit)
                    metadataCache.put(uriKey, CachedSongMetadata(
                        songId = row.id,
                        fileName = row.fileName,
                        filePath = row.filePath,
                        dateAddedSeconds = row.dateAddedSeconds,
                        dateModifiedSeconds = row.dateModifiedSeconds,
                        isEnriched = enrichMetadata || cachedMetadata?.isEnriched == true,
                        metadata = songMetadata,
                        fileSizeBytes = row.fileSizeBytes,
                        durationMs = row.durationMs,
                    ))
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
                        artUri = mediaStoreAlbumArtworkUri(row.volumeName, row.albumId),
                        metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                        albumArtist = songMetadata.albumArtist,
                        volumeNormalization = songMetadata.volumeNormalization,
                    )
                    if (processedRows == totalRows || processedRows % 24 == 0) {
                        progressEmitter.emit(processedRows, totalRows)
                    }
                }
            }
        }

        if (totalRows == 0) {
            progressEmitter.emit(1, 1)
        } else {
            progressEmitter.emit(totalRows, totalRows)
        }

        currentCoroutineContext().ensureActive()
        val safSongs = ElovaireTrace.suspendSection("library_saf_scan") {
            safTreeScanner.scan(scanRoots.safTreeSelections())
        }
        decisionMap.recordSafIncluded(safSongs.size)
        val mergedSongs = ElovaireTrace.section("library_scan_merge") {
            mergeMediaStoreAndSafSongs(
                mediaStoreSongs = songs,
                safSongs = safSongs,
            )
        }
        decisionMap.recordMerge(
            mediaStoreSongCount = songs.size,
            safSongCount = safSongs.size,
            mergedSongCount = mergedSongs.size,
        )
        decisionMap.logSummary()

        metadataCache.retainOnly(scannedMetadataUris)

        val sortedSongs = ElovaireTrace.section("library_song_sort") {
            mergedSongs.sortedByDescending { it.dateAddedSeconds }
        }
        val albums = ElovaireTrace.section("library_album_build") {
            buildAlbumsFromSongs(mergedSongs)
        }
        return LibrarySnapshot(
            songs = sortedSongs,
            albums = albums,
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
        return MediaFilePathResolver.defaultMusicDirectory()
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
            explicitCustomRootPaths = scanRoots.explicitCustomFileRootPaths(),
            explicitCustomRelativeRoots = scanRoots.explicitCustomRelativeRoots(),
        )
    }

    private fun mergeMediaStoreAndSafSongs(
        mediaStoreSongs: List<Song>,
        safSongs: List<Song>,
    ): List<Song> {
        return LibrarySongDuplicateResolver.mergeMediaStoreAndSafSongs(mediaStoreSongs, safSongs)
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
        val normalizedTitle = title.lowercase(Locale.ROOT)
        val normalizedFileName = fileName.lowercase(Locale.ROOT)
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
            .replace(TRAILING_REPLACEMENT_MARKERS, "")
            .trim()
            .ifBlank { title }
    }

    private fun readSongMetadata(
        songId: Long,
        songUri: Uri,
        fileName: String,
        indexedTitle: String,
        indexedArtist: String,
        indexedAlbum: String,
        filePath: String?,
        volumeName: String?,
        mediaStoreYear: Int?,
        fileSizeBytes: Long?,
        durationMs: Long,
        detectedFormat: DetectedAudioFormat,
        genreCache: MutableMap<MediaStoreGenreKey, String?>,
        identityKey: String,
        revisionKey: String?,
    ): SongMetadata {
        val metadata = localMetadataReader.read(
            uri = songUri,
            filePath = filePath,
            fileName = fileName,
            indexed = MetadataSourceValues(
                title = indexedTitle,
                artist = indexedArtist,
                album = indexedAlbum,
                releaseYear = mediaStoreYear,
            ),
            fallbackGenre = genreCache.getOrPut(MediaStoreGenreKey(songId, volumeName)) {
                queryGenre(songId, volumeName)
            },
            identityKey = identityKey,
            revisionKey = revisionKey,
        )
        val resolvedFormat = detectedFormat.displayName
        val sampleRate = metadata.sampleRate ?: detectedFormat.sampleRate
        val bitrate = metadata.bitrate
            ?: detectedFormat.bitrate
            ?: estimateBitrateBitsPerSecond(
                fileSizeBytes = fileSizeBytes,
                durationMs = durationMs,
                resolvedFormat = resolvedFormat,
            )
        return SongMetadata(
            title = metadata.title,
            artist = metadata.artist,
            albumArtist = metadata.albumArtist,
            album = metadata.album,
            releaseYear = metadata.releaseYear,
            genre = metadata.genre,
            format = resolvedFormat,
            quality = AudioQualityFormatter.format(
                container = detectedFormat.container,
                bitDepth = metadata.bitDepth,
                sampleRate = sampleRate,
                bitrate = bitrate,
                codecMimeType = detectedFormat.codecMimeType,
            ),
            trackNumber = metadata.trackNumber,
            discNumber = metadata.discNumber,
            volumeNormalization = metadata.volumeNormalization,
        )
    }

    private fun queryGenre(
        songId: Long,
        volumeName: String?,
    ): String? {
        // The platform genre URI API accepts an Int audio ID. Never truncate a MediaStore Long ID.
        if (!canQueryMediaStoreGenre(songId)) return null

        val volumeNames = mediaStoreGenreVolumes(volumeName)

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

    private fun estimateBitrateBitsPerSecond(
        fileSizeBytes: Long?,
        durationMs: Long,
        resolvedFormat: String,
    ): Int? {
        if (fileSizeBytes == null || fileSizeBytes <= 0L || durationMs <= 0L) return null
        if (resolvedFormat.uppercase(Locale.ROOT) in NON_BITRATE_ESTIMATED_FORMATS) return null
        val seconds = durationMs / 1000.0
        if (seconds <= 0.0) return null
        return ((fileSizeBytes * 8.0) / seconds).toInt().takeIf { it > 0 }
    }

    internal companion object {
        const val MEDIASTORE_ID_QUERY_CHUNK_SIZE = 400
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
        val TRAILING_REPLACEMENT_MARKERS = Regex("""\s*[\uFFFD?]{3,}\s*$""")
        val NON_BITRATE_ESTIMATED_FORMATS = setOf("WAV", "FLAC")
    }

}

internal class MediaStoreQueryUnavailableException : IllegalStateException(
    "MediaStore audio query returned no cursor.",
)

internal data class MediaStoreGenreKey(
    val songId: Long,
    val volumeName: String?,
)

internal fun canQueryMediaStoreGenre(songId: Long): Boolean {
    return songId in 1L..Int.MAX_VALUE.toLong()
}

internal fun mediaStoreGenreVolumes(preferredVolumeName: String?): List<String> {
    return buildList {
        preferredVolumeName?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
        add(MediaStore.VOLUME_EXTERNAL)
        add(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }.distinct()
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
    return extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions
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
