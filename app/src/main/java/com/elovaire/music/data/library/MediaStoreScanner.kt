package elovaire.music.droidbeauty.app.data.library

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.Id3Decoder
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class MediaStoreScanner(
    private val context: Context,
) {
    private val metadataCache = mutableMapOf<Long, CachedSongMetadata>()
    private var preferredLibraryFolderPath: String? = null

    fun setPreferredLibraryFolderPath(path: String?) {
        preferredLibraryFolderPath = path?.trim()?.ifBlank { null }
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
                dateAddedSeconds = song.dateAddedSeconds,
                isEnriched = song.metadataResolved,
                metadata = cachedMetadata,
            )
        }
    }

    fun clearMetadataCache() {
        metadataCache.clear()
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

        var totalSongs = 0
        val songs = mutableListOf<Song>()
        val refreshedMetadataCache = mutableMapOf<Long, CachedSongMetadata>()
        val projection = buildProjection()
        val selection = buildSelection()
        val orderBy = buildOrderBy()
        val allowedRelativeRoots = buildAllowedRelativeLibraryRoots()

        context.contentResolver.query(
            audioCollectionUri(),
            projection,
            selection,
            null,
            orderBy,
        )?.use { cursor ->
            totalSongs = cursor.count.coerceAtLeast(0)
            onProgress?.invoke(0, totalSongs)
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
            val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

            var scannedSongs = 0
            while (cursor.moveToNext()) {
                val relativePath = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
                if (!shouldIncludeRelativePath(relativePath, allowedRelativeRoots)) {
                    continue
                }
                val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
                if (!isSupportedAudioFileName(fileName)) {
                    continue
                }
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val fileSizeBytes = sizeIndex.takeIf { it >= 0 }?.let(cursor::getLong)?.takeIf { it > 0L }
                val durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L)
                val dateAddedSeconds = cursor.getLong(dateAddedIndex)
                val mediaStoreYear = yearIndex.takeIf { it >= 0 }
                    ?.let(cursor::getInt)
                    ?.takeIf { it > 0 }
                val cachedMetadata = metadataCache[id]
                    ?.takeIf {
                        it.fileName == fileName &&
                            it.dateAddedSeconds == dateAddedSeconds &&
                            (!enrichMetadata || it.isEnriched)
                    }
                val songMetadata = cachedMetadata
                    ?.metadata
                    ?: if (enrichMetadata) {
                        readSongMetadata(
                            songId = id,
                            songUri = songUri,
                            fileName = fileName,
                            mediaStoreYear = mediaStoreYear,
                            fileSizeBytes = fileSizeBytes,
                            durationMs = durationMs,
                        )
                    } else {
                        SongMetadata(
                            title = null,
                            artist = null,
                            album = null,
                            releaseYear = mediaStoreYear,
                            genre = null,
                            format = fileName.substringAfterLast('.', "").uppercase().ifBlank { "AUDIO" },
                            quality = null,
                            trackNumber = null,
                            discNumber = null,
                        )
                    }
                val rawTitle = songMetadata.title
                    ?: cursor.getString(titleIndex).orUnknown("Untitled Track")
                val rawArtist = songMetadata.artist
                    ?: cursor.getString(artistIndex).orUnknown("Unknown Artist")
                val rawAlbum = songMetadata.album
                    ?: cursor.getString(albumIndex).orUnknown("Unknown Album")
                val isExplicit = detectExplicit(rawTitle, fileName)
                val title = sanitizeDisplayTitle(rawTitle, isExplicit)
                refreshedMetadataCache[id] = CachedSongMetadata(
                    fileName = fileName,
                    dateAddedSeconds = dateAddedSeconds,
                    isEnriched = enrichMetadata || cachedMetadata?.isEnriched == true,
                    metadata = songMetadata,
                )
                val rawTrack = cursor.getInt(trackIndex)
                songs += Song(
                    id = id,
                    title = title,
                    isExplicit = isExplicit,
                    artist = rawArtist,
                    album = rawAlbum,
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
                    uri = songUri,
                    artUri = albumArtworkUri(albumId),
                    metadataResolved = enrichMetadata || cachedMetadata?.isEnriched == true,
                )
                scannedSongs += 1
                if (scannedSongs == totalSongs || scannedSongs % 24 == 0) {
                    onProgress?.invoke(scannedSongs, totalSongs)
                }
            }
        }

        if (totalSongs == 0) {
            onProgress?.invoke(1, 1)
        } else {
            onProgress?.invoke(totalSongs, totalSongs)
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
        val allowedRelativeRoots = buildAllowedRelativeLibraryRoots()
        context.contentResolver.query(
            audioCollectionUri(),
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH,
            ),
            buildSelection(),
            null,
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val relativePath = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
                if (!shouldIncludeRelativePath(relativePath, allowedRelativeRoots)) {
                    continue
                }
                val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
                if (!isSupportedAudioFileName(fileName)) {
                    continue
                }
                val id = cursor.getLong(idIndex)
                val dateAddedSeconds = cursor.getLong(dateAddedIndex)
                songCount += 1
                newestDateAddedSeconds = maxOf(newestDateAddedSeconds, dateAddedSeconds)
                idChecksum = idChecksum xor (id shl 1) xor dateAddedSeconds
            }
        }
        return LibrarySignature(
            songCount = songCount,
            newestDateAddedSeconds = newestDateAddedSeconds,
            idChecksum = idChecksum,
        )
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

        val audioPaths = scanRoots
            .asSequence()
            .flatMap { root -> root.walkTopDown() }
            .filter { file -> file.isFile && file.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS }
            .map(File::getAbsolutePath)
            .toList()

        if (audioPaths.isEmpty()) return

        val latch = CountDownLatch(audioPaths.size)
        MediaScannerConnection.scanFile(
            context,
            audioPaths.toTypedArray(),
            null,
        ) { _, _ ->
            latch.countDown()
        }
        latch.await(MEDIA_SCAN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    fun refreshMediaIndex(paths: List<String>) {
        val audioPaths = paths
            .map(::File)
            .filter { file ->
                file.exists() &&
                    file.isFile &&
                    file.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS
            }
            .map(File::getAbsolutePath)
            .distinct()
        if (audioPaths.isEmpty()) return

        val latch = CountDownLatch(audioPaths.size)
        MediaScannerConnection.scanFile(
            context,
            audioPaths.toTypedArray(),
            null,
        ) { _, _ ->
            latch.countDown()
        }
        latch.await(TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
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
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.MediaColumns.RELATIVE_PATH,
        )
    }

    private fun buildSelection(): String {
        val isMusicSelection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val audioMimeSelection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%'"
        val positiveDurationSelection = "${MediaStore.Audio.Media.DURATION} > 0"
        return "(($isMusicSelection) OR ($audioMimeSelection)) AND ($positiveDurationSelection)"
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

    private fun buildAllowedRelativeLibraryRoots(): List<String> {
        return buildScanRoots()
            .mapNotNull { root ->
                sharedStorageRelativePath(root.absolutePath)
                    ?.let(::normalizeRelativePath)
                    ?.takeIf { it.isNotBlank() }
            }
            .distinct()
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

    private fun shouldIncludeRelativePath(
        relativePath: String?,
        allowedRelativeRoots: List<String>,
    ): Boolean {
        if (allowedRelativeRoots.isEmpty()) return true
        val normalizedRelativePath = relativePath
            ?.let(::normalizeRelativePath)
            ?.takeIf { it.isNotBlank() }
        if (normalizedRelativePath != null && allowedRelativeRoots.any { allowedRoot ->
                normalizedRelativePath == allowedRoot || normalizedRelativePath.startsWith("$allowedRoot/")
            }
        ) {
            return true
        }
        return normalizedRelativePath == null
    }

    private fun normalizeRelativePath(path: String): String {
        return path.trim().trim('/').replace("//", "/")
    }

    private fun sharedStorageRelativePath(path: String): String? {
        val normalizedPath = path.trim().trimEnd('/').replace("//", "/")
        return STORAGE_ROOT_REGEX
            .replace("$normalizedPath/", "")
            .trim('/')
            .ifBlank { null }
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
        fileName: String,
        mediaStoreYear: Int?,
        fileSizeBytes: Long?,
        durationMs: Long,
    ): SongMetadata {
        val extractorMetadata = readExtractorMetadata(songUri)
        val retrieverMetadata = readRetrieverMetadata(songUri, fileName)
        val resolvedFormat = resolveAudioFormat(
            fileName = fileName,
            mimeType = extractorMetadata.mimeType,
            retrieverMimeType = retrieverMetadata.mimeType,
            bitDepth = retrieverMetadata.bitDepth,
        )
        val year = retrieverMetadata.year ?: mediaStoreYear
        val sampleRate = retrieverMetadata.sampleRate ?: extractorMetadata.sampleRate
        val bitDepth = retrieverMetadata.bitDepth
        val bitrate = retrieverMetadata.bitrate
            ?: extractorMetadata.bitrate
            ?: estimateBitrateBitsPerSecond(
                fileSizeBytes = fileSizeBytes,
                durationMs = durationMs,
                resolvedFormat = resolvedFormat,
            )
        val genre = retrieverMetadata.genre ?: queryGenre(songId)
        return SongMetadata(
            title = retrieverMetadata.title,
            artist = retrieverMetadata.artist,
            album = retrieverMetadata.album,
            releaseYear = year,
            genre = genre,
            format = resolvedFormat,
            quality = formatAudioQuality(
                format = resolvedFormat,
                bitDepth = bitDepth,
                sampleRate = sampleRate,
                bitrate = bitrate,
            ),
            trackNumber = retrieverMetadata.trackNumber,
            discNumber = retrieverMetadata.discNumber,
        )
    }

    private fun readRetrieverMetadata(songUri: Uri, fileName: String): RetrieverMetadata {
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
                    mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
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

    @UnstableApi
    private fun parseId3Metadata(id3Bytes: ByteArray): RetrieverMetadata {
        val metadata = runCatching { Id3Decoder().decode(id3Bytes, id3Bytes.size) }.getOrNull() ?: return RetrieverMetadata()
        var title: String? = null
        var artist: String? = null
        var albumArtist: String? = null
        var album: String? = null
        var year: Int? = null
        var genre: String? = null
        var trackNumber: Int? = null
        var discNumber: Int? = null
        for (index in 0 until metadata.length()) {
            val entry = metadata.get(index)
            if (entry is TextInformationFrame) {
                val value = entry.values.firstOrNull()?.trim()?.takeIf { it.isNotBlank() } ?: continue
                when (entry.id.uppercase(Locale.ROOT)) {
                    "TIT2", "TT2" -> title = title ?: value
                    "TPE1", "TP1" -> artist = artist ?: value
                    "TPE2", "TP2" -> albumArtist = albumArtist ?: value
                    "TALB", "TAL" -> album = album ?: value
                    "TDRC", "TYER", "TORY", "TDAT" -> year = year ?: parseYearFromDateTag(value)
                    "TCON", "TCO" -> genre = genre ?: value.substringBefore(';').substringBefore('/').trim().ifBlank { null }
                    "TRCK", "TRK" -> trackNumber = trackNumber ?: parseTrackNumberTag(value)
                    "TPOS" -> discNumber = discNumber ?: value.substringBefore('/').trim().toIntOrNull()?.takeIf { it > 0 }
                }
            }
        }
        return RetrieverMetadata(
            title = title,
            artist = artist ?: albumArtist,
            album = album,
            year = year,
            genre = genre,
            trackNumber = trackNumber,
            discNumber = discNumber,
        )
    }

    private fun readExtractorMetadata(songUri: Uri): ExtractorMetadata {
        return runCatching {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(context, songUri, emptyMap())
                val format = (0 until extractor.trackCount)
                    .asSequence()
                    .map(extractor::getTrackFormat)
                    .firstOrNull { trackFormat ->
                        trackFormat.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                    }
                ExtractorMetadata(
                    sampleRate = format?.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE),
                    bitrate = format?.getIntegerOrNull(MediaFormat.KEY_BIT_RATE),
                    mimeType = format?.getString(MediaFormat.KEY_MIME),
                )
            } finally {
                runCatching { extractor.release() }
            }
        }.getOrDefault(ExtractorMetadata())
    }

    private fun queryGenre(songId: Long): String? {
        val volumeNames = buildList {
            add("external")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.VOLUME_EXTERNAL)
                add(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
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

    private fun RetrieverMetadata.mergeWith(fallback: RetrieverMetadata): RetrieverMetadata {
        return RetrieverMetadata(
            title = title ?: fallback.title,
            artist = artist ?: fallback.artist,
            album = album ?: fallback.album,
            year = year ?: fallback.year,
            sampleRate = sampleRate ?: fallback.sampleRate,
            bitDepth = bitDepth ?: fallback.bitDepth,
            bitrate = bitrate ?: fallback.bitrate,
            mimeType = mimeType ?: fallback.mimeType,
            genre = genre ?: fallback.genre,
            trackNumber = trackNumber ?: fallback.trackNumber,
            discNumber = discNumber ?: fallback.discNumber,
        )
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

    private fun formatAudioQuality(
        format: String,
        bitDepth: Int?,
        sampleRate: Int?,
        bitrate: Int?,
    ): String? {
        val sampleRateText = sampleRate?.takeIf { it > 0 }?.let(::formatSampleRate)
        val normalizedFormat = format.uppercase()
        return when {
            isLosslessFormat(normalizedFormat) && bitDepth != null && bitDepth > 0 && sampleRateText != null ->
                "${bitDepth}/${sampleRateText}"
            isLossyFormat(normalizedFormat) && bitrate != null && bitrate > 0 && sampleRateText != null ->
                "${(bitrate / 1000f).roundQuality()}/${sampleRateText}"
            bitDepth != null && bitDepth > 0 && sampleRateText != null -> "${bitDepth}/${sampleRateText}"
            bitrate != null && bitrate > 0 && sampleRateText != null -> "${(bitrate / 1000f).roundQuality()}/${sampleRateText}"
            sampleRateText != null -> sampleRateText
            bitrate != null && bitrate > 0 -> "${(bitrate / 1000f).roundQuality()}kbps"
            else -> null
        }
    }

    private fun resolveAudioFormat(
        fileName: String,
        mimeType: String? = null,
        retrieverMimeType: String? = null,
        bitDepth: Int? = null,
    ): String {
        val extension = fileName.substringAfterLast('.', "").uppercase().ifBlank { "AUDIO" }
        return when {
            mimeType.equals("audio/mpeg", ignoreCase = true) -> "MP3"
            mimeType.equals("audio/flac", ignoreCase = true) -> "FLAC"
            mimeType.equals("audio/ogg", ignoreCase = true) -> "OGG"
            mimeType.equals("audio/opus", ignoreCase = true) -> "OPUS"
            mimeType.equals("audio/aac", ignoreCase = true) -> "AAC"
            mimeType.equals("audio/mp4a-latm", ignoreCase = true) && extension == "M4A" -> "AAC"
            mimeType.equals("audio/wav", ignoreCase = true) || mimeType.equals("audio/x-wav", ignoreCase = true) -> "WAV"
            else -> extension
        }
    }

    private fun formatSampleRate(sampleRate: Int): String {
        val khz = sampleRate / 1000f
        val rounded = if (khz % 1f == 0f) khz.toInt().toString() else String.format(Locale.ROOT, "%.1f", khz)
        return "${rounded}kHz"
    }

    private fun Float.roundQuality(): String {
        val rounded = kotlin.math.round(this)
        return if (kotlin.math.abs(this - rounded) < 0.05f) rounded.toInt().toString() else String.format(Locale.ROOT, "%.1f", this)
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

    internal companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
        const val MEDIA_SCAN_TIMEOUT_SECONDS = 8L
        const val TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS = 5L
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
        val SUPPORTED_AUDIO_EXTENSIONS = setOf(
            "mp3",
            "m4a",
            "aac",
            "flac",
            "wav",
            "ogg",
            "opus",
            "amr",
            "3gp",
            "mp4",
            "mka",
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

    private fun MediaFormat.getIntegerOrNull(key: String): Int? {
        return if (containsKey(key)) getInteger(key) else null
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
                { it.fileName.lowercase() },
            ),
        )
    } else {
        albumSongs.sortedBy { it.fileName.lowercase() }
    }
}

private data class CachedSongMetadata(
    val fileName: String,
    val dateAddedSeconds: Long,
    val isEnriched: Boolean,
    val metadata: SongMetadata,
)

private data class SongMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val releaseYear: Int?,
    val genre: String?,
    val format: String,
    val quality: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
)

private data class ExtractorMetadata(
    val sampleRate: Int? = null,
    val bitrate: Int? = null,
    val mimeType: String? = null,
)

private data class RetrieverMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val year: Int? = null,
    val sampleRate: Int? = null,
    val bitDepth: Int? = null,
    val bitrate: Int? = null,
    val mimeType: String? = null,
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
    return extension.lowercase() in MediaStoreScanner.SUPPORTED_AUDIO_EXTENSIONS
}

internal fun isSupportedAudioFileName(fileName: String): Boolean {
    return fileName.substringAfterLast('.', "").let(::isSupportedAudioExtension)
}

internal fun isSupportedLibrarySong(song: Song): Boolean {
    return isSupportedAudioFileName(song.fileName)
}

private val LOSSY_AUDIO_FORMATS = setOf("MP3", "AAC", "OGG", "OPUS", "AMR", "3GP", "MP4", "M4A")
private val LOSSLESS_AUDIO_FORMATS = setOf("FLAC", "WAV")
private val LOSSLESS_QUALITY_REGEX = Regex("""\d{1,2}/\d{1,3}(?:\.\d)?kHz""")
