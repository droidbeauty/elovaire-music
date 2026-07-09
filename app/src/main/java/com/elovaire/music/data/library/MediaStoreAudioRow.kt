package elovaire.music.droidbeauty.app.data.library

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.util.Locale

internal data class MediaStoreAudioRow(
    val id: Long,
    val albumId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val fileName: String,
    val durationMs: Long,
    val track: Int,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long?,
    val fileSizeBytes: Long?,
    val mediaStoreYear: Int?,
    val relativePath: String?,
    val volumeName: String?,
    val filePath: String?,
    val mimeType: String?,
    val isMusic: Boolean?,
    val uri: Uri,
    val extension: String,
)

internal class MediaStoreAudioRowMapper(
    private val context: Context,
    cursor: Cursor,
) {
    private val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
    private val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
    private val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
    private val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
    private val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
    private val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
    private val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
    private val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
    private val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
    private val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
    private val yearIndex = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
    private val dateModifiedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
    private val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
    private val volumeNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.VOLUME_NAME)
    private val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
    private val isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)

    private val dataIndex = cursor.getColumnIndex(MediaFilePathResolver.dataColumn)

    fun row(cursor: Cursor): MediaStoreAudioRow {
        val id = cursor.getLong(idIndex)
        val relativePath = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
        val fileName = cursor.getString(fileNameIndex).orUnknown("unknown-file")
        val volumeName = volumeNameIndex.takeIf { it >= 0 }?.let(cursor::getString)
        val mimeType = mimeTypeIndex.takeIf { it >= 0 }?.let(cursor::getString)?.trim()?.ifBlank { null }
        return MediaStoreAudioRow(
            id = id,
            albumId = cursor.getLong(albumIdIndex),
            title = cursor.getString(titleIndex).orUnknown("Untitled Track"),
            artist = cursor.getString(artistIndex).orUnknown("Unknown Artist"),
            album = cursor.getString(albumIndex).orUnknown("Unknown Album"),
            fileName = fileName,
            durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L),
            track = cursor.getInt(trackIndex),
            dateAddedSeconds = cursor.getLong(dateAddedIndex),
            dateModifiedSeconds = dateModifiedIndex
                .takeIf { it >= 0 && !cursor.isNull(it) }
                ?.let(cursor::getLong)
                ?.takeIf { it > 0L },
            fileSizeBytes = sizeIndex.takeIf { it >= 0 }?.let(cursor::getLong)?.takeIf { it > 0L },
            mediaStoreYear = yearIndex.takeIf { it >= 0 }
                ?.let(cursor::getInt)
                ?.takeIf { it > 0 },
            relativePath = relativePath,
            volumeName = volumeName,
            filePath = MediaFilePathResolver.resolveMediaStoreFilePath(
                context = context,
                rawDataPath = dataIndex.takeIf { it >= 0 }?.let(cursor::getString),
                relativePath = relativePath,
                displayName = fileName,
                volumeName = volumeName,
            ),
            mimeType = mimeType,
            isMusic = isMusicIndex.takeIf { it >= 0 }?.let(cursor::getInt)?.let { it != 0 },
            uri = ContentUris.withAppendedId(
                volumeName
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(MediaStore.Audio.Media::getContentUri)
                    ?: MediaStoreAudioQuery.collectionUri,
                id,
            ),
            extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT),
        )
    }

    private fun String?.orUnknown(fallback: String): String {
        val value = this?.trim().orEmpty()
        return if (value.isBlank() || value == "<unknown>") fallback else value
    }
}
