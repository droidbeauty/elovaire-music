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

    fun refresh(
        forceMediaIndex: Boolean,
        enrichMetadata: Boolean,
        showLoadingIndicator: Boolean,
    )

    fun refresh(showLoadingIndicator: Boolean) {
        refresh(
            forceMediaIndex = false,
            enrichMetadata = false,
            showLoadingIndicator = showLoadingIndicator,
        )
    }

}

interface LibraryStartupController : LibraryReader {
    fun start()
    fun onPermissionChanged(granted: Boolean)
    fun blockNetworkSources(sourceIds: Set<String>)
}

interface LibraryTagUpdateWriter {
    suspend fun applyVerifiedTagEdits(editedSongs: List<Song>)
}
