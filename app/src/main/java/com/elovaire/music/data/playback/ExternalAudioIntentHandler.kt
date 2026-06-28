package elovaire.music.droidbeauty.app.data.playback

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.Locale
import kotlin.math.absoluteValue

internal object ExternalAudioIntentHandler {
    private const val EXTERNAL_ALBUM_ID_BASE = -9_000_000_000_000L
    private const val EXTERNAL_SONG_ID_BASE = -8_000_000_000_000L

    fun canHandle(intent: Intent?): Boolean {
        return intent?.action == Intent.ACTION_VIEW && intent.data != null
    }

    fun buildSong(
        context: Context,
        intent: Intent?,
    ): Song? {
        if (!canHandle(intent)) return null
        val uri = intent?.data ?: return null
        if (!uri.isSupportedScheme()) return null

        val contentResolver = context.contentResolver
        val mimeType = intent.type
            ?: contentResolver.getType(uri)
            ?: uri.extensionMimeType()
        val normalizedMimeType = mimeType?.lowercase(Locale.ROOT)

        val displayName = contentResolver.queryDisplayName(uri)
            ?: uri.lastPathSegment
            ?: "External audio"
        val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val extensionCapability = AudioFormatPolicy.capabilityForExtension(extension)
        val mimeCapability = normalizedMimeType?.let { mime ->
            AudioFormatPolicy.capabilities.firstOrNull { mime in it.mimeTypes }
        }
        val capability = extensionCapability ?: mimeCapability ?: return null

        val title = displayName.substringBeforeLast('.').ifBlank { displayName }
        val durationMs = contentResolver.readDurationMs(context, uri)
        val stableHash = uri.toString().hashCode().toLong().absoluteValue

        return Song(
            id = EXTERNAL_SONG_ID_BASE + stableHash,
            title = title,
            isExplicit = false,
            artist = "Unknown Artist",
            album = "External audio",
            releaseYear = null,
            genre = "",
            audioFormat = capability.displayName,
            audioQuality = null,
            fileName = displayName,
            albumId = EXTERNAL_ALBUM_ID_BASE + stableHash,
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

    private fun Uri.isSupportedScheme(): Boolean {
        return scheme == ContentResolver.SCHEME_CONTENT || scheme == ContentResolver.SCHEME_FILE
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

    private fun ContentResolver.readDurationMs(
        context: Context,
        uri: Uri,
    ): Long {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.coerceAtLeast(0L)
                    ?: 0L
            } finally {
                retriever.release()
            }
        }.getOrDefault(0L)
    }
}
