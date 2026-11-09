package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.platform.readWithProgress

import android.content.Context
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.net.Uri
import elovaire.music.droidbeauty.app.data.audio.CanonicalMetadataResolver
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadata
import elovaire.music.droidbeauty.app.data.audio.EmbeddedTagMetadataReader
import elovaire.music.droidbeauty.app.data.audio.MetadataSourceValues
import elovaire.music.droidbeauty.app.data.audio.toMetadataSourceValues
import elovaire.music.droidbeauty.app.data.library.MediaFailureCategory
import elovaire.music.droidbeauty.app.data.library.LocalAudioMetadataReader
import elovaire.music.droidbeauty.app.data.library.platformMetadataGenre
import elovaire.music.droidbeauty.app.data.library.platformMetadataNumber
import elovaire.music.droidbeauty.app.data.library.platformMetadataYear
import elovaire.music.droidbeauty.app.data.library.MediaFailureDomain
import elovaire.music.droidbeauty.app.data.library.MediaFailureKey
import elovaire.music.droidbeauty.app.data.library.MediaFailureRegistry
import elovaire.music.droidbeauty.app.data.library.mediaFailureCategory
import elovaire.music.droidbeauty.app.data.audio.MediaMetadataRetrieverAdmission
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import java.io.File
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@Suppress("TooGenericExceptionCaught")
internal class NetworkMetadataReader(
    private val registry: NetworkFileSystemRegistry,
    context: Context,
    private val failureRegistry: MediaFailureRegistry = MediaFailureRegistry(),
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
    private val retrieverAdmission: MediaMetadataRetrieverAdmission = MediaMetadataRetrieverAdmission(),
    private val availableSpace: (File) -> Long = { it.usableSpace },
) {
    private val cacheDir = context.applicationContext.cacheDir
    private val embeddedReader = EmbeddedTagMetadataReader(context)
    private val localReader = LocalAudioMetadataReader(context, failureRegistry, resourceTracker, retrieverAdmission)

    suspend fun read(source: NetworkLibrarySource, entry: NetworkFileEntry, force: Boolean = false): NetworkMetadataReadResult? {
        val size = entry.sizeBytes
        if (size != null && size <= 0L) return null
        val failureKey = MediaFailureKey(
            identity = "${source.id}:${entry.path}",
            revision = "${entry.sizeBytes}:${entry.modifiedAtMs}:${entry.etag}",
            domain = MediaFailureDomain.Metadata,
        )
        if (failureRegistry.shouldSuppress(failureKey, force)) return null
        val metadataResource = resourceTracker.acquire(BackendResourceKind.ActiveMetadataRead)
        return try {
            cleanStaleMetadataCopies(cacheDir)
            val reserve = maxOf(MINIMUM_FREE_BYTES, cacheDir.totalSpace / 20L)
            val usable = availableSpace(cacheDir)
            val copyLimit = (usable - reserve).coerceAtLeast(0L)
            val windowLimit = maxOf(copyLimit, (usable - MINIMUM_WINDOW_FREE_BYTES).coerceIn(0L, MAX_WINDOW_BYTES))
            if (copyLimit > 0L && (size == null || size <= copyLimit)) {
                return readStaged(source, entry, size, copyLimit, reserve).also { failureRegistry.recordSuccess(failureKey) }
            }
            if (size == null) {
                failureRegistry.recordFailure(failureKey, MediaFailureCategory.Resource)
                return null
            }
            val platform = try {
                retrieverAdmission.withPermit {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(RangeMediaDataSource(registry, source, entry, size))
                        NetworkMetadataReadResult(
                            succeeded = false,
                            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLongOrNull()
                                ?.takeIf { it > 0L },
                            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                            albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                            releaseYear = platformMetadataYear(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)) {
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                            },
                            genre = platformMetadataGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)),
                            trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                                ?.let(::platformMetadataNumber),
                            discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                                ?.let(::platformMetadataNumber),
                        )
                    } finally {
                        retriever.release()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: IOException) {
                failureRegistry.recordFailure(failureKey, MediaFailureCategory.TransientIo)
                null
            } catch (failure: IllegalArgumentException) {
                failureRegistry.recordFailure(failureKey, mediaFailureCategory(failure))
                null
            } catch (failure: IllegalStateException) {
                failureRegistry.recordFailure(failureKey, mediaFailureCategory(failure))
                null
            }
            val embedded = try {
                when {
                    entry.path.endsWith(".mp3", ignoreCase = true) -> readWindowedMp3(source, entry, size, windowLimit)
                    entry.path.endsWith(".flac", ignoreCase = true) -> readWindowedFlac(source, entry, size, windowLimit)
                    entry.path.endsWith(".wav", ignoreCase = true) -> readWindowedWav(source, entry, size, windowLimit)
                    entry.path.substringAfterLast('.', "").lowercase() in setOf("ogg", "oga") ->
                        readWindowedOgg(source, entry, size, windowLimit)
                    entry.path.substringAfterLast('.', "").lowercase() in setOf("m4a", "m4b", "mp4") ->
                        readWindowedMp4(source, entry, size, windowLimit)
                    else -> null
                }
            } catch (_: IOException) {
                null
            }
            if (embedded == null) {
                if (platform == null) return null
                failureRegistry.recordFailure(failureKey, MediaFailureCategory.Resource)
                platform
            } else {
                if (platform != null) failureRegistry.recordSuccess(failureKey)
                platform?.copy(succeeded = true, embedded = embedded) ?: NetworkMetadataReadResult(
                    succeeded = false, durationMs = null, title = null, artist = null, albumArtist = null,
                    album = null, releaseYear = null, genre = null, trackNumber = null, discNumber = null,
                    embedded = embedded,
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            failureRegistry.recordFailure(failureKey, MediaFailureCategory.TransientIo)
            null
        } catch (failure: IllegalArgumentException) {
            failureRegistry.recordFailure(failureKey, mediaFailureCategory(failure))
            null
        } catch (failure: IllegalStateException) {
            failureRegistry.recordFailure(failureKey, mediaFailureCategory(failure))
            null
        } finally {
            metadataResource.close()
        }
    }

    private suspend fun readStaged(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long?,
        copyLimit: Long,
        reserve: Long,
    ): NetworkMetadataReadResult {
        var temp: File? = null
        return try {
            val extension = entry.path.substringAfterLast('.', "tmp").take(12).filter(Char::isLetterOrDigit)
            val file = File.createTempFile("network-tag-", ".$extension", cacheDir)
            temp = file
            registry.openBlocking(source.id, entry.path, 0L, -1L, NetworkReadPurpose.Metadata).use { handle ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    var nextSpaceCheck = SPACE_CHECK_INTERVAL_BYTES
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = handle.input.readWithProgress(buffer)
                        if (count < 0) break
                        total += count
                        if (total > copyLimit || (size != null && total > size)) {
                            throw IOException("Network file exceeded the metadata copy limit")
                        }
                        if (total >= nextSpaceCheck) {
                            if (availableSpace(cacheDir) < reserve) throw IOException("Insufficient space for network metadata")
                            nextSpaceCheck = total + SPACE_CHECK_INTERVAL_BYTES
                        }
                        output.write(buffer, 0, count)
                    }
                    if (total == 0L || (size != null && total != size)) {
                        throw IOException("Network file was truncated during metadata read")
                    }
                }
            }
            val metadata = localReader.read(Uri.fromFile(file), file.absolutePath, entry.path.substringAfterLast('/'))
            if (metadata.durationMs == null && metadata.title == null && metadata.artist == null &&
                metadata.albumArtist == null && metadata.album == null && metadata.description == null &&
                metadata.releaseYear == null && metadata.genre == null && metadata.trackNumber == null &&
                metadata.discNumber == null && metadata.volumeNormalization == null
            ) throw IOException("Unable to read network file metadata")
            NetworkMetadataReadResult(
                succeeded = true,
                durationMs = metadata.durationMs,
                title = null,
                artist = null,
                albumArtist = null,
                album = null,
                releaseYear = null,
                genre = null,
                trackNumber = null,
                discNumber = null,
                embedded = EmbeddedTagMetadata(
                    title = metadata.title,
                    artist = metadata.artist,
                    albumArtist = metadata.albumArtist,
                    album = metadata.album,
                    description = metadata.description,
                    releaseYear = metadata.releaseYear,
                    genre = metadata.genre,
                    trackNumber = metadata.trackNumber,
                    discNumber = metadata.discNumber,
                    volumeNormalization = metadata.volumeNormalization,
                ),
            )
        } finally {
            temp?.delete()
        }
    }

    private suspend fun readWindowedMp3(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        if (!entry.path.endsWith(".mp3", ignoreCase = true) || size < 10L || copyLimit < MP3_AUDIO_PREFIX_BYTES) return null
        val header = ByteArray(10)
        registry.openBlocking(source.id, entry.path, 0L, header.size.toLong(), NetworkReadPurpose.Metadata).use { handle ->
            var offset = 0
            while (offset < header.size) {
                currentCoroutineContext().ensureActive()
                val count = handle.input.readWithProgress(header, offset, header.size - offset)
                if (count < 0) throw IOException("Network MP3 header was truncated")
                offset += count
            }
        }
        val prefix = mp3MetadataPrefix(header, size, copyLimit) ?: return null
        val tail = minOf(size - prefix, 128L)
        val temporary = File.createTempFile("network-tag-", ".mp3", cacheDir)
        try {
            temporary.outputStream().use { output ->
                copyRemoteRange(source, entry, 0L, prefix, output)
                copyRemoteRange(source, entry, size - tail, tail, output)
            }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private suspend fun readWindowedFlac(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        val prefix = flacMetadataPrefix(size, copyLimit) { position -> readRemoteBytes(source, entry, position, 4) }
            ?: return null
        val temporary = File.createTempFile("network-tag-", ".flac", cacheDir)
        try {
            temporary.outputStream().use { copyRemoteRange(source, entry, 0L, prefix, it) }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private suspend fun readWindowedMp4(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        val range = mp4MetadataRange(size, copyLimit) { position, length ->
            readRemoteBytes(source, entry, position, length)
        } ?: return null
        val temporary = File.createTempFile("network-tag-", ".m4a", cacheDir)
        try {
            temporary.outputStream().use { copyRemoteRange(source, entry, range.first, range.second, it) }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private suspend fun readWindowedOgg(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        val prefix = oggVorbisMetadataPrefix(size, copyLimit) { position, length ->
            readRemoteBytes(source, entry, position, length)
        } ?: return null
        val tail = minOf(size - prefix, OGG_TAIL_BYTES)
        val temporary = File.createTempFile("network-tag-", ".ogg", cacheDir)
        try {
            temporary.outputStream().use { output ->
                copyRemoteRange(source, entry, 0L, prefix, output)
                copyRemoteRange(source, entry, size - tail, tail, output)
            }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private suspend fun readWindowedWav(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        val window = wavMetadataWindow(size, copyLimit) { position, length ->
            readRemoteBytes(source, entry, position, length)
        } ?: return null
        val temporary = File.createTempFile("network-tag-", ".wav", cacheDir)
        try {
            temporary.outputStream().use { output ->
                val header = readRemoteBytes(source, entry, 0L, 12)
                java.nio.ByteBuffer.wrap(header, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                    .putInt((window.size - 8L).toInt())
                output.write(header)
                window.chunks.forEach { chunk ->
                    if (chunk.audio) {
                        output.write(byteArrayOf(100, 97, 116, 97, chunk.audioBytes.toByte(), 0, 0, 0))
                        copyRemoteRange(source, entry, chunk.offset + 8L, chunk.audioBytes.toLong(), output)
                        if (chunk.audioBytes % 2 != 0) output.write(0)
                    } else {
                        copyRemoteRange(source, entry, chunk.offset, chunk.length, output)
                    }
                }
            }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private suspend fun readRemoteBytes(source: NetworkLibrarySource, entry: NetworkFileEntry, position: Long, length: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream(length)
        copyRemoteRange(source, entry, position, length.toLong(), output)
        return output.toByteArray()
    }

    private suspend fun copyRemoteRange(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        position: Long,
        length: Long,
        output: OutputStream,
    ) {
        if (length == 0L) return
        registry.openBlocking(source.id, entry.path, position, length, NetworkReadPurpose.Metadata).use { handle ->
            val buffer = ByteArray(64 * 1024)
            var remaining = length
            while (remaining > 0L) {
                currentCoroutineContext().ensureActive()
                val count = handle.input.readWithProgress(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                if (count < 0) throw IOException("Network metadata was truncated")
                output.write(buffer, 0, count)
                remaining -= count
            }
        }
    }

    private companion object {
        const val MINIMUM_FREE_BYTES = 512L * 1024L * 1024L
        const val MINIMUM_WINDOW_FREE_BYTES = 64L * 1024L * 1024L
        const val SPACE_CHECK_INTERVAL_BYTES = 8L * 1024L * 1024L
        val cleanupLock = Any()
        var staleCopiesCleaned = false

        fun cleanStaleMetadataCopies(directory: File) = synchronized(cleanupLock) {
            if (!staleCopiesCleaned) {
                directory.listFiles { file -> file.isFile && file.name.startsWith("network-tag-") }
                    ?.forEach(File::delete)
                staleCopiesCleaned = true
            }
        }
    }
}

private const val MP3_AUDIO_PREFIX_BYTES = 512L * 1024L
private const val FLAC_AUDIO_PREFIX_BYTES = 64L * 1024L
private const val MAX_WINDOW_BYTES = 16L * 1024L * 1024L
private const val MAX_FLAC_BLOCKS = 256
private const val MAX_MP4_ATOMS = 256
private const val OGG_TAIL_BYTES = 64L * 1024L
private const val OGG_AUDIO_PREFIX_BYTES = 64L * 1024L
private const val MAX_OGG_PAGES = 256
private const val MAX_WAV_CHUNKS = 256

internal data class WavMetadataChunk(val offset: Long, val length: Long, val audio: Boolean, val audioBytes: Int)
internal data class WavMetadataWindow(val chunks: List<WavMetadataChunk>, val size: Long)

internal suspend fun wavMetadataWindow(
    size: Long,
    copyLimit: Long,
    readBytes: suspend (Long, Int) -> ByteArray,
): WavMetadataWindow? {
    if (size < 12L || copyLimit < 12L) return null
    val header = readBytes(0L, 12)
    if (header.size != 12 || !header.copyOfRange(0, 4).contentEquals(byteArrayOf(82, 73, 70, 70)) ||
        !header.copyOfRange(8, 12).contentEquals(byteArrayOf(87, 65, 86, 69))
    ) return null
    val chunks = mutableListOf<WavMetadataChunk>()
    var offset = 12L
    var outputSize = 12L
    var hasFormat = false
    var hasAudio = false
    while (offset < size && chunks.size < MAX_WAV_CHUNKS) {
        if (offset > size - 8L) return null
        val chunk = readBytes(offset, 8)
        if (chunk.size != 8) return null
        val length = (7 downTo 4).fold(0L) { value, index -> (value shl 8) or (chunk[index].toLong() and 0xff) }
        val padded = length + (length and 1L)
        if (padded > size - offset - 8L) return null
        val id = chunk.copyOfRange(0, 4)
        val audio = id.contentEquals(byteArrayOf(100, 97, 116, 97))
        hasFormat = hasFormat || id.contentEquals(byteArrayOf(102, 109, 116, 32))
        hasAudio = hasAudio || audio
        val audioBytes = if (audio) minOf(length, 2L).toInt() else 0
        if (audio && audioBytes == 0) return null
        val retained = if (audio) 8L + audioBytes + (audioBytes % 2) else 8L + padded
        outputSize += retained
        if (outputSize > minOf(copyLimit, MAX_WINDOW_BYTES)) return null
        chunks += WavMetadataChunk(offset, retained, audio, audioBytes)
        offset += 8L + padded
    }
    return if (offset == size && hasFormat && hasAudio) WavMetadataWindow(chunks, outputSize) else null
}

internal suspend fun oggVorbisMetadataPrefix(
    size: Long,
    copyLimit: Long,
    readBytes: suspend (Long, Int) -> ByteArray,
): Long? {
    var offset = 0L
    var serial: ByteArray? = null
    var commentContinues = false
    repeat(MAX_OGG_PAGES) { page ->
        if (size < 27L || offset > size - 27L || offset > MAX_WINDOW_BYTES - 27L) return null
        val header = readBytes(offset, 27)
        if (header.size != 27 || !header.copyOfRange(0, 4).contentEquals(byteArrayOf(79, 103, 103, 83)) || header[4] != 0.toByte()) return null
        val currentSerial = header.copyOfRange(14, 18)
        if (serial != null && !currentSerial.contentEquals(serial)) return null
        serial = currentSerial
        if (page > 1 && commentContinues && header[5].toInt() and 1 == 0) return null
        val segments = header[26].toInt() and 0xff
        if (segments == 0 || offset + 27L + segments > size) return null
        val table = readBytes(offset + 27L, segments)
        if (table.size != segments) return null
        val bodyLength = table.sumOf { it.toInt() and 0xff }
        val bodyStart = offset + 27L + segments
        val end = bodyStart + bodyLength
        if (end > size || end > MAX_WINDOW_BYTES) return null
        if (page < 2) {
            val expected = if (page == 0) 1 else 3
            if (bodyLength < 7 || (table[0].toInt() and 0xff) < 7) return null
            val signature = readBytes(bodyStart, 7)
            if (signature.size != 7 || signature[0].toInt() != expected ||
                !signature.copyOfRange(1, 7).contentEquals(byteArrayOf(118, 111, 114, 98, 105, 115))
            ) return null
        }
        if (page >= 1) {
            commentContinues = table.none { (it.toInt() and 0xff) < 255 }
            if (!commentContinues) {
                val prefix = minOf(size, end + OGG_AUDIO_PREFIX_BYTES)
                val tail = minOf(size - prefix, OGG_TAIL_BYTES)
                return prefix.takeIf { prefix + tail <= minOf(copyLimit, MAX_WINDOW_BYTES) }
            }
        }
        offset = end
    }
    return null
}

internal suspend fun mp4MetadataRange(
    size: Long,
    copyLimit: Long,
    readHeader: suspend (Long, Int) -> ByteArray,
): Pair<Long, Long>? {
    var offset = 0L
    repeat(MAX_MP4_ATOMS) {
        if (size < 8L || offset > size - 8L) return null
        val header = readHeader(offset, 8)
        if (header.size != 8) return null
        var length = (0..3).fold(0L) { value, index -> (value shl 8) or (header[index].toLong() and 0xff) }
        val isMoov = header[4] == 'm'.code.toByte() && header[5] == 'o'.code.toByte() &&
            header[6] == 'o'.code.toByte() && header[7] == 'v'.code.toByte()
        if (length == 1L) {
            if (isMoov || offset > size - 16L) return null
            val extended = readHeader(offset + 8L, 8)
            if (extended.size != 8 || extended[0].toInt() and 0x80 != 0) return null
            length = extended.fold(0L) { value, byte -> (value shl 8) or (byte.toLong() and 0xff) }
            if (length < 16L) return null
        } else if (length < 8L) return null
        if (length > size - offset) return null
        if (isMoov) return (offset to length).takeIf { length <= minOf(copyLimit, MAX_WINDOW_BYTES) }
        offset += length
    }
    return null
}

internal suspend fun flacMetadataPrefix(size: Long, copyLimit: Long, readHeader: suspend (Long) -> ByteArray): Long? {
    if (size < 8L || copyLimit < FLAC_AUDIO_PREFIX_BYTES) return null
    if (!readHeader(0L).contentEquals(byteArrayOf(102, 76, 97, 67))) return null
    var offset = 4L
    var last = false
    var count = 0
    while (!last && ++count <= MAX_FLAC_BLOCKS) {
        if (offset > size - 4L || offset > MAX_WINDOW_BYTES - 4L) return null
        val header = readHeader(offset)
        if (header.size != 4) return null
        val length = ((header[1].toInt() and 0xff) shl 16) or
            ((header[2].toInt() and 0xff) shl 8) or (header[3].toInt() and 0xff)
        offset += 4L + length
        if (offset > size || offset > MAX_WINDOW_BYTES) return null
        last = header[0].toInt() and 0x80 != 0
    }
    if (!last) return null
    val prefix = minOf(size, offset + FLAC_AUDIO_PREFIX_BYTES)
    return prefix.takeIf { it <= minOf(copyLimit, MAX_WINDOW_BYTES) }
}

internal fun mp3MetadataPrefix(header: ByteArray, size: Long, copyLimit: Long): Long? {
    if (header.size != 10 || size < 10L) return null
    val hasId3 = header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()
    val tagSize = if (hasId3) {
        if (header[3].toInt() !in 2..4 || (6..9).any { header[it].toInt() and 0x80 != 0 }) return null
        10L + (6..9).fold(0L) { value, index -> (value shl 7) or (header[index].toLong() and 0x7f) }
    } else 0L
    val prefix = minOf(size, tagSize + MP3_AUDIO_PREFIX_BYTES)
    val tail = minOf(size - prefix, 128L)
    return prefix.takeIf { prefix + tail <= minOf(copyLimit, MAX_WINDOW_BYTES) }
}

internal data class NetworkMetadataReadResult(
    val succeeded: Boolean,
    val durationMs: Long?,
    val title: String?,
    val artist: String?,
    val albumArtist: String?,
    val album: String?,
    val releaseYear: Int?,
    val genre: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val embedded: EmbeddedTagMetadata? = null,
)

internal fun NetworkMetadataReadResult.toMetadataSourceValues(): MetadataSourceValues {
    return MetadataSourceValues(
        title = title,
        artist = artist,
        albumArtist = albumArtist,
        album = album,
        releaseYear = releaseYear,
        genre = genre,
        trackNumber = trackNumber?.toString(),
        discNumber = discNumber?.toString(),
    )
}

internal fun NetworkMetadataReadResult?.resolveMetadata(indexed: MetadataSourceValues) =
    CanonicalMetadataResolver.resolve(
        embedded = this?.embedded?.toMetadataSourceValues(),
        platform = this?.toMetadataSourceValues(),
        indexed = indexed,
    )

private class RangeMediaDataSource(
    private val registry: NetworkFileSystemRegistry,
    private val source: NetworkLibrarySource,
    private val entry: NetworkFileEntry,
    private val size: Long,
) : MediaDataSource() {
    private var bytesFetched = 0L
    private var windowStart = -1L
    private var window = ByteArray(0)
    private var windowLength = 0

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position < 0L || offset < 0 || size < 0 || offset > buffer.size - size) {
            throw IndexOutOfBoundsException()
        }
        if (size == 0) return 0
        if (position >= this.size) return -1
        if (position < windowStart || position >= windowStart + windowLength) {
            loadWindow(position)
        }
        val windowOffset = (position - windowStart).toInt()
        val available = (windowLength - windowOffset).coerceAtLeast(0)
        if (available == 0) return -1
        val count = minOf(size, available)
        window.copyInto(buffer, destinationOffset = offset, startIndex = windowOffset, endIndex = windowOffset + count)
        return count
    }

    override fun getSize(): Long = size

    override fun close() = Unit

    private fun loadWindow(position: Long) {
        val remainingBudget = MAX_METADATA_READ_BYTES - bytesFetched
        if (remainingBudget <= 0L) throw IOException("Network metadata read budget exceeded")
        val start = position
        val requestSize = minOf(WINDOW_BYTES.toLong(), this.size - start, remainingBudget).toInt()
        if (requestSize <= 0) throw IOException("Network metadata read budget exceeded")
        val loaded = ByteArray(requestSize)
        val readHandle = registry.openBlocking(
            sourceId = source.id,
            path = entry.path,
            position = start,
            length = requestSize.toLong(),
            purpose = NetworkReadPurpose.Metadata,
        )
        var total = 0
        try {
            readHandle.use { handle ->
                while (total < requestSize) {
                    val read = handle.input.readWithProgress(loaded, total, requestSize - total)
                    if (read < 0) break
                    total += read
                }
            }
        } finally {
            bytesFetched += total
        }
        if (total == 0) {
            windowStart = start
            windowLength = 0
            window = ByteArray(0)
            return
        }
        windowStart = start
        windowLength = total
        window = if (total == loaded.size) loaded else loaded.copyOf(total)
    }

    private companion object {
        const val MAX_METADATA_READ_BYTES = 2L * 1024L * 1024L
        const val WINDOW_BYTES = 64 * 1024
    }
}
