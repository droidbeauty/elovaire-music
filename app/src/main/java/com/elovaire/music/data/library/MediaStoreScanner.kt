package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@Suppress("TooManyFunctions", "TooGenericExceptionCaught")
internal class MediaStoreScanner(
    private val context: Context,
    indexRefresher: MediaStoreIndexRefresher? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val metadataCache = ScannerMetadataCache()
    private val audioFormatDetector = AudioFormatDetector(context)
    private val localMetadataReader = LocalAudioMetadataReader(context)
    private val scanRoots = LibraryScanRoots()
    private val mediaStoreIndexer = indexRefresher ?: MediaStoreIndexer(
        context = context,
        scanRoots = scanRoots::directFileRoots,
    )
    internal val targetExistenceProbe = MediaTargetExistenceProbe(context)
    fun setLibraryFolders(selections: List<LibraryFolderSelection>): Boolean {
        return scanRoots.setSelections(selections)
    }

    fun currentFilterFingerprint(): String {
        val local = scanRoots.filterFingerprint(FILTER_FINGERPRINT_VERSION)
        return local
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

    fun scanRoots(): List<File> = scanRoots.directFileRoots()

    fun requiresMediaIndexRepair(): Boolean = scanRoots.requiresMediaIndexRepair()

    internal fun hasSafSelections(): Boolean = scanRoots.hasSafSelections()

    internal fun safTreeSelections(): List<LibraryFolderSelection> = scanRoots.safTreeSelections()

    fun invalidateMetadataCacheForPaths(paths: Collection<String>) {
        metadataCache.invalidatePaths(paths)
    }

    fun invalidateMetadataCacheForSongIds(songIds: Collection<Long>) {
        metadataCache.invalidateSongIds(songIds)
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    suspend fun scan(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        mediaStoreGenerationFloor: Long? = null,
        baseMediaStoreSongs: List<Song> = emptyList(),
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        val decisionMap = ScannerDebugLogger.newDecisionMap()
        val indexRefreshJob: Deferred<MediaStoreIndexRefreshResult?>? = when {
            refreshMediaIndex -> {
                val scanContext = currentCoroutineContext()
                CoroutineScope(scanContext + ioDispatcher).async {
                    try {
                        ElovaireTrace.section("library_media_index_refresh") {
                            refreshMediaIndex {
                                scanContext.ensureActive()
                            }
                        }
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Exception) {
                        decisionMap.recordIndexRefreshFailure(failure)
                        null
                    }
                }
            }
            refreshMediaPaths.isNotEmpty() -> {
                val scanContext = currentCoroutineContext()
                CoroutineScope(scanContext + ioDispatcher).async {
                    try {
                        ElovaireTrace.section("library_media_index_refresh_paths") {
                            refreshMediaIndex(refreshMediaPaths) {
                                scanContext.ensureActive()
                            }
                        }
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Exception) {
                        decisionMap.recordIndexRefreshFailure(failure)
                        null
                    }
                }
            }
            else -> null
        }

        val genreCache = mutableMapOf<MediaStoreGenreKey, String?>()
        val progressEmitter = ScannerProgressEmitter(onProgress)

        suspend fun queryPlanForCurrentProvider(useDelta: Boolean): MediaStoreQueryPlan {
            return ElovaireTrace.section("library_mediastore_scan") {
                val deltaQuery = if (
                    useDelta &&
                    mediaStoreGenerationFloor != null &&
                    refreshMediaPaths.isEmpty() &&
                    !refreshMediaIndex &&
                    baseMediaStoreSongs.isNotEmpty()
                ) {
                    ElovaireTrace.section("mediastore_discovery") {
                        ElovaireTrace.section("mediastore_query_delta") {
                            MediaStoreAudioQuery.queryDelta(context.contentResolver, mediaStoreGenerationFloor)
                        }
                    }
                } else {
                    null
                }
                var deltaConsumed = false
                try {
                    val deltaIdentityKeys = if (deltaQuery != null) {
                        runCatching {
                            ElovaireTrace.section("mediastore_delta_identity") {
                                MediaStoreAudioQuery.queryIdentity(context.contentResolver)?.use { cursor ->
                                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                                    val volumeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.VOLUME_NAME)
                                    if (volumeIndex < 0) {
                                        null
                                    } else {
                                        var hasInvalidIdentity = false
                                        buildSet<String> {
                                            while (cursor.moveToNext()) {
                                                val volume = cursor.getString(volumeIndex)
                                                val identity = MediaIdentityResolver.mediaStore(
                                                    volume,
                                                    cursor.getLong(idIndex),
                                                )
                                                if (identity == null) hasInvalidIdentity = true
                                                identity?.stableKey?.let(::add)
                                            }
                                        }
                                            .takeUnless { hasInvalidIdentity }
                                    }
                                }
                            }
                        }.getOrElse { failure ->
                            if (failure is SecurityException) throw failure
                            null
                        }
                    } else {
                        null
                    }
                    val queryResult = if (deltaIdentityKeys != null) {
                        deltaConsumed = true
                        requireNotNull(deltaQuery)
                    } else {
                        ElovaireTrace.section("mediastore_discovery") {
                            ElovaireTrace.section(
                                if (refreshMediaPaths.isEmpty()) "mediastore_query_full" else "mediastore_query_delta",
                            ) {
                                MediaStoreAudioQuery.query(context.contentResolver)
                            }
                        }
                    }
                    MediaStoreQueryPlan(
                        queryResult = queryResult,
                        deltaIdentityKeys = deltaIdentityKeys,
                    )
                } finally {
                    if (!deltaConsumed) deltaQuery?.cursor?.close()
                }
            }
        }

        suspend fun readQuery(
            queryPlan: MediaStoreQueryPlan,
            reportProgress: Boolean,
        ): MediaStoreQueryRead {
            val songs = mutableListOf<Song>()
            val scannedMetadataUris = mutableSetOf<String>()
            var totalRows = 0
            ElovaireTrace.section("library_mediastore_rows") {
                val queryResult = queryPlan.queryResult
                decisionMap.recordProjection(queryResult.projectionKind.name)
                val cursor = queryResult.cursor
                cursor.use {
                    totalRows = cursor.count.coerceAtLeast(0)
                    if (reportProgress) progressEmitter.emit(0, totalRows)
                    val rowMapper = MediaStoreAudioRowMapper(context, cursor)
                    val audioFileFilter = buildAudioFileFilter(
                        allowUnscopedMediaStoreRows = true,
                    )
                    if (queryResult.projectionKind == MediaStoreAudioQuery.ProjectionKind.Compatibility ||
                        !rowMapper.hasRelativePathColumn
                    ) {
                        decisionMap.recordFolderMetadataUnavailable()
                    }

                    val rowProcessor = MediaStoreRowProcessor(
                        context = context,
                        metadataCache = metadataCache,
                        audioFormatDetector = audioFormatDetector,
                        localMetadataReader = localMetadataReader,
                        audioFileFilter = audioFileFilter,
                        enrichMetadata = enrichMetadata,
                        genreCache = genreCache,
                        decisionMap = decisionMap,
                    )
                    var processedRows = 0
                    while (cursor.moveToNext()) {
                        currentCoroutineContext().ensureActive()
                        processedRows += 1
                        try {
                            rowProcessor.process(rowMapper.row(cursor))?.let { processedSong ->
                                scannedMetadataUris += processedSong.identityKey
                                songs += processedSong.song
                            }
                        } catch (failure: CancellationException) {
                            throw failure
                        } catch (failure: RuntimeException) {
                            // One malformed provider row must not hide otherwise valid library rows.
                            decisionMap.recordMediaStoreExclude(
                                "Row processing failed: ${failure::class.simpleName ?: "Unknown"}",
                            )
                        }
                        if (reportProgress && (processedRows == totalRows || processedRows % 24 == 0)) {
                            progressEmitter.emit(processedRows, totalRows)
                        }
                    }
                }
            }
            return MediaStoreQueryRead(
                songs = songs,
                scannedMetadataUris = scannedMetadataUris,
                totalRows = totalRows,
            )
        }

        var queryPlan = queryPlanForCurrentProvider(useDelta = true)
        var queryRead = readQuery(queryPlan, reportProgress = true)

        // A repair request is intentionally independent from the first provider read. If repair
        // succeeds or partially succeeds, re-query once so files newly indexed by the platform
        // are included in this same scan. If repair fails, keep the already-read authoritative
        // rows instead of turning a readable catalog into an unavailable source.
        val indexRefreshResult = indexRefreshJob?.await()
        indexRefreshResult?.let(decisionMap::recordIndexRefresh)
        if (indexRefreshResult != null && indexRefreshResult !is MediaStoreIndexRefreshResult.Unavailable) {
            val repairedQueryPlan = try {
                queryPlanForCurrentProvider(useDelta = false)
            } catch (failure: CancellationException) {
                throw failure
            } catch (_: RuntimeException) {
                null
            }
            if (repairedQueryPlan != null) {
                val repairedRead = try {
                    readQuery(repairedQueryPlan, reportProgress = false)
                } catch (failure: CancellationException) {
                    throw failure
                } catch (_: RuntimeException) {
                    null
                }
                if (repairedRead != null) {
                    queryPlan = repairedQueryPlan
                    queryRead = repairedRead
                } else {
                    repairedQueryPlan.queryResult.cursor.close()
                }
            }
        }

        val totalRows = queryRead.totalRows
        val songs = queryRead.songs
        val scannedMetadataUris = queryRead.scannedMetadataUris.toMutableSet()
        val deltaIdentityKeys = queryPlan.deltaIdentityKeys
        val usingDelta = deltaIdentityKeys != null

        if (totalRows == 0) {
            progressEmitter.emit(1, 1)
        } else {
            progressEmitter.emit(totalRows, totalRows)
        }

        currentCoroutineContext().ensureActive()
        val mergedSongs = if (usingDelta) {
            mergeMediaStoreDelta(
                baseSongs = baseMediaStoreSongs,
                changedSongs = songs,
                currentIdentityKeys = requireNotNull(deltaIdentityKeys),
            )
        } else {
            songs
        }
        decisionMap.logSummary(mergedSongs.size)

        if (usingDelta) {
            scannedMetadataUris.addAll(mergedSongs.mapTo(hashSetOf(), MediaIdentityResolver::stableKey))
        }
        metadataCache.retainOnly(scannedMetadataUris)

        val sortedSongs = ElovaireTrace.section("library_song_sort") {
            mergedSongs.sortedByDescending { it.dateAddedSeconds }
        }
        return ElovaireTrace.section("library_album_build") {
            LibrarySnapshotAssembler.assemble(sortedSongs)
        }
    }

    private fun mergeMediaStoreDelta(
        baseSongs: List<Song>,
        changedSongs: List<Song>,
        currentIdentityKeys: Set<String>,
    ): List<Song> {
        val changedByKey = changedSongs.associateBy(MediaIdentityResolver::stableKey)
        val baseKeys = baseSongs.mapTo(hashSetOf(), MediaIdentityResolver::stableKey)
        return buildList(baseSongs.size + changedSongs.size) {
            baseSongs.forEach { baseSong ->
                val key = MediaIdentityResolver.stableKey(baseSong)
                if (key in currentIdentityKeys) add(changedByKey[key] ?: baseSong)
            }
            changedSongs.forEach { changedSong ->
                if (MediaIdentityResolver.stableKey(changedSong) !in baseKeys) {
                    add(changedSong)
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    internal suspend fun scanSafely(
        refreshMediaIndex: Boolean = false,
        refreshMediaPaths: List<String> = emptyList(),
        enrichMetadata: Boolean = true,
        mediaStoreGenerationFloor: Long? = null,
        baseMediaStoreSongs: List<Song> = emptyList(),
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LocalLibraryScanResult {
        return try {
            LocalLibraryScanResult.Complete(
                scan(
                    refreshMediaIndex = refreshMediaIndex,
                    refreshMediaPaths = refreshMediaPaths,
                    enrichMetadata = enrichMetadata,
                    mediaStoreGenerationFloor = mediaStoreGenerationFloor,
                    baseMediaStoreSongs = baseMediaStoreSongs,
                    onProgress = onProgress,
                ),
            )
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: Exception) {
            LocalLibraryScanResult.Unavailable(failure)
        }
    }

    suspend fun findExistingSongIds(songIds: Set<Long>): Set<Long> {
        if (songIds.isEmpty()) return emptySet()
        return songIds.chunked(MEDIASTORE_ID_QUERY_CHUNK_SIZE).flatMapTo(linkedSetOf()) { chunk ->
            val placeholders = List(chunk.size) { "?" }.joinToString(",")
            context.contentResolver.queryCancellable(
                MediaStoreAudioQuery.collectionUri,
                arrayOf(MediaStore.Audio.Media._ID),
                "${MediaStoreAudioQuery.selection} AND " +
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

    fun refreshMediaIndex(
        shouldContinue: () -> Unit = {},
    ): MediaStoreIndexRefreshResult = mediaStoreIndexer.refreshAll {
        shouldContinue()
    }

    fun refreshMediaIndex(
        paths: List<String>,
        shouldContinue: () -> Unit = {},
    ): MediaStoreIndexRefreshResult = mediaStoreIndexer.refreshPaths(paths) {
        shouldContinue()
    }

    private fun buildAudioFileFilter(
        allowUnscopedMediaStoreRows: Boolean = false,
    ): LibraryAudioFileFilter {
        return LibraryAudioFileFilter(
            selectedRelativeRoots = scanRoots.relativeRoots(),
            libraryRootPaths = scanRoots.normalizedFileRootPaths(),
            explicitCustomRootPaths = scanRoots.explicitCustomFileRootPaths(),
            explicitCustomRelativeRoots = scanRoots.explicitCustomRelativeRoots(),
            allowUnscopedMediaStoreRows = allowUnscopedMediaStoreRows,
        )
    }

    internal companion object {
        const val MEDIASTORE_ID_QUERY_CHUNK_SIZE = 400
    }

}

internal class MediaStoreQueryUnavailableException(cause: Throwable? = null) : IllegalStateException(
    "MediaStore audio query returned no cursor.",
    cause,
)

private data class MediaStoreQueryPlan(
    val queryResult: MediaStoreAudioQuery.QueryResult,
    val deltaIdentityKeys: Set<String>?,
)

private data class MediaStoreQueryRead(
    val songs: List<Song>,
    val scannedMetadataUris: Set<String>,
    val totalRows: Int,
)

internal sealed interface LocalLibraryScanResult {
    data class Complete(val snapshot: LibrarySnapshot) : LocalLibraryScanResult
    data class Unavailable(val failure: Throwable) : LocalLibraryScanResult
}

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
    if (isSupportedAudioFileName(song.fileName)) return true
    val normalizedFormat = song.audioFormat.trim().uppercase(Locale.ROOT)
    return normalizedFormat in SUPPORTED_DETECTED_FORMAT_NAMES
}

private val SUPPORTED_DETECTED_FORMAT_NAMES = setOf(
    "MP3",
    "M4A",
    "M4B",
    "MP4 AUDIO",
    "AAC",
    "FLAC",
    "WAV",
    "OGG",
    "OGG/VORBIS",
    "OGG/OPUS",
    "OGG/FLAC",
    "OPUS",
    "AMR",
    "3GP AUDIO",
    "MKA",
)

private const val FILTER_FINGERPRINT_VERSION = 3

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
