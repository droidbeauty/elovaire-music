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
import java.io.RandomAccessFile
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
            val copyLimit = (availableSpace(cacheDir) - reserve).coerceAtLeast(0L)
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
            if (platform == null) return null
            val embedded = readSparseMp3(source, entry, size, copyLimit)
            if (embedded == null) {
                failureRegistry.recordFailure(failureKey, MediaFailureCategory.Resource)
                platform
            } else {
                failureRegistry.recordSuccess(failureKey)
                platform.copy(succeeded = true, embedded = embedded)
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

    private suspend fun readSparseMp3(
        source: NetworkLibrarySource,
        entry: NetworkFileEntry,
        size: Long,
        copyLimit: Long,
    ): EmbeddedTagMetadata? {
        if (!entry.path.endsWith(".mp3", ignoreCase = true) || size < 10L || copyLimit < SPARSE_AUDIO_PREFIX_BYTES) return null
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
        val tagSize = if (header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()) {
            if ((6..9).any { header[it].toInt() and 0x80 != 0 }) return null
            10L + (6..9).fold(0L) { value, index -> (value shl 7) or (header[index].toLong() and 0x7f) }
        } else 0L
        val prefix = minOf(size, tagSize + SPARSE_AUDIO_PREFIX_BYTES)
        val tail = minOf(size - prefix, 128L)
        if (prefix + tail > minOf(copyLimit, MAX_SPARSE_COPY_BYTES)) return null
        val temporary = File.createTempFile("network-tag-", ".mp3", cacheDir)
        try {
            RandomAccessFile(temporary, "rw").use { output ->
                output.setLength(size)
                suspend fun copyRange(position: Long, length: Long) {
                    if (length == 0L) return
                    output.seek(position)
                    registry.openBlocking(source.id, entry.path, position, length, NetworkReadPurpose.Metadata).use { handle ->
                        val buffer = ByteArray(64 * 1024)
                        var remaining = length
                        while (remaining > 0L) {
                            currentCoroutineContext().ensureActive()
                            val count = handle.input.readWithProgress(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                            if (count < 0) throw IOException("Network MP3 metadata was truncated")
                            output.write(buffer, 0, count)
                            remaining -= count
                        }
                    }
                }
                copyRange(0L, prefix)
                copyRange(size - tail, tail)
            }
            return embeddedReader.read(temporary.absolutePath)
        } finally {
            temporary.delete()
        }
    }

    private companion object {
        const val MINIMUM_FREE_BYTES = 512L * 1024L * 1024L
        const val SPACE_CHECK_INTERVAL_BYTES = 8L * 1024L * 1024L
        const val SPARSE_AUDIO_PREFIX_BYTES = 512L * 1024L
        const val MAX_SPARSE_COPY_BYTES = 16L * 1024L * 1024L
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
