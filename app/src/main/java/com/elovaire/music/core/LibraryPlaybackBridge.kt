package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class LibraryPlaybackBridge(
    private val scope: CoroutineScope,
    private val libraryRepository: LibraryRepository,
    private val playbackManager: PlaybackManager,
) {
    fun start() {
        scope.launch {
            libraryRepository.contentState
                .map { it.songs }
                .distinctUntilChanged()
                .collect(playbackManager::refreshQueuedLibraryMetadataIfNeeded)
        }
    }
}
