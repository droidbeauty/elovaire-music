package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.Dispatchers
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
    ): Song? = withContext(Dispatchers.IO) {
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
        val capability = ExternalAudioMetadataPolicy.resolveCapability(
            displayName = displayName,
            pathSegment = uri.lastPathSegment,
            mimeType = mimeType,
        ) ?: return@withContext null

        val title = ExternalAudioMetadataPolicy.titleFromDisplayName(displayName)
        val durationMs = contentResolver.readDurationMs(context, uri)
        val uriValue = uri.toString()

        Song(
            id = stableExternalId(uriValue, EXTERNAL_SONG_ID_BASE),
            title = title,
            isExplicit = false,
            artist = "Unknown Artist",
            album = "External audio",
            releaseYear = null,
            genre = "",
            audioFormat = capability.displayName,
            audioQuality = null,
            fileName = displayName,
            albumId = stableExternalId(uriValue, EXTERNAL_ALBUM_ID_BASE),
            durationMs = durationMs,
            trackNumber = 0,
            discNumber = 0,
            dateAddedSeconds = 0L,
            dateModifiedSeconds = null,
            uri = uri,
            artUri = null,
            metadataResolved = true,
            albumArtist = null,
        )
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
            try {
                retriever.setDataSource(context, uri)
                ExternalAudioMetadataPolicy.boundedDurationMs(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
                )
            } finally {
                retriever.release()
            }
        }.getOrDefault(0L)
    }
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
