package elovaire.music.droidbeauty.app.data.library.network

import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import elovaire.music.droidbeauty.app.data.library.MediaFailureCategory
import elovaire.music.droidbeauty.app.data.library.MediaFailureDomain
import elovaire.music.droidbeauty.app.data.library.MediaFailureKey
import elovaire.music.droidbeauty.app.data.library.MediaFailureRegistry
import elovaire.music.droidbeauty.app.data.library.mediaFailureCategory
import java.io.IOException
import kotlinx.coroutines.CancellationException

@Suppress("TooGenericExceptionCaught")
internal class NetworkMetadataReader(
    private val registry: NetworkFileSystemRegistry,
    private val failureRegistry: MediaFailureRegistry = MediaFailureRegistry(),
) {
    fun read(source: NetworkLibrarySource, entry: NetworkFileEntry, force: Boolean = false): NetworkMetadataReadResult? {
        val size = entry.sizeBytes ?: return null
        if (size <= 0L) return null
        val failureKey = MediaFailureKey(
            identity = "${source.id}:${entry.path}",
            revision = "${entry.sizeBytes}:${entry.modifiedAtMs}:${entry.etag}",
            domain = MediaFailureDomain.Metadata,
        )
        if (failureRegistry.shouldSuppress(failureKey, force)) return null
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(RangeMediaDataSource(registry, source, entry, size))
                NetworkMetadataReadResult(
                    succeeded = true,
                    durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                        ?.takeIf { it > 0L },
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    releaseYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                        ?.take(4)
                        ?.toIntOrNull(),
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                    trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.substringBefore('/')
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 },
                    discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                        ?.substringBefore('/')
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 },
                ).also { failureRegistry.recordSuccess(failureKey) }
            } finally {
                retriever.release()
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
        val readHandle = registry.openBlocking(source.id, entry.path, start, requestSize.toLong())
        var total = 0
        try {
            readHandle.use { handle ->
                while (total < requestSize) {
                    val read = handle.input.read(loaded, total, requestSize - total)
                    if (read < 0) break
                    if (read == 0) continue
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
