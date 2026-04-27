package elovaire.music.app.data.library

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
import elovaire.music.app.domain.model.LibrarySnapshot
import elovaire.music.app.domain.model.Song
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MediaStoreScanner(
    private val context: Context,
) {
    private val metadataCache = mutableMapOf<Long, CachedSongMetadata>()

    fun primeMetadataCache(
        songs: List<Song>,
    ) {
        metadataCache.clear()
        songs.forEach { song ->
            val hasMeaningfulGenre = song.genre.isNotBlank() && song.genre != "Unknown Genre"
            metadataCache[song.id] = CachedSongMetadata(
                fileName = song.fileName,
                dateAddedSeconds = song.dateAddedSeconds,
                isEnriched = song.releaseYear != null || hasMeaningfulGenre || !song.audioQuality.isNullOrBlank(),
                metadata = SongMetadata(
                    releaseYear = song.releaseYear,
                    genre = song.genre.takeIf { hasMeaningfulGenre },
                    format = song.audioFormat,
                    quality = song.audioQuality,
                ),
            )
        }
    }

    fun scan(
        refreshMediaIndex: Boolean = false,
        enrichMetadata: Boolean = true,
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
    ): LibrarySnapshot {
        if (refreshMediaIndex) {
            refreshMediaIndex()
        }

        var totalSongs = 0
        val songs = mutableListOf<Song>()
        val refreshedMetadataCache = mutableMapOf<Long, CachedSongMetadata>()
        val projection = buildProjection()
        val selection = buildSelection()
        val selectionArgs = buildSelectionArgs()
        val orderBy = buildOrderBy()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
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
            val yearIndex = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

            var scannedSongs = 0
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val title = cursor.getString(titleIndex).orUnknown("Untitled Track")
                val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
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
                        )
                    } else {
                        SongMetadata(
                            releaseYear = mediaStoreYear,
                            genre = null,
                            format = fileName.substringAfterLast('.', "").uppercase().ifBlank { "AUDIO" },
                            quality = null,
                        )
                    }
                refreshedMetadataCache[id] = CachedSongMetadata(
                    fileName = fileName,
                    dateAddedSeconds = dateAddedSeconds,
                    isEnriched = enrichMetadata,
                    metadata = songMetadata,
                )
                val rawTrack = cursor.getInt(trackIndex)
                songs += Song(
                    id = id,
                    title = title,
                    isExplicit = detectExplicit(title, fileName),
                    artist = cursor.getString(artistIndex).orUnknown("Unknown Artist"),
                    album = cursor.getString(albumIndex).orUnknown("Unknown Album"),
                    releaseYear = songMetadata.releaseYear,
                    genre = songMetadata.genre.orUnknown("Unknown Genre"),
                    audioFormat = songMetadata.format,
                    audioQuality = songMetadata.quality,
                    fileName = fileName,
                    albumId = albumId,
                    durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L),
                    trackNumber = normalizeTrackNumber(rawTrack),
                    discNumber = normalizeDiscNumber(rawTrack),
                    dateAddedSeconds = dateAddedSeconds,
                    uri = songUri,
                    artUri = albumArtworkUri(albumId),
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
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATE_ADDED,
            ),
            buildSelection(),
            buildSelectionArgs(),
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
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

    private fun querySongCount(): Int {
        var count = 0
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            buildSelection(),
            buildSelectionArgs(),
            null,
        )?.use { cursor ->
            count = cursor.count.coerceAtLeast(0)
        }
        return count
    }

    fun musicDirectory(): File {
        @Suppress("DEPRECATION")
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    }

    fun refreshMediaIndex() {
        val musicDir = musicDirectory()
        if (!musicDir.exists() || !musicDir.isDirectory) return

        val audioPaths = musicDir
            .walkTopDown()
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

    private fun buildProjection(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
        )
    }

    private fun buildSelection(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (" +
                "${MediaStore.Audio.Media.RELATIVE_PATH} = ? OR " +
                "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?)"
        } else {
            @Suppress("DEPRECATION")
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DATA} LIKE ?"
        }
    }

    private fun buildSelectionArgs(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                "${Environment.DIRECTORY_MUSIC}/",
                "${Environment.DIRECTORY_MUSIC}/%",
            )
        } else {
            arrayOf("${musicDirectory().absolutePath}/%")
        }
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
        }
    }

    private fun readSongMetadata(
        songId: Long,
        songUri: Uri,
        fileName: String,
        mediaStoreYear: Int?,
    ): SongMetadata {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, songUri)
                val extractorMetadata = readExtractorMetadata(songUri)
                val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?.take(4)
                    ?.toIntOrNull()
                    ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                        ?.let(::parseYearFromDateTag)
                    ?: mediaStoreYear
                val sampleRate = extractRetrieverSampleRate(retriever) ?: extractorMetadata.sampleRate
                val bitDepth = extractRetrieverBitDepth(retriever)
                val bitrate = runCatching {
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
                }.getOrNull() ?: extractorMetadata.bitrate
                val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    ?.substringBefore(';')
                    ?.substringBefore('/')
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: queryGenre(songId)

                SongMetadata(
                    releaseYear = year,
                    genre = genre,
                    format = resolveAudioFormat(
                        fileName = fileName,
                        mimeType = extractorMetadata.mimeType,
                    ),
                    quality = formatAudioQuality(
                        format = resolveAudioFormat(
                            fileName = fileName,
                            mimeType = extractorMetadata.mimeType,
                        ),
                        bitDepth = bitDepth,
                        sampleRate = sampleRate,
                        bitrate = bitrate,
                    ),
                )
            } finally {
                runCatching { retriever.release() }
            }
        }.getOrElse {
            SongMetadata(
                releaseYear = null,
                genre = null,
                format = resolveAudioFormat(fileName = fileName),
                quality = null,
            )
        }
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
        val genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", songId.toInt())
        return runCatching {
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

    private fun parseYearFromDateTag(value: String): Int? {
        return YEAR_REGEX.find(value)?.value?.toIntOrNull()
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
    ): String {
        val extension = fileName.substringAfterLast('.', "").uppercase().ifBlank { "AUDIO" }
        return when {
            mimeType.equals("audio/mpeg", ignoreCase = true) -> "MP3"
            mimeType.equals("audio/flac", ignoreCase = true) -> "FLAC"
            mimeType.equals("audio/ogg", ignoreCase = true) -> "OGG"
            mimeType.equals("audio/opus", ignoreCase = true) -> "OPUS"
            mimeType.equals("audio/alac", ignoreCase = true) -> "ALAC"
            mimeType.equals("audio/aac", ignoreCase = true) -> "AAC"
            mimeType.equals("audio/mp4a-latm", ignoreCase = true) && extension == "M4A" -> "AAC"
            mimeType.equals("audio/wav", ignoreCase = true) || mimeType.equals("audio/x-wav", ignoreCase = true) -> "WAV"
            mimeType.equals("audio/aiff", ignoreCase = true) || mimeType.equals("audio/x-aiff", ignoreCase = true) -> "AIFF"
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

    private fun buildOrderBy(): String {
        val artistColumn = MediaStore.Audio.Media.ARTIST
        val albumColumn = MediaStore.Audio.Media.ALBUM
        return "$artistColumn COLLATE NOCASE ASC, $albumColumn COLLATE NOCASE ASC"
    }

    private companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
        const val MEDIA_SCAN_TIMEOUT_SECONDS = 8L
        val YEAR_REGEX = Regex("""\b(19|20)\d{2}\b""")
        val EXPLICIT_MARKERS = listOf(
            "(explicit)",
            "[explicit]",
            " - explicit",
            " explicit version",
        )
        val SUPPORTED_AUDIO_EXTENSIONS = setOf(
            "mp3",
            "m4a",
            "aac",
            "flac",
            "wav",
            "alac",
            "ogg",
            "opus",
            "wma",
            "ape",
            "amr",
            "3gp",
            "mp4",
            "mka",
        )
    }
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
    val releaseYear: Int?,
    val genre: String?,
    val format: String,
    val quality: String?,
)

private data class ExtractorMetadata(
    val sampleRate: Int? = null,
    val bitrate: Int? = null,
    val mimeType: String? = null,
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

private val LOSSY_AUDIO_FORMATS = setOf("MP3", "AAC", "OGG", "OPUS", "WMA", "AMR", "3GP", "MP4", "M4A")
private val LOSSLESS_AUDIO_FORMATS = setOf("FLAC", "ALAC", "WAV", "AIFF", "APE")
private val LOSSLESS_QUALITY_REGEX = Regex("""\d{1,2}/\d{1,3}(?:\.\d)?kHz""")
