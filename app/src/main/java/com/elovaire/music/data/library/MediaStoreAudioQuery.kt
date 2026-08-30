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

    /**
     * Only published media is part of the discoverable catalog.  These columns are platform
     * columns on every API supported by the app, and applying the predicate in the provider
     * query also keeps pending/trashed rows out of delta reconciliation.
     */
    val selection: String =
        "${MediaStore.MediaColumns.IS_PENDING} = 0 AND " +
            "${MediaStore.MediaColumns.IS_TRASHED} = 0"

    val orderBy: String? = null

    private const val DELTA_SELECTION =
        "${MediaStore.MediaColumns.GENERATION_ADDED} > ? OR " +
            "${MediaStore.MediaColumns.GENERATION_MODIFIED} > ?"

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
            resolver.query(collectionUri, compatibilityProjection, selection, null, orderBy)?.let {
                return QueryResult(it, ProjectionKind.Compatibility)
            }
        } catch (failure: SecurityException) {
            throw failure
        } catch (failure: RuntimeException) {
            throw MediaStoreQueryUnavailableException(failure)
        }

        throw MediaStoreQueryUnavailableException(firstFailure)
    }

    /** Returns null when the provider does not support generation-based selection. */
    @Suppress("TooGenericExceptionCaught")
    fun queryDelta(
        resolver: ContentResolver,
        generation: Long,
    ): QueryResult? {
        if (generation < 0L) return null
        return try {
            resolver.query(
                collectionUri,
                deltaProjection,
                "$selection AND ($DELTA_SELECTION)",
                arrayOf(generation.toString(), generation.toString()),
                orderBy,
            )?.let { QueryResult(it, ProjectionKind.Full) }
        } catch (failure: SecurityException) {
            throw failure
        } catch (_: RuntimeException) {
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun queryIdentity(resolver: ContentResolver): Cursor? {
        return try {
            resolver.query(
                collectionUri,
                identityProjection,
                selection,
                null,
                null,
            )
        } catch (failure: SecurityException) {
            throw failure
        } catch (_: RuntimeException) {
            null
        }
    }

    private val identityProjection: Array<String> = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.MediaColumns.VOLUME_NAME,
    )

    private val deltaProjection: Array<String> = projection + arrayOf(
        MediaStore.MediaColumns.GENERATION_ADDED,
        MediaStore.MediaColumns.GENERATION_MODIFIED,
    )
}
