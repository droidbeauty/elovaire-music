package elovaire.music.droidbeauty.app.widget

import android.content.Context
import android.util.AtomicFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal interface WidgetSnapshotStore {
    suspend fun readLatest(): WidgetPlaybackSnapshot?
    suspend fun writeIfChanged(snapshot: WidgetPlaybackSnapshot): Boolean
    suspend fun clear()
}

internal class AtomicWidgetSnapshotStore private constructor(
    private val atomicFile: AtomicFile,
    private val ioDispatcher: CoroutineDispatcher,
) : WidgetSnapshotStore {
    constructor(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(
        AtomicFile(File(context.applicationContext.noBackupFilesDir, SNAPSHOT_FILE_NAME)),
        ioDispatcher,
    )

    internal constructor(
        file: File,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(AtomicFile(file), ioDispatcher)

    private val mutex = Mutex()
    private var hasLoaded = false
    private var cachedSnapshot: WidgetPlaybackSnapshot? = null

    override suspend fun readLatest(): WidgetPlaybackSnapshot? = withContext(ioDispatcher) {
        mutex.withLock { loadLocked() }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun writeIfChanged(snapshot: WidgetPlaybackSnapshot): Boolean = withContext(ioDispatcher) {
        mutex.withLock {
            require(snapshot.schemaVersion == WidgetPlaybackSnapshot.CURRENT_SCHEMA_VERSION)
            val current = loadLocked()
            if (current?.hasSameWidgetContent(snapshot) == true) return@withLock false
            require(snapshot.contentRevision > (current?.contentRevision ?: 0L)) {
                "Widget snapshot revision must increase when widget-visible content changes."
            }

            atomicFile.baseFile.parentFile?.let { parent ->
                check(parent.isDirectory || parent.mkdirs()) { "Unable to create widget snapshot directory." }
            }
            val output = atomicFile.startWrite()
            try {
                output.write(WidgetPlaybackSnapshotCodec.encode(snapshot))
                output.flush()
                atomicFile.finishWrite(output)
                cachedSnapshot = snapshot
                hasLoaded = true
                true
            } catch (failure: Throwable) {
                atomicFile.failWrite(output)
                throw failure
            }
        }
    }

    override suspend fun clear() = withContext(ioDispatcher) {
        mutex.withLock {
            atomicFile.delete()
            cachedSnapshot = null
            hasLoaded = true
        }
    }

    private fun loadLocked(): WidgetPlaybackSnapshot? {
        if (hasLoaded) return cachedSnapshot
        cachedSnapshot = try {
            atomicFile.openRead().use { input ->
                if (atomicFile.baseFile.length() > WidgetPlaybackSnapshotCodec.MAX_FILE_BYTES) {
                    null
                } else {
                    val bytes = input.readAtMost(WidgetPlaybackSnapshotCodec.MAX_FILE_BYTES)
                    WidgetPlaybackSnapshotCodec.decode(bytes)
                }
            }
        } catch (_: IOException) {
            null
        }
        hasLoaded = true
        return cachedSnapshot
    }

    private fun java.io.InputStream.readAtMost(maxBytes: Int): ByteArray? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            totalBytes += count
            if (totalBytes > maxBytes) return null
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private companion object {
        const val SNAPSHOT_FILE_NAME = "widget-playback-state.bin"
    }
}

internal object WidgetPlaybackSnapshotCodec {
    const val MAX_FILE_BYTES = 64 * 1024
    private const val MAGIC = 0x454C5753

    fun encode(snapshot: WidgetPlaybackSnapshot): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeInt(MAGIC)
            output.writeInt(snapshot.schemaVersion)
            output.writeLong(snapshot.contentRevision)
            output.writeBoolean(snapshot.currentSongId != null)
            snapshot.currentSongId?.let(output::writeLong)
            output.writeUTF(snapshot.title)
            output.writeUTF(snapshot.artist)
            output.writeBoolean(snapshot.album != null)
            snapshot.album?.let(output::writeUTF)
            output.writeBoolean(snapshot.artworkIdentity != null)
            snapshot.artworkIdentity?.let { identity ->
                output.writeLong(identity.songId)
                output.writeLong(identity.mediaRevisionSeconds)
            }
            output.writeBoolean(snapshot.isPlaying)
            output.writeBoolean(snapshot.transportShowsPause)
            output.writeByte(snapshot.repeatMode.ordinal)
            output.writeBoolean(snapshot.shuffleEnabled)
            output.writeBoolean(snapshot.durationMs != null)
            snapshot.durationMs?.let(output::writeLong)
            output.writeLong(snapshot.capturedAtElapsedRealtimeMs)
        }
        return bytes.toByteArray()
    }

    fun decode(bytes: ByteArray?): WidgetPlaybackSnapshot? {
        if (bytes == null || bytes.size > MAX_FILE_BYTES) return null
        return try {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                if (input.readInt() != MAGIC) return null
                val schemaVersion = input.readInt()
                if (schemaVersion != WidgetPlaybackSnapshot.CURRENT_SCHEMA_VERSION) return null
                val revision = input.readLong()
                val songId = if (input.readBoolean()) input.readLong() else null
                val title = input.readUTF()
                val artist = input.readUTF()
                val album = if (input.readBoolean()) input.readUTF() else null
                val artwork = if (input.readBoolean()) {
                    WidgetArtworkIdentity(input.readLong(), input.readLong())
                } else {
                    null
                }
                val isPlaying = input.readBoolean()
                val transportShowsPause = input.readBoolean()
                val repeatMode = WidgetRepeatMode.entries.getOrNull(input.readUnsignedByte()) ?: return null
                val shuffleEnabled = input.readBoolean()
                val durationMs = if (input.readBoolean()) input.readLong() else null
                val capturedAt = input.readLong()
                if (input.available() != 0) return null
                if (revision < 0L || capturedAt < 0L) return null
                if (durationMs?.let { it < 0L } == true) return null
                if (artwork?.songId?.let { it != songId } == true) return null
                if (listOf(title, artist, album.orEmpty()).any { it.length > WidgetPlaybackSnapshot.MAX_TEXT_LENGTH }) return null
                WidgetPlaybackSnapshot(
                    schemaVersion = schemaVersion,
                    contentRevision = revision,
                    currentSongId = songId,
                    title = title,
                    artist = artist,
                    album = album,
                    artworkIdentity = artwork,
                    isPlaying = isPlaying,
                    transportShowsPause = transportShowsPause,
                    repeatMode = repeatMode,
                    shuffleEnabled = shuffleEnabled,
                    durationMs = durationMs,
                    capturedAtElapsedRealtimeMs = capturedAt,
                )
            }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
