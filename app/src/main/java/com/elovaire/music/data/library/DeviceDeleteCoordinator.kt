package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class DeviceDeletePlan(
    val songs: List<Song>,
    val uris: List<Uri>,
    val filePaths: Set<String>,
    val parentDirectories: Set<String>,
)

internal class DeviceDeleteCoordinator(
    private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val playbackManager: PlaybackManager,
    private val preferenceStore: PreferenceStore,
    private val invalidateArtwork: (Collection<Uri?>) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun prepareSongDeletePlan(songs: List<Song>): DeviceDeletePlan? {
        val uniqueSongs = songs.distinctBy(Song::id)
        if (uniqueSongs.isEmpty()) return null
        return withContext(ioDispatcher) {
            val filePaths = querySongFilePaths(uniqueSongs)
            DeviceDeletePlan(
                songs = uniqueSongs,
                uris = uniqueSongs.map(Song::uri),
                filePaths = filePaths,
                parentDirectories = filePaths.mapNotNullTo(linkedSetOf()) { path ->
                    File(path).parentFile?.absolutePath
                },
            )
        }
    }

    suspend fun completeDelete(plan: DeviceDeletePlan) {
        invalidateArtwork(plan.songs.flatMap { listOf(it.artUri, it.uri) })
        val deleteResult = libraryRepository.refreshAfterDelete(
            LibraryDeleteRequest(
                songIds = plan.songs.mapTo(linkedSetOf(), Song::id),
                albumIds = plan.songs.mapTo(linkedSetOf(), Song::albumId),
                uris = plan.songs.mapTo(linkedSetOf(), Song::uri),
                filePaths = plan.filePaths,
                uriBySongId = plan.songs.associate { it.id to it.uri },
            ),
        )
        cleanupEmptyDirectories(plan.parentDirectories)
        playbackManager.removeSongsFromQueue(deleteResult.deletedSongIds)
        if (deleteResult.deletedSongIds.isNotEmpty()) {
            preferenceStore.removeSongReferences(deleteResult.deletedSongIds).await()
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
}
