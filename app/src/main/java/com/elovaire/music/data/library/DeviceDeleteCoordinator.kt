package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class DeviceDeleteTarget(
    val songId: Long,
    val albumId: Long,
    val uri: Uri,
    val artUri: Uri?,
)

internal data class DeviceDeletePlan(
    val operationId: String,
    val targets: List<DeviceDeleteTarget>,
    val uris: List<Uri>,
    val filePaths: Set<String>,
    val parentDirectories: Set<String>,
)

internal interface DeviceDeleteHandler {
    suspend fun prepareSongDeletePlan(songs: List<Song>): DeviceDeletePlan?

    suspend fun completeDelete(plan: DeviceDeletePlan)

    suspend fun restorePendingDeletePlan(): DeviceDeletePlan?

    suspend fun clearPendingDelete(operationId: String)
}

internal class DeviceDeleteCoordinator(
    private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val playbackManager: PlaybackManager,
    private val userDataStore: PlaylistStore,
    private val invalidateArtwork: (Collection<Uri?>) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
) : DeviceDeleteHandler {
    private val pendingDeletePreferences = allowStrictModeDiskReads {
        PreferenceStorage(context.applicationContext).preferences
    }

    override suspend fun prepareSongDeletePlan(songs: List<Song>): DeviceDeletePlan? {
        val uniqueSongs = songs.distinctBy(Song::id)
        if (uniqueSongs.isEmpty()) return null
        return withContext(ioDispatcher) {
            val filePaths = querySongFilePaths(uniqueSongs)
            val plan = DeviceDeletePlan(
                operationId = operationIdGenerator.nextId(),
                targets = uniqueSongs.map { song ->
                    DeviceDeleteTarget(song.id, song.albumId, song.uri, song.artUri)
                },
                uris = uniqueSongs.map(Song::uri),
                filePaths = filePaths,
                parentDirectories = filePaths.mapNotNullTo(linkedSetOf()) { path ->
                    File(path).parentFile?.absolutePath
                },
            )
            plan.takeIf(::persistPendingDelete)
        }
    }

    override suspend fun completeDelete(plan: DeviceDeletePlan) {
        invalidateArtwork(plan.targets.flatMap { listOf(it.artUri, it.uri) })
        val deleteResult = libraryRepository.refreshAfterDelete(
            LibraryDeleteRequest(
                songIds = plan.targets.mapTo(linkedSetOf(), DeviceDeleteTarget::songId),
                albumIds = plan.targets.mapTo(linkedSetOf(), DeviceDeleteTarget::albumId),
                uris = plan.targets.mapTo(linkedSetOf(), DeviceDeleteTarget::uri),
                filePaths = plan.filePaths,
                uriBySongId = plan.targets.associate { it.songId to it.uri },
            ),
        )
        cleanupEmptyDirectories(plan.parentDirectories)
        playbackManager.removeSongsFromQueue(deleteResult.deletedSongIds)
        if (deleteResult.deletedSongIds.isNotEmpty()) {
            if (userDataStore.removeSongReferences(deleteResult.deletedSongIds).await() !is
                elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult.Success
            ) {
                return
            }
        }
        if (deleteResult.failed.isEmpty()) clearPendingDelete(plan.operationId)
    }

    override suspend fun restorePendingDeletePlan(): DeviceDeletePlan? = withContext(ioDispatcher) {
        readPendingDelete()
    }

    override suspend fun clearPendingDelete(operationId: String) = withContext(ioDispatcher) {
        check(clearPendingDeleteState(operationId)) {
            "Unable to clear the completed device-delete operation."
        }
    }

    private fun querySongFilePaths(songs: List<Song>): Set<String> {
        val contentResolver = context.contentResolver
        return songs.asSequence()
            .mapNotNull { song -> contentResolver.queryMediaStoreFilePath(context, song.uri) }
            .toSet()
    }

    private suspend fun cleanupEmptyDirectories(paths: Set<String>) {
        withContext(ioDispatcher) {
            paths.asSequence()
                .map(::File)
                .filter { file -> file.exists() && file.isDirectory }
                .sortedByDescending { file -> file.absolutePath.length }
                .forEach { directory ->
                    runCatching {
                        if (directory.listFiles().isNullOrEmpty()) {
                            directory.delete()
                        }
                    }
                }
        }
    }

    private fun persistPendingDelete(plan: DeviceDeletePlan): Boolean {
        if (plan.targets.isEmpty() || plan.targets.size > MAX_PENDING_TARGETS) return false
        val targets = JSONArray().apply {
            plan.targets.forEach { target ->
                put(
                    JSONObject()
                        .put(KEY_SONG_ID, target.songId)
                        .put(KEY_ALBUM_ID, target.albumId)
                        .put(KEY_URI, target.uri.toString())
                        .put(KEY_ART_URI, target.artUri?.toString()),
                )
            }
        }
        val filePaths = JSONArray().apply { plan.filePaths.forEach(::put) }
        val parents = JSONArray().apply { plan.parentDirectories.forEach(::put) }
        val serializedPlan = JSONObject()
            .put(KEY_OPERATION_ID, plan.operationId)
            .put(KEY_TARGETS, targets)
            .put(KEY_FILE_PATHS, filePaths)
            .put(KEY_PARENT_DIRECTORIES, parents)
            .toString()
        return synchronized(pendingDeletePreferences) {
            pendingDeletePreferences.edit()
                .putString(KEY_PENDING_DEVICE_DELETE_OPERATION_ID, plan.operationId)
                .putString(KEY_PENDING_DEVICE_DELETE_PLAN, serializedPlan)
                .commit()
        }
    }

    private fun readPendingDelete(): DeviceDeletePlan? {
        val decoded = runCatching {
            val root = JSONObject(readPendingDeleteState() ?: return@runCatching null)
            val operationId = root.optString(KEY_OPERATION_ID).takeIf(String::isNotBlank)
                ?: return@runCatching null
            val array = root.getJSONArray(KEY_TARGETS)
            if (array.length() == 0 || array.length() > MAX_PENDING_TARGETS) return@runCatching null
            buildList {
                repeat(array.length()) { index ->
                    val item = array.getJSONObject(index)
                    val songId = item.getLong(KEY_SONG_ID).takeIf { it != 0L } ?: return@repeat
                    val uri = Uri.parse(item.getString(KEY_URI))
                    if (uri.scheme.isNullOrBlank()) return@repeat
                    add(
                        DeviceDeleteTarget(
                            songId = songId,
                            albumId = item.optLong(KEY_ALBUM_ID),
                            uri = uri,
                            artUri = item.optString(KEY_ART_URI).takeIf(String::isNotBlank)?.let(Uri::parse),
                        ),
                    )
                }
            }.takeIf { it.size == array.length() }?.let { operationId to it }
        }.getOrNull() ?: return null
        val operationId = decoded.first
        val targets = decoded.second
        if (targets.isEmpty()) return null
        return DeviceDeletePlan(
            operationId = operationId,
            targets = targets,
            uris = targets.map(DeviceDeleteTarget::uri),
            filePaths = readStringSet(KEY_FILE_PATHS),
            parentDirectories = readStringSet(KEY_PARENT_DIRECTORIES),
        )
    }

    private fun readStringSet(key: String): Set<String> {
        val raw = readPendingDeleteState() ?: return emptySet()
        return runCatching {
            val array = JSONObject(raw).optJSONArray(key) ?: return@runCatching emptySet()
            buildSet {
                repeat(array.length()) { index ->
                    array.optString(index).takeIf(String::isNotBlank)?.let(::add)
                }
            }
        }.getOrDefault(emptySet())
    }

    private fun readPendingDeleteState(): String? = synchronized(pendingDeletePreferences) {
        pendingDeletePreferences.getString(KEY_PENDING_DEVICE_DELETE_PLAN, null)
    }

    private fun clearPendingDeleteState(operationId: String): Boolean {
        return synchronized(pendingDeletePreferences) {
            if (pendingDeletePreferences.getString(KEY_PENDING_DEVICE_DELETE_OPERATION_ID, null) != operationId) {
                true
            } else {
                pendingDeletePreferences.edit()
                    .remove(KEY_PENDING_DEVICE_DELETE_OPERATION_ID)
                    .remove(KEY_PENDING_DEVICE_DELETE_PLAN)
                    .commit()
            }
        }
    }

    private companion object {
        const val KEY_OPERATION_ID = "operation_id"
        const val KEY_TARGETS = "targets"
        const val KEY_FILE_PATHS = "file_paths"
        const val KEY_PARENT_DIRECTORIES = "parent_directories"
        const val KEY_SONG_ID = "song_id"
        const val KEY_ALBUM_ID = "album_id"
        const val KEY_URI = "uri"
        const val KEY_ART_URI = "art_uri"
        const val KEY_PENDING_DEVICE_DELETE_OPERATION_ID = "pending_device_delete_operation_id"
        const val KEY_PENDING_DEVICE_DELETE_PLAN = "pending_device_delete_plan"
        const val MAX_PENDING_TARGETS = 10_000
    }
}
