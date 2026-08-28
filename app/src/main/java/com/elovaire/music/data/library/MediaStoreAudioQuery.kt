package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

internal object MediaStoreAudioQuery {
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
    )

    /** Only identity and display name are required to discover a MediaStore audio row. */
    val compatibilityProjection: Array<String> = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
    )

    val collectionUri: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    /** Duration and provider-side collation are not authoritative on all Android 11 providers. */
    val selection: String? = null

    val orderBy: String? = null

    internal enum class ProjectionKind { Full, Compatibility }

    internal data class QueryResult(
        val cursor: Cursor,
        val projectionKind: ProjectionKind,
    )

    @Suppress("TooGenericExceptionCaught")
    fun query(resolver: ContentResolver): QueryResult {
        var firstFailure: Throwable? = null
        try {
            resolver.query(collectionUri, projection, selection, null, orderBy)?.let {
                return QueryResult(it, ProjectionKind.Full)
            }
        } catch (failure: SecurityException) {
            throw failure
        } catch (failure: RuntimeException) {
            firstFailure = failure
        }

        try {
            resolver.query(collectionUri, compatibilityProjection, null, null, null)?.let {
                return QueryResult(it, ProjectionKind.Compatibility)
            }
        } catch (failure: SecurityException) {
            throw failure
        } catch (failure: RuntimeException) {
            throw MediaStoreQueryUnavailableException(failure)
        }

        throw MediaStoreQueryUnavailableException(firstFailure)
    }
}
