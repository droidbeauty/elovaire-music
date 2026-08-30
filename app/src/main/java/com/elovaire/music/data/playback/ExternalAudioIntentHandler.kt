package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal object ExternalAudioIntentHandler {
    private const val EXTERNAL_ALBUM_ID_BASE = -9_000_000_000_000L
    private const val EXTERNAL_SONG_ID_BASE = -8_000_000_000_000L

    fun canHandle(intent: Intent?): Boolean {
        val uri = intent?.data ?: return false
        return intent.action == Intent.ACTION_VIEW &&
            ExternalAudioMetadataPolicy.acceptsUri(uri.scheme, uri.toString().length) &&
            ExternalAudioMetadataPolicy.acceptsDeclaredMimeType(intent.type)
    }

    suspend fun buildSong(
        context: Context,
        intent: Intent?,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Song? = withContext(ioDispatcher) {
        if (!canHandle(intent)) return@withContext null
        val uri = intent?.data ?: return@withContext null
        if (uri.scheme == ContentResolver.SCHEME_FILE && !uri.isReadableFileAudioInput()) return@withContext null

        val contentResolver = context.contentResolver
        val mimeType = intent.type
            ?: contentResolver.safeType(uri)
            ?: uri.extensionMimeType()

        val displayName = ExternalAudioMetadataPolicy.sanitizeDisplayName(
            contentResolver.queryDisplayName(uri)
            ?: uri.lastPathSegment
            ?: "External audio",
        )
        val declaredCapability = ExternalAudioMetadataPolicy.resolveCapability(
            displayName = displayName,
            pathSegment = uri.lastPathSegment,
            mimeType = mimeType,
        )
        if (declaredCapability == null && uri.scheme != ContentResolver.SCHEME_CONTENT) {
            return@withContext null
        }
        val playbackUri = context.resolvePlaybackUri(uri, displayName)
            ?: return@withContext null
        val detected = AudioFormatDetector(context).detect(playbackUri, displayName, mimeType)
        if (!detected.detectionSucceeded || AudioFormatPolicy.playbackSupport(detected) == PlaybackSupport.Unsupported) {
            return@withContext null
        }

        val title = ExternalAudioMetadataPolicy.titleFromDisplayName(displayName)
        val durationMs = detected.durationMs ?: contentResolver.readDurationMs(context, playbackUri)
        val uriValue = playbackUri.toString()

        Song(
            id = stableExternalId(uriValue, EXTERNAL_SONG_ID_BASE),
            title = title,
            isExplicit = false,
            artist = "Unknown Artist",
            album = "External audio",
            releaseYear = null,
            genre = "",
            audioFormat = detected.displayName,
            audioQuality = null,
            fileName = displayName,
            albumId = stableExternalId(uriValue, EXTERNAL_ALBUM_ID_BASE),
            durationMs = durationMs,
            trackNumber = 0,
            discNumber = 0,
            dateAddedSeconds = 0L,
            dateModifiedSeconds = null,
            uri = playbackUri,
            artUri = null,
            metadataResolved = true,
            albumArtist = null,
        )
    }

    private suspend fun Context.resolvePlaybackUri(
        uri: Uri,
        displayName: String,
    ): Uri? {
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) return uri
        if (hasDurableContentAccess(uri)) return uri
        return ExternalAudioPrivateCopy.materialize(this, uri, displayName)
    }

    private fun Context.hasDurableContentAccess(uri: Uri): Boolean {
        val resolver = contentResolver
        val hasPersistedGrant = runCatching {
            resolver.persistedUriPermissions.any { permission ->
                permission.isReadPermission && permissionGrantsUri(permission.uri, uri)
            }
        }.getOrDefault(false)
        if (hasPersistedGrant) return true
        return uri.authority == MediaStore.AUTHORITY && hasAudioReadPermission()
    }

    private fun Context.permissionGrantsUri(
        grantUri: Uri,
        uri: Uri,
    ): Boolean {
        if (grantUri == uri) return true
        return runCatching {
            DocumentsContract.isTreeUri(grantUri) &&
                grantUri.authority == uri.authority &&
                DocumentsContract.buildDocumentUriUsingTree(
                    grantUri,
                    DocumentsContract.getDocumentId(uri),
                ) == uri
        }.getOrDefault(false)
    }

    private fun Uri.isReadableFileAudioInput(): Boolean {
        val file = path?.let(::File) ?: return false
        if (!file.isFile || !file.canRead()) return false
        val displayName = ExternalAudioMetadataPolicy.sanitizeDisplayName(file.name)
        return ExternalAudioMetadataPolicy.resolveCapability(
            displayName = displayName,
            pathSegment = file.name,
            mimeType = extensionMimeType(),
        ) != null
    }

    private fun Uri.extensionMimeType(): String? {
        val extension = lastPathSegment
            ?.substringAfterLast('.', "")
            ?.lowercase(Locale.ROOT)
            ?.takeIf(String::isNotBlank)
            ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun ContentResolver.queryDisplayName(uri: Uri): String? {
        return runCatching {
            query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            }
        }.getOrNull()
    }

    private fun ContentResolver.safeType(uri: Uri): String? {
        return runCatching { getType(uri) }
            .getOrNull()
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun ContentResolver.readDurationMs(
        context: Context,
        uri: Uri,
    ): Long {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            val resource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveRetriever)
            try {
                retriever.setDataSource(context, uri)
                ExternalAudioMetadataPolicy.boundedDurationMs(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
                )
            } finally {
                retriever.release()
                resource.close()
            }
        }.getOrDefault(0L)
    }
}

private object ExternalAudioPrivateCopy {
    private const val DIRECTORY_NAME = "external_audio"
    private const val COPY_BUFFER_SIZE = 32 * 1024
    private const val MIN_REMAINING_BYTES = 4L * 1024L * 1024L
    private const val MAX_COPY_BYTES = 512L * 1024L * 1024L
    private val lockRegistry = mutableMapOf<String, StageLock>()
    private val lockRegistryGuard = Any()

    suspend fun materialize(
        context: Context,
        sourceUri: Uri,
        displayName: String,
    ): Uri? {
        val metadata = queryMetadata(context.contentResolver, sourceUri)
        val cacheKey = if (metadata.hasReliableRevision) {
            externalAudioStageKey(sourceUri, metadata)
        } else {
            freshExternalAudioStageKey(sourceUri)
        }
        return withStageLock(sourceUri.toString()) {
            ExternalAudioStageUsage.registerDirectory(context.noBackupFilesDir.resolve(DIRECTORY_NAME))
            materializeLocked(context, sourceUri, displayName, metadata, cacheKey)
        }
    }

    private suspend fun materializeLocked(
        context: Context,
        sourceUri: Uri,
        displayName: String,
        metadata: ExternalAudioStageMetadata,
        cacheKey: String,
    ): Uri? {
        val directory = context.noBackupFilesDir.resolve(DIRECTORY_NAME)
        if (!directory.exists() && !directory.mkdirs()) return null
        val target = directory.resolve("$cacheKey${extension(displayName)}")
        if (
            metadata.hasReliableRevision &&
            target.isFile &&
            target.length() > 0L &&
            target.length() == metadata.sizeBytes
        ) {
            prune(directory, target)
            return Uri.fromFile(target)
        }
        if (target.exists() && !target.isFile) return null

        val declaredSize = metadata.sizeBytes
        if (declaredSize != null && declaredSize > MAX_COPY_BYTES) return null

        val temporary = runCatching {
            File.createTempFile(".${target.name}.", ".tmp", directory)
        }.getOrNull() ?: return null
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(COPY_BUFFER_SIZE)
                    var total = 0L
                    var nextStorageCheck = 0L
                    while (true) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        if (read == 0) continue
                        if (total > MAX_COPY_BYTES - read) {
                            throw IOException("External audio source exceeds the private copy limit.")
                        }
                        total += read
                        if (total >= nextStorageCheck &&
                            total + MIN_REMAINING_BYTES > StatFs(directory.path).availableBytes
                        ) {
                            throw IOException("Insufficient private storage for external audio.")
                        }
                        output.write(buffer, 0, read)
                        nextStorageCheck = total + (1L * 1024L * 1024L)
                    }
                    output.fd.sync()
                    if (total == 0L) throw IOException("External audio source is empty.")
                    if (declaredSize != null && total != declaredSize) {
                        throw IOException("External audio source changed while it was copied.")
                    }
                }
            } ?: throw IOException("External audio source could not be opened.")
            try {
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (unsupported: AtomicMoveNotSupportedException) {
                throw IOException("The file system cannot atomically commit external audio.", unsupported)
            }
            prune(directory, target)
            Uri.fromFile(target)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            temporary.delete()
            throw cancelled
        } catch (_: IOException) {
            temporary.delete()
            null
        } catch (_: SecurityException) {
            temporary.delete()
            null
        } catch (_: IllegalArgumentException) {
            temporary.delete()
            null
        }
    }

    private suspend fun <T> withStageLock(
        key: String,
        block: suspend () -> T,
    ): T {
        val stageLock = synchronized(lockRegistryGuard) {
            lockRegistry.getOrPut(key, ::StageLock).also { it.references += 1 }
        }
        return try {
            stageLock.mutex.withLock { block() }
        } finally {
            synchronized(lockRegistryGuard) {
                stageLock.references -= 1
                if (stageLock.references == 0) lockRegistry.remove(key, stageLock)
            }
        }
    }

    private fun queryMetadata(
        resolver: android.content.ContentResolver,
        uri: Uri,
    ): ExternalAudioStageMetadata {
        return runCatching {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE, DocumentsContract.Document.COLUMN_LAST_MODIFIED),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use ExternalAudioStageMetadata()
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                ExternalAudioStageMetadata(
                    sizeBytes = sizeIndex.takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let { cursor.getLong(it) }
                        ?.takeIf { it >= 0L },
                    modifiedAtMs = modifiedIndex.takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let { cursor.getLong(it) }
                        ?.takeIf { it >= 0L },
                )
            } ?: ExternalAudioStageMetadata()
        }.getOrDefault(ExternalAudioStageMetadata())
    }

    private fun freshExternalAudioStageKey(sourceUri: Uri): String {
        return externalAudioStageKey(
            sourceUri = sourceUri,
            metadata = ExternalAudioStageMetadata(
                sizeBytes = null,
                modifiedAtMs = UUID.randomUUID().mostSignificantBits,
            ),
        )
    }

    private fun extension(displayName: String): String {
        val suffix = displayName.substringAfterLast('.', "")
            .lowercase(Locale.ROOT)
            .filter { it.isLetterOrDigit() }
            .take(12)
        return suffix.takeIf(String::isNotBlank)?.let { ".$it" }.orEmpty()
    }

    private fun prune(directory: File, keep: File) {
        val now = System.currentTimeMillis()
        directory.listFiles()
            .orEmpty()
            .filter { it != keep }
            .filterNot(ExternalAudioStageUsage::isActive)
            .filter { file ->
                val age = now - file.lastModified()
                (file.name.endsWith(".tmp") && age > MAX_TEMP_FILE_AGE_MS) ||
                    (!file.name.endsWith(".tmp") && age > MAX_CACHE_AGE_MS)
            }
            .forEach(File::delete)

        var totalBytes = directory.listFiles()
            .orEmpty()
            .filter { it.isFile && !it.name.endsWith(".tmp") }
            .sumOf(File::length)
        if (totalBytes <= MAX_CACHE_BYTES) return
        directory.listFiles()
            .orEmpty()
            .filter { it.isFile && it != keep && !it.name.endsWith(".tmp") }
            .filterNot(ExternalAudioStageUsage::isActive)
            .sortedBy(File::lastModified)
            .forEach { file ->
                if (totalBytes <= MAX_CACHE_BYTES) return@forEach
                val fileSize = file.length()
                if (file.delete()) totalBytes -= fileSize
            }
    }

    private const val MAX_CACHE_AGE_MS = 7L * 24L * 60L * 60L * 1_000L
    private const val MAX_TEMP_FILE_AGE_MS = 60L * 60L * 1_000L
    private const val MAX_CACHE_BYTES = 512L * 1024L * 1024L

    private class StageLock {
        val mutex = Mutex()
        var references = 0
    }
}

internal object ExternalAudioStageUsage {
    private val stageDirectory = java.util.concurrent.atomic.AtomicReference<String?>(null)
    private val activePaths = java.util.concurrent.atomic.AtomicReference<Set<String>>(emptySet())

    fun registerDirectory(directory: File) {
        stageDirectory.compareAndSet(null, directory.absolutePath)
    }

    fun setActivePlaybackUris(uris: Collection<Uri>) {
        val directory = stageDirectory.get() ?: return
        val prefix = if (directory.endsWith(File.separator)) directory else "$directory${File.separator}"
        activePaths.set(
            uris.asSequence()
                .mapNotNull(Uri::getPath)
                .filter { it.startsWith(prefix) }
                .toSet(),
        )
    }

    fun isActive(file: File): Boolean = file.absolutePath in activePaths.get()
}

internal data class ExternalAudioStageMetadata(
    val sizeBytes: Long? = null,
    val modifiedAtMs: Long? = null,
) {
    val hasReliableRevision: Boolean
        get() = sizeBytes != null && modifiedAtMs != null
}

internal fun externalAudioStageKey(
    sourceUri: Uri,
    metadata: ExternalAudioStageMetadata,
): String {
    val value = listOf(
        sourceUri.toString(),
        metadata.sizeBytes?.toString().orEmpty(),
        metadata.modifiedAtMs?.toString().orEmpty(),
    ).joinToString("|")
    return MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(Locale.ROOT, byte) }
}

internal fun stableExternalId(
    uriValue: String,
    base: Long,
): Long {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(uriValue.toByteArray(Charsets.UTF_8))
    val positive = ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long and Long.MAX_VALUE
    return base + (positive % EXTERNAL_ID_RANGE)
}

private const val EXTERNAL_ID_RANGE = 1_000_000_000_000L
