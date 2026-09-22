package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryStartupController
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.settings.PortableUserData
import elovaire.music.droidbeauty.app.data.settings.PortableUserDataBackup
import elovaire.music.droidbeauty.app.data.settings.RoomUserDataStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.data.settings.decodePortableUserData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/** Owns restore-once and coalesced portable user-data backup observation. */
@OptIn(FlowPreview::class)
internal class PortableUserDataBackupRuntime(
    private val appScope: CoroutineScope,
    private val portableUserDataBackup: PortableUserDataBackup,
    private val userDataStore: RoomUserDataStore,
    private val libraryRepository: LibraryStartupController,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = appScope.launch {
            var restoreChecked = false
            val contentSongs = libraryRepository.contentState
                .map { content -> content.portableMediaIdentityRevision to content.songs }
                .distinctUntilChanged { previous, next -> previous.first == next.first }
            val scanReadiness = libraryRepository.scanState
                .map { scan -> scan.permissionGranted to scan.isAuthoritative }
                .distinctUntilChanged()
            combine(
                userDataStore.revisionedUserDataSnapshot,
                userDataStore.userDataReadiness,
                contentSongs,
                scanReadiness,
            ) { revisionedSnapshot, readiness, content, scan ->
                PortableUserDataBackupState(
                    revisionedSnapshot = revisionedSnapshot,
                    readiness = readiness,
                    songs = content.second,
                    portableMediaIdentityRevision = content.first,
                    permissionGranted = scan.first,
                    isAuthoritative = scan.second,
                )
            }.map { state -> state.copy(userDataRevision = state.revisionedSnapshot.revision) }
                .distinctUntilChanged()
                .debounce(PORTABLE_USER_DATA_BACKUP_COALESCE_DELAY_MS)
                .collect { state ->
                    if (
                        state.readiness != UserDataReadiness.Ready ||
                        !state.isAuthoritative ||
                        !state.permissionGranted
                    ) return@collect
                    if (!restoreChecked) {
                        val encoded = withContext(ioDispatcher) { portableUserDataBackup.readBytes() }
                        val portable = encoded?.let { withContext(ioDispatcher) { decodePortableUserData(it) } }
                        val backupContainsSongReferences = portable?.hasSongReferences() == true
                        if (encoded != null && state.songs.isEmpty() && backupContainsSongReferences) {
                            return@collect
                        }
                        if (
                            encoded != null &&
                            shouldRestorePortableUserData(
                                localRevision = state.userDataRevision,
                                currentHasPortableData = state.snapshot.hasPortableUserData(),
                                backupRevision = portable?.userDataRevision,
                            )
                        ) {
                            when (userDataStore.restorePortableUserData(encoded, state.songs).await()) {
                                is PlaylistMutationResult.Success -> {
                                    restoreChecked = true
                                    return@collect
                                }
                                else -> return@collect
                            }
                        }
                        restoreChecked = true
                    }
                    if (state.songs.isEmpty() && state.snapshot.hasPortableSongReferences()) return@collect
                    try {
                        withContext(ioDispatcher) {
                            portableUserDataBackup.write(
                                snapshot = state.snapshot,
                                songs = state.songs,
                                userDataRevision = state.userDataRevision,
                                contentRevision = state.portableMediaIdentityRevision,
                            )
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: IOException) {
                        android.util.Log.w(TAG, "Portable user-data backup deferred", failure)
                    } catch (failure: SecurityException) {
                        android.util.Log.w(TAG, "Portable user-data backup deferred", failure)
                    } catch (failure: IllegalArgumentException) {
                        android.util.Log.w(TAG, "Portable user-data backup deferred", failure)
                    } catch (failure: IllegalStateException) {
                        android.util.Log.w(TAG, "Portable user-data backup deferred", failure)
                    }
                }
        }
    }

    fun release() {
        job?.cancel()
        job = null
    }
}

private data class PortableUserDataBackupState(
    val revisionedSnapshot: elovaire.music.droidbeauty.app.data.settings.RevisionedUserDataSnapshot,
    val readiness: UserDataReadiness,
    val songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
    val portableMediaIdentityRevision: String = "",
    val permissionGranted: Boolean,
    val isAuthoritative: Boolean,
    val userDataRevision: Long = revisionedSnapshot.revision,
) {
    val snapshot: UserDataSnapshot get() = revisionedSnapshot.snapshot
}

internal fun shouldRestorePortableUserData(
    localRevision: Long,
    currentHasPortableData: Boolean,
    backupRevision: Long?,
): Boolean = localRevision == 0L && !currentHasPortableData && backupRevision != null && backupRevision >= 0L

private fun UserDataSnapshot.hasPortableUserData(): Boolean {
    return playlists.isNotEmpty() || smartPlaylists.isNotEmpty() || hasPortableSongReferences()
}

private fun UserDataSnapshot.hasPortableSongReferences(): Boolean {
    return playlists.any { it.songIds.isNotEmpty() } ||
        favoriteSongIds.isNotEmpty() ||
        songPlayCounts.isNotEmpty() ||
        recentSongIds.isNotEmpty()
}

private fun PortableUserData.hasSongReferences(): Boolean =
    playlists.any { it.songs.isNotEmpty() } ||
        favoriteSongs.isNotEmpty() ||
        songPlayCounts.isNotEmpty() ||
        recentSongs.isNotEmpty()

private const val PORTABLE_USER_DATA_BACKUP_COALESCE_DELAY_MS = 500L
private const val TAG = "PortableUserDataBackup"
