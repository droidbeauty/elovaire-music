package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.provider.MediaStore

internal object MediaStoreAudioQuery {
    @Suppress("DEPRECATION")
    val projection: Array<String> = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.IS_MUSIC,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.VOLUME_NAME,
        MediaStore.MediaColumns.DATA,
    )

    val collectionUri: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    val selection: String = "${MediaStore.Audio.Media.DURATION} > 0"

    val orderBy: String = "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE ASC, " +
        "${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE ASC"
}
