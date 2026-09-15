package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface LibraryReader {
    val contentState: StateFlow<LibraryContentState>
    val scanState: StateFlow<LibraryScanState>
}

/** Commands exposed to the application shell without leaking the repository implementation. */
interface LibraryActionController {
    fun onPermissionChanged(granted: Boolean)

    fun setLibraryFolders(selections: List<LibraryFolderSelection>)

    fun refresh(intent: LibraryRefreshIntent)
}

/** Narrow library capability used by the device-delete transaction. */
internal interface LibraryDeletePort {
    suspend fun refreshAfterDelete(request: LibraryDeleteRequest): LibraryDeleteResult
}

enum class LibraryRefreshReason {
    UserInitiated,
    PermissionReconciliation,
}

data class LibraryRefreshIntent(
    val reason: LibraryRefreshReason,
    val showLoadingIndicator: Boolean,
) {
    internal val forceMediaIndex: Boolean
        get() = reason == LibraryRefreshReason.UserInitiated

    internal val enrichMetadata: Boolean
        get() = reason == LibraryRefreshReason.UserInitiated
}

interface LibraryStartupController : LibraryReader {
    fun start()
    fun onPermissionChanged(granted: Boolean)
    fun blockNetworkSources(sourceIds: Set<String>)
}

/** Narrow integration edge used by the network-library subsystem. */
internal interface LibraryNetworkController {
    fun unblockNetworkSource(sourceId: String)

    fun setNetworkSources(
        sources: List<elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource>,
        enrichMetadata: Boolean,
        showLoadingIndicator: Boolean,
        forceRefreshSourceIds: Set<String>,
    )
}

interface LibraryTagUpdateWriter {
    suspend fun applyVerifiedTagEdits(editedSongs: List<Song>)
}

/** Result of migrating user-data keys after a scanner proved a media identity relocation. */
internal enum class SongRelocationOutcome {
    Applied,
    RetryableFailure,
    UnrecoverableConflict,
}
