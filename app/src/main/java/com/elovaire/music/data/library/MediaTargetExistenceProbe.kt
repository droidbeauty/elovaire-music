package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import kotlinx.coroutines.CancellationException

/** Checks exact targets so removable-volume IDs and SAF documents stay distinct. */
internal class MediaTargetExistenceProbe(
    context: Context,
) {
    private val resolver = context.applicationContext.contentResolver

    suspend fun findExistingSongIds(targets: Map<Long, Uri>): Set<Long> {
        if (targets.isEmpty()) return emptySet()
        return targets.mapNotNullTo(linkedSetOf()) { (songId, uri) ->
            if (uri.scheme.equals("file", ignoreCase = true)) {
                return@mapNotNullTo songId.takeIf { uri.path?.let(::File)?.isFile == true }
            }
            val projection = if (uri.authority == MediaStore.AUTHORITY) {
                arrayOf(MediaStore.Audio.Media._ID)
            } else {
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            }
            val exists = queryTargetExists {
                val selection = if (uri.authority.equals(MediaStore.AUTHORITY, ignoreCase = true)) {
                    MediaStoreAudioQuery.selection
                } else {
                    null
                }
                resolver.queryCancellable(uri, projection, selection, null, null)?.use { it.moveToFirst() }
                    ?: false
            }
            songId.takeIf { exists }
        }
    }
}

internal suspend fun queryTargetExists(query: suspend () -> Boolean): Boolean = try {
    query()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    false
}
