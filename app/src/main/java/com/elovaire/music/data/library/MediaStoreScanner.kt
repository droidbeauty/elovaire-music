package elovaire.music.droidbeauty.app.data.library

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.resolveMediaStoreFilePath
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioQualityFormatter
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MediaStoreScanner(
    private val context: Context,
) {
    private val metadataCache = mutableMapOf<Long, CachedSongMetadata>()
    private val audioFormatDetector = AudioFormatDetector(context)
    private val embeddedTagMetadataReader = EmbeddedTagMetadataReader()
    private var preferredLibraryFolderPath: String? = null

    fun setPreferredLibraryFolderPath(path: String?): Boolean {
        val cleanedPath = path
            ?.trim()
            ?.ifBlank { null }
            ?.replace('\\', '/')
            ?.trimEnd('/')
        if (normalizeAbsolutePath(preferredLibraryFolderPath.orEmpty()) == normalizeAbsolutePath(cleanedPath.orEmpty())) {
            return false
        }
        preferredLibraryFolderPath = cleanedPath
        return true
    }

    fun currentFilterFingerprint(): String {
        return listOf(
            FILTER_FINGERPRINT_VERSION.toString(),
            normalizeAbsolutePath(preferredLibraryFolderPath.orEmpty()).orEmpty(),
        ).joinToString("::")
    }

    fun primeMetadataCache(
        songs: List<Song>,
    ) {
        metadataCache.clear()
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
            )
            metadataCache[song.id] = CachedSongMetadata(
                fileName = song.fileName,
                filePath = null,
                dateAddedSeconds = song.dateAddedSeconds,
                dateModifiedSeconds = song.dateModifiedSeconds,
                isEnriched = song.metadataResolved,
                metadata = cachedMetadata,
            )
        }
    }

    fun clearMetadataCache() {
        metadataCache.clear()
    }

    fun invalidateMetadataCacheForPaths(paths: Collection<String>) {
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
        metadataCache.entries.removeAll { (_, cached) ->
            cached.filePath in normalizedPaths || cached.fileName in fileNames
        }
    }

    fun invalidateMetadataCacheForSongIds(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        metadataCache.keys.removeAll(songIds.toSet())
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
        val progressThrottler = ScannerProgressThrottler()
        val projection = buildProjection()
        val selection = buildSelection()
        val orderBy = buildOrderBy()
        val audioFileFilter = buildAudioFileFilter()

        context.contentResolver.query(
            audioCollectionUri(),
            projection,
            selection,
            null,
            orderBy,
        )?.use { cursor ->
            totalRows = cursor.count.coerceAtLeast(0)
            emitProgress(onProgress, progressThrottler, 0, totalRows)
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val yearIndex = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val dateModifiedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
            val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            val volumeNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.VOLUME_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
            @Suppress("DEPRECATION")
            val dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

            var processedRows = 0
            while (cursor.moveToNext()) {
                processedRows += 1
                val relativePath = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
                val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val fileSizeBytes = sizeIndex.takeIf { it >= 0 }?.let(cursor::getLong)?.takeIf { it > 0L }
                val durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L)
                val dateAddedSeconds = cursor.getLong(dateAddedIndex)
                val dateModifiedSeconds = dateModifiedIndex
                    .takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let(cursor::getLong)
                    ?.takeIf { it > 0L }
                val volumeName = volumeNameIndex.takeIf { it >= 0 }?.let(cursor::getString)
                val filePath = resolveMediaStoreFilePath(
                    context = context,
                    rawDataPath = dataIndex.takeIf { it >= 0 }?.let(cursor::getString),
                    relativePath = relativePath,
                    displayName = fileName,
                    volumeName = volumeName,
                )
                val mimeType = mimeTypeIndex.takeIf { it >= 0 }?.let(cursor::getString)?.trim()?.ifBlank { null }
                val isMusic = isMusicIndex.takeIf { it >= 0 }?.let(cursor::getInt)?.let { it != 0 }
                val mediaStoreYear = yearIndex.takeIf { it >= 0 }
                    ?.let(cursor::getInt)
                    ?.takeIf { it > 0 }
                val rawTitle = cursor.getString(titleIndex).orUnknown("Untitled Track")
                val rawArtist = cursor.getString(artistIndex).orUnknown("Unknown Artist")
                val rawAlbum = cursor.getString(albumIndex).orUnknown("Unknown Album")
                val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                val detectedFormat = if (AudioFormatPolicy.shouldDetectContainer(extension, enrichMetadata)) {
                    audioFormatDetector.detect(songUri, fileName, mimeType)
                } else {
                    fastDetectedFormat(
                        extension = extension,
                        mimeType = mimeType,
                    )
                }
                val candidate = AudioScanCandidate(
                    id = id,
                    uri = songUri,
                    displayName = fileName,
                    title = rawTitle,
                    artist = rawArtist,
                    album = rawAlbum,
                    durationMs = durationMs,
                    mimeType = mimeType,
                    relativePath = relativePath,
                    absolutePath = filePath,
                    extension = extension,
                    isMusic = isMusic,
                    detectedFormat = detectedFormat,
                )
                when (val decision = audioFileFilter.evaluate(candidate)) {
                    AudioFileFilterDecision.Include -> logPlatformDependentCandidate(candidate)
                    is AudioFileFilterDecision.Exclude -> {
                        logFilteredOutCandidate(candidate, decision.reason)
                        if (processedRows == totalRows || processedRows % 24 == 0) {
                            emitProgress(onProgress, progressThrottler, processedRows, totalRows)
                        }
                        continue
                    }
                }
                val cachedMetadata = metadataCache[id]
                    ?.takeIf {
                        it.fileName == fileName &&
                            (it.filePath == null || filePath == null || it.filePath == filePath) &&
                            it.dateAddedSeconds == dateAddedSeconds &&
                            it.dateModifiedSeconds == dateModifiedSeconds &&
                            (!enrichMetadata || it.isEnriched)
                    }
                val songMetadata = cachedMetadata
                    ?.metadata
                    ?: if (enrichMetadata) {
                        readSongMetadata(
                            songId = id,
                            songUri = songUri,
                            filePath = filePath,
                            mediaStoreYear = mediaStoreYear,
                            fileSizeBytes = fileSizeBytes,
                            durationMs = durationMs,
                            detectedFormat = detectedFormat,
                            genreCache = genreCache,
                        )
                    } else {
                        SongMetadata(
                            title = rawTitle,
                            artist = rawArtist,
                            albumArtist = null,
                            album = rawAlbum,
                            releaseYear = mediaStoreYear,
                            genre = null,
                            format = detectedFormat.displayName,
                            quality = null,
                            trackNumber = null,
                            discNumber = null,
                        )
                    }
                val resolvedTitle = songMetadata.title ?: rawTitle
                val resolvedArtist = songMetadata.artist ?: rawArtist
                val resolvedAlbum = songMetadata.album ?: rawAlbum
                val isExplicit = detectExplicit(resolvedTitle, fileName)
                val title = sanitizeDisplayTitle(resolvedTitle, isExplicit)
                refreshedMetadataCache[id] = CachedSongMetadata(
                    fileName = fileName,
                    filePath = filePath,
                    dateAddedSeconds = dateAddedSeconds,
                    dateModifiedSeconds = dateModifiedSeconds,
                    isEnriched = enrichMetadata || cachedMetadata?.isEnriched == true,
                    metadata = songMetadata,
                )
                val rawTrack = cursor.getInt(trackIndex)
                songs += Song(
                    id = id,
                    title = title,
                    isExplicit = isExplicit,
                    artist = resolvedArtist,
                    album = resolvedAlbum,
                    releaseYear = songMetadata.releaseYear,
                    genre = songMetadata.genre.orUnknown("Unknown Genre"),
                    audioFormat = songMetadata.format,
                    audioQuality = songMetadata.quality,
                    fileName = fileName,
                    albumId = albumId,
                    durationMs = durationMs,
                    trackNumber = songMetadata.trackNumber ?: normalizeTrackNumber(rawTrack),
                    discNumber = songMetadata.discNumber ?: normalizeDiscNumber(rawTrack),
                    dateAddedSeconds = dateAddedSeconds,
                    dateModifiedSeconds = dateModifiedSeconds,
                    uri = songUri,
                    artUri = albumArtworkUri(albumId),
                    metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                    albumArtist = songMetadata.albumArtist,
                )
                if (processedRows == totalRows || processedRows % 24 == 0) {
                    emitProgress(onProgress, progressThrottler, processedRows, totalRows)
                }
            }
        }

        if (totalRows == 0) {
            emitProgress(onProgress, progressThrottler, 1, 1)
        } else {
            emitProgress(onProgress, progressThrottler, totalRows, totalRows)
        }

        metadataCache.clear()
        metadataCache.putAll(refreshedMetadataCache)

        return LibrarySnapshot(
            songs = songs.sortedByDescending { it.dateAddedSeconds },
            albums = buildAlbumsFromSongs(songs),
        )
    }

    internal fun currentSignature(): LibrarySignature {
        var songCount = 0
        var newestDateAddedSeconds = 0L
        var idChecksum = 0L
        val audioFileFilter = buildAudioFileFilter()
        context.contentResolver.query(
            audioCollectionUri(),
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.VOLUME_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATA,
            ),
            buildSelection(),
            null,
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
            val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            val volumeNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.VOLUME_NAME)
            val dateModifiedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
            @Suppress("DEPRECATION")
            val dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                val relativePath = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
                val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
                val volumeName = volumeNameIndex.takeIf { it >= 0 }?.let(cursor::getString)
                val candidate = AudioScanCandidate(
                    id = cursor.getLong(idIndex),
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idIndex)),
                    displayName = fileName,
                    title = cursor.getString(titleIndex),
                    artist = cursor.getString(artistIndex),
                    album = cursor.getString(albumIndex),
                    durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L),
                    mimeType = mimeTypeIndex.takeIf { it >= 0 }?.let(cursor::getString),
                    relativePath = relativePath,
                    absolutePath = resolveMediaStoreFilePath(
                        context = context,
                        rawDataPath = dataIndex.takeIf { it >= 0 }?.let(cursor::getString),
                        relativePath = relativePath,
                        displayName = fileName,
                        volumeName = volumeName,
                    ),
                    extension = fileName.substringAfterLast('.', ""),
                    isMusic = isMusicIndex.takeIf { it >= 0 }?.let(cursor::getInt)?.let { it != 0 },
                    detectedFormat = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                        .takeIf(AudioFormatPolicy::requiresContainerValidation)
                        ?.let {
                            audioFormatDetector.detect(
                                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idIndex)),
                                fileName = fileName,
                                mediaStoreMimeType = mimeTypeIndex.takeIf { index -> index >= 0 }?.let(cursor::getString),
                            )
                        },
                )
                if (audioFileFilter.evaluate(candidate) is AudioFileFilterDecision.Exclude) {
                    continue
                }
                val id = candidate.id
                val dateAddedSeconds = cursor.getLong(dateAddedIndex)
                val modified = dateModifiedIndex
                    .takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let(cursor::getLong)
                    ?.coerceAtLeast(0L)
                    ?: 0L
                songCount += 1
                newestDateAddedSeconds = maxOf(newestDateAddedSeconds, dateAddedSeconds)
                idChecksum = idChecksum xor songSignatureChecksum(
                    id = id,
                    dateAddedSeconds = dateAddedSeconds,
                    dateModifiedSeconds = modified,
                )
            }
        }
        return LibrarySignature(
            songCount = songCount,
            newestDateAddedSeconds = newestDateAddedSeconds,
            idChecksum = idChecksum,
            filterFingerprint = currentFilterFingerprint(),
        )
    }

    fun findExistingSongIds(songIds: Set<Long>): Set<Long> {
        if (songIds.isEmpty()) return emptySet()
        return songIds.chunked(MEDIASTORE_ID_QUERY_CHUNK_SIZE).flatMapTo(linkedSetOf()) { chunk ->
            val placeholders = List(chunk.size) { "?" }.joinToString(",")
            context.contentResolver.query(
                audioCollectionUri(),
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
        val scanRoots = buildScanRoots()
            .filter { it.exists() && it.isDirectory }
            .distinctBy { it.absolutePath }
        if (scanRoots.isEmpty()) return

        val scannerExtensions = AudioFormatPolicy.scannerExtensions
        val pendingChunk = ArrayList<String>(MEDIA_SCANNER_CHUNK_SIZE)

        fun flushChunk() {
            if (pendingChunk.isEmpty()) return
            scanAudioPaths(
                paths = pendingChunk,
                timeoutSeconds = MEDIA_SCAN_TIMEOUT_SECONDS,
            )
            pendingChunk.clear()
        }

        scanRoots.asSequence()
            .flatMap(File::walkTopDown)
            .filter { file -> file.isFile && file.extension.lowercase(Locale.ROOT) in scannerExtensions }
            .map(File::getAbsolutePath)
            .forEach { path ->
                pendingChunk += path
                if (pendingChunk.size >= MEDIA_SCANNER_CHUNK_SIZE) {
                    flushChunk()
                }
            }
        flushChunk()
    }

    fun refreshMediaIndex(paths: List<String>) {
        scanAudioPaths(
            paths = paths,
            timeoutSeconds = TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS,
        )
    }

    private fun scanAudioPaths(
        paths: Iterable<String>,
        timeoutSeconds: Long,
    ) {
        val audioPaths = paths
            .map(::File)
            .filter { file ->
                file.exists() &&
                    file.isFile &&
                    file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions
            }
            .map(File::getAbsolutePath)
            .distinct()
        if (audioPaths.isEmpty()) return

        audioPaths.chunked(MEDIA_SCANNER_CHUNK_SIZE).forEach { chunk ->
            val latch = CountDownLatch(chunk.size)
            MediaScannerConnection.scanFile(
                context,
                chunk.toTypedArray(),
                null,
            ) { _, _ ->
                latch.countDown()
            }
            latch.await(timeoutSeconds, TimeUnit.SECONDS)
        }
    }

    private fun buildProjection(): Array<String> {
        @Suppress("DEPRECATION")
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.VOLUME_NAME,
            MediaStore.MediaColumns.DATA,
        )
    }

    private fun fastDetectedFormat(
        extension: String,
        mimeType: String?,
    ): DetectedAudioFormat {
        val container = AudioFormatPolicy.resolveContainer(extension, mimeType, null)
        return DetectedAudioFormat(
            container = container,
            displayName = AudioFormatPolicy.displayName(container, extension),
            mimeType = mimeType,
            codecMimeType = mimeType,
            detectionSucceeded = false,
            hasAudioTrack = true,
            hasVideoTrack = false,
            decoderAvailable = null,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
        )
    }

    private fun buildSelection(): String {
        val positiveDurationSelection = "${MediaStore.Audio.Media.DURATION} > 0"
        return positiveDurationSelection
    }

    private fun audioCollectionUri(): Uri {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    private fun buildScanRoots(): List<File> {
        val roots = linkedSetOf<File>()
        roots += musicDirectory()
        roots += discoverSecondaryMusicDirectories()
        preferredLibraryFolderPath
            ?.let(::File)
            ?.takeIf { it.exists() && it.isDirectory }
            ?.let(roots::add)
        return roots.toList()
    }

    private fun buildAudioFileFilter(): LibraryAudioFileFilter {
        return LibraryAudioFileFilter(
            preferredMusicFolderPath = preferredLibraryFolderPath,
            preferredRelativeRoots = buildPreferredRelativeLibraryRoots(),
            libraryRootPaths = buildScanRoots()
                .map { normalizeAbsolutePath(it.absolutePath) }
                .toSet(),
        )
    }

    private fun discoverSecondaryMusicDirectories(): List<File> {
        return context
            .getExternalFilesDirs(null)
            .orEmpty()
            .mapNotNull { appSpecificDir ->
                appSpecificDir
                    ?.parentFile
                    ?.parentFile
                    ?.parentFile
                    ?.parentFile
                    ?.resolve(Environment.DIRECTORY_MUSIC)
                    ?.takeIf { it.exists() && it.isDirectory }
            }
            .distinctBy { it.absolutePath }
            .filterNot { it.absolutePath == musicDirectory().absolutePath }
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

    private fun buildPreferredRelativeLibraryRoots(): Set<String> {
        val preferredRoot = preferredLibraryFolderPath
            ?.let(::File)
            ?.takeIf { it.exists() && it.isDirectory }
            ?: return emptySet()
        return setOfNotNull(
            sharedStorageRelativePath(preferredRoot.absolutePath)
                ?.let(::normalizeRelativePath)
                ?.takeIf { it.isNotBlank() },
        )
    }

    private fun normalizeRelativePath(path: String): String {
        return path.trim().trim('/').replace("//", "/")
    }

    private fun normalizeAbsolutePath(path: String): String {
        return path
            .trim()
            .replace('\\', '/')
            .trimEnd('/')
            .lowercase(Locale.ROOT)
    }

    private fun sharedStorageRelativePath(path: String): String? {
        val normalizedPath = path.trim().trimEnd('/').replace("//", "/")
        return STORAGE_ROOT_REGEX
            .replace("$normalizedPath/", "")
            .trim('/')
            .ifBlank { null }
    }

    private fun logFilteredOutCandidate(
        candidate: AudioScanCandidate,
        reason: String,
    ) {
        if (!BuildConfig.DEBUG) return
        val label = candidate.displayName
            ?.takeIf { it.isNotBlank() }
            ?: candidate.title
            ?.takeIf { it.isNotBlank() }
            ?: "audio-${candidate.id}"
        Log.d(TAG, "Excluded $label: $reason")
    }

    private fun logPlatformDependentCandidate(candidate: AudioScanCandidate) {
        if (!BuildConfig.DEBUG) return
        val capability = AudioFormatPolicy.capabilityForExtension(candidate.extension) ?: return
        if (capability.playbackSupport != PlaybackSupport.PlatformDependent) return
        val label = candidate.displayName?.takeIf(String::isNotBlank) ?: "audio-${candidate.id}"
        Log.d(TAG, "Included platform-dependent audio $label (${capability.displayName})")
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
        return runCatching {
            val keyField = MediaMetadataRetriever::class.java.getField("METADATA_KEY_DISC_NUMBER")
            val key = keyField.getInt(null)
            retriever.extractMetadata(key)
                ?.substringBefore('/')
                ?.trim()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
        }.getOrNull()
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

    private fun buildOrderBy(): String {
        val artistColumn = MediaStore.Audio.Media.ARTIST
        val albumColumn = MediaStore.Audio.Media.ALBUM
        return "$artistColumn COLLATE NOCASE ASC, $albumColumn COLLATE NOCASE ASC"
    }

    private fun emitProgress(
        onProgress: ((current: Int, total: Int) -> Unit)?,
        throttler: ScannerProgressThrottler,
        current: Int,
        total: Int,
    ) {
        if (onProgress == null) return
        val safeTotal = total.coerceAtLeast(1)
        val progress = (current.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
        if (throttler.shouldEmit(progress)) {
            onProgress(current, total)
        }
    }

    internal companion object {
        private const val TAG = "LibraryAudioFilter"
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
        const val MEDIA_SCAN_TIMEOUT_SECONDS = 8L
        const val TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS = 5L
        const val MEDIASTORE_ID_QUERY_CHUNK_SIZE = 400
        const val MEDIA_SCANNER_CHUNK_SIZE = 160
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
        val STORAGE_ROOT_REGEX = Regex("""^/storage/[^/]+(?:/[^/]+)?/""")
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

private data class CachedSongMetadata(
    val fileName: String,
    val filePath: String?,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long?,
    val isEnriched: Boolean,
    val metadata: SongMetadata,
)

private class ScannerProgressThrottler(
    private val minStep: Float = 0.01f,
    private val minIntervalMs: Long = 80L,
) {
    private var lastProgress = -1f
    private var lastEmitMs = 0L

    fun shouldEmit(progress: Float): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (progress >= 1f) return true
        if (lastProgress < 0f) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        val enoughProgress = progress - lastProgress >= minStep
        val enoughTime = now - lastEmitMs >= minIntervalMs
        if (enoughProgress || enoughTime) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        return false
    }
}

private data class SongMetadata(
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
)

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
