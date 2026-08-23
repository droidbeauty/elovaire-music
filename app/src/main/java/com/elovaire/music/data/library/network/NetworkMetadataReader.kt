package elovaire.music.droidbeauty.app.data.library.network

import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import java.io.IOException
import kotlinx.coroutines.CancellationException

internal class NetworkMetadataReader(
    private val registry: NetworkFileSystemRegistry,
) {
    fun read(source: NetworkLibrarySource, entry: NetworkFileEntry): NetworkMetadataReadResult? {
        val size = entry.sizeBytes ?: return null
        if (size <= 0L) return null
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
                )
            } finally {
                retriever.release()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
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
    private var bytesRead = 0L

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position < 0L || offset < 0 || size < 0 || offset > buffer.size - size) {
            throw IndexOutOfBoundsException()
        }
        if (size == 0) return 0
        if (position >= this.size) return -1
        val remainingBudget = MAX_METADATA_READ_BYTES - bytesRead
        if (remainingBudget <= 0L) throw IOException("Network metadata read budget exceeded")
        val requestSize = minOf(size.toLong(), this.size - position, remainingBudget).toInt()
        val readHandle = registry.openBlocking(source.id, entry.path, position, requestSize.toLong())
        readHandle.use { handle ->
            var total = 0
            while (total < requestSize) {
                val read = handle.input.read(buffer, offset + total, requestSize - total)
                if (read < 0) break
                if (read == 0) continue
                total += read
                bytesRead += read
            }
            return total.takeIf { it > 0 } ?: -1
        }
    }

    override fun getSize(): Long = size

    override fun close() = Unit

    private companion object {
        const val MAX_METADATA_READ_BYTES = 2L * 1024L * 1024L
    }
}
