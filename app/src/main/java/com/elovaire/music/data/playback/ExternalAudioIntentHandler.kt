package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
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
                permission.uri == uri && permission.isReadPermission
            }
        }.getOrDefault(false)
        if (hasPersistedGrant) return true
        return uri.authority == MediaStore.AUTHORITY && hasAudioReadPermission()
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

    suspend fun materialize(
        context: Context,
        sourceUri: Uri,
        displayName: String,
    ): Uri? {
        val directory = context.noBackupFilesDir.resolve(DIRECTORY_NAME)
        if (!directory.exists() && !directory.mkdirs()) return null
        val target = directory.resolve("${digest(sourceUri.toString())}${extension(displayName)}")
        if (target.isFile && target.length() > 0L) {
            prune(directory, target)
            return Uri.fromFile(target)
        }
        if (target.exists() && !target.delete()) return null

        val declaredSize = runCatching {
            context.contentResolver.query(
                sourceUri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) null
                else cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }
        }.getOrNull()
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
                }
            } ?: throw IOException("External audio source could not be opened.")
            if (!temporary.renameTo(target)) {
                if (target.isFile && target.length() > 0L) {
                    temporary.delete()
                } else {
                    throw IOException("Unable to commit external audio copy.")
                }
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

    private fun digest(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(Locale.ROOT, byte) }
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
