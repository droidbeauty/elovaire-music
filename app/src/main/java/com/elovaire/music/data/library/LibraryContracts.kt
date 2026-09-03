package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface LibraryReader {
    val contentState: StateFlow<LibraryContentState>
    val scanState: StateFlow<LibraryScanState>
}

interface LibraryStartupController : LibraryReader {
    fun start()
    fun onPermissionChanged(granted: Boolean)
    fun blockNetworkSources(sourceIds: Set<String>)
}

interface LibraryTagUpdateWriter {
    suspend fun applyVerifiedTagEdits(editedSongs: List<Song>)
}
